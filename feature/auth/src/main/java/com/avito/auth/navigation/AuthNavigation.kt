package com.avito.auth.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.avito.auth.di.AuthComponent
import com.avito.auth.presentation.AuthScreen
import kotlinx.serialization.Serializable

@Serializable
data object AuthRoute

fun NavGraphBuilder.authScreen(
    authComponentFactory: AuthComponent.Factory,
    webClientId: String,
    onAuthSuccess: () -> Unit
) {
    composable<AuthRoute> {
        AuthScreen(
            authComponentFactory = authComponentFactory,
            webClientId = webClientId,
            onAuthSuccess = onAuthSuccess
        )
    }
}