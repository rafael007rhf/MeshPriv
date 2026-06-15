package br.dev.meshpriv.data.repository

import br.dev.meshpriv.data.local.dao.PeerDao
import br.dev.meshpriv.data.local.entity.PeerEntity
import br.dev.meshpriv.domain.model.Peer
import br.dev.meshpriv.domain.repository.PeerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PeerRepositoryImpl @Inject constructor(
    private val peerDao: PeerDao
) : PeerRepository {

    override suspend fun getPeer(nodeId: String): Peer? =
        peerDao.getByNodeId(nodeId)?.toDomain()

    override fun observePeers(): Flow<List<Peer>> =
        peerDao.observeAll().map { entities -> entities.map { it.toDomain() } }

<<<<<<< HEAD
    override fun observeConnectedPeers(): Flow<List<Peer>> =
        peerDao.observeConnected().map { entities -> entities.map { it.toDomain() } }

    override suspend fun savePeer(peer: Peer) = peerDao.upsert(peer.toEntity())

    override suspend fun setConnected(nodeId: String, isConnected: Boolean, lastSeenAt: Long) =
        peerDao.setConnected(nodeId, isConnected, lastSeenAt)

=======
>>>>>>> 3e40bf5f49eb6e0fe76096429607711a287e07bc
    private fun PeerEntity.toDomain() = Peer(
        nodeId = nodeId,
        nickname = nickname,
        publicKey = publicKey,
        endpointId = endpointId,
        signalStrength = signalStrength,
        lastSeenAt = lastSeenAt,
        isConnected = isConnected
    )
<<<<<<< HEAD

    private fun Peer.toEntity() = PeerEntity(
        nodeId = nodeId,
        nickname = nickname,
        publicKey = publicKey,
        endpointId = endpointId,
        signalStrength = signalStrength,
        lastSeenAt = lastSeenAt,
        isConnected = isConnected
    )
=======
>>>>>>> 3e40bf5f49eb6e0fe76096429607711a287e07bc
}
