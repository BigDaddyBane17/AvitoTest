package com.avito.auth.presentation.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.avito.auth.presentation.AuthIntent
import com.avito.auth.presentation.AuthMode
import com.avito.auth.presentation.AuthUiState
import com.avito.ui.components.ScreenDefaults

@Composable
fun AuthForm(
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
                .padding(ScreenDefaults.ContentPadding)
                .padding(top = 16.dp)
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { onIntent(AuthIntent.DismissMessage) }) {
                        Text("Понятно")
                    }
                }
            }
            state.infoMessage?.let { info ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = info,
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { onIntent(AuthIntent.DismissMessage) }) {
                        Text("Понятно")
                    }
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

