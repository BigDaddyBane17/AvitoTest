package com.avito.firebase.storage

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.amazonaws.event.ProgressListener
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID
import javax.inject.Inject

class S3StorageDataSource @Inject constructor(
    private val context: Context,
    private val amazonS3: AmazonS3,
    private val config: S3Config
) {

    suspend fun uploadFile(
        uri: Uri,
        pathPrefix: String,
        onProgress: (Float) -> Unit = {},
        customKey: String? = null
    ): Uri = withContext(Dispatchers.IO) {
        val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
        val extension = MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(mimeType)
            ?: mimeType.substringAfter('/', "bin")
        val key = customKey ?: "$pathPrefix/${UUID.randomUUID()}.$extension"

        val localFile = uri.toFileOrNull()
        if (localFile != null && localFile.exists()) {
            return@withContext uploadLocalFile(localFile, key, mimeType, onProgress)
        }

        val (inputStream, length) = context.contentResolver.openStreamWithLength(uri)

        val repeatableStream = if (!inputStream.markSupported()) {
            inputStream.close()
            Log.d(TAG, "Creating temp file for non-repeatable stream, key=$key")
            val tempFile = copyStreamToTempFile(uri, extension)
            return@withContext uploadLocalFile(tempFile, key, mimeType, onProgress, deleteAfterUpload = true)
        } else inputStream

        val metadata = ObjectMetadata().apply {
            contentType = mimeType
            contentLength = length ?: 0L
        }

        val uploadStream = if (length != null && length > 0) {
            ProgressInputStream(repeatableStream, length, onProgress)
        } else {
            val bytes = repeatableStream.use { it.readBytes() }
            metadata.contentLength = bytes.size.toLong()
            bytes.inputStream()
        }

        Log.d(TAG, "Uploading key=$key to bucket=${config.bucket}, length=${metadata.contentLength}")
        uploadStream.use { stream ->
            val request = PutObjectRequest(config.bucket, key, stream, metadata)
                .withCannedAcl(CannedAccessControlList.PublicRead)
            amazonS3.putObject(request)
        }
        onProgress(1f)
        Log.d(TAG, "Upload success key=$key")

        val url = "${config.publicBaseUrl.trimEnd('/')}/$key"
        Log.d(TAG, "Public URL = $url")
        url.toUri()
    }

    private fun uploadLocalFile(
        file: File,
        key: String,
        mimeType: String,
        onProgress: (Float) -> Unit,
        deleteAfterUpload: Boolean = false
    ): Uri {
        val metadata = ObjectMetadata().apply {
            contentType = mimeType
            contentLength = file.length()
        }
        var uploadedBytes = 0L
        val request = PutObjectRequest(config.bucket, key, file).apply {
            this.metadata = metadata
            withCannedAcl(CannedAccessControlList.PublicRead)
            generalProgressListener = ProgressListener { event ->
                val bytes = event.bytesTransferred
                if (bytes > 0 && metadata.contentLength > 0) {
                    uploadedBytes += bytes
                    val progress = (uploadedBytes.toFloat() / metadata.contentLength)
                        .coerceIn(0f, 1f)
                    onProgress(progress)
                }
            }
        }
        amazonS3.putObject(request)
        onProgress(1f)
        if (deleteAfterUpload) {
            file.delete()
        }
        val url = "${config.publicBaseUrl.trimEnd('/')}/$key"
        Log.d(TAG, "Upload success key=$key (local file) URL=$url")
        return url.toUri()
    }

    suspend fun uploadJson(
        key: String,
        payload: String,
        isPublic: Boolean = false
    ) = withContext(Dispatchers.IO) {
        val bytes = payload.toByteArray()
        val metadata = ObjectMetadata().apply {
            contentType = "application/json"
            contentLength = bytes.size.toLong()
        }
        val request = PutObjectRequest(
            config.bucket,
            key,
            bytes.inputStream(),
            metadata
        ).apply {
            if (isPublic) {
                withCannedAcl(CannedAccessControlList.PublicRead)
            }
        }
        amazonS3.putObject(request)
    }

    private fun ContentResolver.openStreamWithLength(uri: Uri): Pair<InputStream, Long?> {
        return try {
            val descriptor = openAssetFileDescriptor(uri, "r")
            if (descriptor != null) {
                descriptor.use {
                    val input = it.createInputStream()
                    input to it.length.takeIf { length -> length > 0 }
                }
            } else {
                openInputStream(uri)?.let { stream ->
                    stream to resolveFileSize(uri)
                } ?: throw IllegalStateException("Не удалось открыть выбранный файл")
            }
        } catch (error: Exception) {
            Log.e(TAG, "Failed to open descriptor for $uri", error)
            openInputStream(uri)?.let { stream ->
                stream to resolveFileSize(uri)
            } ?: throw IllegalStateException("Не удалось открыть выбранный файл")
        }
    }

    private fun ContentResolver.resolveFileSize(uri: Uri): Long? =
        when (uri.scheme) {
            ContentResolver.SCHEME_CONTENT -> {
                query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
                    val column = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (column >= 0 && cursor.moveToFirst()) cursor.getLong(column) else null
                }
            }

            ContentResolver.SCHEME_FILE -> uri.path
                ?.let(::File)
                ?.takeIf(File::exists)
                ?.length()

            else -> null
        }

    private fun copyStreamToTempFile(uri: Uri, extension: String): File {
        val dir = File(context.cacheDir, TEMP_DIR).apply { if (!exists()) mkdirs() }
        val tempFile = File.createTempFile("upload_", ".$extension", dir)
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalStateException("Не удалось открыть выбранный файл")
        return tempFile
    }

    private fun Uri.toFileOrNull(): File? =
        runCatching { toFile() }.getOrNull()

    private class ProgressInputStream(
        private val delegate: InputStream,
        private val totalBytes: Long,
        private val onProgress: (Float) -> Unit
    ) : InputStream() {

        private var readBytes: Long = 0
        private var markBytes: Long = 0

        override fun read(): Int {
            val value = delegate.read()
            if (value != -1) {
                readBytes++
                publish()
            }
            return value
        }

        override fun read(b: ByteArray, off: Int, len: Int): Int {
            val result = delegate.read(b, off, len)
            if (result > 0) {
                readBytes += result
                publish()
            }
            return result
        }

        override fun available(): Int = delegate.available()

        override fun close() = delegate.close()

        override fun markSupported(): Boolean = delegate.markSupported()

        override fun mark(readlimit: Int) {
            delegate.mark(readlimit)
            markBytes = readBytes
        }

        override fun reset() {
            delegate.reset()
            readBytes = markBytes
        }

        private fun publish() {
            if (totalBytes <= 0) return
            val progress = (readBytes.toFloat() / totalBytes).coerceIn(0f, 1f)
            onProgress(progress)
        }
    }

    companion object {
        private const val TAG = "S3StorageDataSource"
        private const val TEMP_DIR = "s3_uploads"
    }
}
