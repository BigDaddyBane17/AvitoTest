package com.avito.bookslist.domain.usecase

import com.avito.bookslist.domain.repository.DownloadedBooksRepository
import javax.inject.Inject

class SyncBooksUseCase @Inject constructor(
    private val repository: DownloadedBooksRepository
) {
    suspend operator fun invoke(force: Boolean) = repository.syncRemote(force)
}

