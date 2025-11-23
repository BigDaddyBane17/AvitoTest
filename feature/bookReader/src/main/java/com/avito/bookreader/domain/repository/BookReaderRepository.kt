package com.avito.bookreader.domain.repository

import com.avito.bookreader.domain.model.BookContent

interface BookReaderRepository {
    suspend fun loadBook(bookId: String): Result<BookContent>
    suspend fun saveReadingProgress(bookId: String, position: Int)
    suspend fun getReadingProgress(bookId: String): Int
    suspend fun deleteBook(bookId: String)
}