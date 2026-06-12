package br.dev.meshpriv.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class MeshPacket(
    val packetId: String,            // UUID v4 — usado para deduplicação
    val sourceId: String,            // nodeId do remetente original
    val destinationId: String,       // nodeId do destinatário final
    val encryptedPayload: ByteArray, // conteúdo cifrado com AES-GCM
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
                encryptedPayload.contentEquals(other.encryptedPayload) &&
                senderPublicKey.contentEquals(other.senderPublicKey) &&
                ttl == other.ttl &&
                hopCount == other.hopCount &&
                createdAt == other.createdAt
    }

    override fun hashCode(): Int {
        var result = packetId.hashCode()
        result = 31 * result + encryptedPayload.contentHashCode()
        result = 31 * result + ttl
        result = 31 * result + hopCount
        return result
    }
}
