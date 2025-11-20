package com.avito.auth.presentation

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
fun AuthScreen(
    authComponentFactory: AuthComponent.Factory,
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val authComponent = remember(authComponentFactory) { authComponentFactory.create() }
    val viewModelFactory = remember(authComponent) { authComponent.viewModelFactory() }
    val viewModel: AuthViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is AuthUiState.Content -> AuthForm(
            state = state,
            modifier = modifier,
            onIntent = viewModel::onIntent
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
    onIntent: (AuthIntent) -> Unit
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
                OutlinedTextField(
                    value = state.name,
                    onValueChange = { onIntent(AuthIntent.NameChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Имя") },
                    enabled = !state.isLoading,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    )
                )
            }

            OutlinedTextField(
                value = state.email,
                onValueChange = { onIntent(AuthIntent.EmailChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Email") },
                enabled = !state.isLoading,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )

            OutlinedTextField(
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
                OutlinedTextField(
                    value = state.confirmPassword,
                    onValueChange = { onIntent(AuthIntent.ConfirmPasswordChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Повторите пароль") },
                    enabled = !state.isLoading,
                    singleLine = true,
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