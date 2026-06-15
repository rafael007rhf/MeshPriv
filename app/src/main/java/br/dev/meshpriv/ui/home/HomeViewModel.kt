package br.dev.meshpriv.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.meshpriv.data.crypto.IdentityManager
import br.dev.meshpriv.domain.model.LocalIdentity
import br.dev.meshpriv.domain.usecase.ObserveConnectedPeersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val nodeId: String = "",
    val nickname: String = "",
    val connectedPeerCount: Int = 0,
    val isSearching: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    localIdentity: LocalIdentity,
    identityManager: IdentityManager,
    observeConnectedPeers: ObserveConnectedPeersUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        // Apelido ao vivo: o HomeViewModel é criado após o onboarding, então já reflete o nome
        // escolhido mesmo na primeira sessão (o LocalIdentity singleton ainda teria "Anônimo").
        HomeUiState(nodeId = localIdentity.nodeId, nickname = identityManager.getNickname())
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // Conta peers identificados (handshake concluído), não endpoints crus do Nearby:
        // um endpoint conectado mas ainda sem HELLO não é alguém para quem dá pra enviar.
        viewModelScope.launch {
            observeConnectedPeers().collect { peers ->
                _uiState.update {
                    it.copy(
                        connectedPeerCount = peers.size,
                        isSearching = peers.isEmpty()
                    )
                }
            }
        }
    }
}
