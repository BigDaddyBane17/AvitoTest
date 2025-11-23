package com.avito.avitotest

import android.app.Application
import com.avito.avitotest.di.AppComponent
import com.avito.avitotest.di.DaggerAppComponent

class AvitoApplication : Application() {

    lateinit var appComponent: AppComponent
        private set

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.factory()
            .create(this)
    }
}

