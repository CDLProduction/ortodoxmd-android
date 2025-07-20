plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "md.ortodox.ortodoxmd.widgets"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
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
    implementation(project(":features:calendar"))  // Fix: Schimbat de la ":features" la submodul corect
    implementation("androidx.compose.runtime:runtime-android:1.8.3")  // Păstrează dacă ai nevoie; altfel șterge dacă widget e RemoteViews
}