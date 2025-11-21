package com.avito.bookslist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avito.bookslist.domain.DownloadedBooksRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class BooksListViewModel @Inject constructor(
    private val repository: DownloadedBooksRepository
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
            is BooksListIntent.ItemMoved -> handleManualMove(intent.fromIndex, intent.toIndex)
            is BooksListIntent.SortModeChanged -> updateSortMode(intent.sortMode)
            BooksListIntent.ToastShown -> clearToast()
            is BooksListIntent.BookClicked -> Unit
        }
    }

    private fun observeBooks() {
        viewModelScope.launch {
            repository.booksFlow.collectLatest { books ->
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
            repository.syncRemote(forceRemote)
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
            repository.downloadBook(bookId)
                .onSuccess { showToast("Книга загружена") }
                .onFailure { error -> showToast(error.message ?: "Не удалось скачать книгу") }
        }
    }

    private fun delete(bookId: String) {
        viewModelScope.launch {
            repository.deleteBook(bookId)
                .onSuccess { showToast("Файл удалён") }
                .onFailure { error -> showToast(error.message ?: "Не удалось удалить файл") }
        }
    }

    private fun handleManualMove(from: Int, to: Int) {
        val content = _uiState.value as? BooksListUiState.Content ?: return
        if (content.sortMode != SortMode.Manual) return
        val mutable = content.books.toMutableList()
        if (from !in mutable.indices || to !in mutable.indices) return
        val item = mutable.removeAt(from)
        mutable.add(to, item)
        val reordered = mutable.mapIndexed { index, book -> book.copy(sortOrder = index) }
        _uiState.value = content.copy(books = reordered)
        viewModelScope.launch {
            repository.reorderBooks(reordered.map { it.id })
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
