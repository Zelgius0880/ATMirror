plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlin-android-extensions")
}
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
    implementation (fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation ("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation ("androidx.core:core-ktx:1.3.1")
    implementation ("androidx.appcompat:appcompat:1.2.0")
    testImplementation ("junit:junit:4.13")
    androidTestImplementation ("androidx.test.ext:junit:1.1.1")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.2.0")

    //Paging Library
    val pagingVersion = "2.1.1"
    api ("androidx.paging:paging-runtime-ktx:2.1.2")
    api ("com.firebaseui:firebase-ui-firestore:6.2.1")


}