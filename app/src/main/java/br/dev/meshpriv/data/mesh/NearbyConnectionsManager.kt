package br.dev.meshpriv.data.mesh

import android.content.Context
import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class NearbyConnectionsManager(
    private val context: Context,
    private val localNickname: String
) {

    companion object {
        private const val TAG = "NearbyConnMgr"
        private const val SERVICE_ID = "br.dev.meshpriv.mesh"
        private val STRATEGY = Strategy.P2P_CLUSTER
    }

    private val connectionsClient = Nearby.getConnectionsClient(context)

    private val _connectedEndpoints = MutableStateFlow<List<String>>(emptyList())
    val connectedEndpoints: StateFlow<List<String>> = _connectedEndpoints.asStateFlow()

    // (endpointId de origem, bytes brutos do MeshPacket serializado)
    private val _incomingPackets = MutableSharedFlow<Pair<String, ByteArray>>(extraBufferCapacity = 64)
    val incomingPackets: SharedFlow<Pair<String, ByteArray>> = _incomingPackets.asSharedFlow()

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            Log.i(TAG, "Conexão iniciada com endpoint=$endpointId apelido=${info.endpointName}")
            // Aceitar automaticamente toda solicitação de conexão
            connectionsClient.acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    Log.i(TAG, "Conectado com sucesso: endpoint=$endpointId")
                    _connectedEndpoints.value += endpointId
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    Log.w(TAG, "Conexão rejeitada: endpoint=$endpointId")
                }
                ConnectionsStatusCodes.STATUS_ERROR -> {
                    Log.e(TAG, "Erro na conexão: endpoint=$endpointId código=${result.status.statusCode}")
                }
                else -> {
                    Log.w(TAG, "Resultado inesperado: endpoint=$endpointId código=${result.status.statusCode}")
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
            Log.i(TAG, "Desconectado: endpoint=$endpointId")
            _connectedEndpoints.value -= endpointId
        }
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            Log.i(TAG, "Endpoint encontrado: id=$endpointId nome=${info.endpointName}")
            connectionsClient.requestConnection(localNickname, endpointId, connectionLifecycleCallback)
                .addOnFailureListener { e ->
                    Log.e(TAG, "Falha ao solicitar conexão com $endpointId: ${e.message}")
                }
        }

        override fun onEndpointLost(endpointId: String) {
            Log.i(TAG, "Endpoint perdido: id=$endpointId")
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            payload.asBytes()?.let { bytes ->
                if (!_incomingPackets.tryEmit(endpointId to bytes)) {
                    Log.w(TAG, "Buffer de pacotes cheio — pacote de $endpointId descartado")
                }
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {}
    }

    /** Envia bytes para todos os peers conectados, opcionalmente excluindo a origem (flooding). */
    fun sendToAll(bytes: ByteArray, excludeEndpointId: String? = null) {
        val targets = _connectedEndpoints.value.filter { it != excludeEndpointId }
        if (targets.isEmpty()) return
        connectionsClient.sendPayload(targets, Payload.fromBytes(bytes))
            .addOnFailureListener { e -> Log.e(TAG, "Falha ao enviar payload: ${e.message}") }
    }

    fun start() {
        startAdvertising()
        startDiscovery()
    }

    private fun startAdvertising() {
        val options = AdvertisingOptions.Builder().setStrategy(STRATEGY).build()
        connectionsClient.startAdvertising(localNickname, SERVICE_ID, connectionLifecycleCallback, options)
            .addOnSuccessListener { Log.i(TAG, "Advertising iniciado como '$localNickname'") }
            .addOnFailureListener { e -> Log.e(TAG, "Falha ao iniciar advertising: ${e.message}") }
    }

    private fun startDiscovery() {
        val options = DiscoveryOptions.Builder().setStrategy(STRATEGY).build()
        connectionsClient.startDiscovery(SERVICE_ID, endpointDiscoveryCallback, options)
            .addOnSuccessListener { Log.i(TAG, "Discovery iniciado") }
            .addOnFailureListener { e -> Log.e(TAG, "Falha ao iniciar discovery: ${e.message}") }
    }

    fun stop() {
        connectionsClient.stopAdvertising()
        connectionsClient.stopDiscovery()
        connectionsClient.stopAllEndpoints()
        _connectedEndpoints.value = emptyList()
        Log.i(TAG, "Nearby Connections encerrado")
    }
}
