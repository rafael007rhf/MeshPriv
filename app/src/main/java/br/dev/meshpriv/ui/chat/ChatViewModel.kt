package br.dev.meshpriv.ui.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import br.dev.meshpriv.domain.model.Message
import br.dev.meshpriv.domain.model.Peer
import br.dev.meshpriv.domain.repository.MessageRepository
import br.dev.meshpriv.domain.repository.PeerRepository
import br.dev.meshpriv.domain.usecase.ObserveMessagesUseCase
import br.dev.meshpriv.domain.usecase.SendMessageUseCase
import br.dev.meshpriv.ui.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val peer: Peer? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeMessages: ObserveMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val peerRepository: PeerRepository,
    private val messageRepository: MessageRepository
) : ViewModel() {

    /** nodeId do peer desta conversa — usado pela UI para alinhar os balões. */
    val peerId: String = savedStateHandle.toRoute<Route.Chat>().peerId

    private val _uiState = MutableStateFlow(ChatUiState(isLoading = true))
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val peer = peerRepository.getPeer(peerId)
            _uiState.update { it.copy(peer = peer, isLoading = false) }
        }
        viewModelScope.launch {
            observeMessages(peerId).collect { messages ->
                _uiState.update { it.copy(messages = messages) }
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            runCatching { sendMessageUseCase(peerId, text.trim()) }
                .onFailure { e ->
                    _uiState.update { it.copy(error = "Falha ao enviar: ${e.message}") }
                }
        }
    }

    /**
     * Reenfileira no flooding uma mensagem que falhou: cria um novo envio e remove a antiga
     * (FAILED) para não duplicar na conversa. Útil quando o peer só ficou conhecido depois
     * (a chave pública chega pelo handshake HELLO).
     */
    fun retryMessage(message: Message) {
        viewModelScope.launch {
            runCatching {
                sendMessageUseCase(peerId, message.content)
                messageRepository.deleteMessage(message.messageId)
            }.onFailure { e ->
                _uiState.update { it.copy(error = "Falha ao reenviar: ${e.message}") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
