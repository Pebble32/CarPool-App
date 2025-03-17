plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.carpool"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.carpool"
        minSdk = 26
        targetSdk = 35
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // Retrofit and OkHttp dependencies for networking
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.swiperefreshlayout)
    implementation(libs.logging.interceptor)

    // OpenStreetMap dependencies
    implementation(libs.osmdroid.android)
    implementation(libs.osmdroid.mapsforge)
    implementation(libs.osmdroid.geopackage) {
        exclude(group = "com.j256.ormlite", module = "ormlite-core")
        exclude(group = "com.j256.ormlite", module = "ormlite-android")
    }
    implementation(libs.osmdroid.wms)

    implementation("com.github.MKergall:osmbonuspack:6.9.0") {
        exclude(group = "com.j256.ormlite", module = "ormlite-core")
        exclude(group = "com.j256.ormlite", module = "ormlite-android")
    }

    implementation("com.j256.ormlite:ormlite-android:6.1")

    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}