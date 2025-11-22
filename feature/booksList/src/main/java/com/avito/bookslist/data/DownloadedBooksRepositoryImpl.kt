package com.avito.bookslist.data

import com.avito.bookslist.domain.repository.DownloadedBooksRepository
import com.avito.bookslist.domain.model.LocalBook
import com.avito.bookslist.di.BooksListScope
import com.avito.bookslist.domain.storage.BooksFileStorage
import com.avito.database.source.BooksLocalDataSource
import com.avito.database.model.BookEntity
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@BooksListScope
class DownloadedBooksRepositoryImpl @Inject constructor(
    private val localDataSource: BooksLocalDataSource,
    private val remoteDataSource: S3BooksRemoteDataSource,
    private val fileStorage: BooksFileStorage,
    private val authRepository: com.avito.firebase.auth.domain.repository.AuthRepository
) : DownloadedBooksRepository {

    override val booksFlow: Flow<List<LocalBook>>
        get() {
            val userId = authRepository.currentUserInfo()?.uid
            return if (userId != null) {
                localDataSource.observeBooks(userId).map { entities -> entities.map { it.toDomain() } }
            } else {
                flowOf(emptyList())
            }
        }

    override suspend fun syncRemote(force: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        val userId = authRepository.currentUserInfo()?.uid
            ?: return@withContext Result.failure(IllegalStateException("Пользователь не авторизован"))
        
        // Сохраняем книги с localPath для текущего пользователя ПЕРЕД очисткой
        // Это важно - нужно сохранить ДО удаления, чтобы не потерять данные
        val userBooksWithLocalPath = localDataSource.getBooksWithLocalPath(userId)
            .associateBy { it.id }
        
        // Обновляем userId для книг с пустым userId (из миграции)
        localDataSource.deleteBooksNotBelongingToUser(userId)
        
        if (!force) return@withContext Result.success(Unit)
        
        remoteDataSource.fetchBooks()
            .map { remote ->
                val existingMap = localDataSource.getBooks(userId).associateBy { it.id }
                val remoteIds = remote.map { it.id }.toSet()
                val startingOrder = existingMap.size
                
                // Создаем список сущностей из remote книг
                val entities = remote.mapIndexed { index, meta ->
                    // Убеждаемся, что userId совпадает с текущим пользователем
                    val correctUserId = if (meta.userId == userId) meta.userId else userId
                    
                    existingMap[meta.id]?.let { current ->
                        // Восстанавливаем localPath из сохраненных книг текущего пользователя
                        val restoredLocalPath = userBooksWithLocalPath[meta.id]?.localPath ?: current.localPath
                        
                        current.copy(
                            userId = correctUserId, // Обновляем userId если изменился
                            title = meta.title,
                            author = meta.author,
                            remoteKey = meta.fileKey,
                            remoteUrl = meta.fileUrl,
                            fileSizeBytes = meta.fileSizeBytes,
                            updatedAt = meta.uploadedAt,
                            // Восстанавливаем localPath если книга была скачана текущим пользователем
                            localPath = restoredLocalPath
                        )
                    } ?: BookEntity(
                        id = meta.id,
                        userId = correctUserId,
                        title = meta.title,
                        author = meta.author,
                        remoteKey = meta.fileKey,
                        remoteUrl = meta.fileUrl,
                        fileSizeBytes = meta.fileSizeBytes,
                        addedAt = meta.uploadedAt,
                        updatedAt = meta.uploadedAt,
                        sortOrder = startingOrder + index,
                        // Восстанавливаем localPath только если книга была скачана текущим пользователем
                        localPath = userBooksWithLocalPath[meta.id]?.localPath
                    )
                }.toMutableList()
                
                // Добавляем книги с localPath текущего пользователя, которых нет в remote списке
                // (например, книги, которые были скачаны, но еще не загружены на сервер)
                // Используем userBooksWithLocalPath - это книги, которые точно принадлежат текущему пользователю
                userBooksWithLocalPath.values.forEach { book ->
                    if (book.id !in remoteIds) {
                        // Восстанавливаем книгу с правильным userId (на всякий случай)
                        entities.add(book.copy(userId = userId))
                    }
                }
                
                localDataSource.upsertBooks(entities)
            }
    }

    override suspend fun downloadBook(bookId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val userId = authRepository.currentUserInfo()?.uid
            ?: return@withContext Result.failure(IllegalStateException("Пользователь не авторизован"))
        val book = localDataSource.getBook(bookId, userId)
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
                // Обновляем localPath и убеждаемся, что userId правильный
                localDataSource.updateLocalPath(book.id, destination.absolutePath)
                // Если userId книги не совпадает с текущим, обновляем его
                if (book.userId != userId) {
                    localDataSource.updateBookUserId(book.id, userId)
                }
            }
    }

    override suspend fun deleteBook(bookId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val userId = authRepository.currentUserInfo()?.uid
            ?: return@withContext Result.failure(IllegalStateException("Пользователь не авторизован"))
        val entity = localDataSource.getBook(bookId, userId)
            ?: return@withContext Result.failure(IllegalStateException("Книга не найдена"))
        
        // Удаляем физический файл
        entity.localPath?.let { path ->
            runCatching { java.io.File(path).takeIf { it.exists() }?.delete() }
        }
        
        // Обновляем localPath на null вместо удаления всей записи
        // Это позволяет книге остаться в списке, но с иконкой облачка (доступна для скачивания)
        localDataSource.updateLocalPath(bookId, null)
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

