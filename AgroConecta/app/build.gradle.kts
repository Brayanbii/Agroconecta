plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.agroconecta.app"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.agroconecta.app"
        minSdk = 24
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
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    // Retrofit para conectarnos a Spring Boot
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // Gson para convertir los JSON a objetos Kotlin
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // OkHttp para interceptores, cookies de sesión y configuración HTTP avanzada
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    // Navigation Compose para iniciar migración de navegación estructurada
    implementation("androidx.navigation:navigation-compose:2.8.0")
    // Material Icons Extended para iconografia premium (sin emojis)
    implementation("androidx.compose.material:material-icons-extended")
    // OSMDroid para mapas OpenStreetMap (sin API keys, igual que Leaflet en la web)
    implementation("org.osmdroid:osmdroid-android:6.1.18")
    // Coil para cargar imagenes de producto desde el servidor Spring Boot
    implementation("io.coil-kt:coil-compose:2.6.0")
}
