package com.avito.bookreader.domain.model

data class BookContent(
    val id: String,
    val title: String,
    val author: String,
    val text: String,
    val format: BookFormat
)

enum class BookFormat {
    TXT, EPUB, PDF, UNKNOWN
}

