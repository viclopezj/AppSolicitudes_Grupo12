plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    // Activa KSP en este módulo para que Room genere su código
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.appsolicitudes_grupo12"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.appsolicitudes_grupo12"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // --- Persistencia local: Room 3 (elegida por ser la versión vigente y con futuro) ---
    implementation(libs.androidx.room3.runtime)      // Entity, DAO y Database
    ksp(libs.androidx.room3.compiler)                // Genera el código de Room al compilar
    implementation(libs.androidx.sqlite.bundled)     // Driver SQLite obligatorio en Room 3

    // --- Navegación y ViewModel ---
    implementation(libs.androidx.navigation.compose)           // Moverse entre pantallas
    implementation(libs.androidx.lifecycle.viewmodel.compose)  // viewModel() en Compose

    // Íconos básicos (el + de la lista y la flecha de volver)
    implementation(libs.androidx.compose.material.icons.core)

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}