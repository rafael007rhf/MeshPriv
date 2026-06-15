package br.dev.meshpriv.di

import javax.inject.Qualifier

/**
 * Qualifica o CoroutineScope de nível de aplicação (SupervisorJob + Dispatchers.Default),
 * usado para iniciar o MessageRouter e o MetricsCollector — nunca usar GlobalScope.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
