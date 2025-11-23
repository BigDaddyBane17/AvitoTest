package com.avito.bookreader.presentation

import com.avito.common.reader.FontSize
import com.avito.common.reader.LineSpacing
import com.avito.common.reader.ReadingTheme

sealed interface BookReaderIntent {
    data object ToggleSettings : BookReaderIntent
    data class FontSizeChanged(val size: FontSize) : BookReaderIntent
    data class LineSpacingChanged(val spacing: LineSpacing) : BookReaderIntent
    data class ThemeChanged(val theme: ReadingTheme) : BookReaderIntent
    data class ScrollPositionChanged(val position: Int, val maxPosition: Int) : BookReaderIntent
    data object Retry : BookReaderIntent
    data object DeleteBook : BookReaderIntent
}
