plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "dem.alena.countries"
    compileSdk = 36

    defaultConfig {
        applicationId = "dem.alena.countries"
        minSdk = 31
        targetSdk = 36
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs += "-Xopt-in=androidx.compose.material3.ExperimentalMaterial3Api"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.6.0"
    }
    dependencies {

        // ===== Core =====
        implementation("androidx.core:core-ktx:1.13.1")
        implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")

        // ===== Activity + Compose =====
        implementation("androidx.activity:activity-compose:1.9.2")

        // ===== Compose BOM =====
        implementation(platform("androidx.compose:compose-bom:2024.09.02"))
        implementation("androidx.compose.ui:ui")
        implementation("androidx.compose.ui:ui-tooling-preview")
        implementation("androidx.compose.material3:material3")

        debugImplementation("androidx.compose.ui:ui-tooling")

        // ===== Navigation Compose =====
        implementation("androidx.navigation:navigation-compose:2.8.2")

        // ===== ViewModel =====
        implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")
        implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")

        // ===== Coroutines =====
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

        // ===== Retrofit =====
        implementation("com.squareup.retrofit2:retrofit:2.11.0")
        implementation("com.squareup.retrofit2:converter-gson:2.11.0")

        // ===== Gson =====
        implementation("com.google.code.gson:gson:2.11.0")
    }

}
