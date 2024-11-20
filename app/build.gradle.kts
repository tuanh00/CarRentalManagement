import java.util.Properties
import java.io.FileInputStream
import java.io.FileNotFoundException

plugins {
    alias(libs.plugins.android.application)
   //alias(libs.plugins.google.gms.google.services)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { stream ->
        localProperties.load(stream)
    }
} else {
    throw FileNotFoundException("local.properties file not found. Please create one and add your API keys.")
}

android {
    namespace = "com.example.carrentalapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.carrentalapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Add the API keys to BuildConfig
        buildConfigField("String", "GOOGLE_MAPS_API_KEY", "\"${localProperties["GOOGLE_MAPS_API_KEY"]}\"")
        buildConfigField("String", "STRIPE_PUBLISHABLE_KEY", "\"${localProperties["STRIPE_PUBLISHABLE_KEY"]}\"")
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"${localProperties["GOOGLE_WEB_CLIENT_ID"]}\"")

    }

    // Add the buildFeatures block here
    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            manifestPlaceholders["GOOGLE_MAPS_API_KEY"] = localProperties["GOOGLE_MAPS_API_KEY"] as String
            manifestPlaceholders["STRIPE_PUBLISHABLE_KEY"] = localProperties["STRIPE_PUBLISHABLE_KEY"] as String
        }
        debug {
            manifestPlaceholders["GOOGLE_MAPS_API_KEY"] = localProperties["GOOGLE_MAPS_API_KEY"] as String
            manifestPlaceholders["STRIPE_PUBLISHABLE_KEY"] = localProperties["STRIPE_PUBLISHABLE_KEY"] as String
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.recyclerview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(platform("com.google.firebase:firebase-bom:32.2.3")) // Check for the latest version
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")


    // Add Glide dependencies
    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")

    // Add Google Calendar API dependencies
// Google Play Services Auth
    // implementation("com.google.android.gms:play-services-auth:16.0.1")
    //implementation("com.google.android.gms:play-services-auth:20.4.1")
    implementation("com.google.android.gms:play-services-auth:20.5.0")

// Google API Client Libraries
    implementation("com.google.api-client:google-api-client-android:1.25.0") {
        exclude(group = "org.apache.httpcomponents", module = "httpclient")
        exclude(group = "org.apache.httpcomponents", module = "httpcore")
    }

    implementation("com.google.api-client:google-api-client-gson:1.25.0") {
        exclude(group = "org.apache.httpcomponents", module = "httpclient")
        exclude(group = "org.apache.httpcomponents", module = "httpcore")
    }

// Google Calendar API
    implementation("com.google.apis:google-api-services-calendar:v3-rev411-1.25.0") {
        exclude(group = "org.apache.httpcomponents", module = "httpclient")
        exclude(group = "org.apache.httpcomponents", module = "httpcore")
    }

    //Stripe API
    implementation("com.stripe:stripe-android:20.14.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3") // Optional, for logging

    //Google Map API
    implementation("com.google.android.gms:play-services-maps:18.1.0")

    // Google Places API
    implementation("com.google.android.libraries.places:places:3.2.0")
    implementation("com.google.android.gms:play-services-location:21.0.1") // Latest version as of now

    //Add WorkManager Dependency:
    implementation("androidx.work:work-runtime:2.7.1")
}