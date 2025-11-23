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

    fun observeBooks(userId: String): Flow<List<BookEntity>> = dao.observeBooks(userId)

    suspend fun getBooks(userId: String): List<BookEntity> = dao.getBooks(userId)

    suspend fun getBook(id: String, userId: String): BookEntity? = dao.getBook(id, userId)

    suspend fun upsertBooks(books: List<BookEntity>) {
        dao.upsertBooks(books)
    }

    suspend fun deleteBook(id: String) {
        dao.deleteBook(id)
    }

    suspend fun updateLocalPath(id: String, path: String?) {
        dao.updateLocalPath(id, path)
    }
    
    suspend fun deleteBooksNotBelongingToUser(userId: String) {
        dao.updateEmptyUserId(userId)
        dao.deleteBooksNotBelongingToUser(userId)
    }

    suspend fun updateBookUserId(bookId: String, userId: String) {
        dao.updateBookUserId(bookId, userId)
    }
    
    suspend fun getBooksWithLocalPath(userId: String): List<BookEntity> {
        return dao.getBooksWithLocalPath(userId)
    }

}