package com.avito.bookupload.domain.usecase

import android.net.Uri
import com.avito.bookupload.domain.BookUploadFileManager
import javax.inject.Inject

class CacheBookFileUseCase @Inject constructor(
    private val fileManager: BookUploadFileManager
) {
    suspend operator fun invoke(uri: Uri) = fileManager.cache(uri)
}

