package com.avito.bookupload.domain

import javax.inject.Inject

sealed interface FormValidation {
    data object Valid : FormValidation
    data class Invalid(val reason: String) : FormValidation
}

class BookUploadValidator @Inject constructor() {

    fun isFileSupported(fileName: String?, mimeType: String?): Boolean {
        if (fileName.isNullOrBlank() && mimeType.isNullOrBlank()) return false

        val normalizedName = fileName
            ?.substringAfterLast('.', missingDelimiterValue = "")
            ?.lowercase()

        val normalizedMime = mimeType?.lowercase()

        return normalizedName in SUPPORTED_EXTENSIONS ||
                normalizedMime in SUPPORTED_MIME_TYPES
    }

    fun validateForm(
        title: String,
        author: String,
        cachedFilePath: String?
    ): FormValidation {
        if (title.isBlank()) return FormValidation.Invalid("Укажите название книги")
        if (author.isBlank()) return FormValidation.Invalid("Укажите автора")
        if (cachedFilePath.isNullOrBlank()) return FormValidation.Invalid("Выберите файл книги")
        return FormValidation.Valid
    }

    companion object {
        private val SUPPORTED_EXTENSIONS = setOf("pdf", "epub", "txt")

        private val SUPPORTED_MIME_TYPES = setOf(
            "application/pdf",
            "application/epub+zip",
            "text/plain"
        )
    }
}
