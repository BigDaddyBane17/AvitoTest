package com.avito.bookreader.di

import android.content.Context
import com.avito.bookreader.data.BookReaderRepositoryImpl
import com.avito.bookreader.data.ReadingPreferencesManager
import com.avito.bookreader.domain.repository.BookReaderRepository
import com.avito.database.source.BooksLocalDataSource
import com.avito.firebase.auth.domain.repository.AuthRepository
import dagger.Module
import dagger.Provides

@Module
interface BookReaderModule {

    companion object {
        @Provides
        @BookReaderScope
        fun provideBookReaderRepository(
            localDataSource: BooksLocalDataSource,
            context: Context,
            authRepository: AuthRepository
        ): BookReaderRepository {
            return BookReaderRepositoryImpl(localDataSource, context, authRepository)
        }

        @Provides
        @BookReaderScope
        fun provideReadingPreferencesManager(
            context: Context
        ): ReadingPreferencesManager {
            return ReadingPreferencesManager(context)
        }
    }
}
