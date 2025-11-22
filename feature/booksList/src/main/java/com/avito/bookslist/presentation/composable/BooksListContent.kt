package com.avito.bookslist.presentation.composable

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.avito.bookslist.domain.model.LocalBook
import com.avito.bookslist.presentation.BooksListIntent
import com.avito.bookslist.presentation.BooksListUiState
import com.avito.bookslist.presentation.SortMode
import com.avito.feature.bookslist.R
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun BooksListContent(
    state: BooksListUiState.Content,
    onIntent: (BooksListIntent) -> Unit,
    onBookClick: (String) -> Unit,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    SwipeRefresh(
        state = rememberSwipeRefreshState(state.isRefreshing),
        onRefresh = { onIntent(BooksListIntent.PullToRefresh) },
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search bar
            SearchBar(
                value = state.searchQuery,
                onValueChange = onQueryChange,
                placeholder = "Поиск книг…",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            
            SortModeChipsRow(
                sortMode = state.sortMode,
                onSortModeChange = { onIntent(BooksListIntent.SortModeChanged(it)) }
            )
            if (state.filteredBooks.isEmpty()) {
                BooksListEmptyState(
                    isSearching = state.searchQuery.isNotBlank(),
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                BooksLazyContent(
                    state = state,
                    onBookClick = onBookClick,
                    onDownload = { onIntent(BooksListIntent.Download(it)) },
                    onDelete = { onIntent(BooksListIntent.Delete(it)) }
                )
            }
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
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    
    LaunchedEffect(state.searchQuery, state.sortMode) {
        listState.scrollToItem(0)
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(
            items = state.filteredBooks,
            key = { book -> "${book.id}_${book.isDownloaded}" }
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
                        val isRevealed = dismissState.targetValue != androidx.compose.material.DismissValue.Default
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(MaterialTheme.shapes.large)
                                .background(
                                    if (isRevealed) MaterialTheme.colorScheme.errorContainer
                                    else androidx.compose.ui.graphics.Color.Transparent
                                )
                                .padding(end = 32.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            if (isRevealed) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
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
                            onAction = { onDelete(book.id) },
                            isDownloading = book.id in state.downloadingBookIds
                        )
                    }
                )
            } else {
                BookRow(
                    book = book,
                    onClick = { },
                    onAction = { onDownload(book.id) },
                    isDownloading = book.id in state.downloadingBookIds
                )
            }
        }
    }
}

@Composable
private fun BookRow(
    book: LocalBook,
    onClick: () -> Unit,
    onAction: () -> Unit,
    isDownloading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        ),
        shape = MaterialTheme.shapes.large,
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
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = book.author,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
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
            IconButton(onClick = onAction, enabled = !isDownloading) {
                Icon(
                    imageVector = if (book.isDownloaded)
                        Icons.Outlined.Delete
                    else
                        Icons.Outlined.CloudDownload,
                    contentDescription = null
                )
            }
        }
        if (isDownloading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
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
private fun SortModeChipsRow(
    sortMode: SortMode,
    onSortModeChange: (SortMode) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(SortMode.entries) { mode ->
            AssistChip(
                onClick = { onSortModeChange(mode) },
                label = { Text(text = stringResource(id = mode.titleRes)) },
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

@Composable
private fun SearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    // Используем локальное состояние для TextField, чтобы избежать проблем с курсором
    var textFieldValue by remember { mutableStateOf(value) }
    
    // Синхронизируем только если значение изменилось извне (не пользователем)
    // Используем LaunchedEffect чтобы избежать проблем с рекомпозицией
    LaunchedEffect(value) {
        // Обновляем только если значение действительно изменилось извне
        // и локальное значение отличается от внешнего
        if (textFieldValue != value) {
            textFieldValue = value
        }
    }
    
    TextField(
        value = textFieldValue,
        onValueChange = { newValue ->
            textFieldValue = newValue
            onValueChange(newValue)
        },
        modifier = modifier,
        placeholder = { Text(text = placeholder) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
}

private val SortMode.titleRes: Int
    get() = when (this) {
        SortMode.Manual -> R.string.books_list_sort_manual
        SortMode.Title -> R.string.books_list_sort_title
        SortMode.Date -> R.string.books_list_sort_date
    }

