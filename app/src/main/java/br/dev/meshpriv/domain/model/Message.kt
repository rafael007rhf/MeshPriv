package br.dev.meshpriv.domain.model

data class Message(
    val messageId: String,       // UUID v4
    val senderId: String,        // nodeId do remetente
    val recipientId: String,     // nodeId do destinatário
    val content: String,         // texto descriptografado (só disponível no destinatário)
    val sentAt: Long,            // timestamp de criação (remetente)
    val receivedAt: Long?,       // timestamp de recebimento (null se ainda não entregue)
    val hopCount: Int,           // quantos saltos a mensagem deu até chegar
    val status: MessageStatus
)

// Lado do remetente: SENDING ao despachar, DELIVERED quando o ACK do destinatário retorna.
// Sem timeout no MVP: se o ACK nunca chegar, o status permanece SENDING (aceitável — a
// ausência de confirmação já é o dado; FAILED fica para falhas locais de envio/cifra).
enum class MessageStatus { SENDING, DELIVERED, FAILED }
