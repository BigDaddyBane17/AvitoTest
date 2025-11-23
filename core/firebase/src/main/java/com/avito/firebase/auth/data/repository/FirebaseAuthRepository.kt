package com.avito.firebase.auth.data.repository

import android.net.Uri
import android.util.Log
import com.avito.firebase.auth.domain.model.AuthUserInfo
import com.avito.firebase.auth.domain.repository.AuthRepository
import com.avito.common.firebase.await
import com.avito.firebase.storage.S3StorageDataSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FirebaseAuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val storageDataSource: S3StorageDataSource
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

    override suspend fun signInWithGoogle(idToken: String): Result<Int> =
        runCatching {
            Log.d(TAG, "signInWithGoogle called, idToken length: ${idToken.length}")
            withContext(Dispatchers.IO) {
                try {
                    Log.d(TAG, "Creating GoogleAuthProvider credential")
                    val credential = GoogleAuthProvider.getCredential(idToken, null)
                    Log.d(TAG, "Credential created, signing in with Firebase")
                    val authResult = auth.signInWithCredential(credential).await()
                    val user = authResult.user
                    Log.d(TAG, "Firebase sign-in successful. User: ${user?.uid}, Email: ${user?.email}, DisplayName: ${user?.displayName}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error during Firebase sign-in with Google credential", e)
                    throw e
                }
            }
        }.map {
            Log.d(TAG, "signInWithGoogle completed successfully")
        }.onFailure { error ->
            Log.e(TAG, "signInWithGoogle failed", error)
            Log.e(TAG, "Error type: ${error.javaClass.simpleName}, Message: ${error.message}")
        }

    override suspend fun updateDisplayName(name: String): Result<Unit> =
        runCatching {
            withContext(Dispatchers.IO) {
                Log.d(TAG, "Updating display name for user=${auth.currentUser?.uid}")
                val request = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                auth.currentUser?.updateProfile(request)?.await()
            }
        }.map { }

    override suspend fun updatePhoto(uri: Uri): Result<Uri> =
        runCatching {
            val user = auth.currentUser ?: error("User not authorized")
            Log.d(TAG, "Uploading photo for user=${user.uid}, source=$uri")
            val uploadedUri = storageDataSource.uploadFile(uri, "profiles/${user.uid}")
            withContext(Dispatchers.IO) {
                val request = UserProfileChangeRequest.Builder()
                    .setPhotoUri(uploadedUri)
                    .build()
                user.updateProfile(request).await()
                user.reload().await()
                Log.d(TAG, "Photo updated for user=${user.uid}, newUri=$uploadedUri")
            }
            uploadedUri
        }.onFailure {
            Log.e(TAG, "Failed to update photo", it)
        }

    override fun currentUserInfo(): AuthUserInfo? =
        auth.currentUser?.let { user ->
            AuthUserInfo(
                uid = user.uid,
                displayName = user.displayName,
                email = user.email,
                phone = user.phoneNumber,
                photoUri = user.photoUrl
            )
        }

    override fun isAuthorized(): Boolean = auth.currentUser != null

    override fun signOut() {
        auth.signOut()
    }

    companion object {
        private const val TAG = "FirebaseAuthRepo"
    }
}