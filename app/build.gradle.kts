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
        jvmTarget = "21"  // Aliniat la Java 21
    }
    buildFeatures {
        compose = true
    }
    buildToolsVersion = "36.0.0"
}

room {
    schemaDirectory("$projectDir/schemas")
}

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

    // Adăugat pentru calendar: Hilt (DI, similar Spring)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)  // Adăugat pentru hiltViewModel

    // Adăugat pentru calendar: Room (stocare locală, similar ORM Hibernate)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Adăugat pentru calendar: Retrofit (API calls, similar RestTemplate)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okHttp.logging)
    implementation(libs.gson)

    // Adăugat pentru calendar: Coroutines (async, similar async în Java)
    implementation(libs.coroutines.android)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.material.icons.extended)

    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.ui)

}