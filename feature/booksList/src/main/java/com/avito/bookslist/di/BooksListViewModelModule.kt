package com.avito.bookslist.di

import androidx.lifecycle.ViewModel
import com.avito.bookslist.presentation.BooksListViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface BooksListViewModelModule {

    @Binds
    @IntoMap
    @BooksListViewModelKey(BooksListViewModel::class)
    fun bindBooksListViewModel(viewModel: BooksListViewModel): ViewModel
}

