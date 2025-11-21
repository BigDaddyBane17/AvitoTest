package com.avito.auth.domain.usecase

import com.avito.firebase.auth.AuthRepository
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(name: String, email: String, password: String) =
        authRepository.signUp(name, email, password)
}

