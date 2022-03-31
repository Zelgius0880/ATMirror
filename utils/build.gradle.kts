plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlin-android-extensions")
    id("kotlin-kapt")
}
val kotlinVersion = rootProject.extra.get("kotlinVersion")


android {
    compileSdk = 32

    defaultConfig {
        minSdk = 27
        targetSdk=32

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles ( "consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles (getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation (fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation ("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation ("androidx.core:core-ktx:1.8.0-alpha04")
    implementation ("androidx.appcompat:appcompat:1.4.1")
    testImplementation ("junit:junit:4.13.2")
    androidTestImplementation ("androidx.test.ext:junit:1.1.3")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.4.0")


    api ("gov.nist.math:jama:1.0.3")

}