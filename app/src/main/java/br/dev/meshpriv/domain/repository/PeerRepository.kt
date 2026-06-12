package br.dev.meshpriv.domain.repository

import br.dev.meshpriv.domain.model.Peer

interface PeerRepository {
    /** Busca um peer conhecido pelo nodeId — necessário para obter a chave pública ao cifrar. */
    suspend fun getPeer(nodeId: String): Peer?

    // TODO(semana-N): adicionar observePeers(), upsertPeer() etc. quando a camada Room existir
}
