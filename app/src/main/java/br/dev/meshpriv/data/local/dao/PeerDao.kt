package br.dev.meshpriv.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.dev.meshpriv.data.local.entity.PeerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PeerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(peer: PeerEntity)

    @Query("SELECT * FROM peers WHERE nodeId = :nodeId")
    suspend fun getByNodeId(nodeId: String): PeerEntity?

    @Query("UPDATE peers SET isConnected = :isConnected, lastSeenAt = :lastSeenAt WHERE nodeId = :nodeId")
    suspend fun setConnected(nodeId: String, isConnected: Boolean, lastSeenAt: Long)

    @Query("SELECT * FROM peers ORDER BY lastSeenAt DESC")
    fun observeAll(): Flow<List<PeerEntity>>

    @Query("SELECT * FROM peers WHERE isConnected = 1 ORDER BY lastSeenAt DESC")
    fun observeConnected(): Flow<List<PeerEntity>>
}
