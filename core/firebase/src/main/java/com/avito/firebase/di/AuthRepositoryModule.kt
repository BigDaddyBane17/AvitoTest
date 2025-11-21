package com.avito.firebase.di

import com.avito.di.AppScope
import com.avito.firebase.auth.domain.repository.AuthRepository
import com.avito.firebase.auth.data.repository.FirebaseAuthRepository
import dagger.Binds
import dagger.Module

@Module
interface AuthRepositoryModule {

    @Binds
    @AppScope
    fun bindAuthRepository(impl: FirebaseAuthRepository): AuthRepository
}

