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
 * Envios ficam num mapa de pendências até o ACK retornar (ou o descarte) fechar a métrica
 * no lado do remetente; a entrega no destinatário gera um registro unidirecional próprio.
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
            is MessageRouter.Event.AckReceived -> onAckReceived(event)
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
     * ACK retornou ao remetente: fecha a métrica de entrega do lado do remetente.
     *
     * Semântica revisada de latência: aqui latencyMs = chegada do ACK − envio, medido só
     * pelo relógio local (sem o problema de clock sync), mas inclui a viagem de volta do
     * ACK — é "tempo até a confirmação", não latência unidirecional. O registro
     * unidirecional do destinatário (onMessageDelivered) continua sendo gravado à parte.
     */
    private suspend fun onAckReceived(event: MessageRouter.Event.AckReceived) {
        val pendingSend = mutex.withLock { pending.remove(event.messageId) }
        metricsRepository.saveMetric(
            DeliveryMetric(
                metricId = UUID.randomUUID().toString(),
                messageId = event.messageId,
                sourceId = pendingSend?.sourceId ?: event.sourceId,
                destinationId = pendingSend?.destinationId ?: event.destinationId,
                // Sem envio pendente (ex.: app reiniciado) não há sentAt para comparar
                latencyMs = pendingSend?.let { event.receivedAt - it.sentAt } ?: LATENCIA_NAO_APLICAVEL,
                hopCount = event.hopCount,
                delivered = true,
                batteryLevelStart = pendingSend?.batteryLevelStart ?: BATERIA_DESCONHECIDA,
                batteryLevelEnd = batteryMonitor.getCurrentLevel(),
                networkSize = pendingSend?.networkSize ?: 0,
                recordedAt = clock()
            )
        )
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
