package com.avito.avitotest.di

import android.app.Application
import com.avito.auth.di.AuthComponent
import com.avito.auth.di.AuthFeatureModule
import com.avito.bookreader.di.BookReaderComponent
import com.avito.bookreader.di.BookReaderFeatureModule
import com.avito.bookslist.di.BooksListComponent
import com.avito.bookslist.di.BooksListFeatureModule
import com.avito.bookupload.di.BookUploadComponent
import com.avito.bookupload.di.BookUploadFeatureModule
import com.avito.database.di.DatabaseModule
import com.avito.di.AppScope
import com.avito.firebase.di.AuthRepositoryModule
import com.avito.firebase.di.FirebaseModule
import com.avito.firebase.di.StorageModule
import com.avito.profile.di.ProfileComponent
import com.avito.profile.di.ProfileFeatureModule
import dagger.BindsInstance
import dagger.Component

@AppScope
@Component(
    modules = [
        AppModule::class,
        FirebaseModule::class,
        AuthRepositoryModule::class,
        StorageModule::class,
        DatabaseModule::class,
        AuthFeatureModule::class,
        BooksListFeatureModule::class,
        BookReaderFeatureModule::class,
        BookUploadFeatureModule::class,
        ProfileFeatureModule::class
    ]
)
interface AppComponent {

    fun authComponentFactory(): AuthComponent.Factory
    fun booksListComponentFactory(): BooksListComponent.Factory
    fun bookReaderComponentFactory(): BookReaderComponent.Factory
    fun bookUploadComponentFactory(): BookUploadComponent.Factory
    fun profileComponentFactory(): ProfileComponent.Factory

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance application: Application): AppComponent
    }
}

