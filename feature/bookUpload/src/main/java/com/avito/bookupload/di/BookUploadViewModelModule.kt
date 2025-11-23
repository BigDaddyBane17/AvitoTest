package com.avito.bookupload.di

import androidx.lifecycle.ViewModel
import com.avito.bookupload.presentation.BookUploadViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface BookUploadViewModelModule {

    @Binds
    @IntoMap
    @BookUploadViewModelKey(BookUploadViewModel::class)
    fun bindBookUploadViewModel(viewModel: BookUploadViewModel): ViewModel
}

