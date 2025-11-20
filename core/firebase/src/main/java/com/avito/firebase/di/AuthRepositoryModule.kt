package com.avito.firebase.di

import com.avito.di.AppScope
import com.avito.firebase.auth.AuthRepository
import com.avito.firebase.auth.FirebaseAuthRepository
import dagger.Binds
import dagger.Module

@Module
interface AuthRepositoryModule {

    @Binds
    @AppScope
    fun bindAuthRepository(impl: FirebaseAuthRepository): AuthRepository
}

