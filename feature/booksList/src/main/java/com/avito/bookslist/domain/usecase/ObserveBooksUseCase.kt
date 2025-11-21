package com.avito.bookslist.domain.usecase

import com.avito.bookslist.domain.repository.DownloadedBooksRepository
import com.avito.bookslist.domain.model.LocalBook
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveBooksUseCase @Inject constructor(
    private val repository: DownloadedBooksRepository
) {
    operator fun invoke(): Flow<List<LocalBook>> = repository.booksFlow
}

