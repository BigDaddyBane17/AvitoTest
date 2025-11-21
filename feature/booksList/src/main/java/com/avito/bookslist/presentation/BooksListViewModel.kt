package com.avito.bookslist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avito.bookslist.domain.usecase.DeleteBookUseCase
import com.avito.bookslist.domain.usecase.DownloadBookUseCase
import com.avito.bookslist.domain.usecase.ObserveBooksUseCase
import com.avito.bookslist.domain.usecase.SyncBooksUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class BooksListViewModel @Inject constructor(
    private val observeBooksUseCase: ObserveBooksUseCase,
    private val syncBooksUseCase: SyncBooksUseCase,
    private val downloadBookUseCase: DownloadBookUseCase,
    private val deleteBookUseCase: DeleteBookUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<BooksListUiState>(BooksListUiState.Loading)
    val uiState: StateFlow<BooksListUiState> = _uiState.asStateFlow()

    init {
        observeBooks()
        refresh(forceRemote = true)
    }

    fun onIntent(intent: BooksListIntent) {
        when (intent) {
            is BooksListIntent.QueryChanged -> updateQuery(intent.query)
            is BooksListIntent.Download -> download(intent.bookId)
            is BooksListIntent.Delete -> delete(intent.bookId)
            BooksListIntent.Retry -> refresh(forceRemote = true)
            BooksListIntent.PullToRefresh -> refresh(forceRemote = true)
            is BooksListIntent.SortModeChanged -> updateSortMode(intent.sortMode)
            BooksListIntent.ToastShown -> clearToast()
            is BooksListIntent.BookClicked -> Unit
        }
    }

    private fun observeBooks() {
        viewModelScope.launch {
            observeBooksUseCase().collectLatest { books ->
                val currentQuery = (uiState.value as? BooksListUiState.Content)?.searchQuery.orEmpty()
                val currentSort = (uiState.value as? BooksListUiState.Content)?.sortMode ?: SortMode.Manual
                val isRefreshing = (uiState.value as? BooksListUiState.Content)?.isRefreshing ?: false
                val toast = (uiState.value as? BooksListUiState.Content)?.toastMessage
                _uiState.value = BooksListUiState.Content(
                    books = books,
                    searchQuery = currentQuery,
                    toastMessage = toast,
                    isRefreshing = isRefreshing,
                    sortMode = currentSort
                )
            }
        }
    }

    private fun refresh(forceRemote: Boolean) {
        viewModelScope.launch {
            val hadContent = _uiState.value is BooksListUiState.Content
            if (hadContent) {
                setRefreshing(true)
            } else {
                _uiState.value = BooksListUiState.Loading
            }
            syncBooksUseCase(forceRemote)
                .onFailure { error ->
                    val hasContent = (_uiState.value as? BooksListUiState.Content)
                        ?.books
                        ?.isNotEmpty() == true
                    if (hasContent) {
                        showToast(error.message ?: "Не удалось обновить список")
                    } else {
                        _uiState.value = BooksListUiState.Error(
                            error.message ?: "Не удалось загрузить книги"
                        )
                    }
                }
            if (hadContent) {
                setRefreshing(false)
            }
        }
    }

    private fun updateQuery(query: String) {
        updateContent { it.copy(searchQuery = query) }
    }

    private fun download(bookId: String) {
        viewModelScope.launch {
            downloadBookUseCase(bookId)
                .onSuccess { showToast("Книга загружена") }
                .onFailure { error -> showToast(error.message ?: "Не удалось скачать книгу") }
        }
    }

    private fun delete(bookId: String) {
        viewModelScope.launch {
            deleteBookUseCase(bookId)
                .onSuccess { showToast("Файл удалён") }
                .onFailure { error -> showToast(error.message ?: "Не удалось удалить файл") }
        }
    }

    private fun updateSortMode(sortMode: SortMode) {
        updateContent {
            if (it.sortMode == sortMode) it else it.copy(sortMode = sortMode)
        }
    }

    private fun setRefreshing(value: Boolean) {
        val current = ensureContent()
        _uiState.value = current.copy(isRefreshing = value)
    }

    private fun showToast(message: String) {
        updateContent { it.copy(toastMessage = message) }
    }

    private fun clearToast() {
        updateContent { it.copy(toastMessage = null) }
    }

    private fun updateContent(transform: (BooksListUiState.Content) -> BooksListUiState.Content) {
        val current = ensureContent()
        _uiState.value = transform(current)
    }

    private fun ensureContent(): BooksListUiState.Content {
        val current = _uiState.value as? BooksListUiState.Content
        if (current != null) return current
        val fallback = BooksListUiState.Content(books = emptyList())
        _uiState.value = fallback
        return fallback
    }
}
