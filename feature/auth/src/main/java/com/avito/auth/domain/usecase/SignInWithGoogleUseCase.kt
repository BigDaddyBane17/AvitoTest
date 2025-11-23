package com.avito.auth.domain.usecase

import android.util.Log
import com.avito.firebase.auth.domain.repository.AuthRepository
import javax.inject.Inject

class SignInWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(idToken: String): Result<Int> {
        Log.d(TAG, "SignInWithGoogleUseCase invoked, idToken length: ${idToken.length}")
        return authRepository.signInWithGoogle(idToken)
            .onSuccess {
                Log.d(TAG, "SignInWithGoogleUseCase completed successfully")
            }
            .onFailure { error ->
                Log.e(TAG, "SignInWithGoogleUseCase failed", error)
            }
    }

    companion object {
        private const val TAG = "SignInWithGoogleUC"
    }
}

