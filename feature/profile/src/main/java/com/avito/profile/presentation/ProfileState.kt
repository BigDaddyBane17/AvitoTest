package com.avito.profile.presentation

import android.net.Uri

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Content(
        val displayName: String,
        val email: String,
        val phone: String,
        val photoUrl: Uri?,
        val isEditing: Boolean = false,
        val isSaving: Boolean = false,
        val message: String? = null,
        val errorMessage: String? = null
    ) : ProfileUiState
}
