package com.avito.bookslist.domain

import kotlinx.coroutines.flow.Flow

interface DownloadedBooksRepository {
    val booksFlow: Flow<List<LocalBook>>
    suspend fun syncRemote(force: Boolean = false): Result<Unit>
    suspend fun downloadBook(bookId: String): Result<Unit>
    suspend fun deleteBook(bookId: String): Result<Unit>
}


