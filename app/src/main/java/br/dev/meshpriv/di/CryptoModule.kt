package br.dev.meshpriv.di

import android.content.Context
import br.dev.meshpriv.data.crypto.CryptoManager
import br.dev.meshpriv.data.crypto.IdentityManager
import br.dev.meshpriv.domain.model.LocalIdentity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CryptoModule {

    @Provides
    @Singleton
    fun provideIdentityManager(@ApplicationContext context: Context): IdentityManager =
        IdentityManager(context)

    // Na primeira injeção gera o par de chaves; nas seguintes carrega das preferências
    @Provides
    @Singleton
    fun provideLocalIdentity(identityManager: IdentityManager): LocalIdentity =
        identityManager.getIdentity()

    @Provides
    @Singleton
    fun provideCryptoManager(localIdentity: LocalIdentity): CryptoManager =
        CryptoManager(localIdentity.privateKey)
}
