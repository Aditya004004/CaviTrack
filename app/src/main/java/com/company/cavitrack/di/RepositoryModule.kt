package com.company.cavitrack.di

import com.company.cavitrack.data.repository.FirestoreInventoryRepository
import com.company.cavitrack.domain.repository.InventoryRepository
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
    abstract fun bindInventoryRepository(
        firestoreInventoryRepository: FirestoreInventoryRepository
    ): InventoryRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: com.company.cavitrack.data.repository.AuthRepositoryImpl
    ): com.company.cavitrack.domain.repository.AuthRepository
}
