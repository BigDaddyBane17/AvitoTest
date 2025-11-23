package com.avito.profile.di

import dagger.Subcomponent

@ProfileScope
@Subcomponent(
    modules = [
        ProfileModule::class,
        ProfileViewModelModule::class
    ]
)
interface ProfileComponent {

    fun viewModelFactory(): ProfileViewModelFactory

    @Subcomponent.Factory
    interface Factory {
        fun create(): ProfileComponent
    }
}

