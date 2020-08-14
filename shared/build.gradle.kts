import org.gradle.kotlin.dsl.kotlin


val kotlinVersion = rootProject.extra.get("kotlinVersion")
val getProps = rootProject.extra["getProps"] as (String) -> String

plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlin-android-extensions")
    id("com.google.gms.google-services")
    id("kotlin-kapt")
}

android {
    compileSdkVersion( 29)

    sourceSets {
        /*test.java.srcDirs += 'src/test/kotlin'
        androidTest.java.srcDirs += 'src/androidTest/kotlin'*/
    }

    defaultConfig {
        minSdkVersion (27)
        targetSdkVersion (29)
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles ("consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles (getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("String", "FIREBASE_PHONE_KEY", getProps("firestore.phone_key") )
            buildConfigField("String", "FIREBASE_MIRROR_KEY", getProps("firestore.mirror_key") )
            buildConfigField("boolean", "HUE_TESTS", "false")
            buildConfigField("String", "MIRROR_HUE_NAME", getProps("hue.mirror_name") )
            buildConfigField("String", "PHONE_HUE_NAME", getProps("hue.phone_name") )
        }

        getByName("debug") {
            buildConfigField("String", "FIREBASE_PHONE_KEY", getProps("firestore.phone_key") )
            buildConfigField("String", "FIREBASE_MIRROR_KEY", getProps("firestore.mirror_key") )
            buildConfigField("boolean", "HUE_TESTS", "false")
            buildConfigField("String", "MIRROR_HUE_NAME", getProps("hue.mirror_name") )
            buildConfigField("String", "PHONE_HUE_NAME", getProps("hue.phone_name") )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}

dependencies {
    implementation (fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation ("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation ("androidx.core:core-ktx:1.5.0-alpha01")
    implementation ("androidx.appcompat:appcompat:1.2.0")
    implementation (project(path= ":utils"))

    //Web
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.8.1")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")


    //KTX & coroutines
    val lifecycle_version = "2.2.0"
    api ("androidx.core:core-ktx:1.5.0-alpha01")
    api ("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")
    api ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.8")

    //Firebase
    implementation ("com.google.firebase:firebase-firestore-ktx:21.5.0")
    implementation ("com.google.firebase:firebase-auth:19.3.2")

    // ViewModel and LiveData
    api  ("androidx.lifecycle:lifecycle-extensions:$lifecycle_version")
    kapt ("androidx.lifecycle:lifecycle-runtime:$lifecycle_version")
    kapt ("androidx.lifecycle:lifecycle-common-java8:$lifecycle_version")
    // For Kotlin use kapt instead of kapt
    // alternately - if using Java8, use the following instead of lifecycle-compiler
    implementation( "androidx.lifecycle:lifecycle-common-java8:$lifecycle_version")


    // Tests
    androidTestImplementation ("androidx.test.ext:junit:1.1.1")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.2.0")
    androidTestImplementation ("org.mockito:mockito-core:3.4.6")
    testImplementation ("org.mockito:mockito-core:3.4.6")
    androidTestImplementation ("androidx.arch.core:core-testing:2.1.0")
// (Required) Writing and executing Unit Tests on the JUnit Platform


    //Paging Library
    val paging_version = "2.1.2"
    implementation( "androidx.paging:paging-runtime-ktx:$paging_version")


    // alternatively - without Android dependencies for testing
    testImplementation ("androidx.paging:paging-common:${paging_version}")
    // For Kotlin use paging-common-ktx


}
