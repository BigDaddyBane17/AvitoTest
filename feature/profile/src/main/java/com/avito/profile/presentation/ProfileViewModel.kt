package com.avito.profile.presentation

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avito.profile.domain.usecase.GetProfileInfoUseCase
import com.avito.profile.domain.usecase.LogoutUseCase
import com.avito.profile.domain.usecase.UpdateDisplayNameUseCase
import com.avito.profile.domain.usecase.UpdatePhotoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class ProfileViewModel @Inject constructor(
    private val getProfileInfoUseCase: GetProfileInfoUseCase,
    private val updateDisplayNameUseCase: UpdateDisplayNameUseCase,
    private val updatePhotoUseCase: UpdatePhotoUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        refreshUser()
    }

    fun onIntent(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.DisplayNameChanged -> updateContent {
                it.copy(
                    displayName = intent.value,
                    errorMessage = null,
                    message = null
                )
            }
            ProfileIntent.ToggleEditMode -> updateContent {
                it.copy(
                    isEditing = !it.isEditing,
                    message = null,
                    errorMessage = null
                )
            }
            ProfileIntent.SaveChanges -> saveDisplayName()
            ProfileIntent.Logout -> logout()
            is ProfileIntent.PhotoSelected -> uploadPhoto(intent.uri)
            ProfileIntent.DismissMessage -> updateContent { it.copy(message = null, errorMessage = null) }
        }
    }

    private fun refreshUser() {
        val user = getProfileInfoUseCase()
        _uiState.value = ProfileUiState.Content(
            displayName = user?.displayName.orEmpty(),
            email = user?.email.orEmpty(),
            phone = user?.phone.orEmpty(),
            photoUrl = user?.photoUri
        )
    }

    private fun saveDisplayName() {
        val current = _uiState.value as? ProfileUiState.Content ?: return
        val name = current.displayName.trim()
        if (name.isEmpty()) {
            updateContent { it.copy(errorMessage = "Имя не может быть пустым") }
            return
        }

        viewModelScope.launch {
            updateContent { it.copy(isSaving = true) }
            updateDisplayNameUseCase(name)
                .onSuccess {
                    Log.d(TAG, "Display name updated")
                    refreshUser()
                    updateContent {
                        it.copy(
                            isSaving = false,
                            isEditing = false,
                            message = DISPLAY_NAME_UPDATED_MESSAGE
                        )
                    }
                }
                .onFailure {
                    Log.e(TAG, "Failed to update display name", it)
                    updateContent {
                        it.copy(
                        isSaving = false,
                        errorMessage = it.message ?: "Не удалось обновить имя"
                        )
                    }
                }
        }
    }

    private fun uploadPhoto(uri: Uri) {
        viewModelScope.launch {
            updateContent { it.copy(isSaving = true) }
            updatePhotoUseCase(uri)
                .onSuccess {
                    Log.d(TAG, "Photo upload success: $it")
                    refreshUser()
                    updateContent {
                        it.copy(
                            isSaving = false,
                            message = PHOTO_UPDATED_MESSAGE
                        )
                    }
                }
                .onFailure {
                    Log.e(TAG, "Failed to upload photo", it)
                    updateContent {
                        it.copy(
                        isSaving = false,
                        errorMessage = it.message ?: "Не удалось загрузить фото"
                        )
                    }
                }
        }
    }

    private fun logout() {
        logoutUseCase()
        Log.d(TAG, "User signed out")
        when (val current = _uiState.value) {
            is ProfileUiState.Content -> _uiState.value = current.copy(message = LOGOUT_MESSAGE)
            ProfileUiState.Loading -> _uiState.value = ProfileUiState.Content(
                displayName = "",
                email = "",
                phone = "",
                photoUrl = null,
                message = LOGOUT_MESSAGE
            )
        }
    }

    private fun updateContent(transform: (ProfileUiState.Content) -> ProfileUiState.Content) {
        val current = _uiState.value as? ProfileUiState.Content ?: return
        _uiState.value = transform(current)
    }

    companion object {
        internal const val LOGOUT_MESSAGE = "logout"
        private const val PHOTO_UPDATED_MESSAGE = "Фото обновлено"
        private const val DISPLAY_NAME_UPDATED_MESSAGE = "Имя обновлено"
        private const val TAG = "ProfileViewModel"
    }
}
