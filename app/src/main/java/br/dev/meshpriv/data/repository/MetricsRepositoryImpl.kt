package br.dev.meshpriv.data.repository

import br.dev.meshpriv.data.local.dao.MetricDao
import br.dev.meshpriv.data.local.entity.MetricEntity
import br.dev.meshpriv.domain.model.DeliveryMetric
import br.dev.meshpriv.domain.repository.MetricsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MetricsRepositoryImpl @Inject constructor(
    private val metricDao: MetricDao
) : MetricsRepository {

    override suspend fun saveMetric(metric: DeliveryMetric) {
        metricDao.insert(metric.toEntity())
    }

    override suspend fun getAllMetrics(): List<DeliveryMetric> =
        metricDao.getAll().map { it.toDomain() }

    override fun observeMetrics(): Flow<List<DeliveryMetric>> =
        metricDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    private fun DeliveryMetric.toEntity() = MetricEntity(
        metricId = metricId,
        messageId = messageId,
        sourceId = sourceId,
        destinationId = destinationId,
        latencyMs = latencyMs,
        hopCount = hopCount,
        delivered = delivered,
        batteryLevelStart = batteryLevelStart,
        batteryLevelEnd = batteryLevelEnd,
        networkSize = networkSize,
        recordedAt = recordedAt
    )

    private fun MetricEntity.toDomain() = DeliveryMetric(
        metricId = metricId,
        messageId = messageId,
        sourceId = sourceId,
        destinationId = destinationId,
        latencyMs = latencyMs,
        hopCount = hopCount,
        delivered = delivered,
        batteryLevelStart = batteryLevelStart,
        batteryLevelEnd = batteryLevelEnd,
        networkSize = networkSize,
        recordedAt = recordedAt
    )
}
