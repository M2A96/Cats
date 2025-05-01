plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.dagger.hilt.android")
    kotlin("kapt")
    id("com.google.devtools.ksp")
    id("org.jlleitschuh.gradle.ktlint")
}

// Configure ktlint
ktlint {
    android.set(true)
    outputToConsole.set(true)
    outputColorName.set("RED")
    // Allow build to continue even with style violations during development
    ignoreFailures.set(true)
    // Enable experimental rules for advanced style checking
    enableExperimentalRules.set(true)
    // Disable some rules that might conflict with Android Studio's default formatting
    disabledRules.set(
        setOf(
            "no-wildcard-imports"
        )
    )
    filter {
        exclude("**/generated/**")
        include("**/kotlin/**")
    }
}

// Add ktlint format task to run before build
// This ensures code is automatically formatted before compilation
tasks.named("preBuild") {
    dependsOn("ktlintFormat")
}

android {
    namespace = "io.maa96.cats"
    compileSdk = 35

    defaultConfig {
        applicationId = "io.maa96.cats"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // retrofit
    implementation(libs.retrofit)
    implementation(libs.gson)
    implementation(libs.stetho)
    implementation(libs.stetho.okhttp3)
    implementation(libs.converter.gson)

    implementation(libs.logging.interceptor)

    // hilt
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    kapt(libs.hilt.android.compiler)

    // room persistence library
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // coil image loader
    implementation(libs.coil.compose)
    implementation(libs.coil.kt.compose)

    implementation(libs.androidx.navigation.compose)
}
