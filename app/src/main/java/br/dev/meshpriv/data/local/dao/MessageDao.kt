package br.dev.meshpriv.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.dev.meshpriv.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: MessageEntity)

    @Query("UPDATE messages SET status = :status, receivedAt = :receivedAt WHERE messageId = :messageId")
    suspend fun updateStatus(messageId: String, status: String, receivedAt: Long?)

    @Query(
        """
        SELECT * FROM messages
        WHERE (senderId = :localNodeId AND recipientId = :peerNodeId)
           OR (senderId = :peerNodeId AND recipientId = :localNodeId)
        ORDER BY sentAt ASC
        """
    )
    fun observeConversation(localNodeId: String, peerNodeId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages ORDER BY sentAt ASC")
    fun observeAll(): Flow<List<MessageEntity>>
}
