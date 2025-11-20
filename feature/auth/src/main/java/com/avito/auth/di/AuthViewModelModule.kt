package com.avito.auth.di

import androidx.lifecycle.ViewModel
import com.avito.auth.presentation.AuthViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface AuthViewModelModule {

    @Binds
    @IntoMap
    @AuthViewModelKey(AuthViewModel::class)
    fun bindAuthViewModel(viewModel: AuthViewModel): ViewModel
}

