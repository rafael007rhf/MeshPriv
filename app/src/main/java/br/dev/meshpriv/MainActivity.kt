package br.dev.meshpriv

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import br.dev.meshpriv.data.mesh.NearbyConnectionsManager
import br.dev.meshpriv.ui.navigation.AppNavGraph
import br.dev.meshpriv.ui.theme.MeshPrivTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var nearbyManager: NearbyConnectionsManager

    private val requiredPermissions: Array<String>
        get() = buildList {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_ADVERTISE)
                add(Manifest.permission.BLUETOOTH_CONNECT)
                add(Manifest.permission.BLUETOOTH_SCAN)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.NEARBY_WIFI_DEVICES)
            }
        }.toTypedArray()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val denied = results.filterValues { !it }.keys
        if (denied.isEmpty()) {
            Log.i("MainActivity", "Todas as permissões concedidas — iniciando Nearby")
            nearbyManager.start()
        } else {
            Log.w("MainActivity", "Permissões negadas: $denied — Nearby não iniciará")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        permissionLauncher.launch(requiredPermissions)

        setContent {
            MeshPrivTheme {
                AppNavGraph()
            }
        }
    }

    // Sem stop() em onDestroy: o NearbyConnectionsManager agora é singleton de aplicação —
    // parar aqui derrubaria a mesh em toda rotação de tela
}
