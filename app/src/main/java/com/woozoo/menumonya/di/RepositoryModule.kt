package com.woozoo.menumonya.di

import com.woozoo.menumonya.repository.FireStoreRepository
import com.woozoo.menumonya.repository.FireStoreRepositoryImpl
import com.woozoo.menumonya.repository.RemoteConfigRepository
import com.woozoo.menumonya.repository.RemoteConfigRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Singleton
    @Binds
    abstract fun bindFireStoreRepository(repository: FireStoreRepositoryImpl): FireStoreRepository

    @Singleton
    @Binds
    abstract fun bindRemoteConfigRepository(repository: RemoteConfigRepositoryImpl): RemoteConfigRepository
}