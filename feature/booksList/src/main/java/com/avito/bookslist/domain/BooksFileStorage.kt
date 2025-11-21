package com.avito.bookslist.domain

import android.content.Context
import com.avito.bookslist.di.BooksListScope
import java.io.File
import javax.inject.Inject

@BooksListScope
class BooksFileStorage @Inject constructor(
    private val context: Context
) {

    fun resolveLibraryDir(): File =
        File(context.filesDir, LIBRARY_DIR).apply { if (!exists()) mkdirs() }

    fun createDestinationFile(bookId: String, extension: String): File {
        val safeExt = extension.ifBlank { "bin" }
        return File(resolveLibraryDir(), "$bookId.$safeExt")
    }

    companion object {
        private const val LIBRARY_DIR = "books_library"
    }
}


