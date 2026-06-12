package br.dev.meshpriv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.dev.meshpriv.domain.model.Peer
import br.dev.meshpriv.ui.theme.MeshPrivTheme

/** Cartão de peer: apelido, nodeId abreviado (4 chars), sinal e status de conexão. */
@Composable
fun PeerCard(
    peer: Peer,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(if (peer.isConnected) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = peer.nickname,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = peer.nodeId.take(4),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Sinal: ${peer.signalStrength}%",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = if (peer.isConnected) "Conectado" else "Offline",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PeerCardPreview() {
    MeshPrivTheme {
        PeerCard(
            peer = Peer(
                nodeId = "A1B2C3D4E5F6A7B8",
                nickname = "Maria",
                publicKey = ByteArray(32),
                endpointId = "XYZW",
                signalStrength = 82,
                lastSeenAt = 0L,
                isConnected = true
            ),
            onClick = {}
        )
    }
}
