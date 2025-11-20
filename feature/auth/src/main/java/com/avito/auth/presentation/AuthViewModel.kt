package com.avito.auth.presentation

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avito.firebase.auth.AuthRepository
import com.avito.firebase.auth.FirebaseAuthRepository
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository = FirebaseAuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Content())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onIntent(intent: AuthIntent) {
        when (intent) {
            is AuthIntent.EmailChanged -> updateContent {
                it.copy(
                    email = intent.value,
                    errorMessage = null,
                    infoMessage = null
                )
            }
            is AuthIntent.PasswordChanged -> updateContent {
                it.copy(
                    password = intent.value,
                    errorMessage = null,
                    infoMessage = null
                )
            }
            is AuthIntent.ConfirmPasswordChanged -> updateContent {
                it.copy(
                    confirmPassword = intent.value,
                    errorMessage = null,
                    infoMessage = null
                )
            }
            is AuthIntent.NameChanged -> updateContent {
                it.copy(
                    name = intent.value,
                    errorMessage = null,
                    infoMessage = null
                )
            }
            AuthIntent.TogglePasswordVisibility -> updateContent { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            AuthIntent.ToggleMode -> {
                if (isLoading()) return
                updateContent {
                    val nextMode = if (it.mode == AuthMode.SignIn) AuthMode.SignUp else AuthMode.SignIn
                    AuthUiState.Content(
                        mode = nextMode,
                        email = it.email,
                        password = "",
                        confirmPassword = "",
                        name = if (nextMode == AuthMode.SignUp) it.name else "",
                    )
                }
            }
            AuthIntent.Submit -> submit()
            AuthIntent.ForgotPassword -> sendReset()
            AuthIntent.DismissMessage -> updateContent { it.copy(errorMessage = null, infoMessage = null) }
        }
    }

    private fun submit() {
        val current = _uiState.value as? AuthUiState.Content ?: return
        if (current.isLoading) return

        val validationError = validate(current)
        if (validationError != null) {
            updateContent { current.copy(errorMessage = validationError) }
            return
        }

        _uiState.value = current.copy(
            isLoading = true,
            errorMessage = null,
            infoMessage = null
        ).withValidation()

        viewModelScope.launch {
            val email = current.email.trim()
            val password = current.password
            val result = if (current.mode == AuthMode.SignIn) {
                authRepository.signIn(email, password)
            } else {
                authRepository.signUp(current.name.trim(), email, password)
            }

            result
                .onSuccess { _uiState.value = AuthUiState.Success }
                .onFailure { error ->
                    updateContent {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.toReadableMessage()
                        )
                    }
                }
        }
    }

    private fun sendReset() {
        val current = _uiState.value as? AuthUiState.Content ?: return
        if (current.isLoading) return

        val email = current.email.trim()
        if (!email.isValidEmail()) {
            updateContent { current.copy(errorMessage = "Укажите корректный email для сброса") }
            return
        }

        _uiState.value = current.copy(
            isLoading = true,
            errorMessage = null,
            infoMessage = null
        ).withValidation()

        viewModelScope.launch {
            val result = authRepository.sendPasswordReset(email)
            result.onSuccess {
                updateContent {
                    it.copy(
                        isLoading = false,
                        infoMessage = "Мы отправили письмо на $email",
                        errorMessage = null
                    )
                }
            }.onFailure { error ->
                updateContent {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.toReadableMessage()
                    )
                }
            }
        }
    }

    private fun updateContent(transform: (AuthUiState.Content) -> AuthUiState.Content) {
        val current = _uiState.value as? AuthUiState.Content ?: AuthUiState.Content()
        _uiState.value = transform(current).withValidation()
    }

    private fun AuthUiState.Content.withValidation(): AuthUiState.Content {
        val emailReady = email.isNotBlank() && password.length >= MIN_PASSWORD_LENGTH
        val submitEnabled = when (mode) {
            AuthMode.SignIn -> emailReady
            AuthMode.SignUp -> emailReady && name.isNotBlank() && password == confirmPassword
        }
        return copy(isSubmitEnabled = submitEnabled)
    }

    private fun validate(state: AuthUiState.Content): String? {
        val email = state.email.trim()
        if (!email.isValidEmail()) {
            return "Введите корректный email"
        }
        if (state.password.length < MIN_PASSWORD_LENGTH) {
            return "Пароль должен содержать не менее $MIN_PASSWORD_LENGTH символов"
        }
        if (state.mode == AuthMode.SignUp) {
            if (state.name.isBlank()) {
                return "Введите имя"
            }
            if (state.password != state.confirmPassword) {
                return "Пароли не совпадают"
            }
        }
        return null
    }

    private fun Throwable.toReadableMessage(): String = when (this) {
        is FirebaseAuthInvalidCredentialsException -> "Неверный email или пароль"
        is FirebaseAuthInvalidUserException -> "Пользователь не найден"
        is FirebaseAuthUserCollisionException -> "Такой email уже зарегистрирован"
        else -> message ?: "Не удалось выполнить запрос"
    }

    private fun String.isValidEmail(): Boolean =
        isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(trim()).matches()

    private fun isLoading(): Boolean =
        (_uiState.value as? AuthUiState.Content)?.isLoading == true

    companion object {
        private const val MIN_PASSWORD_LENGTH = 6
    }
}