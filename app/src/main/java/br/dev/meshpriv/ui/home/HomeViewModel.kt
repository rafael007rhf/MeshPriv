package br.dev.meshpriv.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
<<<<<<< HEAD
import br.dev.meshpriv.data.crypto.IdentityManager
import br.dev.meshpriv.domain.model.LocalIdentity
import br.dev.meshpriv.domain.usecase.ObserveConnectedPeersUseCase
=======
import br.dev.meshpriv.data.mesh.NearbyConnectionsManager
import br.dev.meshpriv.domain.model.LocalIdentity
>>>>>>> 3e40bf5f49eb6e0fe76096429607711a287e07bc
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
<<<<<<< HEAD
    identityManager: IdentityManager,
    observeConnectedPeers: ObserveConnectedPeersUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        // Apelido ao vivo: o HomeViewModel é criado após o onboarding, então já reflete o nome
        // escolhido mesmo na primeira sessão (o LocalIdentity singleton ainda teria "Anônimo").
        HomeUiState(nodeId = localIdentity.nodeId, nickname = identityManager.getNickname())
=======
    nearbyManager: NearbyConnectionsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(nodeId = localIdentity.nodeId, nickname = localIdentity.nickname)
>>>>>>> 3e40bf5f49eb6e0fe76096429607711a287e07bc
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
<<<<<<< HEAD
        // Conta peers identificados (handshake concluído), não endpoints crus do Nearby:
        // um endpoint conectado mas ainda sem HELLO não é alguém para quem dá pra enviar.
        viewModelScope.launch {
            observeConnectedPeers().collect { peers ->
                _uiState.update {
                    it.copy(
                        connectedPeerCount = peers.size,
                        isSearching = peers.isEmpty()
=======
        viewModelScope.launch {
            nearbyManager.connectedEndpoints.collect { endpoints ->
                _uiState.update {
                    it.copy(
                        connectedPeerCount = endpoints.size,
                        isSearching = endpoints.isEmpty()
>>>>>>> 3e40bf5f49eb6e0fe76096429607711a287e07bc
                    )
                }
            }
        }
    }
}
