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

<<<<<<< HEAD
    @Query("DELETE FROM messages WHERE messageId = :messageId")
    suspend fun delete(messageId: String)

=======
>>>>>>> 3e40bf5f49eb6e0fe76096429607711a287e07bc
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
