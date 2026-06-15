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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.dev.meshpriv.domain.model.Peer
import br.dev.meshpriv.ui.theme.MeshPrivTheme
import br.dev.meshpriv.ui.theme.SignalOffline
import br.dev.meshpriv.ui.theme.SignalOnline

/** Cartão de peer: avatar, apelido, nodeId abreviado e chip de status de conexão. */
@Composable
fun PeerCard(
    peer: Peer,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NodeAvatar(nickname = peer.nickname, seed = peer.nodeId)
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = peer.nickname,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = peer.nodeId.take(8),
                    style = MaterialTheme.typography.labelLarge,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusChip(isConnected = peer.isConnected)
        }
    }
}

@Composable
private fun StatusChip(isConnected: Boolean) {
    val color = if (isConnected) SignalOnline else SignalOffline
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = if (isConnected) "Conectado" else "Offline",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PeerCardPreview() {
    MeshPrivTheme {
        Column(modifier = Modifier.padding(16.dp)) {
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
            Spacer(Modifier.size(8.dp))
            PeerCard(
                peer = Peer(
                    nodeId = "FF00AA11BB22CC33",
                    nickname = "João",
                    publicKey = ByteArray(32),
                    endpointId = "QRST",
                    signalStrength = 0,
                    lastSeenAt = 0L,
                    isConnected = false
                ),
                onClick = {}
            )
        }
    }
}
