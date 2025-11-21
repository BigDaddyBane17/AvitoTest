package com.avito.bookupload.domain

import android.net.Uri
import com.avito.bookupload.domain.model.CachedBookFile

interface BookFileCache {

    suspend fun cache(uri: Uri): CachedBookFile
}
