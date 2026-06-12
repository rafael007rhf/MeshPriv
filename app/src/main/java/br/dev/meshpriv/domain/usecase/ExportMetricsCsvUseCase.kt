package br.dev.meshpriv.domain.usecase

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import br.dev.meshpriv.domain.model.DeliveryMetric
import br.dev.meshpriv.domain.repository.MetricsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExportMetricsCsvUseCase(
    private val metricsRepository: MetricsRepository,
    private val context: Context
) {

    companion object {
        const val CSV_HEADER =
            "metricId,messageId,sourceId,destinationId,latencyMs,hopCount,delivered,batteryStart,batteryEnd,networkSize,recordedAt"
        private const val FILE_PREFIX = "meshpriv_metrics_"
        private const val MIME_TYPE_CSV = "text/csv"
    }

    /** Gera o conteúdo CSV a partir das métricas persistidas. */
    suspend fun generateCsv(): String = buildCsv(metricsRepository.getAllMetrics())

    /**
     * Exporta o CSV para o armazenamento externo do app e retorna um Intent ACTION_SEND
     * pronto para ser disparado com Intent.createChooser.
     */
    suspend fun export(): Intent {
        val csv = generateCsv()
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = withContext(Dispatchers.IO) {
            File(context.getExternalFilesDir(null), "$FILE_PREFIX$timestamp.csv").apply {
                writeText(csv, Charsets.UTF_8)
            }
        }

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return Intent(Intent.ACTION_SEND).apply {
            type = MIME_TYPE_CSV
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun buildCsv(metrics: List<DeliveryMetric>): String = buildString {
        appendLine(CSV_HEADER)
        metrics.forEach { m ->
            appendLine(
                "${m.metricId},${m.messageId},${m.sourceId},${m.destinationId},${m.latencyMs}," +
                        "${m.hopCount},${m.delivered},${m.batteryLevelStart},${m.batteryLevelEnd}," +
                        "${m.networkSize},${m.recordedAt}"
            )
        }
    }
}
