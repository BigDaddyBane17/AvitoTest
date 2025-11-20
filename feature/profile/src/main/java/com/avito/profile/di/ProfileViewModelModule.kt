package com.avito.profile.di

import androidx.lifecycle.ViewModel
import com.avito.profile.presentation.ProfileViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface ProfileViewModelModule {

    @Binds
    @IntoMap
    @ProfileViewModelKey(ProfileViewModel::class)
    fun bindProfileViewModel(viewModel: ProfileViewModel): ViewModel
}

