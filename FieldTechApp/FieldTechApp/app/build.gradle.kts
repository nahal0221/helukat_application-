plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    // Disabled until a real google-services.json from your Firebase project is added —
    // this plugin fails the build immediately without one. See README "Push notifications".
    // id("com.google.gms.google-services")
}

android {
    namespace = "com.yourcompany.fieldtech"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.yourcompany.fieldtech"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // Base URL is injected per build type so staging/prod can differ.
        buildConfigField("String", "BASE_URL", "\"https://api.yourcompany.com/v1/\"")
        buildConfigField("String", "WS_URL", "\"wss://api.yourcompany.com/v1/ws\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
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
    // Core / Compose
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")

    // Material Icons Extended (needed for icons like Sync that aren't in the default set)
    implementation("androidx.compose.material:material-icons-extended:1.6.8")

    // Hilt DI
    implementation("com.google.dagger:hilt-android:2.51.1")
    ksp("com.google.dagger:hilt-android-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("androidx.hilt:hilt-work:1.2.0")
    ksp("androidx.hilt:hilt-compiler:1.2.0")

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")

    // Room (offline queue + cache)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // WorkManager (sync worker)
    implementation("androidx.work:work-runtime-ktx:2.9.1")

    // DataStore (token storage)
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Firebase (push notifications, per API §7) — re-enable alongside the
    // google-services plugin above once google-services.json is added.
    // implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    // implementation("com.google.firebase:firebase-messaging-ktx")

    // Location (GPS-stamped time logs, per §5)
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Image loading
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
