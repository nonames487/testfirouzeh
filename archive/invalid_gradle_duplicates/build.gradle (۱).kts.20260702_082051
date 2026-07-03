plugins {
    id("com.android.application")
}

android {
    namespace = "ai.arena.hisabdar.modern1405"
    compileSdk = 35

    defaultConfig {
        applicationId = "ai.arena.hisabdar.modern1405"
        minSdk = 26
        targetSdk = 35
        versionCode = 1917
        versionName = "10.0.7-mvvm-ultimate"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // --- TASK 2: API KEY CONFIGURATION (avoid hardcoding) ---
        buildConfigField("String", "SYNC_API_KEY", "\"firoozeh-secret-bazaar-key-1405\"")
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

    // --- TASK 2: Enable BuildConfig generation for targetSdk 35 ---
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.6.1")
    implementation("androidx.lifecycle:lifecycle-livedata:2.6.1")
    
    // Room SQLite v8
    implementation("androidx.room:room-runtime:2.5.2")
    annotationProcessor("androidx.room:room-compiler:2.5.2")
    
    // OkHttp3 & Gson
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.google.code.gson:gson:2.10.1")
    
    // ML Kit & Barcode
    implementation("com.google.android.gms:play-services-mlkit-text-recognition:19.0.0")
    implementation("com.google.android.gms:play-services-mlkit-barcode-scanning:18.2.0")
    
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
