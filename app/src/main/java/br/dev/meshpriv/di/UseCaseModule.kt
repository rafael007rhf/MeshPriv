package br.dev.meshpriv.di

import android.content.Context
import br.dev.meshpriv.domain.repository.MetricsRepository
import br.dev.meshpriv.domain.usecase.ExportMetricsCsvUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    // Provider manual porque ExportMetricsCsvUseCase é compartilhado com testes JVM puros
    // e por isso não carrega anotações de injeção no construtor
    @Provides
    fun provideExportMetricsCsvUseCase(
        metricsRepository: MetricsRepository,
        @ApplicationContext context: Context
    ): ExportMetricsCsvUseCase = ExportMetricsCsvUseCase(metricsRepository, context)
}
