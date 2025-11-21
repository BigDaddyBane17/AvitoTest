plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}


fun Project.stringProp(name: String, default: String = ""): String =
    (findProperty(name) as String?) ?: default

val S3_ENDPOINT: String = project.stringProp("S3_ENDPOINT", "https://storage.yandexcloud.net")
val S3_REGION: String = project.stringProp("S3_REGION", "ru-central1")
val S3_BUCKET: String = project.stringProp("S3_BUCKET", "demo-bucket")
val S3_ACCESS_KEY: String = project.stringProp("S3_ACCESS_KEY", "")
val S3_SECRET_KEY: String = project.stringProp("S3_SECRET_KEY", "")
val S3_PUBLIC_BASE_URL: String =
    project.stringProp("S3_PUBLIC_BASE_URL", "https://storage.yandexcloud.net/$S3_BUCKET")

android {
    namespace = "com.avito.core.firebase"
    compileSdk = 36

    defaultConfig {
        minSdk = 28

        // Прокидываем всё в BuildConfig, чтобы использовать в runtime
        buildConfigField("String", "S3_ENDPOINT", "\"$S3_ENDPOINT\"")
        buildConfigField("String", "S3_REGION", "\"$S3_REGION\"")
        buildConfigField("String", "S3_BUCKET", "\"$S3_BUCKET\"")
        buildConfigField("String", "S3_ACCESS_KEY", "\"$S3_ACCESS_KEY\"")
        buildConfigField("String", "S3_SECRET_KEY", "\"$S3_SECRET_KEY\"")
        buildConfigField("String", "S3_PUBLIC_BASE_URL", "\"$S3_PUBLIC_BASE_URL\"")
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:di"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.bundles.coroutines)
    implementation(libs.serialization.json)

    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)

    implementation(libs.aws.android.sdk.s3.v2220)

    implementation(libs.dagger)
    ksp(libs.dagger.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
