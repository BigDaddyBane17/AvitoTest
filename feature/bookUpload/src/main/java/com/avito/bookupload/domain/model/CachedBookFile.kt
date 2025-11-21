package com.avito.bookupload.domain.model

import android.net.Uri

data class CachedBookFile(
    val originalUri: Uri,
    val cachedUri: Uri,
    val displayName: String,
    val mimeType: String,
    val absolutePath: String,
    val sizeBytes: Long
)