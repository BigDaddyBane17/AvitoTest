package com.avito.bookreader.domain.usecase

import com.avito.bookreader.domain.repository.BookReaderRepository
import javax.inject.Inject

class SaveReadingProgressUseCase @Inject constructor(
    private val repository: BookReaderRepository
) {
    suspend operator fun invoke(bookId: String, position: Int) =
        repository.saveReadingProgress(bookId, position)
}

