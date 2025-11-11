plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.mit.bodhiq"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.mit.bodhiq"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Core Android dependencies
    implementation(libs.appcompat)
    implementation(libs.material)
    
    // Fragment and Navigation
    implementation("androidx.fragment:fragment:1.6.2")
    implementation("androidx.navigation:navigation-fragment:2.7.6")
    implementation("androidx.navigation:navigation-ui:2.7.6")
    
    // RecyclerView and SwipeRefreshLayout
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    
    // Room database
    implementation(libs.room.runtime)
    implementation(libs.room.rxjava3)
    
    // Firebase BOM - ensures compatible versions
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    
    // Firebase & Google Sign-In
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)
    
    annotationProcessor(libs.room.compiler)
    
    // RxJava3 for asynchronous operations
    implementation(libs.rxjava3)
    implementation(libs.rxandroid)
    
    // Hilt dependency injection
    implementation(libs.hilt.android)
    annotationProcessor(libs.hilt.compiler)
    

    
    // Lifecycle and ViewModel
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)
    
    // DataStore for preferences
    implementation(libs.datastore.preferences)
    implementation("androidx.datastore:datastore-preferences-rxjava3:1.0.0")
    
    // Gson for JSON serialization
    implementation(libs.gson)
    
    // ML Kit for OCR - Text Recognition V2 with support for printed and handwritten text
    implementation("com.google.mlkit:text-recognition:16.0.1")
    implementation("com.google.android.gms:play-services-mlkit-text-recognition:19.0.0")
    
    // Camera and Image handling
    implementation("androidx.camera:camera-core:1.3.1")
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")
    
    // Image loading and processing
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    
    // Google AI Gemini API
    implementation("com.google.ai.client.generativeai:generativeai:0.2.2")
    
    // HTTP client for API calls
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    
    // Activity Result APIs
    implementation("androidx.activity:activity:1.8.2")
    
    // ViewPager2 for tabs
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    
    // ZXing for QR code generation
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    
    // Testing dependencies
    testImplementation(libs.junit)
    testImplementation("org.robolectric:robolectric:4.11.1")
    testImplementation("org.mockito:mockito-core:5.8.0")
    testImplementation("androidx.test:core:1.5.0")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}