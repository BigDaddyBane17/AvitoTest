package com.avito.bookupload.domain.usecase

import com.avito.bookupload.domain.BookUploadValidator
import javax.inject.Inject

class ValidateBookUploadUseCase @Inject constructor(
    private val validator: BookUploadValidator
) {
    fun validateForm(title: String, author: String, cachedFilePath: String?) =
        validator.validateForm(title, author, cachedFilePath)

    fun isFileSupported(fileName: String?, mimeType: String?) =
        validator.isFileSupported(fileName, mimeType)
}

