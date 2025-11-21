package com.avito.profile.domain.usecase

import com.avito.firebase.auth.AuthRepository
import javax.inject.Inject

class GetProfileInfoUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke() = authRepository.currentUserInfo()
}

