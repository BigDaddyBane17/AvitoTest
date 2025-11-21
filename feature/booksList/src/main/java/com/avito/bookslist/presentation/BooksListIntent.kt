package com.avito.bookslist.presentation

sealed interface BooksListIntent {
    data class QueryChanged(val query: String) : BooksListIntent
    data class BookClicked(val bookId: String) : BooksListIntent
    data class Download(val bookId: String) : BooksListIntent
    data class Delete(val bookId: String) : BooksListIntent
    data class ItemMoved(val fromIndex: Int, val toIndex: Int) : BooksListIntent
    data class SortModeChanged(val sortMode: SortMode) : BooksListIntent
    data object Retry : BooksListIntent
    data object PullToRefresh : BooksListIntent
    data object ToastShown : BooksListIntent
}


