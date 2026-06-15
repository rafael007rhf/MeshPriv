package br.dev.meshpriv.domain.repository

import br.dev.meshpriv.domain.model.DeliveryMetric
import kotlinx.coroutines.flow.Flow

interface MetricsRepository {
    suspend fun saveMetric(metric: DeliveryMetric)
    suspend fun getAllMetrics(): List<DeliveryMetric>
    fun observeMetrics(): Flow<List<DeliveryMetric>>
}
