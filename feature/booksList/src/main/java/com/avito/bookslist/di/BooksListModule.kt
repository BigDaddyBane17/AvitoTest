package com.avito.bookslist.di

import android.content.Context
import com.amazonaws.services.s3.AmazonS3
import com.avito.bookslist.data.DownloadedBooksRepositoryImpl
import com.avito.bookslist.data.S3BooksRemoteDataSource
import com.avito.bookslist.domain.BooksFileStorage
import com.avito.bookslist.domain.DownloadedBooksRepository
import com.avito.database.BooksLocalDataSource
import com.avito.firebase.auth.AuthRepository
import com.avito.firebase.storage.S3Config
import dagger.Module
import dagger.Provides
import kotlinx.serialization.json.Json

@Module
object BooksListModule {

    @Provides
    @BooksListScope
    fun provideBooksFileStorage(context: Context): BooksFileStorage = BooksFileStorage(context)

    @Provides
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Provides
    fun provideRemoteDataSource(
        amazonS3: AmazonS3,
        s3Config: S3Config,
        authRepository: AuthRepository,
        json: Json
    ): S3BooksRemoteDataSource = S3BooksRemoteDataSource(amazonS3, s3Config, authRepository, json)

    @Provides
    @BooksListScope
    fun provideBooksRepository(
        localDataSource: BooksLocalDataSource,
        remoteDataSource: S3BooksRemoteDataSource,
        fileStorage: BooksFileStorage
    ): DownloadedBooksRepository = DownloadedBooksRepositoryImpl(localDataSource, remoteDataSource, fileStorage)
}
