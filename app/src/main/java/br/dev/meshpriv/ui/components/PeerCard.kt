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
<<<<<<< HEAD
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
=======
import androidx.compose.material3.Card
>>>>>>> 3e40bf5f49eb6e0fe76096429607711a287e07bc
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
<<<<<<< HEAD
import androidx.compose.ui.text.font.FontFamily
=======
import androidx.compose.ui.graphics.Color
>>>>>>> 3e40bf5f49eb6e0fe76096429607711a287e07bc
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.dev.meshpriv.domain.model.Peer
import br.dev.meshpriv.ui.theme.MeshPrivTheme
<<<<<<< HEAD
import br.dev.meshpriv.ui.theme.SignalOffline
import br.dev.meshpriv.ui.theme.SignalOnline

/** Cartão de peer: avatar, apelido, nodeId abreviado e chip de status de conexão. */
=======

/** Cartão de peer: apelido, nodeId abreviado (4 chars), sinal e status de conexão. */
>>>>>>> 3e40bf5f49eb6e0fe76096429607711a287e07bc
@Composable
fun PeerCard(
    peer: Peer,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
<<<<<<< HEAD
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
=======
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
>>>>>>> 3e40bf5f49eb6e0fe76096429607711a287e07bc
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
<<<<<<< HEAD
            NodeAvatar(nickname = peer.nickname, seed = peer.nodeId)
            Spacer(modifier = Modifier.width(14.dp))
=======
            Spacer(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(if (peer.isConnected) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline)
            )
            Spacer(modifier = Modifier.width(12.dp))
>>>>>>> 3e40bf5f49eb6e0fe76096429607711a287e07bc
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = peer.nickname,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
<<<<<<< HEAD
                    text = peer.nodeId.take(8),
                    style = MaterialTheme.typography.labelLarge,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusChip(isConnected = peer.isConnected)
=======
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
>>>>>>> 3e40bf5f49eb6e0fe76096429607711a287e07bc
        }
    }
}

<<<<<<< HEAD
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

=======
>>>>>>> 3e40bf5f49eb6e0fe76096429607711a287e07bc
@Preview(showBackground = true)
@Composable
private fun PeerCardPreview() {
    MeshPrivTheme {
<<<<<<< HEAD
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
=======
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
>>>>>>> 3e40bf5f49eb6e0fe76096429607711a287e07bc
    }
}
