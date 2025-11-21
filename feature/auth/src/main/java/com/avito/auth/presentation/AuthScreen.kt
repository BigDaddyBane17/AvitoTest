package com.avito.auth.presentation

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.avito.auth.di.AuthComponent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun AuthScreen(
    authComponentFactory: AuthComponent.Factory,
    webClientId: String,
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier,
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
            Surface(
                modifier = modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun AuthForm(
    state: AuthUiState.Content,
    modifier: Modifier = Modifier,
    onIntent: (AuthIntent) -> Unit,
    onGoogleSignInClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val isSignIn = state.mode == AuthMode.SignIn

    Surface(
        modifier = modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 32.dp, bottom = 16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (isSignIn) "Вход" else "Регистрация",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = if (isSignIn) {
                    "Авторизуйтесь, чтобы получить доступ к своим книгам."
                } else {
                    "Создайте аккаунт, чтобы сохранять избранное и прогресс чтения."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (state.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (!isSignIn) {
                TextField(
                    value = state.name,
                    onValueChange = { onIntent(AuthIntent.NameChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Имя") },
                    enabled = !state.isLoading,
                    singleLine = true,
                    shape = MaterialTheme.shapes.large,
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                    ),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    )
                )
            }

            TextField(
                value = state.email,
                onValueChange = { onIntent(AuthIntent.EmailChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Email") },
                enabled = !state.isLoading,
                singleLine = true,
                shape = MaterialTheme.shapes.large,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )

            TextField(
                value = state.password,
                onValueChange = { onIntent(AuthIntent.PasswordChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Пароль") },
                trailingIcon = {
                    TextButton(
                        onClick = { onIntent(AuthIntent.TogglePasswordVisibility) },
                        enabled = !state.isLoading
                    ) {
                        Text(
                            text = if (state.isPasswordVisible) "Скрыть" else "Показать"
                        )
                    }
                },
                enabled = !state.isLoading,
                singleLine = true,
                shape = MaterialTheme.shapes.large,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                ),
                visualTransformation = if (state.isPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = if (isSignIn) ImeAction.Done else ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onIntent(AuthIntent.Submit) }
                )
            )

            if (!isSignIn) {
                TextField(
                    value = state.confirmPassword,
                    onValueChange = { onIntent(AuthIntent.ConfirmPasswordChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Повторите пароль") },
                    enabled = !state.isLoading,
                    singleLine = true,
                    shape = MaterialTheme.shapes.large,
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { onIntent(AuthIntent.Submit) }
                    )
                )
            }

            state.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            state.infoMessage?.let { info ->
                Text(
                    text = info,
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (state.errorMessage != null || state.infoMessage != null) {
                TextButton(
                    onClick = { onIntent(AuthIntent.DismissMessage) },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Понятно")
                }
            }

            Button(
                onClick = { onIntent(AuthIntent.Submit) },
                enabled = state.isSubmitEnabled && !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(text = if (isSignIn) "Войти" else "Создать аккаунт")
            }

            OutlinedButton(
                onClick = onGoogleSignInClick,
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Продолжить с Google")
            }

            if (isSignIn) {
                TextButton(
                    onClick = { onIntent(AuthIntent.ForgotPassword) },
                    enabled = !state.isLoading,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Забыли пароль?")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = { onIntent(AuthIntent.ToggleMode) },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isSignIn) {
                        "Нет аккаунта? Зарегистрируйтесь"
                    } else {
                        "Уже есть аккаунт? Войдите"
                    },
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}