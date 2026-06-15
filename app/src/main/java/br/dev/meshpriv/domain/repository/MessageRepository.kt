package br.dev.meshpriv.domain.repository

import br.dev.meshpriv.domain.model.Message
import br.dev.meshpriv.domain.model.MessageStatus
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    suspend fun saveMessage(message: Message)
    suspend fun updateStatus(messageId: String, status: MessageStatus, receivedAt: Long?)

    /** Remove uma mensagem — usado no "tentar novamente" para não duplicar a que falhou. */
    suspend fun deleteMessage(messageId: String)
    fun observeConversation(localNodeId: String, peerNodeId: String): Flow<List<Message>>
    fun observeAll(): Flow<List<Message>>
}
