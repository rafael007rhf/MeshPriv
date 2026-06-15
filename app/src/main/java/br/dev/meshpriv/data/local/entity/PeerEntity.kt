package br.dev.meshpriv.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "peers")
data class PeerEntity(
    @PrimaryKey val nodeId: String,
    val nickname: String,
    val publicKey: ByteArray,    // convertida para Base64 via TypeConverter
    val endpointId: String,
    val signalStrength: Int,
    val lastSeenAt: Long,
    val isConnected: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PeerEntity) return false
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
