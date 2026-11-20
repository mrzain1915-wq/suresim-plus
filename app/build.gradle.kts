plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.droidtune.suresim"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.droidtune.suresim"
        minSdk = 31
        targetSdk = 36
        versionCode = 2
        versionName = "1.0.1"
    }

    // FIX for "App not installed": pin the debug signing key.
    // The GitHub runner creates a fresh Android debug key on every run,
    // so every rebuild has a DIFFERENT signature and Android refuses to
    // install it over the previous build of the same package — exactly
    // what happened with NOWAY Edge Scanner v2.3.0. A committed
    // debug.keystore (app/debug.keystore, standard debug credentials
    // below, SHA-256 98:67:57:9D:B5:24:12:27:C1:F7:32:D6:78:BA:D8:5E:
    // 82:A0:8C:9E:D7:8A:05:F4:09:E0:36:96:D2:42:25:C5) makes every cloud
    // build sign with the SAME key, so updates install over it.
    // This is a debug key only - never use it for a Play Store release.
    signingConfigs {
        getByName("debug") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        debug {
            versionNameSuffix = "-debug"
            signingConfig = signingConfigs.getByName("debug")
        }
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
}

// AGP 9: android.kotlinOptions is removed — use the top-level kotlin
// compilerOptions block exposed by AGP's built-in Kotlin support.
kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    // libxposed API is compile-only: the framework provides it at runtime.
    compileOnly(libs.xposed.api)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.activity.compose)
}
