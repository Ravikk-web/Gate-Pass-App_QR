plugins {
    alias(libs.plugins.androidApplication)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.gate_pass_app_qr"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.gate_pass_app_qr"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
    }
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
    buildToolsVersion = "34.0.0"
}

dependencies {

    //Biometric Dependancy
    implementation("androidx.biometric:biometric:1.1.0")

    //Notification Dependancy
    implementation("androidx.core:core:1.10.0")

    implementation ("com.squareup.picasso:picasso:2.8")
    // Dependency for FirebaseUI for Firestore
    implementation ("com.firebaseui:firebase-ui-firestore:8.0.0")
    // Dependency for Firestore
    implementation ("com.google.firebase:firebase-firestore:23.0.3")
    //Dependancy for QR feature
    implementation("com.github.androidmads:QRGenerator:1.0.1")
    implementation("com.google.zxing:core:3.4.1")
    implementation("com.journeyapps:zxing-android-embedded:4.2.0")
    implementation("eu.livotov.labs.android:CAMView:2.0.1@aar") {setTransitive(true)}


    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.annotation)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}