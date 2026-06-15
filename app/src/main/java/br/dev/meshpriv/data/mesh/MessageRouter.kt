package br.dev.meshpriv.data.mesh

import br.dev.meshpriv.data.crypto.CryptoManager
import br.dev.meshpriv.domain.model.LocalIdentity
import br.dev.meshpriv.domain.model.MeshFrame
import br.dev.meshpriv.domain.model.MeshPacket
import br.dev.meshpriv.domain.model.Message
import br.dev.meshpriv.domain.model.MessageStatus
import br.dev.meshpriv.domain.model.PacketType
import br.dev.meshpriv.domain.model.Peer
import br.dev.meshpriv.domain.repository.MessageRepository
import br.dev.meshpriv.domain.repository.PeerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Decide o destino de cada MeshPacket: entregar, descartar ou retransmitir (flooding com TTL).
 */
class MessageRouter(
    private val localIdentity: LocalIdentity,
    private val cryptoManager: CryptoManager,
    private val seenCache: SeenMessageCache,
    private val nearbyManager: NearbyConnectionsManager,
    private val peerRepository: PeerRepository,
    private val messageRepository: MessageRepository,
    // Apelido lido ao vivo no HELLO; o default preserva os testes que não passam provider.
    private val nicknameProvider: () -> String = { localIdentity.nickname }
) {

    companion object {
        const val TTL_INICIAL = 7
    }

    private val json = Json { ignoreUnknownKeys = true }

    // Mapa endpointId (volátil, do Nearby) → nodeId (estável, da identidade), preenchido pelo
    // handshake HELLO. Permite marcar o peer certo como offline quando o Nearby só nos dá o
    // endpointId na desconexão. Concorrente: alimentado pela recepção de HELLO e lido pela
    // observação de desconexões, em coroutines distintas.
    private val endpointToNode = ConcurrentHashMap<String, String>()

    private val _deliveredMessages = MutableSharedFlow<Message>(extraBufferCapacity = 64)
    val deliveredMessages: SharedFlow<Message> = _deliveredMessages.asSharedFlow()

    private val _events = MutableSharedFlow<Event>(extraBufferCapacity = 64)
    val events: SharedFlow<Event> = _events.asSharedFlow()

    /** Eventos consumidos pelo MetricsCollector — base das métricas do artigo. */
    sealed class Event {
        data class MessageSent(val message: Message, val networkSize: Int) : Event()
        data class PacketDroppedTtl(
            val packetId: String,
            val sourceId: String,
            val destinationId: String,
            val hopCount: Int
        ) : Event()
        data class DecryptFailed(val packetId: String) : Event()
        data class SendFailed(val recipientId: String, val reason: String) : Event()

        /** ACK de entrega retornou ao remetente original (este nó). */
        data class AckReceived(
            val messageId: String,      // mensagem original confirmada
            val sourceId: String,       // remetente original da mensagem (este nó)
            val destinationId: String,  // destinatário original, que gerou o ACK
            val hopCount: Int,          // saltos que o ACK percorreu na volta
            val receivedAt: Long        // chegada do ACK pelo relógio local do remetente
        ) : Event()
    }

    /** Conecta o roteador ao fluxo de bytes brutos vindos do Nearby Connections. */
    fun start(scope: CoroutineScope) {
        // Frames recebidos: pacotes roteáveis vão ao flooding; HELLO popula o PeerRepository
        scope.launch {
            nearbyManager.incomingPackets.collect { (endpointId, bytes) ->
                // Bytes malformados (peer com versão incompatível) são descartados em silêncio
                val frame = runCatching {
                    json.decodeFromString<MeshFrame>(bytes.decodeToString())
                }.getOrNull() ?: return@collect
                when (frame) {
                    is MeshFrame.Mesh -> onPacketReceived(endpointId, frame.packet)
                    is MeshFrame.Hello -> onHelloReceived(endpointId, frame)
                }
            }
        }

        // Handshake de identidade derivado da lista de conectados do Nearby:
        // endpoint que aparece → apresentar-se (HELLO); endpoint que some → marcar peer offline.
        scope.launch {
            var previous = emptySet<String>()
            nearbyManager.connectedEndpoints.collect { current ->
                val currentSet = current.toSet()
                (currentSet - previous).forEach { sendHello(it) }
                (previous - currentSet).forEach { onEndpointDisconnected(it) }
                previous = currentSet
            }
        }
    }

    suspend fun onPacketReceived(fromEndpointId: String?, packet: MeshPacket) {
        // 1. Deduplicação — pacote já visto é eco do flooding
        if (seenCache.hasSeen(packet.packetId)) return
        seenCache.markSeen(packet.packetId)

        // 2. Pacote destinado a este nó: tratar conforme o tipo.
        // O when exaustivo é a guarda contra loop infinito: só MESSAGE gera ACK —
        // um ACK que chega ao destino termina aqui, nunca produz outro ACK.
        if (packet.destinationId == localIdentity.nodeId) {
            when (packet.type) {
                PacketType.MESSAGE -> {
                    if (deliver(packet)) sendAck(packet)
                }
                PacketType.ACK -> handleAck(packet)
            }
            return
        }

        // 3. TTL esgotado: descartar e registrar a falha de entrega
        if (packet.ttl <= 0) {
            _events.emit(
                Event.PacketDroppedTtl(packet.packetId, packet.sourceId, packet.destinationId, packet.hopCount)
            )
            return
        }

        // 4. Retransmitir para todos os peers conectados exceto a origem.
        // Vale para MESSAGE e ACK: num nó relay, um ACK em trânsito é só encaminhado
        val forwarded = packet.copy(ttl = packet.ttl - 1, hopCount = packet.hopCount + 1)
        nearbyManager.sendToAll(encode(forwarded), excludeEndpointId = fromEndpointId)
    }

    suspend fun sendMessage(recipientId: String, content: String): Message {
        val now = System.currentTimeMillis()
        val messageId = UUID.randomUUID().toString()

        val peer = peerRepository.getPeer(recipientId)
        if (peer == null) {
            _events.emit(Event.SendFailed(recipientId, "Peer desconhecido: chave pública indisponível"))
            return Message(
                messageId = messageId,
                senderId = localIdentity.nodeId,
                recipientId = recipientId,
                content = content,
                sentAt = now,
                receivedAt = null,
                hopCount = 0,
                status = MessageStatus.FAILED
            )
        }

        val encryptedPayload = cryptoManager.encrypt(content, peer.publicKey)
        val packet = MeshPacket(
            packetId = messageId,
            sourceId = localIdentity.nodeId,
            destinationId = recipientId,
            encryptedPayload = encryptedPayload,
            senderPublicKey = localIdentity.publicKey,
            ttl = TTL_INICIAL,
            hopCount = 0,
            createdAt = now
        )

        // Registrar antes de enviar — evita reprocessar o próprio eco quando o flooding devolver o pacote
        seenCache.markSeen(packet.packetId)
        nearbyManager.sendToAll(encode(packet), excludeEndpointId = null)

        val message = Message(
            messageId = messageId,
            senderId = localIdentity.nodeId,
            recipientId = recipientId,
            content = content,
            sentAt = now,
            receivedAt = null,
            hopCount = 0,
            status = MessageStatus.SENDING
        )
        _events.emit(Event.MessageSent(message, networkSize = nearbyManager.connectedEndpoints.value.size))
        return message
    }

    /** @return true se a mensagem foi entregue — só entregas reais geram ACK. */
    private suspend fun deliver(packet: MeshPacket): Boolean {
        val content = runCatching {
            cryptoManager.decrypt(packet.encryptedPayload, packet.senderPublicKey)
        }.getOrElse {
            // Falha na tag GCM ou payload corrompido — nunca entregar conteúdo não autenticado
            _events.emit(Event.DecryptFailed(packet.packetId))
            return false
        }

        // A persistência com Room é feita pelo coletor de deliveredMessages na Application
        _deliveredMessages.emit(
            Message(
                messageId = packet.packetId,
                senderId = packet.sourceId,
                recipientId = localIdentity.nodeId,
                content = content,
                sentAt = packet.createdAt,
                receivedAt = System.currentTimeMillis(),
                // +1 conta o enlace final (origem→destino). Assim o hopCount = nº de enlaces
                // percorridos: entrega direta = 1, via 1 relay (A→B→C) = 2 (ver CLAUDE.md §16).
                hopCount = packet.hopCount + 1,
                status = MessageStatus.DELIVERED
            )
        )
        return true
    }

    /**
     * Confirma a entrega ao remetente original: ACK roteado de volta pelo mesmo flooding,
     * com packetId novo (para a deduplicação tratá-lo como pacote independente) e ttl cheio.
     * Sem exclusão de endpoint: o caminho de volta costuma ser justamente o peer de onde
     * a mensagem veio.
     */
    private suspend fun sendAck(original: MeshPacket) {
        val ack = MeshPacket(
            packetId = UUID.randomUUID().toString(),
            sourceId = localIdentity.nodeId,
            destinationId = original.sourceId,
            type = PacketType.ACK,
            // Payload em claro: só o messageId original — sem conteúdo confidencial (ver MeshPacket)
            encryptedPayload = original.packetId.encodeToByteArray(),
            senderPublicKey = localIdentity.publicKey,
            ttl = TTL_INICIAL,
            hopCount = 0,
            createdAt = System.currentTimeMillis()
        )
        // Registrar antes de enviar — evita reprocessar o próprio eco do ACK
        seenCache.markSeen(ack.packetId)
        nearbyManager.sendToAll(encode(ack), excludeEndpointId = null)
    }

    /**
     * ACK chegou ao remetente original: a mensagem foi confirmada no destinatário.
     * Marca DELIVERED ("✓✓ entregue" no ChatScreen) e emite evento para o MetricsCollector.
     * Sem timeout no MVP: se o ACK nunca chegar, a mensagem permanece SENDING.
     */
    private suspend fun handleAck(packet: MeshPacket) {
        val messageId = packet.encryptedPayload.decodeToString()
        val ackReceivedAt = System.currentTimeMillis()
        messageRepository.updateStatus(messageId, MessageStatus.DELIVERED, ackReceivedAt)
        _events.emit(
            Event.AckReceived(
                messageId = messageId,
                sourceId = packet.destinationId,
                destinationId = packet.sourceId,
                // Mesma convenção da entrega: +1 para contar enlaces percorridos pelo ACK na volta
                hopCount = packet.hopCount + 1,
                receivedAt = ackReceivedAt
            )
        )
    }

    /**
     * Apresenta este nó a um endpoint recém-conectado. Envio ponto-a-ponto (sendTo, não
     * flooding): o HELLO não é roteável e só interessa ao vizinho direto.
     * Transmite apenas a chave PÚBLICA — a privada nunca sai do dispositivo.
     */
    internal fun sendHello(toEndpointId: String) {
        val hello = MeshFrame.Hello(
            nodeId = localIdentity.nodeId,
            publicKey = localIdentity.publicKey,
            nickname = nicknameProvider(),
            sentAt = System.currentTimeMillis()
        )
        nearbyManager.sendTo(toEndpointId, encodeFrame(hello))
    }

    /**
     * HELLO recebido: registra o mapeamento endpointId→nodeId e persiste o peer com sua chave
     * pública. É isso que permite cifrar mensagens para ele (sendMessage) e listá-lo na UI.
     */
    internal suspend fun onHelloReceived(fromEndpointId: String, hello: MeshFrame.Hello) {
        endpointToNode[fromEndpointId] = hello.nodeId
        peerRepository.savePeer(
            Peer(
                nodeId = hello.nodeId,
                nickname = hello.nickname,
                publicKey = hello.publicKey,
                endpointId = fromEndpointId,
                // Nearby Connections (P2P_CLUSTER) não expõe RSSI — sinal não medido no MVP
                signalStrength = 0,
                lastSeenAt = System.currentTimeMillis(),
                isConnected = true
            )
        )
    }

    /**
     * Endpoint perdido: marca o peer correspondente como offline mas mantém o registro
     * (histórico, conforme o CLAUDE.md). Endpoint sem HELLO prévio é ignorado.
     */
    internal suspend fun onEndpointDisconnected(endpointId: String) {
        val nodeId = endpointToNode.remove(endpointId) ?: return
        peerRepository.setConnected(nodeId, isConnected = false, lastSeenAt = System.currentTimeMillis())
    }

    private fun encode(packet: MeshPacket): ByteArray = encodeFrame(MeshFrame.Mesh(packet))

    private fun encodeFrame(frame: MeshFrame): ByteArray =
        json.encodeToString(MeshFrame.serializer(), frame).encodeToByteArray()
}
