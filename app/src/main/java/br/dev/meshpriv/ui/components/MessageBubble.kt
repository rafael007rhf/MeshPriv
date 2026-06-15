package br.dev.meshpriv.ui.components

<<<<<<< HEAD
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
=======
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
>>>>>>> 3e40bf5f49eb6e0fe76096429607711a287e07bc
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
<<<<<<< HEAD
import androidx.compose.ui.graphics.Color
=======
>>>>>>> 3e40bf5f49eb6e0fe76096429607711a287e07bc
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.dev.meshpriv.domain.model.Message
import br.dev.meshpriv.domain.model.MessageStatus
import br.dev.meshpriv.ui.theme.MeshPrivTheme
<<<<<<< HEAD
import br.dev.meshpriv.ui.theme.SignalOnline
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Balão de mensagem: enviadas alinhadas à direita, recebidas à esquerda.
 * Recebidas exibem o hop count; enviadas exibem o status de entrega e, quando falham,
 * uma ação "Tentar novamente". Toda bolha mostra um timestamp relativo.
=======

/**
 * Balão de mensagem: enviadas alinhadas à direita, recebidas à esquerda.
 * Mensagens recebidas exibem o hop count; enviadas exibem o status de entrega.
>>>>>>> 3e40bf5f49eb6e0fe76096429607711a287e07bc
 */
@Composable
fun MessageBubble(
    message: Message,
    isOwnMessage: Boolean,
<<<<<<< HEAD
    modifier: Modifier = Modifier,
    now: Long = System.currentTimeMillis(),
    onRetry: (() -> Unit)? = null
=======
    modifier: Modifier = Modifier
>>>>>>> 3e40bf5f49eb6e0fe76096429607711a287e07bc
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = if (isOwnMessage) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isOwnMessage) 16.dp else 4.dp,
                bottomEnd = if (isOwnMessage) 4.dp else 16.dp
            ),
            color = if (isOwnMessage) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
<<<<<<< HEAD
                    val timestamp = if (isOwnMessage) message.sentAt else (message.receivedAt ?: message.sentAt)
                    Text(
                        text = relativeTime(timestamp, now),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
=======
>>>>>>> 3e40bf5f49eb6e0fe76096429607711a287e07bc
                    if (isOwnMessage) {
                        Text(
                            text = deliveryStatusLabel(message.status),
                            style = MaterialTheme.typography.labelSmall,
<<<<<<< HEAD
                            color = deliveryStatusColor(message.status)
                        )
                    } else {
                        Text(
                            text = "↪ ${message.hopCount} ${if (message.hopCount == 1) "salto" else "saltos"}",
=======
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "${message.hopCount} ${if (message.hopCount == 1) "salto" else "saltos"}",
>>>>>>> 3e40bf5f49eb6e0fe76096429607711a287e07bc
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
<<<<<<< HEAD
                // Reenvio só faz sentido para uma mensagem própria que falhou
                if (isOwnMessage && message.status == MessageStatus.FAILED && onRetry != null) {
                    Text(
                        text = "Tentar novamente",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 2.dp)
                            .clickable { onRetry() }
                    )
                }
=======
>>>>>>> 3e40bf5f49eb6e0fe76096429607711a287e07bc
            }
        }
    }
}

<<<<<<< HEAD
private fun relativeTime(timestampMs: Long, nowMs: Long): String {
    val diff = nowMs - timestampMs
    return when {
        diff < 60_000L -> "agora"
        diff < 3_600_000L -> "há ${diff / 60_000L} min"
        diff < 86_400_000L -> "há ${diff / 3_600_000L} h"
        else -> SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(timestampMs))
    }
}

=======
>>>>>>> 3e40bf5f49eb6e0fe76096429607711a287e07bc
private fun deliveryStatusLabel(status: MessageStatus): String = when (status) {
    MessageStatus.SENDING -> "✓ enviada"
    MessageStatus.DELIVERED -> "✓✓ entregue"
    MessageStatus.FAILED -> "✗ falhou"
}

<<<<<<< HEAD
@Composable
private fun deliveryStatusColor(status: MessageStatus): Color = when (status) {
    MessageStatus.SENDING -> MaterialTheme.colorScheme.onSurfaceVariant
    MessageStatus.DELIVERED -> SignalOnline
    MessageStatus.FAILED -> MaterialTheme.colorScheme.error
}

=======
>>>>>>> 3e40bf5f49eb6e0fe76096429607711a287e07bc
@Preview(showBackground = true)
@Composable
private fun MessageBubbleEnviadaPreview() {
    MeshPrivTheme {
        MessageBubble(
            message = Message(
                messageId = "1",
                senderId = "A1B2C3D4",
                recipientId = "E5F6A7B8",
                content = "Oi Maria, a mesh está funcionando!",
                sentAt = 0L,
                receivedAt = null,
                hopCount = 0,
                status = MessageStatus.SENDING
            ),
<<<<<<< HEAD
            isOwnMessage = true,
            now = 120_000L
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MessageBubbleFalhouPreview() {
    MeshPrivTheme {
        MessageBubble(
            message = Message(
                messageId = "3",
                senderId = "A1B2C3D4",
                recipientId = "E5F6A7B8",
                content = "Mensagem que não saiu",
                sentAt = 0L,
                receivedAt = null,
                hopCount = 0,
                status = MessageStatus.FAILED
            ),
            isOwnMessage = true,
            now = 0L,
            onRetry = {}
=======
            isOwnMessage = true
>>>>>>> 3e40bf5f49eb6e0fe76096429607711a287e07bc
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MessageBubbleRecebidaPreview() {
    MeshPrivTheme {
        MessageBubble(
            message = Message(
                messageId = "2",
                senderId = "E5F6A7B8",
                recipientId = "A1B2C3D4",
                content = "Oi Rafael, recebido via relay!",
                sentAt = 0L,
                receivedAt = 1200L,
                hopCount = 2,
                status = MessageStatus.DELIVERED
            ),
<<<<<<< HEAD
            isOwnMessage = false,
            now = 1200L
=======
            isOwnMessage = false
>>>>>>> 3e40bf5f49eb6e0fe76096429607711a287e07bc
        )
    }
}
