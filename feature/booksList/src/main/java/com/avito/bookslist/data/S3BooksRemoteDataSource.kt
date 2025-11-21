package com.avito.bookslist.data

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.services.s3.model.ListObjectsRequest
import com.avito.firebase.auth.domain.repository.AuthRepository
import com.avito.firebase.storage.model.S3Config
import com.avito.firebase.storage.model.S3BookMetadata
import java.io.BufferedReader
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class S3BooksRemoteDataSource @Inject constructor(
    private val amazonS3: AmazonS3,
    private val s3Config: S3Config,
    private val authRepository: AuthRepository,
    private val json: Json
) {

    suspend fun fetchBooks(): Result<List<S3BookMetadata>> = withContext(Dispatchers.IO) {
        val userId = authRepository.currentUserInfo()?.uid
            ?: return@withContext Result.failure(IllegalStateException("Пользователь не авторизован"))
        runCatching {
            val prefix = "books/$userId/meta/"
            val request = ListObjectsRequest()
                .withBucketName(s3Config.bucket)
                .withPrefix(prefix)

            val result = mutableListOf<S3BookMetadata>()
            var listing = amazonS3.listObjects(request)
            while (true) {
                listing.objectSummaries
                    .filter { it.key.endsWith(".json") }
                    .forEach { summary ->
                        val payload = amazonS3
                            .getObject(GetObjectRequest(s3Config.bucket, summary.key))
                            .objectContent
                            .bufferedReader()
                            .use(BufferedReader::readText)
                        result += json.decodeFromString<S3BookMetadata>(payload)
                    }
                if (listing.isTruncated) {
                    listing = amazonS3.listNextBatchOfObjects(listing)
                } else break
            }
            result
        }
    }

    suspend fun downloadBook(
        remoteKey: String,
        destination: java.io.File,
        onProgress: (Float) -> Unit = {}
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            amazonS3.getObject(GetObjectRequest(s3Config.bucket, remoteKey)).use { s3Object ->
                val total = s3Object.objectMetadata.contentLength
                var copied = 0L
                s3Object.objectContent.use { input ->
                    destination.outputStream().use { output ->
                        val buffer = ByteArray(DEFAULT_BUFFER)
                        while (true) {
                            val read = input.read(buffer)
                            if (read == -1) break
                            output.write(buffer, 0, read)
                            copied += read
                            if (total > 0) {
                                val progress = (copied.toFloat() / total).coerceIn(0f, 1f)
                                onProgress(progress)
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val DEFAULT_BUFFER = 8 * 1024
    }
}


