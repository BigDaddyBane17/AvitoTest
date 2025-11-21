package com.avito.profile.domain.usecase

import com.avito.firebase.auth.AuthRepository
import javax.inject.Inject

class UpdateDisplayNameUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(name: String) = authRepository.updateDisplayName(name)
}

