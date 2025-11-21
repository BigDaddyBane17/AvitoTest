package com.avito.bookreader.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avito.bookreader.data.ReadingPreferencesManager
import com.avito.bookreader.di.BookId
import com.avito.bookreader.domain.BookReaderRepository
import com.avito.bookreader.domain.FontSize
import com.avito.bookreader.domain.LineSpacing
import com.avito.bookreader.domain.ReadingTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class BookReaderViewModel @Inject constructor(
    private val repository: BookReaderRepository,
    private val preferencesManager: ReadingPreferencesManager,
    @BookId private val bookId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<BookReaderUiState>(BookReaderUiState.Loading)
    val uiState: StateFlow<BookReaderUiState> = _uiState.asStateFlow()

    init {
        loadBook()
        observePreferences()
    }

    fun onIntent(intent: BookReaderIntent) {
        when (intent) {
            BookReaderIntent.ToggleSettings -> toggleSettings()
            is BookReaderIntent.FontSizeChanged -> changeFontSize(intent.size)
            is BookReaderIntent.LineSpacingChanged -> changeLineSpacing(intent.spacing)
            is BookReaderIntent.ThemeChanged -> changeTheme(intent.theme)
            is BookReaderIntent.ScrollPositionChanged -> updateScrollPosition(intent.position, intent.maxPosition)
            BookReaderIntent.Retry -> loadBook()
            BookReaderIntent.DeleteBook -> deleteBook()
        }
    }

    private fun deleteBook() {
        viewModelScope.launch {
            repository.deleteBook(bookId)
            // После удаления показываем состояние ошибки, что книга удалена
            _uiState.value = BookReaderUiState.Error("Книга удалена")
        }
    }

    private fun loadBook() {
        viewModelScope.launch {
            _uiState.value = BookReaderUiState.Loading
            repository.loadBook(bookId)
                .onSuccess { book ->
                    val savedPosition = repository.getReadingProgress(bookId)
                    _uiState.value = BookReaderUiState.Content(
                        book = book,
                        fontSize = preferencesManager.fontSize.value,
                        lineSpacing = preferencesManager.lineSpacing.value,
                        theme = preferencesManager.theme.value,
                        scrollPosition = savedPosition
                    )
                }
                .onFailure { error ->
                    _uiState.value = BookReaderUiState.Error(
                        error.message ?: "Не удалось загрузить книгу"
                    )
                }
        }
    }

    private fun observePreferences() {
        viewModelScope.launch {
            preferencesManager.fontSize.collectLatest { fontSize ->
                updateContent { it.copy(fontSize = fontSize) }
            }
        }
        viewModelScope.launch {
            preferencesManager.lineSpacing.collectLatest { spacing ->
                updateContent { it.copy(lineSpacing = spacing) }
            }
        }
        viewModelScope.launch {
            preferencesManager.theme.collectLatest { theme ->
                updateContent { it.copy(theme = theme) }
            }
        }
    }

    private fun toggleSettings() {
        updateContent { it.copy(isSettingsVisible = !it.isSettingsVisible) }
    }

    private fun changeFontSize(size: FontSize) {
        preferencesManager.setFontSize(size)
    }

    private fun changeLineSpacing(spacing: LineSpacing) {
        preferencesManager.setLineSpacing(spacing)
    }

    private fun changeTheme(theme: ReadingTheme) {
        preferencesManager.setTheme(theme)
    }

    private fun updateScrollPosition(position: Int, maxPosition: Int) {
        val current = _uiState.value as? BookReaderUiState.Content ?: return
        val progress = if (maxPosition > 0) {
            (position.toFloat() / maxPosition) * 100f
        } else 0f

        _uiState.value = current.copy(
            scrollPosition = position,
            readingProgress = progress.coerceIn(0f, 100f)
        )

        viewModelScope.launch {
            repository.saveReadingProgress(bookId, position)
        }
    }

    private fun updateContent(transform: (BookReaderUiState.Content) -> BookReaderUiState.Content) {
        val current = _uiState.value as? BookReaderUiState.Content ?: return
        _uiState.value = transform(current)
    }
}
