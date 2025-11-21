package com.avito.bookupload.presentation

import android.net.Uri

sealed interface BookUploadUiState {
    val title: String
    val author: String
    val selectedFileName: String?
    val selectedFileUri: Uri?
    val cachedFilePath: String?
    val mimeType: String?
    val fileSizeBytes: Long?
    val infoMessage: String?
    val errorMessage: String?
    val progress: Int
    val isUploading: Boolean
    val showRetry: Boolean
    val showSuccessAnimation: Boolean

    data class Idle(
        override val title: String = "",
        override val author: String = "",
        override val selectedFileName: String? = null,
        override val selectedFileUri: Uri? = null,
        override val cachedFilePath: String? = null,
        override val mimeType: String? = null,
        override val fileSizeBytes: Long? = null,
        override val infoMessage: String? = null,
        override val errorMessage: String? = null,
        override val progress: Int = 0,
        override val isUploading: Boolean = false,
        override val showRetry: Boolean = false,
        override val showSuccessAnimation: Boolean = false
    ) : BookUploadUiState

    data class Uploading(
        override val title: String,
        override val author: String,
        override val selectedFileName: String?,
        override val selectedFileUri: Uri?,
        override val cachedFilePath: String?,
        override val mimeType: String?,
        override val fileSizeBytes: Long?,
        override val progress: Int,
        override val infoMessage: String? = null,
        override val errorMessage: String? = null
    ) : BookUploadUiState {
        override val isUploading: Boolean = true
        override val showRetry: Boolean = false
        override val showSuccessAnimation: Boolean = false
    }

    data class Success(
        override val title: String,
        override val author: String,
        override val selectedFileName: String?,
        override val selectedFileUri: Uri?,
        override val cachedFilePath: String?,
        override val mimeType: String?,
        override val fileSizeBytes: Long?,
        val fileUrl: String,
        val localPath: String?,
        override val infoMessage: String?,
        override val progress: Int = 100
    ) : BookUploadUiState {
        override val errorMessage: String? = null
        override val isUploading: Boolean = false
        override val showRetry: Boolean = false
        override val showSuccessAnimation: Boolean = true
    }

    data class Error(
        override val title: String,
        override val author: String,
        override val selectedFileName: String?,
        override val selectedFileUri: Uri?,
        override val cachedFilePath: String?,
        override val mimeType: String?,
        override val fileSizeBytes: Long?,
        override val errorMessage: String?,
        override val infoMessage: String? = null,
        override val progress: Int = 0
    ) : BookUploadUiState {
        override val isUploading: Boolean = false
        override val showRetry: Boolean = true
        override val showSuccessAnimation: Boolean = false
    }
}
