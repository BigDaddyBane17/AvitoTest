package com.avito.firebase.auth.domain.repository

import android.net.Uri
import com.avito.firebase.auth.domain.model.AuthUserInfo

/**
 * Abstraction over authentication provider so feature modules do not depend
 * on concrete Firebase classes.
 */

interface AuthRepository {
    suspend fun signIn(email: String, password: String): Result<Unit>
    suspend fun signUp(name: String, email: String, password: String): Result<Unit>
    suspend fun sendPasswordReset(email: String): Result<Unit>
    suspend fun signInWithGoogle(idToken: String): Result<Unit>
    suspend fun updateDisplayName(name: String): Result<Unit>
    suspend fun updatePhoto(uri: Uri): Result<Uri>
    fun currentUserInfo(): AuthUserInfo?
    fun isAuthorized(): Boolean
    fun signOut()
}