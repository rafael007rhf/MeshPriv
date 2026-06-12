package br.dev.meshpriv.domain.repository

import br.dev.meshpriv.domain.model.Peer
import kotlinx.coroutines.flow.Flow

interface PeerRepository {
    /** Busca um peer conhecido pelo nodeId — necessário para obter a chave pública ao cifrar. */
    suspend fun getPeer(nodeId: String): Peer?

    /** Observa todos os peers conhecidos (conectados ou não — histórico incluído). */
    fun observePeers(): Flow<List<Peer>>
}
