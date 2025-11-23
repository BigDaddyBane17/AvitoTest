package com.avito.auth.presentation

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.avito.auth.di.AuthComponent
import com.avito.auth.presentation.composable.AuthForm
import com.avito.navigation.TopBarConfig
import com.avito.ui.components.UiLoadingState
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun AuthScreen(
    authComponentFactory: AuthComponent.Factory,
    webClientId: String,
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    onTopBarConfigChange: (TopBarConfig?) -> Unit
) {
    val context = LocalContext.current
    val authComponent = remember(authComponentFactory) { authComponentFactory.create() }
    val viewModelFactory = remember(authComponent) { authComponent.viewModelFactory() }
    val viewModel: AuthViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val googleSignInOptions = remember(webClientId) {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember(googleSignInOptions, context) {
        GoogleSignIn.getClient(context, googleSignInOptions)
    }

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val token = account?.idToken
            if (token != null) {
                viewModel.onIntent(AuthIntent.GoogleSignInToken(token))
            } else {
                viewModel.onIntent(AuthIntent.GoogleSignInFailed("Google не вернул токен"))
            }
        } catch (e: ApiException) {
            val message = when (result.resultCode) {
                Activity.RESULT_CANCELED -> "Вход через Google отменён"
                else -> e.message ?: "Не удалось выполнить вход через Google"
            }
            viewModel.onIntent(AuthIntent.GoogleSignInFailed(message))
        }
    }

    LaunchedEffect(uiState) {
        val content = uiState as? AuthUiState.Content ?: return@LaunchedEffect
        val isSignIn = content.mode == AuthMode.SignIn
        onTopBarConfigChange(
            TopBarConfig(
                title = if (isSignIn) "Вход в аккаунт" else "Регистрация",
                actions = {
                    IconButton(onClick = { viewModel.onIntent(AuthIntent.ToggleMode) }) {
                        Icon(
                            imageVector = if (isSignIn) Icons.Default.Add else Icons.Default.AccountCircle,
                            contentDescription = if (isSignIn) "Регистрация" else "Войти",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        )
    }

    when (val state = uiState) {
        is AuthUiState.Content -> AuthForm(
            state = state,
            modifier = modifier,
            onIntent = viewModel::onIntent,
            onGoogleSignInClick = {
                googleSignInClient.signOut().addOnCompleteListener {
                    googleLauncher.launch(googleSignInClient.signInIntent)
                }
            }
        )
        AuthUiState.Success -> {
            LaunchedEffect(Unit) {
                onAuthSuccess()
            }
            UiLoadingState(modifier = modifier.fillMaxSize())
        }
    }
}

