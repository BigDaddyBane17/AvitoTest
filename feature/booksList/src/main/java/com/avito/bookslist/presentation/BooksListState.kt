package com.avito.bookslist.presentation

import com.avito.bookslist.domain.model.LocalBook

sealed interface BooksListUiState {
    data object Loading : BooksListUiState
    data class Content(
        val books: List<LocalBook>,
        val searchQuery: String = "",
        val toastMessage: String? = null,
        val isRefreshing: Boolean = false,
        val sortMode: SortMode = SortMode.Manual,
        val downloadingBookIds: Set<String> = emptySet()
    ) : BooksListUiState {
        val filteredBooks: List<LocalBook>
            get() {
                val ordered = when (sortMode) {
                    SortMode.Manual -> books.sortedBy { it.sortOrder }
                    SortMode.Title -> books.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.title })
                    SortMode.Date -> books.sortedByDescending { it.addedAt }
                }
                return if (searchQuery.isBlank()) {
                    ordered
                } else {
                    ordered.filter {
                        it.title.contains(searchQuery, true) ||
                            it.author.contains(searchQuery, true)
                    }
                }
            }
    }
    data class Error(val message: String) : BooksListUiState
}

enum class SortMode {
    Manual, Title, Date
}

