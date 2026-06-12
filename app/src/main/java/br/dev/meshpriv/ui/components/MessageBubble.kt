package br.dev.meshpriv.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.dev.meshpriv.domain.model.Message
import br.dev.meshpriv.domain.model.MessageStatus
import br.dev.meshpriv.ui.theme.MeshPrivTheme

/**
 * Balão de mensagem: enviadas alinhadas à direita, recebidas à esquerda.
 * Mensagens recebidas exibem o hop count; enviadas exibem o status de entrega.
 */
@Composable
fun MessageBubble(
    message: Message,
    isOwnMessage: Boolean,
    modifier: Modifier = Modifier
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
                    if (isOwnMessage) {
                        Text(
                            text = deliveryStatusLabel(message.status),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "${message.hopCount} ${if (message.hopCount == 1) "salto" else "saltos"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun deliveryStatusLabel(status: MessageStatus): String = when (status) {
    MessageStatus.SENDING -> "✓ enviada"
    MessageStatus.DELIVERED -> "✓✓ entregue"
    MessageStatus.FAILED -> "✗ falhou"
}

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
            isOwnMessage = true
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
            isOwnMessage = false
        )
    }
}
