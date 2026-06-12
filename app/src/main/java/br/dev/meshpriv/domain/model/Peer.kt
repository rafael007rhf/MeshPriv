package br.dev.meshpriv.domain.model

data class Peer(
    val nodeId: String,          // identificador único do peer
    val nickname: String,
    val publicKey: ByteArray,
    val endpointId: String,      // ID do Nearby Connections (volátil, muda por sessão)
    val signalStrength: Int,     // 0–100, estimado pelo Nearby Connections
    val lastSeenAt: Long,        // timestamp Unix ms
    val isConnected: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Peer) return false
        return nodeId == other.nodeId &&
                nickname == other.nickname &&
                publicKey.contentEquals(other.publicKey) &&
                endpointId == other.endpointId &&
                signalStrength == other.signalStrength &&
                lastSeenAt == other.lastSeenAt &&
                isConnected == other.isConnected
    }

    override fun hashCode(): Int {
        var result = nodeId.hashCode()
        result = 31 * result + publicKey.contentHashCode()
        result = 31 * result + endpointId.hashCode()
        return result
    }
}
