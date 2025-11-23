package com.avito.bookslist.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.avito.bookslist.di.BooksListComponent
import com.avito.bookslist.presentation.BooksListScreen
import com.avito.navigation.TopBarConfig
import com.avito.ui.transition.standardOverlayEnter
import com.avito.ui.transition.standardOverlayExit
import com.avito.ui.transition.standardOverlayPopEnter
import com.avito.ui.transition.standardOverlayPopExit
import kotlinx.serialization.Serializable

@Serializable
data object BooksListRoute

fun NavGraphBuilder.booksListScreen(
    booksListComponentFactory: BooksListComponent.Factory,
    onBookClick: (String) -> Unit = {},
    onTopBarConfigChange: (TopBarConfig?) -> Unit
) {
    composable<BooksListRoute>(
        enterTransition = { standardOverlayEnter() },
        exitTransition = { standardOverlayExit() },
        popEnterTransition = { standardOverlayPopEnter() },
        popExitTransition = { standardOverlayPopExit() }
    ) {
        BooksListScreen(
            booksListComponentFactory = booksListComponentFactory,
            onBookClick = onBookClick,
            onTopBarConfigChange = onTopBarConfigChange
        )
    }
}

