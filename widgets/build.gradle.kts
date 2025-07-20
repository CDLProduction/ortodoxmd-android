plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "md.ortodox.ortodoxmd.widgets"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        targetSdk = 36  // Adăugat pentru matching
    }

    buildTypes {
        debug {
            isMinifyEnabled = false  // Adăugat pentru debug variant
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":data"))
//    implementation(project(":features-calendar"))  // Path corect
    implementation("androidx.compose.runtime:runtime-android:1.8.3")  // Dacă nevoie; șterge dacă widget nu e Compose-based
}