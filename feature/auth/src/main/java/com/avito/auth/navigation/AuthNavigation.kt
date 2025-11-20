package com.avito.auth.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.avito.auth.presentation.AuthScreen
import kotlinx.serialization.Serializable

@Serializable
data object AuthRoute

fun NavGraphBuilder.authScreen(
    onAuthSuccess: () -> Unit
) {
    composable<AuthRoute> {
        AuthScreen(
            onAuthSuccess = onAuthSuccess
        )
    }
}