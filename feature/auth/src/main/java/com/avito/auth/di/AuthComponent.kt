package com.avito.auth.di

import dagger.Subcomponent

@AuthScope
@Subcomponent(
    modules = [
        AuthModule::class,
        AuthViewModelModule::class
    ]
)
interface AuthComponent {

    fun viewModelFactory(): AuthViewModelFactory

    @Subcomponent.Factory
    interface Factory {
        fun create(): AuthComponent
    }
}

