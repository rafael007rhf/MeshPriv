package br.dev.meshpriv.ui.peers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.meshpriv.domain.model.Peer
import br.dev.meshpriv.domain.usecase.ObservePeersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PeersUiState(
    val peers: List<Peer> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class PeersViewModel @Inject constructor(
    observePeers: ObservePeersUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PeersUiState())
    val uiState: StateFlow<PeersUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observePeers().collect { peers ->
                _uiState.update { it.copy(peers = peers, isLoading = false) }
            }
        }
    }
}
