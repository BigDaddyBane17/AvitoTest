package com.avito.bookslist.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.avito.bookslist.presentation.BooksListScreen
import kotlinx.serialization.Serializable

@Serializable
data object BooksListRoute

fun NavGraphBuilder.booksListScreen(
    onBookClick: (String) -> Unit = {}
) {
    composable<BooksListRoute> {
        BooksListScreen(onBookClick = onBookClick)
    }
}

