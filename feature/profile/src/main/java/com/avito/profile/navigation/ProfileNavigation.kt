package com.avito.profile.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.avito.profile.di.ProfileComponent
import com.avito.profile.presentation.ProfileScreen
import com.avito.navigation.TopBarConfig
import com.avito.ui.transition.standardOverlayEnter
import com.avito.ui.transition.standardOverlayExit
import com.avito.ui.transition.standardOverlayPopEnter
import com.avito.ui.transition.standardOverlayPopExit
import kotlinx.serialization.Serializable

@Serializable
data object ProfileRoute

fun NavGraphBuilder.profileScreen(
    profileComponentFactory: ProfileComponent.Factory,
    onLogout: () -> Unit = {},
    onTopBarConfigChange: (TopBarConfig?) -> Unit
) {
    composable<ProfileRoute>(
        enterTransition = { standardOverlayEnter() },
        exitTransition = { standardOverlayExit() },
        popEnterTransition = { standardOverlayPopEnter() },
        popExitTransition = { standardOverlayPopExit() }
    ) {
        ProfileScreen(
            profileComponentFactory = profileComponentFactory,
            onLogout = onLogout,
            onTopBarConfigChange = onTopBarConfigChange
        )
    }
}

