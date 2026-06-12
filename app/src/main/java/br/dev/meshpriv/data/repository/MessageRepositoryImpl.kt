package br.dev.meshpriv.data.repository

import br.dev.meshpriv.data.local.dao.MessageDao
import br.dev.meshpriv.data.local.entity.MessageEntity
import br.dev.meshpriv.domain.model.Message
import br.dev.meshpriv.domain.model.MessageStatus
import br.dev.meshpriv.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val messageDao: MessageDao
) : MessageRepository {

    override suspend fun saveMessage(message: Message) {
        messageDao.insert(message.toEntity())
    }

    override suspend fun updateStatus(messageId: String, status: MessageStatus, receivedAt: Long?) {
        messageDao.updateStatus(messageId, status.name, receivedAt)
    }

    override fun observeConversation(localNodeId: String, peerNodeId: String): Flow<List<Message>> =
        messageDao.observeConversation(localNodeId, peerNodeId)
            .map { entities -> entities.map { it.toDomain() } }

    override fun observeAll(): Flow<List<Message>> =
        messageDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    private fun Message.toEntity() = MessageEntity(
        messageId = messageId,
        senderId = senderId,
        recipientId = recipientId,
        content = content,
        sentAt = sentAt,
        receivedAt = receivedAt,
        hopCount = hopCount,
        status = status.name
    )

    private fun MessageEntity.toDomain() = Message(
        messageId = messageId,
        senderId = senderId,
        recipientId = recipientId,
        content = content,
        sentAt = sentAt,
        receivedAt = receivedAt,
        hopCount = hopCount,
        status = MessageStatus.valueOf(status)
    )
}
