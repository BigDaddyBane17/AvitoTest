package com.avito.bookreader.data

import android.content.Context
import android.content.SharedPreferences
import com.avito.bookreader.di.BookReaderScope
import com.avito.common.reader.FontSize
import com.avito.common.reader.LineSpacing
import com.avito.common.reader.ReadingTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@BookReaderScope
class ReadingPreferencesManager @Inject constructor(
    private val context: Context
) {
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val _fontSize = MutableStateFlow(loadFontSize())
    val fontSize: StateFlow<FontSize> = _fontSize.asStateFlow()

    private val _lineSpacing = MutableStateFlow(loadLineSpacing())
    val lineSpacing: StateFlow<LineSpacing> = _lineSpacing.asStateFlow()

    private val _theme = MutableStateFlow(loadTheme())
    val theme: StateFlow<ReadingTheme> = _theme.asStateFlow()

    fun setFontSize(size: FontSize) {
        _fontSize.value = size
        prefs.edit().putString(KEY_FONT_SIZE, size.name).apply()
    }

    fun setLineSpacing(spacing: LineSpacing) {
        _lineSpacing.value = spacing
        prefs.edit().putString(KEY_LINE_SPACING, spacing.name).apply()
    }

    fun setTheme(theme: ReadingTheme) {
        _theme.value = theme
        prefs.edit().putString(KEY_THEME, theme.name).apply()
    }

    private fun loadFontSize(): FontSize {
        val name = prefs.getString(KEY_FONT_SIZE, FontSize.MEDIUM.name)
        return FontSize.entries.find { it.name == name } ?: FontSize.MEDIUM
    }

    private fun loadLineSpacing(): LineSpacing {
        val name = prefs.getString(KEY_LINE_SPACING, LineSpacing.NORMAL.name)
        return LineSpacing.entries.find { it.name == name } ?: LineSpacing.NORMAL
    }

    private fun loadTheme(): ReadingTheme {
        val name = prefs.getString(KEY_THEME, ReadingTheme.LIGHT.name)
        return ReadingTheme.entries.find { it.name == name } ?: ReadingTheme.LIGHT
    }

    companion object {
        private const val PREFS_NAME = "reading_preferences"
        private const val KEY_FONT_SIZE = "font_size"
        private const val KEY_LINE_SPACING = "line_spacing"
        private const val KEY_THEME = "theme"
    }
}

