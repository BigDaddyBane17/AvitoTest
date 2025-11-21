plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.avito.feature.bookslist"
    compileSdk = 36

    defaultConfig {
        minSdk = 28
    }

    buildFeatures {
        compose = true
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
    implementation(project(":core:ui"))
    implementation(project(":core:navigation"))
    implementation(project(":core:firebase"))
    implementation(project(":core:database"))
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose.base)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.compose.material)
    implementation(libs.navigation.compose)
    implementation(libs.bundles.coroutines)
    implementation(libs.dagger)
    ksp(libs.dagger.compiler)
    implementation(libs.lottie)
    implementation(libs.lottie.compose)
    api(libs.serialization.json)
    implementation(libs.accompanist.swiperefresh)
    implementation(libs.androidx.material.icons.extended)

    implementation(libs.amazon.s3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
