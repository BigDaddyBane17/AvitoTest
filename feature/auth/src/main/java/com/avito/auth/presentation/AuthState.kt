package com.avito.auth.presentation

sealed interface AuthUiState {
    data class Content(
        val mode: AuthMode = AuthMode.SignIn,
        val name: String = "",
        val email: String = "",
        val password: String = "",
        val confirmPassword: String = "",
        val isPasswordVisible: Boolean = false,
        val isSubmitEnabled: Boolean = false,
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val infoMessage: String? = null
    ) : AuthUiState

    data object Success : AuthUiState
}

enum class AuthMode {
    SignIn,
    SignUp
}