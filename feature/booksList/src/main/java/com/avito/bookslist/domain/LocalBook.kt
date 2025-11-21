package com.avito.bookslist.domain

data class LocalBook(
    val id: String,
    val title: String,
    val author: String,
    val remoteKey: String,
    val remoteUrl: String,
    val coverUrl: String? = null,
    val fileSizeBytes: Long = 0L,
    val addedAt: Long = 0L,
    val updatedAt: Long = 0L,
    val localPath: String? = null,
    val sortOrder: Int = Int.MAX_VALUE
) {
    val isDownloaded: Boolean get() = !localPath.isNullOrBlank()
}


