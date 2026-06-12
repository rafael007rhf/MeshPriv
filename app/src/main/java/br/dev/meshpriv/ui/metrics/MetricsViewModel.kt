package br.dev.meshpriv.ui.metrics

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.meshpriv.domain.model.DeliveryMetric
import br.dev.meshpriv.domain.repository.MetricsRepository
import br.dev.meshpriv.domain.usecase.ExportMetricsCsvUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MetricsUiState(
    val totalSent: Int = 0,
    val totalDelivered: Int = 0,
    val totalFailed: Int = 0,
    val avgLatencyMs: Long = 0L,
    val minLatencyMs: Long = 0L,
    val maxLatencyMs: Long = 0L,
    val avgHopCount: Double = 0.0,
    val avgBatteryDrop: Double = 0.0,
    val isExporting: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MetricsViewModel @Inject constructor(
    metricsRepository: MetricsRepository,
    private val exportMetricsCsvUseCase: ExportMetricsCsvUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MetricsUiState())
    val uiState: StateFlow<MetricsUiState> = _uiState.asStateFlow()

    // Intent ACTION_SEND pronto — a tela dispara o chooser de compartilhamento
    private val _exportEvents = MutableSharedFlow<Intent>()
    val exportEvents: SharedFlow<Intent> = _exportEvents.asSharedFlow()

    init {
        viewModelScope.launch {
            metricsRepository.observeMetrics().collect { metrics ->
                _uiState.update { aggregate(metrics, it) }
            }
        }
    }

    fun exportCsv() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            runCatching { exportMetricsCsvUseCase.export() }
                .onSuccess { intent -> _exportEvents.emit(intent) }
                .onFailure { e ->
                    _uiState.update { it.copy(error = "Falha ao exportar CSV: ${e.message}") }
                }
            _uiState.update { it.copy(isExporting = false) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun aggregate(metrics: List<DeliveryMetric>, current: MetricsUiState): MetricsUiState {
        // Latência só faz sentido para entregas confirmadas (descartes registram -1)
        val delivered = metrics.filter { it.delivered }
        val latencies = delivered.map { it.latencyMs }.filter { it >= 0 }
        val batteryDrops = delivered
            .filter { it.batteryLevelStart >= 0 && it.batteryLevelEnd >= 0 }
            .map { (it.batteryLevelStart - it.batteryLevelEnd).toDouble() }

        return current.copy(
            totalSent = metrics.size,
            totalDelivered = delivered.size,
            totalFailed = metrics.size - delivered.size,
            avgLatencyMs = if (latencies.isNotEmpty()) latencies.average().toLong() else 0L,
            minLatencyMs = latencies.minOrNull() ?: 0L,
            maxLatencyMs = latencies.maxOrNull() ?: 0L,
            avgHopCount = if (delivered.isNotEmpty()) {
                delivered.map { it.hopCount }.average()
            } else 0.0,
            avgBatteryDrop = if (batteryDrops.isNotEmpty()) batteryDrops.average() else 0.0
        )
    }
}
