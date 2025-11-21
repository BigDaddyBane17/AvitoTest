package com.avito.common.reader

enum class FontSize(val scale: Float) {
    SMALL(0.85f),
    MEDIUM(1.0f),
    LARGE(1.15f),
    EXTRA_LARGE(1.35f)
}

enum class LineSpacing(val value: Float) {
    COMPACT(1.2f),
    NORMAL(1.5f),
    RELAXED(1.8f)
}

enum class ReadingTheme {
    LIGHT,
    DARK,
    SEPIA
}

