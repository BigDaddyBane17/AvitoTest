package com.avito.bookupload.di

import dagger.Subcomponent

@BookUploadScope
@Subcomponent(
    modules = [
        BookUploadModule::class,
        BookUploadViewModelModule::class
    ]
)
interface BookUploadComponent {

    fun viewModelFactory(): BookUploadViewModelFactory

    @Subcomponent.Factory
    interface Factory {
        fun create(): BookUploadComponent
    }
}

