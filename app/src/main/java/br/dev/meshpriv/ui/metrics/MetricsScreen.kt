package br.dev.meshpriv.ui.metrics

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.dev.meshpriv.ui.components.MetricRow
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetricsScreen(
    onNavigateBack: () -> Unit,
    viewModel: MetricsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.exportEvents.collect { intent ->
            context.startActivity(Intent.createChooser(intent, "Exportar métricas"))
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Métricas") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Mensagens", style = MaterialTheme.typography.titleMedium)
                    MetricRow(label = "Enviadas", value = "${uiState.totalSent}")
                    MetricRow(label = "Entregues", value = "${uiState.totalDelivered}")
                    MetricRow(label = "Com falha", value = "${uiState.totalFailed}")
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Latência (entregues)", style = MaterialTheme.typography.titleMedium)
                    MetricRow(label = "Média", value = "${uiState.avgLatencyMs} ms")
                    MetricRow(label = "Mínima", value = "${uiState.minLatencyMs} ms")
                    MetricRow(label = "Máxima", value = "${uiState.maxLatencyMs} ms")
                    HorizontalDivider()
                    MetricRow(
                        label = "Hop count médio",
                        value = String.format(Locale.US, "%.1f", uiState.avgHopCount)
                    )
                    MetricRow(
                        label = "Consumo médio de bateria",
                        value = String.format(Locale.US, "%.1f%%", uiState.avgBatteryDrop)
                    )
                }
            }

            Button(
                onClick = viewModel::exportCsv,
                enabled = !uiState.isExporting,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (uiState.isExporting) "Exportando..." else "Exportar CSV")
            }
        }
    }
}
