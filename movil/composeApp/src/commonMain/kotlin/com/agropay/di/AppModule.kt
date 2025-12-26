package com.agropay.di

import com.agropay.core.network.HttpClientFactory
import com.agropay.core.util.BrowserLauncher
import com.agropay.data.remote.AuthService
import com.agropay.data.remote.SyncApiService
import com.agropay.data.repository.AuthRepository
import com.agropay.data.repository.CacheRepository
import com.agropay.data.repository.TareoRepository
import com.agropay.presentation.login.AuthViewModel
import com.agropay.presentation.sync.SyncViewModel
import com.agropay.presentation.tareo.AddEmployeesViewModel
import com.agropay.presentation.tareo.CreateTareoViewModel
import com.agropay.presentation.tareo.TareoListViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

expect fun createBrowserLauncher(): BrowserLauncher

val appModule = module {

    single { HttpClientFactory.create() }

    single(qualifier = org.koin.core.qualifier.named("authenticated")) {
        HttpClientFactory.createWithAuth(get())
    }

    single(named("baseUrl")) { 
        "http://10.181.78.8:10000"
    }

    single {
        SyncApiService(
            client = get(qualifier = org.koin.core.qualifier.named("authenticated")),
            baseUrl = get(named("baseUrl"))
        )
    }

    single {
        AuthService(
            httpClient = get(),
            baseUrl = get(named("baseUrl"))
        )
    }

    single { AuthRepository(get()) }

    factory { CacheRepository(get()) }
    factoryOf(::TareoRepository)
    factory { com.agropay.data.repository.UserInfoRepository(get(), get()) }

    factoryOf(::AuthViewModel)

    factoryOf(::SyncViewModel)

    factoryOf(::CreateTareoViewModel)
    factory { AddEmployeesViewModel(get(), get(), get()) }
    factoryOf(::TareoListViewModel)
    factory { parameters ->
        com.agropay.presentation.tareo.TareoDetailViewModel(
            get(), 
            get(), 
            parameters.get<String>()
        ) 
    }

    single { createBrowserLauncher() }
}
