plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "com.chronie.helperunited.base"
    compileSdk = 37

    defaultConfig {
        minSdk = 35
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        buildConfig = false
    }
}

dependencies {
    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    
    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
}
