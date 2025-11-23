package com.avito.bookreader.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avito.bookreader.di.BookId
import com.avito.common.reader.FontSize
import com.avito.common.reader.LineSpacing
import com.avito.common.reader.ReadingTheme
import com.avito.bookreader.domain.usecase.DeleteLocalBookUseCase
import com.avito.bookreader.domain.usecase.GetReadingProgressUseCase
import com.avito.bookreader.domain.usecase.LoadBookUseCase
import com.avito.bookreader.domain.usecase.ReadingPreferencesUseCase
import com.avito.bookreader.domain.usecase.SaveReadingProgressUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class BookReaderViewModel @Inject constructor(
    private val loadBookUseCase: LoadBookUseCase,
    private val saveReadingProgressUseCase: SaveReadingProgressUseCase,
    private val getReadingProgressUseCase: GetReadingProgressUseCase,
    private val deleteLocalBookUseCase: DeleteLocalBookUseCase,
    private val readingPreferencesUseCase: ReadingPreferencesUseCase,
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

    private fun loadBook() {
        viewModelScope.launch {
            _uiState.value = BookReaderUiState.Loading
            loadBookUseCase(bookId)
                .onSuccess { book ->
                    val savedPosition = getReadingProgressUseCase(bookId)
                    _uiState.value = BookReaderUiState.Content(
                        book = book,
                        fontSize = readingPreferencesUseCase.fontSize.value,
                        lineSpacing = readingPreferencesUseCase.lineSpacing.value,
                        theme = readingPreferencesUseCase.theme.value,
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
            readingPreferencesUseCase.fontSize.collectLatest { fontSize ->
                updateContent { it.copy(fontSize = fontSize) }
            }
        }
        viewModelScope.launch {
            readingPreferencesUseCase.lineSpacing.collectLatest { spacing ->
                updateContent { it.copy(lineSpacing = spacing) }
            }
        }
        viewModelScope.launch {
            readingPreferencesUseCase.theme.collectLatest { theme ->
                updateContent { it.copy(theme = theme) }
            }
        }
    }

    private fun toggleSettings() {
        updateContent { it.copy(isSettingsVisible = !it.isSettingsVisible) }
    }

    private fun changeFontSize(size: FontSize) {
        readingPreferencesUseCase.setFontSize(size)
    }

    private fun changeLineSpacing(spacing: LineSpacing) {
        readingPreferencesUseCase.setLineSpacing(spacing)
    }

    private fun changeTheme(theme: ReadingTheme) {
        readingPreferencesUseCase.setTheme(theme)
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
            saveReadingProgressUseCase(bookId, position)
        }
    }

    private fun deleteBook() {
        viewModelScope.launch {
            deleteLocalBookUseCase(bookId)
            _uiState.value = BookReaderUiState.Error("Книга удалена")
        }
    }

    private fun updateContent(transform: (BookReaderUiState.Content) -> BookReaderUiState.Content) {
        val current = _uiState.value as? BookReaderUiState.Content ?: return
        _uiState.value = transform(current)
    }
}
