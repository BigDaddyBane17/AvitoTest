package com.avito.bookreader.presentation

import com.avito.bookreader.domain.BookContent
import com.avito.bookreader.domain.FontSize
import com.avito.bookreader.domain.LineSpacing
import com.avito.bookreader.domain.ReadingTheme

sealed interface BookReaderUiState {
    data object Loading : BookReaderUiState
    data class Error(val message: String) : BookReaderUiState
    data class Content(
        val book: BookContent,
        val fontSize: FontSize = FontSize.MEDIUM,
        val lineSpacing: LineSpacing = LineSpacing.NORMAL,
        val theme: ReadingTheme = ReadingTheme.LIGHT,
        val scrollPosition: Int = 0,
        val isSettingsVisible: Boolean = false,
        val readingProgress: Float = 0f
    ) : BookReaderUiState
}
