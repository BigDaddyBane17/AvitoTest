package com.avito.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.avito.database.model.BookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    @Query("SELECT * FROM books WHERE userId = :userId ORDER BY sortOrder ASC, title COLLATE NOCASE ASC")
    fun observeBooks(userId: String): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE userId = :userId ORDER BY sortOrder ASC, title COLLATE NOCASE ASC")
    suspend fun getBooks(userId: String): List<BookEntity>

    @Query("SELECT * FROM books WHERE id = :id AND userId = :userId LIMIT 1")
    suspend fun getBook(id: String, userId: String): BookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBooks(entities: List<BookEntity>)

    @Query("DELETE FROM books WHERE id = :id")
    suspend fun deleteBook(id: String)

    @Query("UPDATE books SET localPath = :localPath WHERE id = :id")
    suspend fun updateLocalPath(id: String, localPath: String?)

    @Query("UPDATE books SET sortOrder = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(id: String, sortOrder: Int)
    
    @Query("DELETE FROM books WHERE userId != :userId")
    suspend fun deleteBooksNotBelongingToUser(userId: String)
    
    @Query("UPDATE books SET userId = :userId WHERE userId = ''")
    suspend fun updateEmptyUserId(userId: String)
    
    @Query("UPDATE books SET userId = :userId WHERE id = :bookId")
    suspend fun updateBookUserId(bookId: String, userId: String)
    
    @Query("UPDATE books SET userId = :userId WHERE localPath IS NOT NULL AND localPath != '' AND (userId = '' OR userId != :userId)")
    suspend fun updateUserIdForLocalBooks(userId: String)
    
    @Query("SELECT * FROM books WHERE userId = '' OR userId != :userId")
    suspend fun getBooksNotBelongingToUser(userId: String): List<BookEntity>
    
    @Query("SELECT * FROM books WHERE userId = :userId AND localPath IS NOT NULL AND localPath != ''")
    suspend fun getBooksWithLocalPath(userId: String): List<BookEntity>
    
    @Query("SELECT * FROM books WHERE localPath IS NOT NULL AND localPath != ''")
    suspend fun getAllBooksWithLocalPath(): List<BookEntity>
}

