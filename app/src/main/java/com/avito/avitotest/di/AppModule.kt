package com.avito.avitotest.di

import android.app.Application
import android.content.Context
import com.avito.di.AppScope
import dagger.Module
import dagger.Provides

@Module
class AppModule {

    @Provides
    @AppScope
    fun provideContext(application: Application): Context = application.applicationContext
}

