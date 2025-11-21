package com.avito.bookreader.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.avito.bookreader.di.BookReaderComponent
import com.avito.bookreader.presentation.BookReaderScreen
import kotlinx.serialization.Serializable

@Serializable
data class BookReaderRoute(val bookId: String)

fun NavGraphBuilder.bookReaderScreen(
    bookReaderComponentFactory: BookReaderComponent.Factory,
    onSettingsClickChanged: ((() -> Unit)?) -> Unit
) {
    composable<BookReaderRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<BookReaderRoute>()
        BookReaderScreen(
            bookReaderComponentFactory = bookReaderComponentFactory,
            bookId = route.bookId,
            onSettingsClickChanged = onSettingsClickChanged
        )
    }
}
