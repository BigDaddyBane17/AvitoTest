package com.avito.bookupload.work

import android.content.Context
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.avito.core.firebase.BuildConfig
import com.avito.firebase.storage.model.S3Config
import com.avito.firebase.storage.S3StorageDataSource
import com.avito.firebase.storage.model.S3BookMetadata
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.UUID

class BookUploadWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val title = inputData.getString(KEY_TITLE).orEmpty()
        val author = inputData.getString(KEY_AUTHOR).orEmpty()
        val filePath = inputData.getString(KEY_FILE_PATH).orEmpty()
        val originalName = inputData.getString(KEY_FILE_NAME).orEmpty()
        val mimeType = inputData.getString(KEY_MIME_TYPE).orEmpty()

        if (title.isBlank() || author.isBlank() || filePath.isBlank()) {
            return Result.failure(workDataOf(KEY_ERROR to "Недостаточно данных для загрузки"))
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
            ?: return Result.failure(workDataOf(KEY_ERROR to "Пользователь не авторизован"))

        val json = Json { encodeDefaults = true }
        val progressScope = CoroutineScope(coroutineContext)

        return runCatching {
            val cachedFile = File(filePath)
            if (!cachedFile.exists()) throw IllegalStateException("Файл недоступен")

            val storageDataSource = createStorageDataSource()
            val bookId = UUID.randomUUID().toString()
            val filesPrefix = "books/${currentUser.uid}/files"
            val extension = cachedFile.extension.ifBlank {
                mimeType.substringAfterLast('/', "bin")
            }
            val customKey = "$filesPrefix/$bookId.$extension"
            val uploadUri = storageDataSource.uploadFile(
                uri = cachedFile.toUri(),
                pathPrefix = filesPrefix,
                onProgress = { progress ->
                    val percent = (progress * 100).toInt()
                    progressScope.launch {
                        setProgress(
                            workDataOf(KEY_PROGRESS to percent)
                        )
                    }
                },
                customKey = customKey
            )

            val metadata = S3BookMetadata(
                id = bookId,
                title = title,
                author = author,
                fileKey = customKey,
                fileUrl = uploadUri.toString(),
                fileName = originalName,
                mimeType = mimeType,
                fileSizeBytes = cachedFile.length(),
                uploadedAt = System.currentTimeMillis()
            )

            val metaKey = "books/${currentUser.uid}/meta/$bookId.json"
            storageDataSource.uploadJson(
                key = metaKey,
                payload = json.encodeToString(metadata)
            )

            val localCopy = persistLocally(cachedFile, originalName)

            Result.success(
                workDataOf(
                    KEY_OUTPUT_FILE_URL to uploadUri.toString(),
                    KEY_OUTPUT_TITLE to title,
                    KEY_OUTPUT_AUTHOR to author,
                    KEY_OUTPUT_LOCAL_PATH to localCopy.absolutePath
                )
            )
        }.getOrElse { error ->
            Result.failure(
                workDataOf(
                    KEY_ERROR to (error.message ?: "Не удалось загрузить книгу")
                )
            )
        }
    }

    private fun createStorageDataSource(): S3StorageDataSource {
        val config = S3Config(
            endpoint = BuildConfig.S3_ENDPOINT,
            region = BuildConfig.S3_REGION,
            bucket = BuildConfig.S3_BUCKET,
            accessKey = BuildConfig.S3_ACCESS_KEY,
            secretKey = BuildConfig.S3_SECRET_KEY,
            publicBaseUrl = BuildConfig.S3_PUBLIC_BASE_URL
        )

        val credentials = BasicAWSCredentials(config.accessKey, config.secretKey)
        val client = AmazonS3Client(credentials, ClientConfiguration()).apply {
            setEndpoint(config.endpoint)
        }
        return S3StorageDataSource(applicationContext, client, config)
    }

    private suspend fun persistLocally(source: File, originalName: String): File =
        withContext(Dispatchers.IO) {
            val libraryDir = File(applicationContext.filesDir, LOCAL_LIBRARY_DIR).apply {
                if (!exists()) mkdirs()
            }
            val destination = File(
                libraryDir,
                "${System.currentTimeMillis()}_${originalName.ifBlank { source.name }}"
            )
            FileInputStream(source).use { input ->
                FileOutputStream(destination).use { output ->
                    input.copyTo(output)
                }
            }
            destination
        }

    companion object {
        const val KEY_FILE_PATH = "book_file_path"
        const val KEY_FILE_NAME = "book_file_name"
        const val KEY_MIME_TYPE = "book_file_mime"
        const val KEY_TITLE = "book_title"
        const val KEY_AUTHOR = "book_author"

        const val KEY_PROGRESS = "book_upload_progress"
        const val KEY_ERROR = "book_upload_error"
        const val KEY_OUTPUT_FILE_URL = "book_upload_file_url"
        const val KEY_OUTPUT_TITLE = "book_upload_title"
        const val KEY_OUTPUT_AUTHOR = "book_upload_author"
        const val KEY_OUTPUT_LOCAL_PATH = "book_upload_local_path"

        private const val LOCAL_LIBRARY_DIR = "books_library"
    }
}
