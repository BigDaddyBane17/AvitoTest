package com.avito.bookslist.data

import com.avito.bookslist.domain.DownloadedBooksRepository
import com.avito.bookslist.domain.LocalBook
import com.avito.bookslist.di.BooksListScope
import com.avito.bookslist.domain.BooksFileStorage
import com.avito.database.BooksLocalDataSource
import com.avito.database.model.BookEntity
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@BooksListScope
class DownloadedBooksRepositoryImpl @Inject constructor(
    private val localDataSource: BooksLocalDataSource,
    private val remoteDataSource: S3BooksRemoteDataSource,
    private val fileStorage: BooksFileStorage
) : DownloadedBooksRepository {

    override val booksFlow: Flow<List<LocalBook>> =
        localDataSource.observeBooks().map { entities -> entities.map { it.toDomain() } }

    override suspend fun syncRemote(force: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        if (!force) return@withContext Result.success(Unit)
        remoteDataSource.fetchBooks()
            .map { remote ->
                val existingMap = localDataSource.getBooks().associateBy { it.id }
                val startingOrder = existingMap.size
                val entities = remote.mapIndexed { index, meta ->
                    existingMap[meta.id]?.let { current ->
                        current.copy(
                            title = meta.title,
                            author = meta.author,
                            remoteKey = meta.fileKey,
                            remoteUrl = meta.fileUrl,
                            fileSizeBytes = meta.fileSizeBytes,
                            updatedAt = meta.uploadedAt
                        )
                    } ?: BookEntity(
                        id = meta.id,
                        title = meta.title,
                        author = meta.author,
                        remoteKey = meta.fileKey,
                        remoteUrl = meta.fileUrl,
                        fileSizeBytes = meta.fileSizeBytes,
                        addedAt = meta.uploadedAt,
                        updatedAt = meta.uploadedAt,
                        sortOrder = startingOrder + index
                    )
                }
                localDataSource.upsertBooks(entities)
            }
    }

    override suspend fun downloadBook(bookId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val book = localDataSource.getBook(bookId)
            ?: return@withContext Result.failure(IllegalStateException("Книга не найдена"))
        if (book.localPath?.isNotBlank() == true) {
            return@withContext Result.success(Unit)
        }
        val destination = fileStorage.createDestinationFile(
            bookId = book.id,
            extension = book.remoteKey.substringAfterLast('.', "bin")
        )
        return@withContext remoteDataSource.downloadBook(book.remoteKey, destination)
            .onSuccess {
                localDataSource.updateLocalPath(book.id, destination.absolutePath)
            }
    }

    override suspend fun deleteBook(bookId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val entity = localDataSource.getBook(bookId)
            ?: return@withContext Result.failure(IllegalStateException("Книга не найдена"))
        entity.localPath?.let { path ->
            runCatching { java.io.File(path).takeIf { it.exists() }?.delete() }
        }
        localDataSource.deleteBook(bookId)
        return@withContext Result.success(Unit)
    }

    private fun BookEntity.toDomain(): LocalBook = LocalBook(
        id = id,
        title = title,
        author = author,
        remoteKey = remoteKey,
        remoteUrl = remoteUrl,
        coverUrl = coverUrl,
        fileSizeBytes = fileSizeBytes,
        addedAt = addedAt,
        updatedAt = updatedAt,
        localPath = localPath,
        sortOrder = sortOrder
    )
}

