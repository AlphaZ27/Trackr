package com.example.trackr.di

import com.example.trackr.data.repository.AuthRepositoryImpl
import com.example.trackr.data.repository.KBRepositoryImpl
import com.example.trackr.domain.repository.AuthRepository
import com.example.trackr.data.repository.TicketRepositoryImpl
import com.example.trackr.domain.repository.KBRepository
import com.example.trackr.domain.repository.TicketRepository
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
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindTicketRepository(impl: TicketRepositoryImpl): TicketRepository

    @Binds
    @Singleton
    abstract fun bindKBRepository(impl: KBRepositoryImpl): KBRepository
}