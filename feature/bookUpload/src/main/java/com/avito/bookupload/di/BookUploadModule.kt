package com.avito.bookupload.di

import android.content.Context
import androidx.work.WorkManager
import com.avito.bookupload.data.BookUploadFileManager
import com.avito.bookupload.domain.BookUploadValidator
import dagger.Module
import dagger.Provides

@Module
object BookUploadModule {

    @Provides
    @BookUploadScope
    fun provideWorkManager(context: Context): WorkManager = WorkManager.getInstance(context)

    @Provides
    fun provideFileManager(context: Context): BookUploadFileManager = BookUploadFileManager(context)

    @Provides
    fun provideValidator(): BookUploadValidator = BookUploadValidator()
}

