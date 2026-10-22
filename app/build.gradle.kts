plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21"
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.ngilizcequizapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.mewenglish.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
        buildConfig = true
    }
}

dependencies {
    // --- ANDROID & COMPOSE BOM ---
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.google.mlkit:language-id:17.0.4")
    implementation("com.google.mlkit:translate:17.0.3")

    // --- JSON & STORAGE (Yerel veri yönetimi için) ---
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // --- NAVIGASYON VE GÖRSEL ---
    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("com.airbnb.android:lottie-compose:6.4.1")

    // --- MEDYA VE IKONLAR ---
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-ui:1.3.1")
    implementation("androidx.compose.material:material-icons-extended:1.6.0")
    implementation("androidx.compose.material:material-icons-extended")

    // --- TEST VE DEBUG ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    debugImplementation(libs.androidx.compose.ui.tooling)


    // --- FIREBASE FIRESTORE & AUTH ---
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")

    // --- VIEWMODEL & COMPOSE LIFECYCLE ---
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    // --- GOOGLE SIGN IN & AUTH ---
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    // --- GOOGLE PLAY IN-APP BILLING ---
    implementation("com.android.billingclient:billing-ktx:7.1.0")

    // --- GOOGLE MOBILE ADS (ADMOB) ---
    implementation("com.google.android.gms:play-services-ads:23.6.0")

    // --- SECURITY & ENCRYPTED STORAGE ---
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
}
