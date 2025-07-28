plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}
hilt {
    enableAggregatingTask = false
}

android {
    namespace = "md.ortodox.ortodoxmd"
    compileSdk = 36

    java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))  // Aliniat la Java 21

    defaultConfig {
        applicationId = "md.ortodox.ortodoxmd"
        minSdk = 25
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
        freeCompilerArgs = listOf("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
        // Aliniat la Java 21
    }
    buildFeatures {
        compose = true
    }
    buildToolsVersion = "36.0.0"
}
configurations.all {
    resolutionStrategy {
        force(libs.androidx.media3.common)
        force(libs.androidx.media3.exoplayer)
        force(libs.androidx.media3.session)
        force(libs.androidx.media3.ui)
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

// ... (secțiunea plugins și android rămân neschimbate)

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okHttp.logging)
    implementation(libs.gson)

    // Coroutines
    implementation(libs.coroutines.android)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.material.icons.extended)

    // --- CORECȚIE APLICATĂ AICI ---
    // Dependențe pentru Media Player
    implementation(libs.androidx.media3.common)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.ui)

    // Dependență pentru descărcări
    implementation(libs.androidx.work.runtime.ktx)

    // Dependența de compatibilitate necesară pentru PlayerNotificationManager
    implementation(libs.androidx.media)

    // Adaugă această linie pentru încărcarea imaginilor
    implementation(libs.coil.compose)

}
