package com.avito.bookupload.presentation.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.avito.bookupload.presentation.BookUploadUiState
import com.avito.feature.bookupload.R
import com.avito.ui.transition.standardFadeIn
import com.avito.ui.transition.standardFadeOut
import kotlin.math.max

@Composable
fun BookUploadContent(
    state: BookUploadUiState,
    onTitleChange: (String) -> Unit,
    onAuthorChange: (String) -> Unit,
    onPickFile: () -> Unit,
    onUpload: () -> Unit,
    onRetry: () -> Unit,
    onReset: () -> Unit,
    onDismissMessage: () -> Unit,
    modifier: Modifier = Modifier
) {
    UploadForm(
        state = state,
        onTitleChange = onTitleChange,
        onAuthorChange = onAuthorChange,
        onPickFile = onPickFile,
        onUpload = onUpload,
        onRetry = onRetry,
        onReset = onReset,
        onDismissMessage = onDismissMessage,
        modifier = modifier
    )
}

@Composable
private fun UploadForm(
    state: BookUploadUiState,
    onTitleChange: (String) -> Unit,
    onAuthorChange: (String) -> Unit,
    onPickFile: () -> Unit,
    onUpload: () -> Unit,
    onRetry: () -> Unit,
    onReset: () -> Unit,
    onDismissMessage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val titleValue = state.titleValue
    val authorValue = state.authorValue
    val selectedFile = state.selectedFileNameValue
    val fileSize = state.fileSizeValue
    val isUploading = state is BookUploadUiState.Uploading
    val canUpload = state.cachedFilePathValue != null && !isUploading
    val progressFraction = state.progressValue / 100f
    val errorMessage = state.errorMessageValue
    val infoMessage = state.infoMessageValue
    val showRetry = state is BookUploadUiState.Error
    val showSuccessAnimation = state is BookUploadUiState.Success
    val animatedProgress = androidx.compose.animation.core.animateFloatAsState(
        targetValue = progressFraction,
        label = "book-upload-progress"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextField(
            value = titleValue,
            onValueChange = onTitleChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(id = R.string.book_upload_title_hint)) },
            enabled = !isUploading,
            singleLine = true,
            shape = MaterialTheme.shapes.large,
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                disabledIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
            )
        )

        TextField(
            value = authorValue,
            onValueChange = onAuthorChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(id = R.string.book_upload_author_hint)) },
            enabled = !isUploading,
            singleLine = true,
            shape = MaterialTheme.shapes.large,
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                disabledIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
            )
        )

        Button(
            onClick = onPickFile,
            enabled = !isUploading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(id = R.string.book_upload_pick_file))
        }

        Text(
            text = stringResource(id = R.string.book_upload_supported_formats),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        AnimatedVisibility(
            visible = selectedFile != null,
            enter = standardFadeIn(),
            exit = standardFadeOut()
        ) {
            FileCard(fileName = selectedFile.orEmpty(), fileSize = fileSize)
        }

        AnimatedVisibility(
            visible = isUploading,
            enter = standardFadeIn(),
            exit = standardFadeOut()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.book_upload_uploading),
                    style = MaterialTheme.typography.bodyMedium
                )
                LinearProgressIndicator(
                    progress = animatedProgress.value.coerceIn(0f, 1f),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (errorMessage != null) {
            ErrorCard(
                message = errorMessage,
                onRetry = onRetry,
                showRetry = showRetry
            )
        }

        if (!infoMessage.isNullOrBlank()) {
            InfoCard(
                message = infoMessage,
                onDismiss = onDismissMessage
            )
        }

        AnimatedVisibility(
            visible = showSuccessAnimation,
            enter = standardFadeIn(),
            exit = standardFadeOut()
        ) {
            SuccessAnimation()
        }

        Button(
            onClick = onUpload,
            enabled = canUpload,
            modifier = Modifier.fillMaxWidth()
        ) {
            when {
                isUploading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(text = stringResource(id = R.string.book_upload_uploading))
                }

                else -> Text(text = stringResource(id = R.string.book_upload_upload))
            }
        }

        if (state is BookUploadUiState.Success) {
            TextButton(onClick = onReset) {
                Text(text = stringResource(id = R.string.book_upload_upload_another))
            }
        }
    }
}

@Composable
private fun FileCard(
    fileName: String,
    fileSize: Long?
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = fileName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (fileSize != null) {
                val sizeKb = max(1, fileSize / 1024)
                Text(
                    text = stringResource(id = R.string.book_upload_file_size, sizeKb),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ErrorCard(
    message: String,
    onRetry: () -> Unit,
    showRetry: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            if (showRetry) {
                Button(
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(id = R.string.book_upload_retry))
                }
            }
        }
    }
}

@Composable
private fun InfoCard(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.book_upload_hide))
            }
        }
    }
}

@Composable
private fun SuccessAnimation() {
    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.RawRes(R.raw.book_upload_success)
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(160.dp)
        )
    }
}

private val BookUploadUiState.titleValue: String
    get() = when (this) {
        is BookUploadUiState.Idle -> title
        is BookUploadUiState.Uploading -> title
        is BookUploadUiState.Success -> title
        is BookUploadUiState.Error -> title
    }

private val BookUploadUiState.authorValue: String
    get() = when (this) {
        is BookUploadUiState.Idle -> author
        is BookUploadUiState.Uploading -> author
        is BookUploadUiState.Success -> author
        is BookUploadUiState.Error -> author
    }

private val BookUploadUiState.selectedFileNameValue: String?
    get() = when (this) {
        is BookUploadUiState.Idle -> selectedFileName
        is BookUploadUiState.Uploading -> selectedFileName
        is BookUploadUiState.Success -> selectedFileName
        is BookUploadUiState.Error -> selectedFileName
    }

private val BookUploadUiState.fileSizeValue: Long?
    get() = when (this) {
        is BookUploadUiState.Idle -> fileSizeBytes
        is BookUploadUiState.Uploading -> fileSizeBytes
        is BookUploadUiState.Success -> fileSizeBytes
        is BookUploadUiState.Error -> fileSizeBytes
    }

private val BookUploadUiState.cachedFilePathValue: String?
    get() = when (this) {
        is BookUploadUiState.Idle -> cachedFilePath
        is BookUploadUiState.Uploading -> cachedFilePath
        is BookUploadUiState.Success -> cachedFilePath
        is BookUploadUiState.Error -> cachedFilePath
    }

private val BookUploadUiState.infoMessageValue: String?
    get() = when (this) {
        is BookUploadUiState.Idle -> infoMessage
        is BookUploadUiState.Uploading -> infoMessage
        is BookUploadUiState.Success -> infoMessage
        is BookUploadUiState.Error -> infoMessage
    }

private val BookUploadUiState.errorMessageValue: String?
    get() = when (this) {
        is BookUploadUiState.Idle -> errorMessage
        is BookUploadUiState.Uploading -> errorMessage
        is BookUploadUiState.Success -> errorMessage
        is BookUploadUiState.Error -> errorMessage
    }

private val BookUploadUiState.progressValue: Int
    get() = when (this) {
        is BookUploadUiState.Idle -> progress
        is BookUploadUiState.Uploading -> progress
        is BookUploadUiState.Success -> progress
        is BookUploadUiState.Error -> progress
    }

