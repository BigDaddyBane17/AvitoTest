package com.avito.bookupload.presentation

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.avito.bookupload.di.BookUploadComponent
import com.avito.bookupload.presentation.composable.BookUploadContent
import com.avito.navigation.TopBarConfig
import com.avito.ui.components.ScreenDefaults
import com.avito.ui.transition.standardOverlayTransform

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BookUploadScreen(
    bookUploadComponentFactory: BookUploadComponent.Factory,
    onUploadCompleted: () -> Unit,
    modifier: Modifier = Modifier,
    onTopBarConfigChange: (TopBarConfig?) -> Unit
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

    LaunchedEffect(Unit) {
        onTopBarConfigChange(
            TopBarConfig(
                title = "Загрузка книги",
                showBackButton = true
            )
        )
    }

    AnimatedContent(
        targetState = uiState,
        contentKey = { it::class },
        transitionSpec = { standardOverlayTransform() },
        label = "book_upload_state",
        modifier = modifier
            .fillMaxSize()
            .padding(ScreenDefaults.ContentPadding)
            .padding(top = 16.dp)
    ) { state ->
        when (state) {
            is BookUploadUiState.Idle,
            is BookUploadUiState.Uploading,
            is BookUploadUiState.Success,
            is BookUploadUiState.Error -> {
                BookUploadContent(
                    state = state,
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
}

