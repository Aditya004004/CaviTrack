package com.company.cavitrack.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoroutineModule {
    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope {
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            android.util.Log.e("ApplicationScope", "Unhandled coroutine exception", throwable)
        }
        return CoroutineScope(SupervisorJob() + Dispatchers.Default + exceptionHandler)
    }
}

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ApplicationScope
