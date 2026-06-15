package br.dev.meshpriv.di

import android.content.Context
import br.dev.meshpriv.data.crypto.CryptoManager
<<<<<<< HEAD
import br.dev.meshpriv.data.crypto.IdentityManager
=======
>>>>>>> 3e40bf5f49eb6e0fe76096429607711a287e07bc
import br.dev.meshpriv.data.mesh.MessageRouter
import br.dev.meshpriv.data.mesh.NearbyConnectionsManager
import br.dev.meshpriv.data.mesh.SeenMessageCache
import br.dev.meshpriv.data.metrics.BatteryMonitor
import br.dev.meshpriv.data.metrics.MetricsCollector
import br.dev.meshpriv.domain.model.LocalIdentity
import br.dev.meshpriv.domain.repository.MessageRepository
import br.dev.meshpriv.domain.repository.MetricsRepository
import br.dev.meshpriv.domain.repository.PeerRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MeshModule {

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Provides
    @Singleton
    fun provideNearbyConnectionsManager(
        @ApplicationContext context: Context,
<<<<<<< HEAD
        identityManager: IdentityManager
    ): NearbyConnectionsManager =
        NearbyConnectionsManager(context) { identityManager.getNickname() }
=======
        localIdentity: LocalIdentity
    ): NearbyConnectionsManager =
        NearbyConnectionsManager(context, localNickname = localIdentity.nickname)
>>>>>>> 3e40bf5f49eb6e0fe76096429607711a287e07bc

    @Provides
    @Singleton
    fun provideSeenMessageCache(): SeenMessageCache = SeenMessageCache()

    @Provides
    @Singleton
    fun provideMessageRouter(
        localIdentity: LocalIdentity,
        cryptoManager: CryptoManager,
        seenCache: SeenMessageCache,
        nearbyManager: NearbyConnectionsManager,
        peerRepository: PeerRepository,
<<<<<<< HEAD
        messageRepository: MessageRepository,
        identityManager: IdentityManager
    ): MessageRouter =
        MessageRouter(
            localIdentity, cryptoManager, seenCache, nearbyManager, peerRepository, messageRepository
        ) { identityManager.getNickname() }
=======
        messageRepository: MessageRepository
    ): MessageRouter =
        MessageRouter(localIdentity, cryptoManager, seenCache, nearbyManager, peerRepository, messageRepository)
>>>>>>> 3e40bf5f49eb6e0fe76096429607711a287e07bc

    @Provides
    @Singleton
    fun provideMetricsCollector(
        metricsRepository: MetricsRepository,
        batteryMonitor: BatteryMonitor
    ): MetricsCollector = MetricsCollector(metricsRepository, batteryMonitor)

    @Provides
    fun provideBatteryMonitor(@ApplicationContext context: Context): BatteryMonitor =
        BatteryMonitor(context)
}
