plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.kapt")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "md.ortodox.ortodoxmd.features.calendar"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        targetSdk = 36  // Adăugat pentru variant matching (AgpVersionAttr)
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isMinifyEnabled = false  // Adăugat explicit pentru generate debug variant (BuildTypeAttr 'debug')
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":data"))

    val composeBom = platform("androidx.compose:compose-bom:2025.06.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.runtime:runtime")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("com.google.dagger:hilt-android:2.53.1")
    kapt("com.google.dagger:hilt-compiler:2.53.1")
}