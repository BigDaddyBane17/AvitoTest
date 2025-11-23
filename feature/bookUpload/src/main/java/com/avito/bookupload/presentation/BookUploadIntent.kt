package com.avito.bookupload.presentation

import android.net.Uri

sealed interface BookUploadIntent {
    data class TitleChanged(val value: String) : BookUploadIntent
    data class AuthorChanged(val value: String) : BookUploadIntent
    data class FileSelected(val uri: Uri) : BookUploadIntent
    data object UploadClicked : BookUploadIntent
    data object Retry : BookUploadIntent
    data object ResetSuccess : BookUploadIntent
    data object DismissMessage : BookUploadIntent
}
