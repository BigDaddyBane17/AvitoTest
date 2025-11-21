package com.avito.bookslist.domain.usecase

import com.avito.bookslist.domain.DownloadedBooksRepository
import com.avito.bookslist.domain.LocalBook
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveBooksUseCase @Inject constructor(
    private val repository: DownloadedBooksRepository
) {
    operator fun invoke(): Flow<List<LocalBook>> = repository.booksFlow
}

