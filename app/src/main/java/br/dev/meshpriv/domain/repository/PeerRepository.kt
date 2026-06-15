package br.dev.meshpriv.domain.repository

import br.dev.meshpriv.domain.model.Peer
import kotlinx.coroutines.flow.Flow

interface PeerRepository {
    /** Busca um peer conhecido pelo nodeId — necessário para obter a chave pública ao cifrar. */
    suspend fun getPeer(nodeId: String): Peer?

    /** Observa todos os peers conhecidos (conectados ou não — histórico incluído). */
    fun observePeers(): Flow<List<Peer>>
<<<<<<< HEAD

    /** Observa só os peers atualmente conectados E identificados (handshake HELLO concluído). */
    fun observeConnectedPeers(): Flow<List<Peer>>

    /** Insere ou atualiza um peer — chamado pelo handshake HELLO ao descobrir a identidade. */
    suspend fun savePeer(peer: Peer)

    /** Atualiza só o estado de conexão de um peer (mantém o registro como histórico). */
    suspend fun setConnected(nodeId: String, isConnected: Boolean, lastSeenAt: Long)
=======
>>>>>>> 3e40bf5f49eb6e0fe76096429607711a287e07bc
}
