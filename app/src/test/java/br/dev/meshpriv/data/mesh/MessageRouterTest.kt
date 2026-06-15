package br.dev.meshpriv.data.mesh

import app.cash.turbine.test
import br.dev.meshpriv.data.crypto.CryptoManager
import br.dev.meshpriv.domain.model.LocalIdentity
import br.dev.meshpriv.domain.model.MeshFrame
import br.dev.meshpriv.domain.model.MeshPacket
import br.dev.meshpriv.domain.model.MessageStatus
import br.dev.meshpriv.domain.model.PacketType
import br.dev.meshpriv.domain.model.Peer
import br.dev.meshpriv.domain.repository.MessageRepository
import br.dev.meshpriv.domain.repository.PeerRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.mockk.Called
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

class MessageRouterTest {

    private val localIdentity = LocalIdentity(
        nodeId = "AAAA1111",
        nickname = "Rafael",
        publicKey = ByteArray(32) { 1 },
        privateKey = ByteArray(32) { 2 }
    )

    private lateinit var cryptoManager: CryptoManager
    private lateinit var nearbyManager: NearbyConnectionsManager
    private lateinit var peerRepository: PeerRepository
    private lateinit var messageRepository: MessageRepository
    private lateinit var seenCache: SeenMessageCache
    private lateinit var router: MessageRouter

    @Before
    fun setUp() {
        cryptoManager = mockk()
        nearbyManager = mockk(relaxed = true)
        peerRepository = mockk()
        messageRepository = mockk(relaxed = true)
        seenCache = SeenMessageCache()
        router = MessageRouter(
            localIdentity, cryptoManager, seenCache, nearbyManager, peerRepository, messageRepository
        )
    }

    private fun packet(
        destinationId: String,
        ttl: Int = 7,
        hopCount: Int = 0,
        packetId: String = UUID.randomUUID().toString()
    ) = MeshPacket(
        packetId = packetId,
        sourceId = "CCCC3333",
        destinationId = destinationId,
        encryptedPayload = byteArrayOf(1, 2, 3),
        senderPublicKey = ByteArray(32) { 4 },
        ttl = ttl,
        hopCount = hopCount,
        createdAt = 1000L
    )

    private fun ackPacket(
        destinationId: String,
        originalMessageId: String = "msg-original",
        ttl: Int = 7,
        hopCount: Int = 0,
        packetId: String = UUID.randomUUID().toString()
    ) = MeshPacket(
        packetId = packetId,
        sourceId = "CCCC3333",
        destinationId = destinationId,
        type = PacketType.ACK,
        encryptedPayload = originalMessageId.encodeToByteArray(),
        senderPublicKey = ByteArray(32) { 4 },
        ttl = ttl,
        hopCount = hopCount,
        createdAt = 1000L
    )

    @Test
    fun `pacote destinado a este nó é entregue`() = runTest {
        every { cryptoManager.decrypt(any(), any()) } returns "olá, Rafael"

        router.deliveredMessages.test {
            router.onPacketReceived("ep-origem", packet(destinationId = "AAAA1111", hopCount = 2))

            val mensagem = awaitItem()
            assertEquals("olá, Rafael", mensagem.content)
            assertEquals("CCCC3333", mensagem.senderId)
            // Pacote chega com hopCount=2 (2 relays anteriores) + enlace final = 3 enlaces
            assertEquals(3, mensagem.hopCount)
            assertEquals(MessageStatus.DELIVERED, mensagem.status)
        }
        // Pacote entregue não é retransmitido — o único envio após a entrega é o ACK
        val bytesSlot = slot<ByteArray>()
        verify(exactly = 1) { nearbyManager.sendToAll(capture(bytesSlot), isNull()) }
        val enviado = decodeMesh(bytesSlot.captured)
        assertEquals(PacketType.ACK, enviado.type)
    }

    @Test
    fun `mensagem entregue gera ACK de volta para o remetente original`() = runTest {
        every { cryptoManager.decrypt(any(), any()) } returns "olá"
        val original = packet(destinationId = "AAAA1111", hopCount = 2, packetId = "msg-original")

        router.onPacketReceived("ep-origem", original)

        val bytesSlot = slot<ByteArray>()
        verify { nearbyManager.sendToAll(capture(bytesSlot), isNull()) }
        val ack = decodeMesh(bytesSlot.captured)
        assertEquals(PacketType.ACK, ack.type)
        assertEquals("AAAA1111", ack.sourceId)                 // quem recebeu envia o ACK
        assertEquals("CCCC3333", ack.destinationId)            // de volta ao remetente original
        assertEquals("msg-original", ack.encryptedPayload.decodeToString())
        assertEquals(MessageRouter.TTL_INICIAL, ack.ttl)
        assertEquals(0, ack.hopCount)
        assertTrue(ack.packetId != original.packetId)          // packetId novo para o flooding
        // O próprio eco do ACK não deve ser reprocessado
        assertTrue(seenCache.hasSeen(ack.packetId))
    }

    @Test
    fun `ACK entregue ao remetente original marca a mensagem como DELIVERED`() = runTest {
        router.events.test {
            router.onPacketReceived("ep", ackPacket(destinationId = "AAAA1111", hopCount = 2))

            val evento = awaitItem()
            assertTrue(evento is MessageRouter.Event.AckReceived)
            assertEquals("msg-original", (evento as MessageRouter.Event.AckReceived).messageId)
            // ACK chega com hopCount=2 + enlace final = 3 enlaces na volta
            assertEquals(3, evento.hopCount)
        }
        coVerify(exactly = 1) {
            messageRepository.updateStatus("msg-original", MessageStatus.DELIVERED, any())
        }
    }

    @Test
    fun `ACK em nó relay é retransmitido e não entregue`() = runTest {
        val bytesSlot = slot<ByteArray>()

        router.onPacketReceived("ep-origem", ackPacket(destinationId = "OUTRO999", ttl = 5, hopCount = 1))

        verify { nearbyManager.sendToAll(capture(bytesSlot), eq("ep-origem")) }
        val retransmitido = decodeMesh(bytesSlot.captured)
        assertEquals(PacketType.ACK, retransmitido.type)
        assertEquals(4, retransmitido.ttl)
        assertEquals(2, retransmitido.hopCount)
        // Relay não trata o ACK como seu: nenhuma mensagem local é atualizada
        coVerify(exactly = 0) { messageRepository.updateStatus(any(), any(), any()) }
    }

    @Test
    fun `ACK nunca gera outro ACK`() = runTest {
        router.onPacketReceived("ep", ackPacket(destinationId = "AAAA1111"))

        // ACK consumido no destino não dispara nenhum novo envio (guarda contra loop infinito)
        verify { nearbyManager wasNot Called }
    }

    @Test
    fun `ACK duplicado é deduplicado`() = runTest {
        val ack = ackPacket(destinationId = "AAAA1111", originalMessageId = "msg-x")

        router.onPacketReceived("ep", ack)
        router.onPacketReceived("ep", ack)

        coVerify(exactly = 1) {
            messageRepository.updateStatus("msg-x", MessageStatus.DELIVERED, any())
        }
    }

    @Test
    fun `pacote com ttl zero é descartado e gera evento de falha`() = runTest {
        router.events.test {
            router.onPacketReceived("ep", packet(destinationId = "OUTRO999", ttl = 0, hopCount = 7))

            val evento = awaitItem()
            assertTrue(evento is MessageRouter.Event.PacketDroppedTtl)
            assertEquals(7, (evento as MessageRouter.Event.PacketDroppedTtl).hopCount)
        }
        verify { nearbyManager wasNot Called }
    }

    @Test
    fun `pacote duplicado é descartado`() = runTest {
        val pacote = packet(destinationId = "OUTRO999", ttl = 5)

        router.onPacketReceived("ep", pacote)
        router.onPacketReceived("ep", pacote)

        // Só a primeira recepção retransmite
        verify(exactly = 1) { nearbyManager.sendToAll(any(), eq("ep")) }
    }

    @Test
    fun `pacote para outro nó é retransmitido com ttl decrementado e hop incrementado`() = runTest {
        val bytesSlot = slot<ByteArray>()
        val pacote = packet(destinationId = "OUTRO999", ttl = 5, hopCount = 1)

        router.onPacketReceived("ep-origem", pacote)

        verify { nearbyManager.sendToAll(capture(bytesSlot), eq("ep-origem")) }
        val retransmitido = decodeMesh(bytesSlot.captured)
        assertEquals(4, retransmitido.ttl)
        assertEquals(2, retransmitido.hopCount)
        assertEquals(pacote.packetId, retransmitido.packetId)
        assertEquals(pacote.sourceId, retransmitido.sourceId)
    }

    @Test
    fun `enviar mensagem cria pacote com ttl 7 e registra no cache de vistos`() = runTest {
        val destinatario = Peer(
            nodeId = "BBBB2222",
            nickname = "Maria",
            publicKey = ByteArray(32) { 3 },
            endpointId = "ep-2",
            signalStrength = 80,
            lastSeenAt = 0L,
            isConnected = true
        )
        coEvery { peerRepository.getPeer("BBBB2222") } returns destinatario
        every { cryptoManager.encrypt("oi", destinatario.publicKey) } returns byteArrayOf(9, 9, 9)
        every { nearbyManager.connectedEndpoints } returns MutableStateFlow(listOf("ep-2"))

        val bytesSlot = slot<ByteArray>()
        val mensagem = router.sendMessage("BBBB2222", "oi")

        assertEquals(MessageStatus.SENDING, mensagem.status)
        verify { nearbyManager.sendToAll(capture(bytesSlot), isNull()) }
        val pacote = decodeMesh(bytesSlot.captured)
        assertEquals(MessageRouter.TTL_INICIAL, pacote.ttl)
        assertEquals(0, pacote.hopCount)
        assertEquals("AAAA1111", pacote.sourceId)
        // O próprio eco do flooding não deve ser reprocessado
        assertTrue(seenCache.hasSeen(pacote.packetId))
    }

    @Test
    fun `enviar para peer desconhecido retorna mensagem com status FAILED`() = runTest {
        coEvery { peerRepository.getPeer("ZZZZ0000") } returns null

        val mensagem = router.sendMessage("ZZZZ0000", "oi")

        assertEquals(MessageStatus.FAILED, mensagem.status)
        verify { nearbyManager wasNot Called }
    }

    // --- Handshake de identidade (HELLO) ---

    @Test
    fun `sendHello envia a identidade local (chave pública) ao endpoint`() = runTest {
        val bytesSlot = slot<ByteArray>()

        router.sendHello("ep-novo")

        verify { nearbyManager.sendTo(eq("ep-novo"), capture(bytesSlot)) }
        val frame = Json.decodeFromString<MeshFrame>(bytesSlot.captured.decodeToString())
        assertTrue(frame is MeshFrame.Hello)
        frame as MeshFrame.Hello
        assertEquals("AAAA1111", frame.nodeId)
        assertEquals("Rafael", frame.nickname)
        // Transmite a chave PÚBLICA, nunca a privada
        assertTrue(frame.publicKey.contentEquals(localIdentity.publicKey))
        assertTrue(!frame.publicKey.contentEquals(localIdentity.privateKey))
    }

    @Test
    fun `HELLO recebido salva o peer com chave pública e o marca conectado`() = runTest {
        coEvery { peerRepository.savePeer(any()) } returns Unit
        val hello = MeshFrame.Hello(
            nodeId = "BBBB2222",
            publicKey = ByteArray(32) { 7 },
            nickname = "Maria",
            sentAt = 123L
        )

        router.onHelloReceived("ep-maria", hello)

        val peerSlot = slot<Peer>()
        coVerify { peerRepository.savePeer(capture(peerSlot)) }
        assertEquals("BBBB2222", peerSlot.captured.nodeId)
        assertEquals("ep-maria", peerSlot.captured.endpointId)
        assertEquals("Maria", peerSlot.captured.nickname)
        assertTrue(peerSlot.captured.isConnected)
        assertTrue(peerSlot.captured.publicKey.contentEquals(ByteArray(32) { 7 }))
    }

    @Test
    fun `desconexão marca offline o peer mapeado pelo HELLO`() = runTest {
        coEvery { peerRepository.savePeer(any()) } returns Unit
        coEvery { peerRepository.setConnected(any(), any(), any()) } returns Unit
        router.onHelloReceived(
            "ep-maria",
            MeshFrame.Hello("BBBB2222", ByteArray(32) { 7 }, "Maria", 0L)
        )

        router.onEndpointDisconnected("ep-maria")

        coVerify { peerRepository.setConnected("BBBB2222", false, any()) }
    }

    @Test
    fun `desconexão de endpoint sem HELLO prévio não toca o repositório`() = runTest {
        router.onEndpointDisconnected("ep-fantasma")

        coVerify(exactly = 0) { peerRepository.setConnected(any(), any(), any()) }
    }

    private fun decodeMesh(bytes: ByteArray): MeshPacket =
        (Json.decodeFromString<MeshFrame>(bytes.decodeToString()) as MeshFrame.Mesh).packet
}
