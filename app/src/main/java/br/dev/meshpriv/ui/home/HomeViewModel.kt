package br.dev.meshpriv.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.meshpriv.data.mesh.NearbyConnectionsManager
import br.dev.meshpriv.domain.model.LocalIdentity
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
    nearbyManager: NearbyConnectionsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(nodeId = localIdentity.nodeId, nickname = localIdentity.nickname)
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            nearbyManager.connectedEndpoints.collect { endpoints ->
                _uiState.update {
                    it.copy(
                        connectedPeerCount = endpoints.size,
                        isSearching = endpoints.isEmpty()
                    )
                }
            }
        }
    }
}
