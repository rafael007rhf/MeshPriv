package br.dev.meshpriv.domain.usecase

import br.dev.meshpriv.domain.model.Peer
import br.dev.meshpriv.domain.repository.PeerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Peers atualmente conectados E já identificados pelo handshake HELLO — base da contagem
 * exibida na Home. Difere de [ObservePeersUseCase], que inclui o histórico de peers offline.
 */
class ObserveConnectedPeersUseCase @Inject constructor(
    private val peerRepository: PeerRepository
) {
    operator fun invoke(): Flow<List<Peer>> = peerRepository.observeConnectedPeers()
}
