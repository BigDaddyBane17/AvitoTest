package com.avito.profile.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.avito.profile.presentation.ProfileScreen
import kotlinx.serialization.Serializable

@Serializable
data object ProfileRoute

fun NavGraphBuilder.profileScreen(
    onLogout: () -> Unit = {}
) {
    composable<ProfileRoute> {
        ProfileScreen(onLogout = onLogout)
    }
}

