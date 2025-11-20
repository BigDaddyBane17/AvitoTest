package com.avito.firebase.auth

import android.net.Uri

data class AuthUserInfo(
    val uid: String,
    val displayName: String?,
    val email: String?,
    val phone: String?,
    val photoUri: Uri?
)

