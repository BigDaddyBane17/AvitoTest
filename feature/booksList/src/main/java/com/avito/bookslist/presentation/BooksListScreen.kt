package com.avito.bookslist.presentation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.avito.bookslist.di.BooksListComponent
import com.avito.bookslist.domain.model.LocalBook
import com.avito.feature.bookslist.R
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BooksListScreen(
    booksListComponentFactory: BooksListComponent.Factory,
    onBookClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val component = remember(booksListComponentFactory) { booksListComponentFactory.create() }
    val viewModelFactory = remember(component) { component.viewModelFactory() }
    val viewModel: BooksListViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val toastMessage = (uiState as? BooksListUiState.Content)?.toastMessage
    LaunchedEffect(toastMessage) {
        if (!toastMessage.isNullOrBlank()) {
            Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
            viewModel.onIntent(BooksListIntent.ToastShown)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            BooksListTopBar(
                state = uiState,
                onQueryChange = { viewModel.onIntent(BooksListIntent.QueryChanged(it)) },
                onSortModeChange = { viewModel.onIntent(BooksListIntent.SortModeChanged(it)) }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            BooksListUiState.Loading -> BooksListLoadingState(
                modifier = Modifier.padding(padding)
            )

            is BooksListUiState.Error -> BooksListErrorState(
                message = state.message,
                onRetry = { viewModel.onIntent(BooksListIntent.Retry) },
                modifier = Modifier.padding(padding)
            )

            is BooksListUiState.Content -> {
                SwipeRefresh(
                    state = rememberSwipeRefreshState(state.isRefreshing),
                    onRefresh = { viewModel.onIntent(BooksListIntent.PullToRefresh) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    if (state.filteredBooks.isEmpty()) {
                        BooksListEmptyState(
                            isSearching = state.searchQuery.isNotBlank(),
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        BooksLazyContent(
                            state = state,
                            onBookClick = onBookClick,
                            onDownload = { viewModel.onIntent(BooksListIntent.Download(it)) },
                            onDelete = { viewModel.onIntent(BooksListIntent.Delete(it)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BooksListTopBar(
    state: BooksListUiState,
    onQueryChange: (String) -> Unit,
    onSortModeChange: (SortMode) -> Unit
) {
    val query = (state as? BooksListUiState.Content)?.searchQuery.orEmpty()
    val sortMode = (state as? BooksListUiState.Content)?.sortMode ?: SortMode.Manual

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = stringResource(id = R.string.books_list_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null
                )
            },
            placeholder = {
                Text(
                    text = stringResource(
                        id = R.string.books_list_search_placeholder
                    )
                )
            },
            singleLine = true,
            shape = MaterialTheme.shapes.large,
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        SortModeChipsRow(
            sortMode = sortMode,
            onSortModeChange = onSortModeChange
        )
    }
}

@Composable
private fun SortModeChipsRow(
    sortMode: SortMode,
    onSortModeChange: (SortMode) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(SortMode.entries) { mode ->
            AssistChip(
                onClick = { onSortModeChange(mode) },
                label = { Text(text = stringResource(id = mode.titleRes)) },
                leadingIcon = {
                    if (mode == SortMode.Manual) {
                        Icon(Icons.Outlined.DragHandle, contentDescription = null)
                    } else {
                        Icon(Icons.Outlined.Refresh, contentDescription = null)
                    }
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (mode == sortMode)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun BooksLazyContent(
    state: BooksListUiState.Content,
    onBookClick: (String) -> Unit,
    onDownload: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(
            bottom = 80.dp,
            top = 8.dp,
            start = 16.dp,
            end = 16.dp
        )
    ) {
        items(
            items = state.filteredBooks,
            key = { book -> book.id }
        ) { book ->
            val dismissState = rememberDismissState(confirmStateChange = { value ->
                if (value == androidx.compose.material.DismissValue.DismissedToStart && book.isDownloaded) {
                    onDelete(book.id)
                }
                false
            })

            if (book.isDownloaded) {
                SwipeToDismiss(
                    state = dismissState,
                    directions = setOf(androidx.compose.material.DismissDirection.EndToStart),
                    background = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.errorContainer)
                                .padding(end = 32.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    },
                    dismissContent = {
                        BookRow(
                            book = book,
                            onClick = { 
                                if (book.isDownloaded) {
                                    onBookClick(book.id)
                                }
                            },
                            onAction = {
                                onDelete(book.id)
                            }
                        )
                    }
                )
            } else {
                BookRow(
                    book = book,
                    onClick = { },
                    onAction = {
                        onDownload(book.id)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookRow(
    book: LocalBook,
    onClick: () -> Unit,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BookThumbnail(
                initial = book.title.firstOrNull(),
                isDownloaded = book.isDownloaded
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = book.author,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!book.isDownloaded) {
                    Text(
                        text = stringResource(
                            id = R.string.books_list_available_download
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            IconButton(onClick = onAction) {
                Icon(
                    imageVector = if (book.isDownloaded)
                        Icons.Outlined.Delete
                    else
                        Icons.Outlined.CloudDownload,
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
private fun BookThumbnail(
    initial: Char?,
    isDownloaded: Boolean
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .background(
                color = if (isDownloaded)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial?.toString().orEmpty(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun BooksListEmptyState(
    isSearching: Boolean,
    modifier: Modifier = Modifier
) {
    val animationRes = if (isSearching) {
        R.raw.books_search_empty
    } else {
        R.raw.books_empty
    }
    val messageRes = if (isSearching) {
        R.string.books_list_empty_search
    } else {
        R.string.books_list_empty
    }

    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(animationRes)
    )
    val progress by animateLottieCompositionAsState(composition)

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(220.dp)
        )
        Text(
            text = stringResource(id = messageRes),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun BooksListErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = onRetry) {
            Text(text = stringResource(id = R.string.books_list_retry))
        }
    }
}

@Composable
private fun BooksListLoadingState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

private val SortMode.titleRes: Int
    get() = when (this) {
        SortMode.Manual -> R.string.books_list_sort_manual
        SortMode.Title -> R.string.books_list_sort_title
        SortMode.Date -> R.string.books_list_sort_date
    }
