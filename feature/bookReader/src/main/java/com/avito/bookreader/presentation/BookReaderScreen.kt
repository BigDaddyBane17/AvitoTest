package com.avito.bookreader.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.avito.bookreader.di.BookReaderComponent
import com.avito.bookreader.presentation.composable.ContentState
import com.avito.bookreader.presentation.composable.ErrorState
import com.avito.navigation.TopBarConfig
import com.avito.ui.components.UiLoadingState

@Composable
fun BookReaderScreen(
    bookReaderComponentFactory: BookReaderComponent.Factory,
    bookId: String,
    modifier: Modifier = Modifier,
    onTopBarConfigChange: (TopBarConfig?) -> Unit
) {
    val component = remember(bookReaderComponentFactory, bookId) {
        bookReaderComponentFactory.create(bookId)
    }
    val viewModelFactory = remember(component) { component.viewModelFactory() }
    val viewModel: BookReaderViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        when (uiState) {
            is BookReaderUiState.Content -> {
                onTopBarConfigChange(
                    TopBarConfig(
                        title = (uiState as BookReaderUiState.Content).book.title,
                        showBackButton = true,
                        actions = {
                            IconButton(onClick = { viewModel.onIntent(BookReaderIntent.ToggleSettings) }) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Настройки",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    )
                )
            }
            else -> {
                onTopBarConfigChange(
                    TopBarConfig(
                        title = "Чтение",
                        showBackButton = true
                    )
                )
            }
        }
    }

    when (val state = uiState) {
        BookReaderUiState.Loading -> UiLoadingState(modifier = modifier)
        is BookReaderUiState.Error -> ErrorState(
            message = state.message,
            onRetry = { viewModel.onIntent(BookReaderIntent.Retry) },
            onDelete = { viewModel.onIntent(BookReaderIntent.DeleteBook) },
            modifier = modifier
        )
        is BookReaderUiState.Content -> ContentState(
            state = state,
            onIntent = viewModel::onIntent,
            modifier = modifier
        )
    }
}

