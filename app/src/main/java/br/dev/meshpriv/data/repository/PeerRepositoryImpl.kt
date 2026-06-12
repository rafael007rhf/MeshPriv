package br.dev.meshpriv.data.repository

import br.dev.meshpriv.data.local.dao.PeerDao
import br.dev.meshpriv.data.local.entity.PeerEntity
import br.dev.meshpriv.domain.model.Peer
import br.dev.meshpriv.domain.repository.PeerRepository
import javax.inject.Inject

class PeerRepositoryImpl @Inject constructor(
    private val peerDao: PeerDao
) : PeerRepository {

    override suspend fun getPeer(nodeId: String): Peer? =
        peerDao.getByNodeId(nodeId)?.toDomain()

    private fun PeerEntity.toDomain() = Peer(
        nodeId = nodeId,
        nickname = nickname,
        publicKey = publicKey,
        endpointId = endpointId,
        signalStrength = signalStrength,
        lastSeenAt = lastSeenAt,
        isConnected = isConnected
    )
}
