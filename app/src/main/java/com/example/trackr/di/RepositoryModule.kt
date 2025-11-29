package com.example.trackr.di

import com.example.trackr.data.repository.AnalyticsRepositoryImpl
import com.example.trackr.data.repository.AuthRepositoryImpl
import com.example.trackr.data.repository.DashboardRepositoryImpl
import com.example.trackr.data.repository.DataStoreRepositoryImpl
import com.example.trackr.data.repository.KBRepositoryImpl
import com.example.trackr.data.repository.SLARepositoryImpl
import com.example.trackr.domain.repository.AuthRepository
import com.example.trackr.data.repository.TicketRepositoryImpl
import com.example.trackr.domain.repository.AnalyticsRepository
import com.example.trackr.domain.repository.DashboardRepository
import com.example.trackr.domain.repository.DataStoreRepository
import com.example.trackr.domain.repository.KBRepository
import com.example.trackr.domain.repository.SLARepository
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

    @Binds
    @Singleton
    abstract fun bindDashboardRepository(impl: DashboardRepositoryImpl): DashboardRepository

    @Binds
    @Singleton
    abstract fun bindDataStoreRepository(impl: DataStoreRepositoryImpl): DataStoreRepository

    @Binds
    @Singleton
    abstract fun bindSLARepository(impl: SLARepositoryImpl): SLARepository

    @Binds
    @Singleton
    abstract fun bindAnalyticsRepository(impl: AnalyticsRepositoryImpl): AnalyticsRepository
}