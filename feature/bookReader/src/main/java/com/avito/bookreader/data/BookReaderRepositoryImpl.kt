package com.avito.bookreader.data

import android.content.Context
import android.content.SharedPreferences
import com.avito.bookreader.di.BookReaderScope
import com.avito.bookreader.domain.BookContent
import com.avito.bookreader.domain.BookFormat
import com.avito.bookreader.domain.BookReaderRepository
import com.avito.database.BooksLocalDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@BookReaderScope
class BookReaderRepositoryImpl @Inject constructor(
    private val localDataSource: BooksLocalDataSource,
    private val context: Context
) : BookReaderRepository {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override suspend fun loadBook(bookId: String): Result<BookContent> = withContext(Dispatchers.IO) {
        runCatching {
            val bookEntity = localDataSource.getBook(bookId)
                ?: throw IllegalStateException("Книга не найдена")

            val localPath = bookEntity.localPath
                ?: throw IllegalStateException("Книга не скачана")

            val file = File(localPath)
            if (!file.exists()) {
                throw IllegalStateException("Файл книги не найден")
            }

            val text = file.readText()
            val format = detectFormat(file.extension)

            BookContent(
                id = bookEntity.id,
                title = bookEntity.title,
                author = bookEntity.author,
                text = text,
                format = format
            )
        }
    }

    override suspend fun saveReadingProgress(bookId: String, position: Int) {
        withContext(Dispatchers.IO) {
            prefs.edit()
                .putInt(progressKey(bookId), position)
                .apply()
        }
    }

    override suspend fun getReadingProgress(bookId: String): Int {
        return withContext(Dispatchers.IO) {
            prefs.getInt(progressKey(bookId), 0)
        }
    }

    override suspend fun deleteBook(bookId: String) {
        withContext(Dispatchers.IO) {
            val bookEntity = localDataSource.getBook(bookId)
            bookEntity?.localPath?.let { path ->
                val file = File(path)
                if (file.exists()) {
                    file.delete()
                }
            }
            // Удаляем localPath из базы данных
            localDataSource.updateLocalPath(bookId, null)
        }
    }

    private fun detectFormat(extension: String): BookFormat {
        return when (extension.lowercase()) {
            "txt" -> BookFormat.TXT
            "epub" -> BookFormat.EPUB
            "pdf" -> BookFormat.PDF
            else -> BookFormat.UNKNOWN
        }
    }

    private fun progressKey(bookId: String) = "reading_progress_$bookId"

    companion object {
        private const val PREFS_NAME = "book_reader_prefs"
    }
}

