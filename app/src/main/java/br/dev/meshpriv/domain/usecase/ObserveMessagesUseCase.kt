package br.dev.meshpriv.domain.usecase

import br.dev.meshpriv.domain.model.LocalIdentity
import br.dev.meshpriv.domain.model.Message
import br.dev.meshpriv.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveMessagesUseCase @Inject constructor(
    private val messageRepository: MessageRepository,
    private val localIdentity: LocalIdentity
) {
    /** Observa a conversa entre este nó e o peer informado, ordenada por envio. */
    operator fun invoke(peerNodeId: String): Flow<List<Message>> =
        messageRepository.observeConversation(localIdentity.nodeId, peerNodeId)
}
