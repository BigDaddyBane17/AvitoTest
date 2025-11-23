package com.avito.bookupload.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.avito.bookupload.domain.BookUploadValidator
import com.avito.bookupload.domain.FormValidation
import com.avito.bookupload.domain.usecase.CacheBookFileUseCase
import com.avito.bookupload.domain.usecase.ValidateBookUploadUseCase
import com.avito.bookupload.work.BookUploadWorker
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class BookUploadViewModel @Inject constructor(
    private val cacheBookFileUseCase: CacheBookFileUseCase,
    private val validateBookUploadUseCase: ValidateBookUploadUseCase,
    private val workManager: WorkManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<BookUploadUiState>(BookUploadUiState.Idle())
    val uiState: StateFlow<BookUploadUiState> = _uiState.asStateFlow()

    private var currentWorkId: UUID? = null
    private var progressJob: Job? = null

    fun onIntent(intent: BookUploadIntent) {
        when (intent) {
            is BookUploadIntent.TitleChanged -> updateIdleState {
                it.copy(title = intent.value, errorMessage = null, infoMessage = null)
            }

            is BookUploadIntent.AuthorChanged -> updateIdleState {
                it.copy(author = intent.value, errorMessage = null, infoMessage = null)
            }

            is BookUploadIntent.FileSelected -> cacheFile(intent.uri)

            BookUploadIntent.UploadClicked -> startUpload()

            BookUploadIntent.Retry -> startUpload()

            BookUploadIntent.ResetSuccess -> resetAfterSuccess()

            BookUploadIntent.DismissMessage -> dismissMessages()
        }
    }

    private fun cacheFile(uri: Uri) {
        val current = editableState()
        viewModelScope.launch {
            runCatching { cacheBookFileUseCase(uri) }
                .onSuccess { cached ->
                    if (!validateBookUploadUseCase.isFileSupported(cached.displayName, cached.mimeType)) {
                        _uiState.value = BookUploadUiState.Error(
                            title = current.title,
                            author = current.author,
                            selectedFileName = null,
                            selectedFileUri = null,
                            cachedFilePath = null,
                            mimeType = null,
                            fileSizeBytes = null,
                            errorMessage = "Этот формат не поддерживается"
                        )
                        return@onSuccess
                    }
                    _uiState.value = BookUploadUiState.Idle(
                        title = current.title,
                        author = current.author,
                        selectedFileName = cached.displayName,
                        selectedFileUri = cached.originalUri,
                        cachedFilePath = cached.absolutePath,
                        mimeType = cached.mimeType,
                        fileSizeBytes = cached.sizeBytes,
                        infoMessage = null,
                        errorMessage = null
                    )
                }
                .onFailure { error ->
                    _uiState.value = BookUploadUiState.Error(
                        title = current.title,
                        author = current.author,
                        selectedFileName = current.selectedFileName,
                        selectedFileUri = current.selectedFileUri,
                        cachedFilePath = current.cachedFilePath,
                        mimeType = current.mimeType,
                        fileSizeBytes = current.fileSizeBytes,
                        errorMessage = error.message ?: "Не удалось обработать файл"
                    )
                }
        }
    }

    private fun startUpload() {
        val current = (_uiState.value as? BookUploadUiState.Idle)
            ?: (_uiState.value as? BookUploadUiState.Error)?.toIdle()
            ?: return

        when (val validation = validateBookUploadUseCase.validateForm(current.title, current.author, current.cachedFilePath)) {
            is FormValidation.Invalid -> {
                _uiState.value = current.copy(errorMessage = validation.reason)
                return
            }

            FormValidation.Valid -> Unit
        }

        val cachedPath = current.cachedFilePath ?: return
        val request = OneTimeWorkRequestBuilder<BookUploadWorker>()
            .setInputData(
                workDataOf(
                    BookUploadWorker.KEY_TITLE to current.title,
                    BookUploadWorker.KEY_AUTHOR to current.author,
                    BookUploadWorker.KEY_FILE_PATH to cachedPath,
                    BookUploadWorker.KEY_FILE_NAME to (current.selectedFileName ?: "book.txt"),
                    BookUploadWorker.KEY_MIME_TYPE to (current.mimeType ?: "application/octet-stream")
                )
            )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        currentWorkId?.let { workManager.cancelWorkById(it) }
        currentWorkId = request.id

        workManager.enqueueUniqueWork(
            "book_upload_${request.id}",
            ExistingWorkPolicy.REPLACE,
            request
        )
        observeWork(request.id)

        _uiState.value = BookUploadUiState.Uploading(
            title = current.title,
            author = current.author,
            selectedFileName = current.selectedFileName,
            selectedFileUri = current.selectedFileUri,
            cachedFilePath = cachedPath,
            mimeType = current.mimeType,
            fileSizeBytes = current.fileSizeBytes,
            progress = 0
        )
    }

    private fun observeWork(id: UUID) {
        progressJob?.cancel()
        progressJob = workManager.getWorkInfoByIdFlow(id)
            .onEach { info -> handleWorkInfo(info) }
            .launchIn(viewModelScope)
    }

    private fun handleWorkInfo(info: WorkInfo?) {
        info ?: return
        when (info.state) {
            WorkInfo.State.SUCCEEDED -> handleSuccess(info)
            WorkInfo.State.FAILED -> handleFailure(info)
            WorkInfo.State.CANCELLED -> _uiState.value = editableState()
            WorkInfo.State.RUNNING -> updateProgress(info)
            else -> Unit
        }
    }

    private fun updateProgress(info: WorkInfo) {
        val current = _uiState.value as? BookUploadUiState.Uploading ?: return
        val progressValue = info.progress.getInt(BookUploadWorker.KEY_PROGRESS, current.progress)
        _uiState.value = current.copy(progress = progressValue.coerceIn(0, 100))
    }

    private fun handleSuccess(info: WorkInfo) {
        val snapshot = (_uiState.value as? BookUploadUiState.Uploading)?.snapshot()
            ?: editableState().snapshot()
        val fileUrl = info.outputData.getString(BookUploadWorker.KEY_OUTPUT_FILE_URL).orEmpty()
        val localPath = info.outputData.getString(BookUploadWorker.KEY_OUTPUT_LOCAL_PATH)

        _uiState.value = BookUploadUiState.Success(
            title = snapshot.title,
            author = snapshot.author,
            selectedFileName = snapshot.selectedFileName,
            selectedFileUri = snapshot.selectedFileUri,
            cachedFilePath = snapshot.cachedFilePath,
            mimeType = snapshot.mimeType,
            fileSizeBytes = snapshot.fileSizeBytes,
            fileUrl = fileUrl,
            localPath = localPath
        )
        progressJob?.cancel()
    }

    private fun handleFailure(info: WorkInfo) {
        val current = editableState()
        val error = info.outputData.getString(BookUploadWorker.KEY_ERROR)
            ?: "Не удалось загрузить книгу"
        _uiState.value = BookUploadUiState.Error(
            title = current.title,
            author = current.author,
            selectedFileName = current.selectedFileName,
            selectedFileUri = current.selectedFileUri,
            cachedFilePath = current.cachedFilePath,
            mimeType = current.mimeType,
            fileSizeBytes = current.fileSizeBytes,
            errorMessage = error
        )
        progressJob?.cancel()
    }

    private fun resetAfterSuccess() {
        val current = _uiState.value
        if (current is BookUploadUiState.Success) {
            _uiState.value = BookUploadUiState.Idle(
                title = "",
                author = "",
                selectedFileName = null,
                selectedFileUri = null,
                cachedFilePath = null,
                mimeType = null,
                fileSizeBytes = null,
                infoMessage = null,
                errorMessage = null
            )
        }
    }

    private fun dismissMessages() {
        when (val state = _uiState.value) {
            is BookUploadUiState.Idle -> _uiState.value = state.copy(infoMessage = null, errorMessage = null)
            is BookUploadUiState.Success -> _uiState.value = state.copy(infoMessage = null, errorMessage = null)
            is BookUploadUiState.Error -> _uiState.value = state.copy(infoMessage = null, errorMessage = null)
            else -> Unit
        }
    }

    private fun updateIdleState(transform: (BookUploadUiState.Idle) -> BookUploadUiState.Idle) {
        val current = editableState()
        _uiState.value = transform(current)
    }

    private fun editableState(): BookUploadUiState.Idle =
        when (val current = _uiState.value) {
            is BookUploadUiState.Idle -> current
            is BookUploadUiState.Uploading -> BookUploadUiState.Idle(
                title = current.title,
                author = current.author,
                selectedFileName = current.selectedFileName,
                selectedFileUri = current.selectedFileUri,
                cachedFilePath = current.cachedFilePath,
                mimeType = current.mimeType,
                fileSizeBytes = current.fileSizeBytes
            )

            is BookUploadUiState.Success -> current.toIdle()
            is BookUploadUiState.Error -> current.toIdle()
        }

    private fun BookUploadUiState.Success.toIdle(): BookUploadUiState.Idle =
        BookUploadUiState.Idle(
            title = title,
            author = author,
            selectedFileName = selectedFileName,
            selectedFileUri = selectedFileUri,
            cachedFilePath = cachedFilePath,
            mimeType = mimeType,
            fileSizeBytes = fileSizeBytes
        )

    private fun BookUploadUiState.Error.toIdle(): BookUploadUiState.Idle =
        BookUploadUiState.Idle(
            title = title,
            author = author,
            selectedFileName = selectedFileName,
            selectedFileUri = selectedFileUri,
            cachedFilePath = cachedFilePath,
            mimeType = mimeType,
            fileSizeBytes = fileSizeBytes
        )

    private fun BookUploadUiState.snapshot(): UploadSnapshot = when (this) {
        is BookUploadUiState.Idle -> UploadSnapshot(
            title = title,
            author = author,
            selectedFileName = selectedFileName,
            selectedFileUri = selectedFileUri,
            cachedFilePath = cachedFilePath,
            mimeType = mimeType,
            fileSizeBytes = fileSizeBytes
        )

        is BookUploadUiState.Uploading -> UploadSnapshot(
            title = title,
            author = author,
            selectedFileName = selectedFileName,
            selectedFileUri = selectedFileUri,
            cachedFilePath = cachedFilePath,
            mimeType = mimeType,
            fileSizeBytes = fileSizeBytes
        )

        is BookUploadUiState.Success -> UploadSnapshot(
            title = title,
            author = author,
            selectedFileName = selectedFileName,
            selectedFileUri = selectedFileUri,
            cachedFilePath = cachedFilePath,
            mimeType = mimeType,
            fileSizeBytes = fileSizeBytes
        )

        is BookUploadUiState.Error -> UploadSnapshot(
            title = title,
            author = author,
            selectedFileName = selectedFileName,
            selectedFileUri = selectedFileUri,
            cachedFilePath = cachedFilePath,
            mimeType = mimeType,
            fileSizeBytes = fileSizeBytes
        )
    }

    private data class UploadSnapshot(
        val title: String,
        val author: String,
        val selectedFileName: String?,
        val selectedFileUri: Uri?,
        val cachedFilePath: String?,
        val mimeType: String?,
        val fileSizeBytes: Long?
    )
}