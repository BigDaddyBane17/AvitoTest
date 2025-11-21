package com.avito.auth.domain.usecase

import com.avito.firebase.auth.domain.repository.AuthRepository
import javax.inject.Inject

class SendPasswordResetUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String) =
        authRepository.sendPasswordReset(email)
}

