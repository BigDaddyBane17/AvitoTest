package com.avito.database.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.avito.database.dao.BookDao
import com.avito.database.model.BookEntity

@Database(
    entities = [BookEntity::class],
    version = 1,
    exportSchema = false
)
abstract class BooksDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
}