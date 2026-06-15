package br.dev.meshpriv

import android.app.Application
import br.dev.meshpriv.data.mesh.MessageRouter
import br.dev.meshpriv.data.metrics.MetricsCollector
import br.dev.meshpriv.di.ApplicationScope
import br.dev.meshpriv.domain.repository.MessageRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class MeshPrivApplication : Application() {

    @Inject
    lateinit var messageRouter: MessageRouter

    @Inject
    lateinit var metricsCollector: MetricsCollector

    @Inject
    lateinit var messageRepository: MessageRepository

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        // Singletons que processam fluxos durante toda a vida do app —
        // iniciados uma única vez aqui, nunca em Activities
        messageRouter.start(applicationScope)
        metricsCollector.start(applicationScope, messageRouter)

        // Persiste mensagens entregues a este nó — é o que alimenta o ChatScreen do destinatário
        applicationScope.launch {
            messageRouter.deliveredMessages.collect { messageRepository.saveMessage(it) }
        }
    }
}
