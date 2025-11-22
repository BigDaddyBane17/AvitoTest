package com.avito.bookreader.data

import android.content.Context
import android.content.SharedPreferences
import com.avito.bookreader.di.BookReaderScope
import com.avito.bookreader.domain.model.BookContent
import com.avito.bookreader.domain.model.BookFormat
import com.avito.bookreader.domain.repository.BookReaderRepository
import com.avito.database.source.BooksLocalDataSource
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.zip.ZipFile
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
            PDFBoxResourceLoader.init(context)
            
            val bookEntity = localDataSource.getBook(bookId)
                ?: throw IllegalStateException("Книга не найдена")

            val localPath = bookEntity.localPath
                ?: throw IllegalStateException("Книга не скачана")

            val file = File(localPath)
            if (!file.exists()) {
                throw IllegalStateException("Файл книги не найден")
            }

            val format = detectFormat(file.extension)
            val text = when (format) {
                BookFormat.TXT -> parseTxt(file)
                BookFormat.EPUB -> parseEpub(file)
                BookFormat.PDF -> parsePdf(file)
                BookFormat.UNKNOWN -> file.readText()
            }

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
            localDataSource.deleteBook(bookId)
        }
    }

    private fun parseTxt(file: File): String {
        return file.readText(Charsets.UTF_8)
    }

    private fun parseEpub(file: File): String {
        val builder = StringBuilder()
        
        ZipFile(file).use { zipFile ->
            val entries = zipFile.entries()
            val contentFiles = mutableListOf<String>()
            
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                val name = entry.name.lowercase()
                
                if ((name.endsWith(".html") || name.endsWith(".xhtml") || name.endsWith(".htm"))
                    && !entry.isDirectory) {
                    contentFiles.add(entry.name)
                }
            }
            
            contentFiles.sorted().forEach { fileName ->
                zipFile.getEntry(fileName)?.let { entry ->
                    zipFile.getInputStream(entry).use { inputStream ->
                        val content = inputStream.readBytes().toString(Charsets.UTF_8)
                        
                        val cleanContent = content
                            .replace(Regex("<script[^>]*>.*?</script>", RegexOption.DOT_MATCHES_ALL), "")
                            .replace(Regex("<style[^>]*>.*?</style>", RegexOption.DOT_MATCHES_ALL), "")
                            .replace(Regex("<[^>]*>"), "")
                            .replace("&nbsp;", " ")
                            .replace("&quot;", "\"")
                            .replace("&amp;", "&")
                            .replace("&lt;", "<")
                            .replace("&gt;", ">")
                            .replace("&apos;", "'")
                            .replace(Regex("\\s+"), " ")
                            .trim()
                        
                        if (cleanContent.isNotBlank()) {
                            builder.append(cleanContent)
                            builder.append("\n\n")
                        }
                    }
                }
            }
        }
        
        return builder.toString().trim()
    }

    private fun parsePdf(file: File): String {
        PDDocument.load(file).use { document ->
            val stripper = PDFTextStripper()
            return stripper.getText(document)
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

