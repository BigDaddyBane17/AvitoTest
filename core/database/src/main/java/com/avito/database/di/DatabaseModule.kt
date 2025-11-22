package com.avito.database.di

import android.content.Context
import androidx.room.Room
import com.avito.database.database.BooksDatabase
import com.avito.database.dao.BookDao
import com.avito.di.AppScope
import dagger.Module
import dagger.Provides

@Module
object DatabaseModule {

    @Provides
    @AppScope
    fun provideBooksDatabase(context: Context): BooksDatabase =
        Room.databaseBuilder(
            context,
            BooksDatabase::class.java,
            "books.db"
        )
        .addMigrations(BooksDatabase.MIGRATION_1_2)
        .build()

    @Provides
    fun provideBookDao(database: BooksDatabase): BookDao = database.bookDao()
}


