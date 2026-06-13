package br.dev.meshpriv.domain.model

import kotlinx.serialization.Serializable

/**
 * Tipo do pacote na mesh.
 * MESSAGE transporta conteúdo cifrado; ACK confirma ao remetente original que a
 * mensagem chegou ao destinatário final (fecha o "✓✓ entregue" e a métrica de entrega).
 */
@Serializable
enum class PacketType { MESSAGE, ACK }

@Serializable
data class MeshPacket(
    val packetId: String,            // UUID v4 — usado para deduplicação
    val sourceId: String,            // nodeId do remetente original
    val destinationId: String,       // nodeId do destinatário final
    // Default MESSAGE: pacotes de versões antigas do protocolo (sem o campo) continuam
    // sendo decodificados normalmente — compatibilidade retroativa
    val type: PacketType = PacketType.MESSAGE,
    // Para type=MESSAGE: conteúdo cifrado com AES-GCM.
    // Para type=ACK: o messageId original em texto claro (UTF-8). Sem criptografia no MVP —
    // o ACK só referencia um ID já visível no tráfego (packetId da MESSAGE), não há
    // conteúdo confidencial a proteger.
    val encryptedPayload: ByteArray,
    val senderPublicKey: ByteArray,  // chave pública do remetente (para decriptografia)
    val ttl: Int,                    // Time To Live — começa em 7, decrementado a cada hop
    val hopCount: Int,               // incrementado a cada hop
    val createdAt: Long              // timestamp de criação no remetente
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MeshPacket) return false
        return packetId == other.packetId &&
                sourceId == other.sourceId &&
                destinationId == other.destinationId &&
                type == other.type &&
                encryptedPayload.contentEquals(other.encryptedPayload) &&
                senderPublicKey.contentEquals(other.senderPublicKey) &&
                ttl == other.ttl &&
                hopCount == other.hopCount &&
                createdAt == other.createdAt
    }

    override fun hashCode(): Int {
        var result = packetId.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + encryptedPayload.contentHashCode()
        result = 31 * result + ttl
        result = 31 * result + hopCount
        return result
    }
}
