plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlin-android-extensions")
}
val kotlinVersion = rootProject.extra.get("kotlinVersion")


android {
    compileSdk = 33

    defaultConfig {
        minSdk =  23
        targetSdk =33

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
    implementation (fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation ("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation ("androidx.core:core-ktx:1.8.0")
    implementation ("androidx.appcompat:appcompat:1.4.2")
    testImplementation ("junit:junit:4.13.2")
    androidTestImplementation ("androidx.test.ext:junit:1.1.3")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.4.0")

    //Paging Library
    val pagingVersion = "2.1.1"
    api ("androidx.paging:paging-runtime-ktx:3.1.1")
    api ("com.google.firebase:firebase-firestore-ktx:24.2.0")
    api ("com.firebaseui:firebase-ui-firestore:8.0.1")


}