package com.avito.bookupload.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.avito.bookupload.di.BookUploadComponent
import com.avito.bookupload.presentation.BookUploadScreen
import com.avito.navigation.TopBarConfig
import com.avito.ui.transition.standardOverlayEnter
import com.avito.ui.transition.standardOverlayExit
import com.avito.ui.transition.standardOverlayPopEnter
import com.avito.ui.transition.standardOverlayPopExit
import kotlinx.serialization.Serializable

@Serializable
data object BookUploadRoute

fun NavGraphBuilder.bookUploadScreen(
    bookUploadComponentFactory: BookUploadComponent.Factory,
    onUploadCompleted: () -> Unit = {},
    onTopBarConfigChange: (TopBarConfig?) -> Unit
) {
    composable<BookUploadRoute>(
        enterTransition = { standardOverlayEnter() },
        exitTransition = { standardOverlayExit() },
        popEnterTransition = { standardOverlayPopEnter() },
        popExitTransition = { standardOverlayPopExit() }
    ) {
        BookUploadScreen(
            bookUploadComponentFactory = bookUploadComponentFactory,
            onUploadCompleted = onUploadCompleted,
            onTopBarConfigChange = onTopBarConfigChange
        )
    }
}

