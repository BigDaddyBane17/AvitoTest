package com.avito.database.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.avito.database.dao.BookDao
import com.avito.database.model.BookEntity

@Database(
    entities = [BookEntity::class],
    version = 2,
    exportSchema = false
)
abstract class BooksDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    
    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE books ADD COLUMN userId TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}