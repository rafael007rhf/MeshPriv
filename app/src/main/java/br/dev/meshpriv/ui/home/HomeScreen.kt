package br.dev.meshpriv.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun HomeScreen(
    onNavigateToPeers: () -> Unit,
    onNavigateToMetrics: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeContent(
        uiState = uiState,
        onNavigateToPeers = onNavigateToPeers,
        onNavigateToMetrics = onNavigateToMetrics
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onNavigateToPeers: () -> Unit,
    onNavigateToMetrics: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("MeshPriv") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = uiState.nickname,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "ID: ${uiState.nodeId}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Spacer(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(
                                    if (uiState.isSearching) MaterialTheme.colorScheme.outline
                                    else Color(0xFF4CAF50)
                                )
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = if (uiState.isSearching) "Buscando peers..." else "Conectado à mesh",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Peers conectados: ${uiState.connectedPeerCount}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Button(onClick = onNavigateToPeers, modifier = Modifier.fillMaxWidth()) {
                Text("Peers")
            }
            OutlinedButton(onClick = onNavigateToMetrics, modifier = Modifier.fillMaxWidth()) {
                Text("Métricas")
            }
        }
    }
}
