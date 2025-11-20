package com.avito.bookreader.di

import dagger.Subcomponent

@BookReaderScope
@Subcomponent(
    modules = [
        BookReaderModule::class,
        BookReaderViewModelModule::class
    ]
)
interface BookReaderComponent {

    fun viewModelFactory(): BookReaderViewModelFactory

    @Subcomponent.Factory
    interface Factory {
        fun create(): BookReaderComponent
    }
}

