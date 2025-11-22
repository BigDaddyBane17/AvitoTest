package com.avito.bookslist.presentation

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.avito.bookslist.di.BooksListComponent
import com.avito.bookslist.presentation.composable.BooksListContent
import com.avito.navigation.TopBarConfig
import com.avito.navigation.TopBarSearchConfig
import com.avito.ui.components.ScreenDefaults
import com.avito.ui.components.UiErrorState
import com.avito.ui.components.UiLoadingState
import com.avito.ui.transition.standardOverlayTransform
import com.avito.feature.bookslist.R

@Composable
fun BooksListScreen(
    booksListComponentFactory: BooksListComponent.Factory,
    onBookClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    onTopBarConfigChange: (TopBarConfig?) -> Unit
) {
    val component = remember(booksListComponentFactory) { booksListComponentFactory.create() }
    val viewModelFactory = remember(component) { component.viewModelFactory() }
    val viewModel: BooksListViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Устанавливаем TopBarConfig сразу при входе на экран
    LaunchedEffect(Unit) {
        val initialConfig = TopBarConfig(
            title = "Мои книги"
        )
        onTopBarConfigChange(initialConfig)
        viewModel.onIntent(BooksListIntent.Refresh)
    }

    val toastMessage = (uiState as? BooksListUiState.Content)?.toastMessage
    LaunchedEffect(toastMessage) {
        if (!toastMessage.isNullOrBlank()) {
            Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
            viewModel.onIntent(BooksListIntent.ToastShown)
        }
    }

    AnimatedContent(
        targetState = uiState,
        contentKey = { it::class },
        transitionSpec = { standardOverlayTransform() },
        label = "books_list_state",
        modifier = modifier.fillMaxSize()
    ) { state ->
        when (state) {
            BooksListUiState.Loading -> UiLoadingState()

            is BooksListUiState.Error -> UiErrorState(
                message = state.message,
                retryText = stringResource(id = R.string.books_list_retry),
                onRetry = { viewModel.onIntent(BooksListIntent.Retry) }
            )

            is BooksListUiState.Content -> BooksListContent(
                state = state,
                onIntent = viewModel::onIntent,
                onBookClick = onBookClick,
                onQueryChange = { query -> viewModel.onIntent(BooksListIntent.QueryChanged(query)) }
            )
                }
    }
}

