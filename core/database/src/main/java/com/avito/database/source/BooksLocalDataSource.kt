package com.avito.database.source

import com.avito.database.dao.BookDao
import com.avito.database.model.BookEntity
import com.avito.di.AppScope
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@AppScope
class BooksLocalDataSource @Inject constructor(
    private val dao: BookDao
) {

    fun observeBooks(): Flow<List<BookEntity>> = dao.observeBooks()

    suspend fun getBooks(): List<BookEntity> = dao.getBooks()

    suspend fun getBook(id: String): BookEntity? = dao.getBook(id)

    suspend fun upsertBooks(books: List<BookEntity>) {
        dao.upsertBooks(books)
    }

    suspend fun deleteBook(id: String) {
        dao.deleteBook(id)
    }

    suspend fun updateLocalPath(id: String, path: String?) {
        dao.updateLocalPath(id, path)
    }

}