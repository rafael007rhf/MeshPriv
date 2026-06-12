package br.dev.meshpriv.domain.usecase

import br.dev.meshpriv.domain.model.Peer
import br.dev.meshpriv.domain.repository.PeerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObservePeersUseCase @Inject constructor(
    private val peerRepository: PeerRepository
) {
    operator fun invoke(): Flow<List<Peer>> = peerRepository.observePeers()
}
