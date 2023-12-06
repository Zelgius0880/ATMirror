
plugins {
    id("com.android.library")
    kotlin("android")
}

group = "org.example"
version = "1.0-SNAPSHOT"

val kotlinVersion = rootProject.extra.get("kotlinVersion")


android {
    compileSdk= 34

    defaultConfig {
        minSdk= 23
        targetSdk=34

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles ("consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles (getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    namespace = "com.zelgius.driver.eink"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

dependencies {
    // junit 5
    testImplementation ("org.junit.jupiter:junit-jupiter-params:5.7.0-M1")
    testRuntimeOnly ("org.junit.jupiter:junit-jupiter-engine:5.7.0-M1")
    api ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
}

