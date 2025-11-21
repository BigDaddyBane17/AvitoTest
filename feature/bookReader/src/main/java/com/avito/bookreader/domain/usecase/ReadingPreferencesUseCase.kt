package com.avito.bookreader.domain.usecase

import com.avito.bookreader.data.ReadingPreferencesManager
import com.avito.common.reader.FontSize
import com.avito.common.reader.LineSpacing
import com.avito.common.reader.ReadingTheme
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ReadingPreferencesUseCase @Inject constructor(
    private val manager: ReadingPreferencesManager
) {
    val fontSize: StateFlow<FontSize> = manager.fontSize
    val lineSpacing: StateFlow<LineSpacing> = manager.lineSpacing
    val theme: StateFlow<ReadingTheme> = manager.theme

    fun setFontSize(size: FontSize) = manager.setFontSize(size)
    fun setLineSpacing(spacing: LineSpacing) = manager.setLineSpacing(spacing)
    fun setTheme(theme: ReadingTheme) = manager.setTheme(theme)
}

