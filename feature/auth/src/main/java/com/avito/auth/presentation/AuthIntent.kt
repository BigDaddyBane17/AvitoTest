package com.avito.auth.presentation

sealed interface AuthIntent {
    data class EmailChanged(val value: String) : AuthIntent
    data class PasswordChanged(val value: String) : AuthIntent
    data class ConfirmPasswordChanged(val value: String) : AuthIntent
    data class NameChanged(val value: String) : AuthIntent
    data object TogglePasswordVisibility : AuthIntent
    data object ToggleMode : AuthIntent
    data object Submit : AuthIntent
    data object ForgotPassword : AuthIntent
    data object DismissMessage : AuthIntent
}