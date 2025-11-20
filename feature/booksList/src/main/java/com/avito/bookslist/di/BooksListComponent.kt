package com.avito.bookslist.di

import dagger.Subcomponent

@BooksListScope
@Subcomponent(
    modules = [
        BooksListModule::class,
        BooksListViewModelModule::class
    ]
)
interface BooksListComponent {

    fun viewModelFactory(): BooksListViewModelFactory

    @Subcomponent.Factory
    interface Factory {
        fun create(): BooksListComponent
    }
}

