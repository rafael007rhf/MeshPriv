package br.dev.meshpriv.data.metrics

import br.dev.meshpriv.data.mesh.MessageRouter
import br.dev.meshpriv.domain.model.DeliveryMetric
import br.dev.meshpriv.domain.model.Message
import br.dev.meshpriv.domain.model.MessageStatus
import br.dev.meshpriv.domain.repository.MetricsRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MetricsCollectorTest {

    private lateinit var metricsRepository: MetricsRepository
    private lateinit var batteryMonitor: BatteryMonitor
    private lateinit var collector: MetricsCollector
    private val metricSlot = slot<DeliveryMetric>()

    @Before
    fun setUp() {
        metricsRepository = mockk()
        batteryMonitor = mockk()
        collector = MetricsCollector(metricsRepository, batteryMonitor, clock = { 9999L })
        coEvery { metricsRepository.saveMetric(capture(metricSlot)) } returns Unit
    }

    private fun message(
        messageId: String = "msg-1",
        sentAt: Long = 1000L,
        receivedAt: Long? = null,
        hopCount: Int = 0,
        status: MessageStatus = MessageStatus.SENDING
    ) = Message(
        messageId = messageId,
        senderId = "AAAA1111",
        recipientId = "BBBB2222",
        content = "oi",
        sentAt = sentAt,
        receivedAt = receivedAt,
        hopCount = hopCount,
        status = status
    )

    @Test
    fun `envio seguido de entrega gera métrica com latência correta`() = runTest {
        // 80% no envio, 78% na entrega
        every { batteryMonitor.getCurrentLevel() } returnsMany listOf(80, 78)

        collector.onRouterEvent(
            MessageRouter.Event.MessageSent(message(sentAt = 1000L), networkSize = 3)
        )
        collector.onMessageDelivered(
            message(sentAt = 1000L, receivedAt = 1500L, hopCount = 2, status = MessageStatus.DELIVERED)
        )

        val metric = metricSlot.captured
        assertEquals(500L, metric.latencyMs)
        assertEquals(2, metric.hopCount)
        assertTrue(metric.delivered)
        assertEquals(80, metric.batteryLevelStart)
        assertEquals(78, metric.batteryLevelEnd)
        assertEquals(3, metric.networkSize)
        assertEquals("AAAA1111", metric.sourceId)
        assertEquals("BBBB2222", metric.destinationId)
        assertEquals(9999L, metric.recordedAt)
    }

    @Test
    fun `ACK recebido fecha métrica do remetente com delivered true`() = runTest {
        every { batteryMonitor.getCurrentLevel() } returnsMany listOf(80, 78)

        collector.onRouterEvent(
            MessageRouter.Event.MessageSent(message(sentAt = 1000L), networkSize = 3)
        )
        collector.onRouterEvent(
            MessageRouter.Event.AckReceived(
                messageId = "msg-1",
                sourceId = "AAAA1111",
                destinationId = "BBBB2222",
                hopCount = 2,
                receivedAt = 1700L
            )
        )

        val metric = metricSlot.captured
        assertTrue(metric.delivered)
        // Latência do remetente = tempo até o ACK voltar (relógio local, semântica ida + volta)
        assertEquals(700L, metric.latencyMs)
        assertEquals(2, metric.hopCount)
        assertEquals(80, metric.batteryLevelStart)
        assertEquals(78, metric.batteryLevelEnd)
        assertEquals(3, metric.networkSize)
        assertEquals("AAAA1111", metric.sourceId)
        assertEquals("BBBB2222", metric.destinationId)
    }

    @Test
    fun `ACK sem envio pendente registra entrega sem latência`() = runTest {
        every { batteryMonitor.getCurrentLevel() } returns 70

        // Ex.: app reiniciado entre o envio e a volta do ACK — não há sentAt para comparar
        collector.onRouterEvent(
            MessageRouter.Event.AckReceived(
                messageId = "msg-9",
                sourceId = "AAAA1111",
                destinationId = "BBBB2222",
                hopCount = 1,
                receivedAt = 5000L
            )
        )

        val metric = metricSlot.captured
        assertTrue(metric.delivered)
        assertEquals(MetricsCollector.LATENCIA_NAO_APLICAVEL, metric.latencyMs)
        assertEquals(MetricsCollector.BATERIA_DESCONHECIDA, metric.batteryLevelStart)
    }

    @Test
    fun `pacote descartado por ttl gera métrica com delivered false`() = runTest {
        every { batteryMonitor.getCurrentLevel() } returnsMany listOf(90, 85)

        collector.onRouterEvent(
            MessageRouter.Event.MessageSent(message(messageId = "msg-2"), networkSize = 2)
        )
        collector.onRouterEvent(
            MessageRouter.Event.PacketDroppedTtl(
                packetId = "msg-2",
                sourceId = "AAAA1111",
                destinationId = "BBBB2222",
                hopCount = 7
            )
        )

        val metric = metricSlot.captured
        assertFalse(metric.delivered)
        assertEquals(MetricsCollector.LATENCIA_NAO_APLICAVEL, metric.latencyMs)
        assertEquals(7, metric.hopCount)
        assertEquals(90, metric.batteryLevelStart)
        assertEquals(85, metric.batteryLevelEnd)
        assertEquals(2, metric.networkSize)
    }

    @Test
    fun `entrega sem envio pendente registra métrica do lado do destinatário`() = runTest {
        every { batteryMonitor.getCurrentLevel() } returns 70

        // Cenário real: o destinatário não tem o envio no mapa de pendências
        collector.onMessageDelivered(
            message(sentAt = 1000L, receivedAt = 1800L, hopCount = 2, status = MessageStatus.DELIVERED)
        )

        val metric = metricSlot.captured
        assertEquals(800L, metric.latencyMs)
        assertTrue(metric.delivered)
        assertEquals(MetricsCollector.BATERIA_DESCONHECIDA, metric.batteryLevelStart)
        assertEquals(70, metric.batteryLevelEnd)
    }
}
