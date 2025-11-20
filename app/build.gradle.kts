plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.firebase)
}

android {
    namespace = "com.avito.avitotest"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.avito.avitotest"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
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
    implementation(project(":core:database"))
    implementation(project(":core:firebase"))
    implementation(project(":core:navigation"))
    implementation(project(":core:ui"))
    implementation(project(":core:di"))
    implementation(project(":feature:auth"))
    implementation(project(":feature:booksList"))
    implementation(project(":feature:bookReader"))
    implementation(project(":feature:bookUpload"))
    implementation(project(":feature:profile"))


    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose.base)
    implementation(libs.bundles.firebase)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.navigation.compose)
    implementation(libs.bundles.coroutines)
    implementation(libs.dagger)
    ksp(libs.dagger.compiler)
    implementation(libs.lottie)
    implementation(libs.lottie.compose)
    androidTestImplementation(platform(libs.firebase.bom))
    androidTestImplementation(libs.firebase.auth)
    androidTestImplementation(libs.firebase.firestore)
    androidTestImplementation(libs.firebase.storage)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
