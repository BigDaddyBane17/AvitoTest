package com.avito.firebase.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthRepository {

    override suspend fun signIn(email: String, password: String): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                auth.signInWithEmailAndPassword(email, password).await()
            }
        }.map { }

    override suspend fun signUp(name: String, email: String, password: String): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val updates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                result.user?.updateProfile(updates)?.await()
            }
        }.map { }

    override suspend fun sendPasswordReset(email: String): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                auth.sendPasswordResetEmail(email).await()
            }
        }.map { }

    override fun isAuthorized(): Boolean = auth.currentUser != null

    override fun signOut() {
        auth.signOut()
    }
}

