package com.avito.bookslist.domain.usecase

import com.avito.bookslist.domain.repository.DownloadedBooksRepository
import javax.inject.Inject

class DeleteBookUseCase @Inject constructor(
    private val repository: DownloadedBooksRepository
) {
    suspend operator fun invoke(bookId: String) = repository.deleteBook(bookId)
}

