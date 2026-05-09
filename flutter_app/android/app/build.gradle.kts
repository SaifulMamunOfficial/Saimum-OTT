plugins {
    id("com.android.application")
    id("kotlin-android")
    // The Flutter Gradle Plugin must be applied after the Android and Kotlin Gradle plugins.
    id("dev.flutter.flutter-gradle-plugin")
}

android {
    namespace = "com.saimum.saimummusic"
    compileSdk = flutter.compileSdkVersion
    ndkVersion = flutter.ndkVersion

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.saimum.saimummusic"
        minSdk = flutter.minSdkVersion
        targetSdk = flutter.targetSdkVersion
        // Version 1.0.0 — increment versionCode for every Play Store submission.
        versionCode = 1
        versionName = "1.0.0"
        multiDexEnabled = true
        // KeyStore alias used by the legacy Scytale encryption — must match old app exactly.
        buildConfigField("String", "ENC_KEY", "\"saimum_music_key\"")
    }

    buildTypes {
        release {
            // TODO: Replace debug signing with a proper release keystore before shipping.
            signingConfig = signingConfigs.getByName("debug")

            // Enable R8 / ProGuard for release — shrinks APK and obfuscates code.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    implementation("com.yakivmospan:scytale:1.0.1")
}

flutter {
    source = "../.."
}
