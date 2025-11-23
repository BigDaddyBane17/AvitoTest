package com.avito.profile.domain.usecase

import android.net.Uri
import com.avito.firebase.auth.domain.repository.AuthRepository
import javax.inject.Inject

class UpdatePhotoUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(uri: Uri) = authRepository.updatePhoto(uri)
}

