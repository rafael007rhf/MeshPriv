package br.dev.meshpriv.di

import android.content.Context
import br.dev.meshpriv.data.local.MeshPrivDatabase
import br.dev.meshpriv.data.local.dao.MessageDao
import br.dev.meshpriv.data.local.dao.MetricDao
import br.dev.meshpriv.data.local.dao.PeerDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MeshPrivDatabase =
        MeshPrivDatabase.getInstance(context)

    @Provides
    fun provideMessageDao(database: MeshPrivDatabase): MessageDao = database.messageDao()

    @Provides
    fun providePeerDao(database: MeshPrivDatabase): PeerDao = database.peerDao()

    @Provides
    fun provideMetricDao(database: MeshPrivDatabase): MetricDao = database.metricDao()
}
