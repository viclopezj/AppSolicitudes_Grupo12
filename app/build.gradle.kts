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
    implementation(libs.androidx.sqlite.async)       // executeSQL() para las migraciones

    // --- Navegación y ViewModel ---
    implementation(libs.androidx.navigation.compose)           // Moverse entre pantallas
    implementation(libs.androidx.lifecycle.viewmodel.compose)  // viewModel() en Compose

    // Íconos básicos (el + de la lista y la flecha de volver)
    implementation(libs.androidx.compose.material.icons.core)

    // --- Cámara (QR) ---
    val cameraxVersion = "1.4.2"
    implementation("androidx.camera:camera-core:$cameraxVersion")       // Base de CameraX
    implementation("androidx.camera:camera-camera2:$cameraxVersion")    // Conecta CameraX con la cámara del teléfono
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")  // Abre/cierra la cámara según el ciclo de vida
    implementation("androidx.camera:camera-view:$cameraxVersion")       // PreviewView: muestra la imagen de la cámara

    // ML Kit: lee el código QR desde la imagen de la cámara
    implementation("com.google.mlkit:barcode-scanning:17.3.0")

    // ZXing core: GENERA el QR de cada solicitud (para poder probar el escáner)
    implementation("com.google.zxing:core:3.5.3")

    // --- Imágenes (foto de evidencia) ---
    implementation(libs.coil.compose)   // Coil: AsyncImage muestra la foto guardada

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}