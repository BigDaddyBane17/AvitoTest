package com.avito.firebase.di

import android.content.Context
import com.amazonaws.ClientConfiguration
import com.avito.core.firebase.BuildConfig
import com.avito.di.AppScope
import com.avito.firebase.storage.model.S3Config
import com.avito.firebase.storage.S3StorageDataSource
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3Client
import dagger.Module
import dagger.Provides

@Module
object StorageModule {

    @Provides
    @AppScope
    fun provideS3Config(): S3Config = S3Config(
        endpoint = BuildConfig.S3_ENDPOINT,
        region = BuildConfig.S3_REGION,
        bucket = BuildConfig.S3_BUCKET,
        accessKey = BuildConfig.S3_ACCESS_KEY,
        secretKey = BuildConfig.S3_SECRET_KEY,
        publicBaseUrl = BuildConfig.S3_PUBLIC_BASE_URL
    )

    @Provides
    @AppScope
    fun provideAmazonS3(config: S3Config): AmazonS3 {
        val credentials = BasicAWSCredentials(config.accessKey, config.secretKey)

        return AmazonS3Client(
            credentials,
            ClientConfiguration()
        ).apply {
            setEndpoint(config.endpoint)
        }
    }

    @Provides
    @AppScope
    fun provideS3DataSource(
        context: Context,
        amazonS3: AmazonS3,
        config: S3Config
    ): S3StorageDataSource = S3StorageDataSource(context, amazonS3, config)
}

