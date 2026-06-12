package br.dev.meshpriv.domain.model

data class LocalIdentity(
    val nodeId: String,
    val nickname: String,
    val publicKey: ByteArray,
    val privateKey: ByteArray   // NUNCA logar, NUNCA serializar para rede
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LocalIdentity) return false
        return nodeId == other.nodeId &&
                nickname == other.nickname &&
                publicKey.contentEquals(other.publicKey) &&
                privateKey.contentEquals(other.privateKey)
    }

    override fun hashCode(): Int {
        var result = nodeId.hashCode()
        result = 31 * result + nickname.hashCode()
        result = 31 * result + publicKey.contentHashCode()
        result = 31 * result + privateKey.contentHashCode()
        return result
    }
}
