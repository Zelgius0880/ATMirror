plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlin-android-extensions")
}

group = "org.example"
version = "1.0-SNAPSHOT"

val kotlinVersion = rootProject.extra.get("kotlinVersion")


android {
    compileSdkVersion( 29)

    defaultConfig {
        minSdkVersion( 23)
        targetSdkVersion (29)
        versionCode = 1
        versionName  = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles ("consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles (getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    // junit 5
    testImplementation ("org.junit.jupiter:junit-jupiter-params:5.7.0-M1")
    testRuntimeOnly ("org.junit.jupiter:junit-jupiter-engine:5.7.0-M1")
    api ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.8")
}
