package com.avito.bookreader.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.avito.bookreader.di.BookReaderComponent
import com.avito.bookreader.presentation.BookReaderScreen
import com.avito.navigation.TopBarConfig
import com.avito.ui.transition.standardOverlayEnter
import com.avito.ui.transition.standardOverlayExit
import com.avito.ui.transition.standardOverlayPopEnter
import com.avito.ui.transition.standardOverlayPopExit
import kotlinx.serialization.Serializable

@Serializable
data class BookReaderRoute(val bookId: String)

fun NavGraphBuilder.bookReaderScreen(
    bookReaderComponentFactory: BookReaderComponent.Factory,
    onTopBarConfigChange: (TopBarConfig?) -> Unit
) {
    composable<BookReaderRoute>(
        enterTransition = { standardOverlayEnter() },
        exitTransition = { standardOverlayExit() },
        popEnterTransition = { standardOverlayPopEnter() },
        popExitTransition = { standardOverlayPopExit() }
    ) { backStackEntry ->
        val route = backStackEntry.toRoute<BookReaderRoute>()
        BookReaderScreen(
            bookReaderComponentFactory = bookReaderComponentFactory,
            bookId = route.bookId,
            onTopBarConfigChange = onTopBarConfigChange
        )
    }
}
