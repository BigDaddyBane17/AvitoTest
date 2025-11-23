package com.avito.auth.presentation

import android.app.Activity
import android.util.Log
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
        Log.d("AuthScreen", "Creating GoogleSignInOptions with webClientId: ${webClientId.take(20)}...")
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember(googleSignInOptions, context) {
        Log.d("AuthScreen", "Creating GoogleSignInClient")
        GoogleSignIn.getClient(context, googleSignInOptions)
    }

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d("AuthScreen", "Google sign-in result received. Result code: ${result.resultCode}")
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            Log.d("AuthScreen", "Google account retrieved. Email: ${account?.email}, Has idToken: ${account?.idToken != null}")
            val token = account?.idToken
            if (token != null) {
                Log.d("AuthScreen", "Google idToken received, length: ${token.length}")
                viewModel.onIntent(AuthIntent.GoogleSignInToken(token))
            } else {
                Log.e("AuthScreen", "Google account retrieved but idToken is null")
                viewModel.onIntent(AuthIntent.GoogleSignInFailed("Google не вернул токен"))
            }
        } catch (e: ApiException) {
            Log.e("AuthScreen", "ApiException during Google sign-in", e)
            Log.e("AuthScreen", "ApiException status code: ${e.statusCode}")
            val message = when (result.resultCode) {
                Activity.RESULT_CANCELED -> {
                    Log.d("AuthScreen", "User canceled Google sign-in")
                    "Вход через Google отменён"
                }
                else -> {
                    val errorMsg = e.message ?: "Не удалось выполнить вход через Google"
                    Log.e("AuthScreen", "Google sign-in failed: $errorMsg")
                    errorMsg
                }
            }
            viewModel.onIntent(AuthIntent.GoogleSignInFailed(message))
        } catch (e: Exception) {
            Log.e("AuthScreen", "Unexpected exception during Google sign-in", e)
            viewModel.onIntent(AuthIntent.GoogleSignInFailed("Неожиданная ошибка: ${e.message}"))
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
                Log.d("AuthScreen", "Google sign-in button clicked")
                googleSignInClient.signOut().addOnCompleteListener {
                    Log.d("AuthScreen", "Google sign-out completed, launching sign-in intent")
                    try {
                        val signInIntent = googleSignInClient.signInIntent
                        Log.d("AuthScreen", "Sign-in intent created, launching...")
                        googleLauncher.launch(signInIntent)
                    } catch (e: Exception) {
                        Log.e("AuthScreen", "Error launching Google sign-in intent", e)
                        viewModel.onIntent(AuthIntent.GoogleSignInFailed("Ошибка запуска авторизации: ${e.message}"))
                    }
                }.addOnFailureListener { e ->
                    Log.e("AuthScreen", "Error during Google sign-out", e)
                    try {
                        googleLauncher.launch(googleSignInClient.signInIntent)
                    } catch (ex: Exception) {
                        Log.e("AuthScreen", "Error launching Google sign-in intent after sign-out failure", ex)
                        viewModel.onIntent(AuthIntent.GoogleSignInFailed("Ошибка запуска авторизации: ${ex.message}"))
                    }
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


