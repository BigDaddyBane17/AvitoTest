package com.avito.profile.presentation

import android.net.Uri

sealed interface ProfileIntent {
    data class DisplayNameChanged(val value: String) : ProfileIntent
    data object ToggleEditMode : ProfileIntent
    data object SaveChanges : ProfileIntent
    data object Logout : ProfileIntent
    data class PhotoSelected(val uri: Uri) : ProfileIntent
    data object DismissMessage : ProfileIntent
}
