import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
}

kotlin {
    androidTarget {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }

    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "LittleCloudKit"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        androidMain.dependencies {
            implementation("androidx.core:core-ktx:1.15.0")
            implementation("androidx.activity:activity-compose:1.9.3")
            implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
            implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
            implementation(compose.preview)
        }
    }
}

compose.resources {
    packageOfResClass = "com.littlecloud.app.resources"
    publicResClass = false
    generateResClass = always
}

android {
    namespace = "com.littlecloud.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "org.littlecloudproject.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 27
        versionName = "1.0-beta2"
    }

    // One shared debug key in the repository, so test builds from any machine (Android Studio
    // or CI) install over each other. Release builds will use a private key kept out of git.
    signingConfigs {
        getByName("debug") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
        // The private upload key for Google Play, given to CI as secrets (never committed).
        val ks = System.getenv("LC_UPLOAD_KEYSTORE")
        if (ks != null && file(ks).exists()) {
            create("upload") {
                storeFile = file(ks)
                storePassword = System.getenv("LC_UPLOAD_STORE_PASSWORD")
                keyAlias = System.getenv("LC_UPLOAD_KEY_ALIAS") ?: "upload"
                keyPassword = System.getenv("LC_UPLOAD_KEY_PASSWORD") ?: System.getenv("LC_UPLOAD_STORE_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.findByName("upload")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}
