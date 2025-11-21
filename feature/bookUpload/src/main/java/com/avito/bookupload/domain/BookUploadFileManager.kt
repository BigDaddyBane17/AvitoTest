package com.avito.bookupload.domain

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject

class BookUploadFileManager @Inject constructor(
    private val context: Context
) {

    suspend fun cache(uri: Uri): CachedBookFile = withContext(Dispatchers.IO) {
        val displayName = resolveDisplayName(uri)
            ?: "book_${UUID.randomUUID()}"
        val mimeType = context.contentResolver.getType(uri) ?: guessMimeFromName(displayName)

        val cacheDir = File(context.cacheDir, CACHE_DIR).apply { if (!exists()) mkdirs() }
        val cachedFile = File(cacheDir, "${UUID.randomUUID()}_${displayName}")

        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(cachedFile).use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalStateException("Не удалось открыть файл")

        CachedBookFile(
            originalUri = uri,
            cachedUri = cachedFile.toUri(),
            displayName = displayName,
            mimeType = mimeType,
            absolutePath = cachedFile.absolutePath,
            sizeBytes = cachedFile.length()
        )
    }

    fun resolveDisplayName(uri: Uri): String? {
        val contentResolver = context.contentResolver
        return when (uri.scheme) {
            ContentResolver.SCHEME_FILE -> uri.lastPathSegment
            ContentResolver.SCHEME_CONTENT -> contentResolver
                .query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
                }

            else -> null
        }
    }

    private fun guessMimeFromName(name: String): String =
        when (name.substringAfterLast('.', "").lowercase()) {
            "pdf" -> MIME_PDF
            "epub" -> MIME_EPUB
            "txt" -> MIME_TEXT
            else -> MIME_OCTET
        }

    data class CachedBookFile(
        val originalUri: Uri,
        val cachedUri: Uri,
        val displayName: String,
        val mimeType: String,
        val absolutePath: String,
        val sizeBytes: Long
    )

    companion object {
        private const val CACHE_DIR = "book_uploads"
        private const val MIME_TEXT = "text/plain"
        private const val MIME_EPUB = "application/epub+zip"
        private const val MIME_PDF = "application/pdf"
        private const val MIME_OCTET = "application/octet-stream"
    }
}

