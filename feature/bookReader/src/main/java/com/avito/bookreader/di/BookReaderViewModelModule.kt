package com.avito.bookreader.di

import androidx.lifecycle.ViewModel
import com.avito.bookreader.presentation.BookReaderViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface BookReaderViewModelModule {

    @Binds
    @IntoMap
    @BookReaderViewModelKey(BookReaderViewModel::class)
    fun bindBookReaderViewModel(viewModel: BookReaderViewModel): ViewModel
}

