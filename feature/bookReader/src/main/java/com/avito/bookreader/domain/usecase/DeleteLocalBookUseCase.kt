package com.avito.bookreader.domain.usecase

import com.avito.bookreader.domain.repository.BookReaderRepository
import javax.inject.Inject

class DeleteLocalBookUseCase @Inject constructor(
    private val repository: BookReaderRepository
) {
    suspend operator fun invoke(bookId: String) = repository.deleteBook(bookId)
}

