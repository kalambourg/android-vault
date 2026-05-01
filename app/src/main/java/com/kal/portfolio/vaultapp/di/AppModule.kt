package com.kal.portfolio.vaultapp.di

import android.content.Context
import com.kal.portfolio.vaultapp.data.VaultRepositoryImpl
import com.kal.portfolio.vaultapp.domain.VaultRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindVaultRepository(
        impl: VaultRepositoryImpl
    ): VaultRepository

    companion object {

        @Provides
        @Singleton
        fun provideContext(
            @ApplicationContext context: Context
        ): Context = context
    }
}