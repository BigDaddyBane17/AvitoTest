package com.avito.bookupload.presentation

import android.net.Uri

sealed interface BookUploadUiState {

    data class Idle(
        val title: String = "",
        val author: String = "",
        val selectedFileName: String? = null,
        val selectedFileUri: Uri? = null,
        val cachedFilePath: String? = null,
        val mimeType: String? = null,
        val fileSizeBytes: Long? = null,
        val infoMessage: String? = null,
        val errorMessage: String? = null,
        val progress: Int = 0
    ) : BookUploadUiState


    data class Uploading(
        val title: String,
        val author: String,
        val selectedFileName: String?,
        val selectedFileUri: Uri?,
        val cachedFilePath: String?,
        val mimeType: String?,
        val fileSizeBytes: Long?,
        val infoMessage: String? = null,
        val errorMessage: String? = null,
        val progress: Int = 0
    ) : BookUploadUiState

    data class Success(
        val title: String,
        val author: String,
        val selectedFileName: String?,
        val selectedFileUri: Uri?,
        val cachedFilePath: String?,
        val mimeType: String?,
        val fileSizeBytes: Long?,
        val fileUrl: String,
        val localPath: String?,
        val infoMessage: String? = "Книга успешно загружена",
        val errorMessage: String? = null,
        val progress: Int = 100
    ) : BookUploadUiState

    data class Error(
        val title: String,
        val author: String,
        val selectedFileName: String?,
        val selectedFileUri: Uri?,
        val cachedFilePath: String?,
        val mimeType: String?,
        val fileSizeBytes: Long?,
        val infoMessage: String? = null,
        val errorMessage: String?,
        val progress: Int = 0
    ) : BookUploadUiState
}
