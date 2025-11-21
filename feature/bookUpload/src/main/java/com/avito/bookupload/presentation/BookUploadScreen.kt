package com.avito.bookupload.presentation

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.avito.bookupload.di.BookUploadComponent
import com.avito.feature.bookupload.R
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookUploadScreen(
    bookUploadComponentFactory: BookUploadComponent.Factory,
    onUploadCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val component = remember(bookUploadComponentFactory) {
        bookUploadComponentFactory.create()
    }
    val viewModelFactory = remember(component) { component.viewModelFactory() }
    val viewModel: BookUploadViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val mimeTypes = remember {
        arrayOf("application/pdf", "application/epub+zip", "text/plain")
    }
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri?.let {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            viewModel.onIntent(BookUploadIntent.FileSelected(it))
        }
    }

    val isSuccess = uiState is BookUploadUiState.Success
    LaunchedEffect(isSuccess) {
        if (isSuccess) onUploadCompleted()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(id = R.string.book_upload_title)) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            UploadForm(
                state = uiState,
                onTitleChange = { viewModel.onIntent(BookUploadIntent.TitleChanged(it)) },
                onAuthorChange = { viewModel.onIntent(BookUploadIntent.AuthorChanged(it)) },
                onPickFile = { filePicker.launch(mimeTypes) },
                onUpload = { viewModel.onIntent(BookUploadIntent.UploadClicked) },
                onRetry = { viewModel.onIntent(BookUploadIntent.Retry) },
                onReset = { viewModel.onIntent(BookUploadIntent.ResetSuccess) },
                onDismissMessage = { viewModel.onIntent(BookUploadIntent.DismissMessage) }
            )
        }
    }
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
) {
    val titleValue = state.title
    val authorValue = state.author
    val selectedFile = state.selectedFileName
    val fileSize = state.fileSizeBytes
    val isUploading = state.isUploading
    val canUpload = state.cachedFilePath != null && !isUploading
    val progressValue = state.progress / 100f
    val animatedProgress = androidx.compose.animation.core.animateFloatAsState(
        targetValue = progressValue,
        label = "book-upload-progress"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = titleValue,
            onValueChange = onTitleChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(id = R.string.book_upload_title_hint)) },
            enabled = !isUploading,
            singleLine = true
        )

        OutlinedTextField(
            value = authorValue,
            onValueChange = onAuthorChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(id = R.string.book_upload_author_hint)) },
            enabled = !isUploading,
            singleLine = true
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

        AnimatedVisibility(visible = selectedFile != null) {
            FileCard(fileName = selectedFile.orEmpty(), fileSize = fileSize)
        }

        AnimatedVisibility(visible = isUploading) {
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

        if (state.errorMessage != null) {
            ErrorCard(
                message = state.errorMessage ?: "",
                onRetry = onRetry,
                showRetry = state.showRetry
            )
        }

        if (state.infoMessage != null && state.infoMessage?.isNotBlank() == true) {
            InfoCard(
                message = state.infoMessage ?: "",
                onDismiss = onDismissMessage
            )
        }

        AnimatedVisibility(visible = state.showSuccessAnimation) {
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
                        modifier = Modifier
                            .size(20.dp),
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
                OutlinedButton(onClick = onRetry) {
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
