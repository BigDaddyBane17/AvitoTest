package com.avito.firebase.auth

/**
 * Abstraction over authentication provider so feature modules do not depend
 * on concrete Firebase classes.
 */
interface AuthRepository {
    suspend fun signIn(email: String, password: String): Result<Unit>
    suspend fun signUp(name: String, email: String, password: String): Result<Unit>
    suspend fun sendPasswordReset(email: String): Result<Unit>
    suspend fun signInWithGoogle(idToken: String): Result<Unit>
    fun isAuthorized(): Boolean
    fun signOut()
}

