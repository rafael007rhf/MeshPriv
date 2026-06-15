package br.dev.meshpriv.domain.model

import kotlinx.serialization.Serializable

/**
 * Envelope de fio (wire) que distingue os dois tipos de frame que trafegam pelo Nearby:
 *
 * - [Mesh]: pacote roteável (MESSAGE/ACK), sujeito a flooding/TTL/deduplicação
 * - [Hello]: handshake ponto-a-ponto de identidade, trocado ao conectar — NUNCA roteado
 *
 * Isolar o handshake num frame próprio mantém [MeshPacket] com a semântica documentada
 * (só MESSAGE e ACK) e impede que a apresentação de identidade entre no flooding.
 */
@Serializable
sealed class MeshFrame {

    /** Pacote da mesh propriamente dito — entra no roteamento. */
    @Serializable
    data class Mesh(val packet: MeshPacket) : MeshFrame()

    /**
     * Apresentação de um nó ao conectar: anuncia nodeId, chave pública e apelido para que
     * o peer consiga (a) cifrar mensagens de volta e (b) exibir o nó na lista de peers.
     *
     * A chave transmitida é a PÚBLICA — a privada nunca trafega na rede (restrição absoluta).
     */
    @Serializable
    data class Hello(
        val nodeId: String,
        val publicKey: ByteArray,
        val nickname: String,
        val sentAt: Long
    ) : MeshFrame() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Hello) return false
            return nodeId == other.nodeId &&
                    publicKey.contentEquals(other.publicKey) &&
                    nickname == other.nickname &&
                    sentAt == other.sentAt
        }

        override fun hashCode(): Int {
            var result = nodeId.hashCode()
            result = 31 * result + publicKey.contentHashCode()
            result = 31 * result + nickname.hashCode()
            result = 31 * result + sentAt.hashCode()
            return result
        }
    }
}
