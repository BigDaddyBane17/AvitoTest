package com.avito.bookreader.domain.usecase

import com.avito.bookreader.domain.BookReaderRepository
import javax.inject.Inject

class GetReadingProgressUseCase @Inject constructor(
    private val repository: BookReaderRepository
) {
    suspend operator fun invoke(bookId: String) = repository.getReadingProgress(bookId)
}

