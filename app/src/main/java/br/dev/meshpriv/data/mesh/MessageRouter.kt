package br.dev.meshpriv.data.mesh

import br.dev.meshpriv.data.crypto.CryptoManager
import br.dev.meshpriv.domain.model.LocalIdentity
import br.dev.meshpriv.domain.model.MeshPacket
import br.dev.meshpriv.domain.model.Message
import br.dev.meshpriv.domain.model.MessageStatus
import br.dev.meshpriv.domain.repository.PeerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.UUID

/**
 * Decide o destino de cada MeshPacket: entregar, descartar ou retransmitir (flooding com TTL).
 */
class MessageRouter(
    private val localIdentity: LocalIdentity,
    private val cryptoManager: CryptoManager,
    private val seenCache: SeenMessageCache,
    private val nearbyManager: NearbyConnectionsManager,
    private val peerRepository: PeerRepository
) {

    companion object {
        const val TTL_INICIAL = 7
    }

    private val json = Json { ignoreUnknownKeys = true }

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
    }

    /** Conecta o roteador ao fluxo de bytes brutos vindos do Nearby Connections. */
    fun start(scope: CoroutineScope) {
        scope.launch {
            nearbyManager.incomingPackets.collect { (endpointId, bytes) ->
                // Bytes malformados (peer com versão incompatível) são descartados em silêncio
                val packet = runCatching {
                    json.decodeFromString<MeshPacket>(bytes.decodeToString())
                }.getOrNull() ?: return@collect
                onPacketReceived(endpointId, packet)
            }
        }
    }

    suspend fun onPacketReceived(fromEndpointId: String?, packet: MeshPacket) {
        // 1. Deduplicação — pacote já visto é eco do flooding
        if (seenCache.hasSeen(packet.packetId)) return
        seenCache.markSeen(packet.packetId)

        // 2. Pacote destinado a este nó: entregar à camada de domínio
        if (packet.destinationId == localIdentity.nodeId) {
            deliver(packet)
            return
        }

        // 3. TTL esgotado: descartar e registrar a falha de entrega
        if (packet.ttl <= 0) {
            _events.emit(
                Event.PacketDroppedTtl(packet.packetId, packet.sourceId, packet.destinationId, packet.hopCount)
            )
            return
        }

        // 4. Retransmitir para todos os peers conectados exceto a origem
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

    private suspend fun deliver(packet: MeshPacket) {
        val content = runCatching {
            cryptoManager.decrypt(packet.encryptedPayload, packet.senderPublicKey)
        }.getOrElse {
            // Falha na tag GCM ou payload corrompido — nunca entregar conteúdo não autenticado
            _events.emit(Event.DecryptFailed(packet.packetId))
            return
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
                hopCount = packet.hopCount,
                status = MessageStatus.DELIVERED
            )
        )
    }

    private fun encode(packet: MeshPacket): ByteArray =
        json.encodeToString(MeshPacket.serializer(), packet).encodeToByteArray()
}
