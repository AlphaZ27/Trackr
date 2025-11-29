plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
    id("kotlin-kapt")
}

android {
    namespace = "com.example.trackr"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.trackr"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
//        javaCompileOptions {
//            annotationProcessorOptions {
//                arguments ["dagger.hilt.android.internal.rootComponentPackage"] = "com.example.trackr.TrackrApp"
//            }
//        }
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
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/LICENSE-notice.md"
        }
    }
}

dependencies {

    // Simple Light weight charting library
    implementation("com.github.tehras:charts:0.2.4-alpha")

    // Hilt for Dependency Injection
    implementation("com.google.dagger:hilt-android:2.57.2")
    implementation(libs.androidx.compose.foundation)
    kapt("com.google.dagger:hilt-compiler:2.57.2")
    implementation("androidx.hilt:hilt-navigation-compose:1.3.0")
    kapt("androidx.hilt:hilt-compiler:1.2.0")

    // Work Manager Dependency
    //implementation(platform("androidx.work:work-bom:2.9.0"))
    implementation("androidx.work:work-runtime-ktx:2.11.0")
    implementation("androidx.hilt:hilt-work:1.2.0")


    //implementation("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.9.0")
    //
    //


    // Jetpack Compose
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.4")
    implementation("androidx.activity:activity-compose:1.11.0")
    implementation(platform("androidx.compose:compose-bom:2025.11.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:1.4.0")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.9.6")


    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:34.5.0"))
    implementation(libs.firebase.firestore)
    implementation("com.google.firebase:firebase-auth-ktx:23.2.1")
    implementation("com.google.firebase:firebase-firestore-ktx:25.1.4")
    implementation("com.google.firebase:firebase-messaging")

    // datastore
    implementation("androidx.datastore:datastore-preferences:1.1.7")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    //coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation("io.mockk:mockk-android:1.14.6")
    androidTestImplementation("io.mockk:mockk-android:1.14.6")
    // Coroutines testing
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    // Hilt Testing
    testImplementation("com.google.dagger:hilt-android-testing:2.57.2")
    kaptTest("com.google.dagger:hilt-compiler:2.57.2")
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}