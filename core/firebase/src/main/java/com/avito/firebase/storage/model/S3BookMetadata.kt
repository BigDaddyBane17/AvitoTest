package com.avito.firebase.storage.model

import kotlinx.serialization.Serializable

@Serializable
data class S3BookMetadata(
    val id: String,
    val title: String,
    val author: String,
    val fileKey: String,
    val fileUrl: String,
    val fileName: String,
    val mimeType: String,
    val fileSizeBytes: Long,
    val uploadedAt: Long
)


