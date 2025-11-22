package com.avito.auth.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.avito.auth.di.AuthComponent
import com.avito.auth.presentation.AuthScreen
import com.avito.navigation.TopBarConfig
import com.avito.ui.transition.standardOverlayEnter
import com.avito.ui.transition.standardOverlayExit
import com.avito.ui.transition.standardOverlayPopEnter
import com.avito.ui.transition.standardOverlayPopExit
import kotlinx.serialization.Serializable

@Serializable
data object AuthRoute

fun NavGraphBuilder.authScreen(
    authComponentFactory: AuthComponent.Factory,
    webClientId: String,
    onAuthSuccess: () -> Unit,
    onTopBarConfigChange: (TopBarConfig?) -> Unit
) {
    composable<AuthRoute>(
        enterTransition = { standardOverlayEnter() },
        exitTransition = { standardOverlayExit() },
        popEnterTransition = { standardOverlayPopEnter() },
        popExitTransition = { standardOverlayPopExit() }
    ) {
        AuthScreen(
            authComponentFactory = authComponentFactory,
            webClientId = webClientId,
            onAuthSuccess = onAuthSuccess,
            onTopBarConfigChange = onTopBarConfigChange
        )
    }
}