package com.avito.bookreader.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.avito.bookreader.presentation.BookReaderScreen
import kotlinx.serialization.Serializable

@Serializable
data object BookReaderRoute

fun NavGraphBuilder.bookReaderScreen() {
    composable<BookReaderRoute> {
        BookReaderScreen()
    }
}

