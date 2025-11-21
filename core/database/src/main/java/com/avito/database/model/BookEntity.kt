package com.avito.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey val id: String,
    val title: String,
    val author: String,
    val remoteKey: String,
    val remoteUrl: String,
    val coverUrl: String? = null,
    val fileSizeBytes: Long = 0L,
    val addedAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val localPath: String? = null,
    val sortOrder: Int = Int.MAX_VALUE
)


