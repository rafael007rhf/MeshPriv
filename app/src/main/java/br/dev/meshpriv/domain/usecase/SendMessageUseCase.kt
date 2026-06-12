package br.dev.meshpriv.domain.usecase

import br.dev.meshpriv.data.mesh.MessageRouter
import br.dev.meshpriv.domain.model.Message
import br.dev.meshpriv.domain.repository.MessageRepository
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val messageRouter: MessageRouter,
    private val messageRepository: MessageRepository
) {
    /**
     * Cifra e despacha a mensagem pela mesh e a persiste localmente —
     * a persistência alimenta o Flow observado pelo ChatScreen.
     */
    suspend operator fun invoke(recipientId: String, content: String): Message {
        val message = messageRouter.sendMessage(recipientId, content)
        messageRepository.saveMessage(message)
        return message
    }
}
