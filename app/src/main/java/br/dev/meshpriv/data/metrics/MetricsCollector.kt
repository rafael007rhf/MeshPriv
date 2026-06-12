package br.dev.meshpriv.data.metrics

import br.dev.meshpriv.data.mesh.MessageRouter
import br.dev.meshpriv.domain.model.DeliveryMetric
import br.dev.meshpriv.domain.model.Message
import br.dev.meshpriv.domain.repository.MetricsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

/**
 * Componente mais crítico do projeto: os dados registrados aqui são o experimento do artigo.
 *
 * Consome os eventos do MessageRouter e persiste DeliveryMetric completas.
 * Envios ficam num mapa de pendências até a entrega (ou descarte) fechar a métrica.
 */
class MetricsCollector(
    private val metricsRepository: MetricsRepository,
    private val batteryMonitor: BatteryMonitor,
    private val clock: () -> Long = System::currentTimeMillis
) {

    companion object {
        const val LATENCIA_NAO_APLICAVEL = -1L
        const val BATERIA_DESCONHECIDA = -1
    }

    private data class PendingSend(
        val sourceId: String,
        val destinationId: String,
        val sentAt: Long,
        val batteryLevelStart: Int,
        val networkSize: Int
    )

    private val mutex = Mutex()
    private val pending = mutableMapOf<String, PendingSend>()

    fun start(scope: CoroutineScope, router: MessageRouter) {
        scope.launch { router.events.collect { onRouterEvent(it) } }
        scope.launch { router.deliveredMessages.collect { onMessageDelivered(it) } }
    }

    suspend fun onRouterEvent(event: MessageRouter.Event) {
        when (event) {
            is MessageRouter.Event.MessageSent -> onMessageSent(event)
            is MessageRouter.Event.PacketDroppedTtl -> onPacketDropped(event)
            // Falhas de cifra/envio não geram DeliveryMetric — não medem a rede
            is MessageRouter.Event.DecryptFailed -> Unit
            is MessageRouter.Event.SendFailed -> Unit
        }
    }

    private suspend fun onMessageSent(event: MessageRouter.Event.MessageSent) {
        mutex.withLock {
            pending[event.message.messageId] = PendingSend(
                sourceId = event.message.senderId,
                destinationId = event.message.recipientId,
                sentAt = event.message.sentAt,
                batteryLevelStart = batteryMonitor.getCurrentLevel(),
                networkSize = event.networkSize
            )
        }
    }

    /**
     * Entrega no destinatário. latencyMs depende dos relógios do remetente e do destinatário
     * estarem sincronizados — limitação registrada no artigo.
     */
    suspend fun onMessageDelivered(message: Message) {
        val pendingSend = mutex.withLock { pending.remove(message.messageId) }
        val receivedAt = message.receivedAt ?: clock()
        metricsRepository.saveMetric(
            DeliveryMetric(
                metricId = UUID.randomUUID().toString(),
                messageId = message.messageId,
                sourceId = message.senderId,
                destinationId = message.recipientId,
                latencyMs = receivedAt - message.sentAt,
                hopCount = message.hopCount,
                delivered = true,
                batteryLevelStart = pendingSend?.batteryLevelStart ?: BATERIA_DESCONHECIDA,
                batteryLevelEnd = batteryMonitor.getCurrentLevel(),
                networkSize = pendingSend?.networkSize ?: 0,
                recordedAt = clock()
            )
        )
    }

    private suspend fun onPacketDropped(event: MessageRouter.Event.PacketDroppedTtl) {
        val pendingSend = mutex.withLock { pending.remove(event.packetId) }
        metricsRepository.saveMetric(
            DeliveryMetric(
                metricId = UUID.randomUUID().toString(),
                messageId = event.packetId,
                sourceId = event.sourceId,
                destinationId = event.destinationId,
                latencyMs = LATENCIA_NAO_APLICAVEL,
                hopCount = event.hopCount,
                delivered = false,
                batteryLevelStart = pendingSend?.batteryLevelStart ?: BATERIA_DESCONHECIDA,
                batteryLevelEnd = batteryMonitor.getCurrentLevel(),
                networkSize = pendingSend?.networkSize ?: 0,
                recordedAt = clock()
            )
        )
    }
}
