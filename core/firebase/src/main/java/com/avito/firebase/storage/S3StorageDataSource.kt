package com.avito.firebase.storage

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class S3StorageDataSource @Inject constructor(
    private val context: Context,
    private val amazonS3: AmazonS3,
    private val config: S3Config
) {

    suspend fun uploadFile(uri: Uri, pathPrefix: String): Uri = withContext(Dispatchers.IO) {
        val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
        val extension = MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(mimeType)
            ?: "jpg"
        val key = "$pathPrefix/${UUID.randomUUID()}.$extension"

        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalStateException("Не удалось открыть выбранный файл")

        val metadata = ObjectMetadata().apply {
            contentLength = bytes.size.toLong()
            contentType = mimeType
        }

        Log.d(TAG, "Uploading key=$key to bucket=${config.bucket}, size=${bytes.size}")
        bytes.inputStream().use { stream ->
            val request = PutObjectRequest(config.bucket, key, stream, metadata)
                .withCannedAcl(CannedAccessControlList.PublicRead)
            amazonS3.putObject(request)
        }
        Log.d(TAG, "Upload success key=$key")

        val url = "${config.publicBaseUrl.trimEnd('/')}/$key"
        Log.d(TAG, "Public URL = $url")
        url.toUri()
    }


    companion object {
        private const val TAG = "S3StorageDataSource"
    }
}
