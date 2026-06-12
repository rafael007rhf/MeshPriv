package br.dev.meshpriv.di

import br.dev.meshpriv.data.repository.MessageRepositoryImpl
import br.dev.meshpriv.data.repository.MetricsRepositoryImpl
import br.dev.meshpriv.data.repository.PeerRepositoryImpl
import br.dev.meshpriv.domain.repository.MessageRepository
import br.dev.meshpriv.domain.repository.MetricsRepository
import br.dev.meshpriv.domain.repository.PeerRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMessageRepository(impl: MessageRepositoryImpl): MessageRepository

    @Binds
    @Singleton
    abstract fun bindPeerRepository(impl: PeerRepositoryImpl): PeerRepository

    @Binds
    @Singleton
    abstract fun bindMetricsRepository(impl: MetricsRepositoryImpl): MetricsRepository
}
