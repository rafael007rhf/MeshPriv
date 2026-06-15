package br.dev.meshpriv.ui.home

import app.cash.turbine.test
import br.dev.meshpriv.data.crypto.IdentityManager
import br.dev.meshpriv.domain.model.LocalIdentity
import br.dev.meshpriv.domain.model.Peer
import br.dev.meshpriv.domain.usecase.ObserveConnectedPeersUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val localIdentity = LocalIdentity(
        nodeId = "AAAA1111",
        nickname = "Rafael",
        publicKey = ByteArray(32) { 1 },
        privateKey = ByteArray(32) { 2 }
    )

    private lateinit var observeConnectedPeers: ObserveConnectedPeersUseCase
    private lateinit var identityManager: IdentityManager

    @Before
    fun setUp() {
        // viewModelScope despacha no Main — substituído por um dispatcher de teste
        Dispatchers.setMain(UnconfinedTestDispatcher())
        observeConnectedPeers = mockk()
        identityManager = mockk()
        every { identityManager.getNickname() } returns "Rafael"
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun peer(nodeId: String) = Peer(
        nodeId = nodeId,
        nickname = "Peer $nodeId",
        publicKey = ByteArray(32) { 3 },
        endpointId = "ep-$nodeId",
        signalStrength = 0,
        lastSeenAt = 0L,
        isConnected = true
    )

    @Test
    fun `estado inicial expõe identidade local e indica busca por peers`() = runTest {
        every { observeConnectedPeers() } returns flowOf(emptyList())

        val viewModel = HomeViewModel(localIdentity, identityManager, observeConnectedPeers)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("AAAA1111", state.nodeId)
            assertEquals("Rafael", state.nickname)
            assertEquals(0, state.connectedPeerCount)
            assertTrue(state.isSearching)
        }
    }

    @Test
    fun `peers identificados atualizam a contagem e encerram a busca`() = runTest {
        val peers = MutableStateFlow(emptyList<Peer>())
        every { observeConnectedPeers() } returns peers

        val viewModel = HomeViewModel(localIdentity, identityManager, observeConnectedPeers)

        viewModel.uiState.test {
            assertTrue(awaitItem().isSearching) // ainda sem peers

            peers.value = listOf(peer("BBBB2222"), peer("CCCC3333"))
            val state = awaitItem()
            assertEquals(2, state.connectedPeerCount)
            assertFalse(state.isSearching)
        }
    }
}
