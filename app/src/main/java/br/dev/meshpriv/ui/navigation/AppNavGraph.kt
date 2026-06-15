package br.dev.meshpriv.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.dev.meshpriv.ui.chat.ChatScreen
import br.dev.meshpriv.ui.home.HomeScreen
import br.dev.meshpriv.ui.metrics.MetricsScreen
import br.dev.meshpriv.ui.peers.PeersScreen
import kotlinx.serialization.Serializable

/** Rotas tipadas do app — serializadas pelo Navigation Compose. */
sealed class Route {
    @Serializable
    data object Home : Route()

    @Serializable
    data object Peers : Route()

    @Serializable
    data class Chat(val peerId: String) : Route()

    @Serializable
    data object Metrics : Route()
}

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Route.Home) {
        composable<Route.Home> {
            HomeScreen(
                onNavigateToPeers = { navController.navigate(Route.Peers) },
                onNavigateToMetrics = { navController.navigate(Route.Metrics) }
            )
        }
        composable<Route.Peers> {
            PeersScreen(
                onPeerClick = { peerId -> navController.navigate(Route.Chat(peerId)) },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable<Route.Chat> {
            ChatScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable<Route.Metrics> {
            MetricsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
