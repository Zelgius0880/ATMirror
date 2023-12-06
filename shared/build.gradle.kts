import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import org.gradle.kotlin.dsl.kotlin
import java.io.FileInputStream
import java.util.Properties
val getProps by extra {
    fun(propName: String): String {
        val propsFile = rootProject.file("local.properties")
        return if (propsFile.exists()) {
            val props = Properties()
            props.load(FileInputStream(propsFile))
            props[propName] as String
        } else {
            ""
        }
    }
}

val kotlinVersion = rootProject.extra.get("kotlinVersion")
//val getProps = rootProject.extra["getProps"] as (String) -> String

plugins {
    id("com.android.library")
    kotlin("android")
    id("com.google.gms.google-services")
    id("kotlin-kapt")
}

android {
    compileSdk = 34

    sourceSets {
        /*test.java.srcDirs += 'src/test/kotlin'
        androidTest.java.srcDirs += 'src/androidTest/kotlin'*/
    }

    defaultConfig {
        minSdk =27

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles ("consumer-rules.pro")

        buildConfigField("String", "NETATMO_CLIENT_ID", getProps("netatmo.client.id"))
        buildConfigField("String", "NETATMO_MAC", getProps("netatmo.mac"))
        buildConfigField("String", "NETATMO_MODULE_MAC", getProps("netatmo.module.mac"))
        buildConfigField("String", "NETATMO_CLIENT_ID", getProps("netatmo.client.id"))
        buildConfigField("String", "NETATMO_CLIENT_SECRET", getProps("netatmo.client.secret"))
        buildConfigField("String", "NETATMO_USER_PASSWORD", getProps("netatmo.user.password"))
        buildConfigField("String", "NETATMO_USER_EMAIL", getProps("netatmo.user.email"))
        buildConfigField("String","EMAIL", gradleLocalProperties(rootDir).getProperty("firebase.email"))
        buildConfigField("String","PASSWORD", gradleLocalProperties(rootDir).getProperty("firebase.password"))
        buildConfigField("String", "HUE_DEFAULT_IP", gradleLocalProperties(rootDir).getProperty("hue.defaultId"))
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles (getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("boolean", "HUE_TESTS", "false")
        }

        getByName("debug") {
            buildConfigField("boolean", "HUE_TESTS", "false")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
    namespace = "zelgius.com.atmirror.shared"
}

dependencies {
    implementation (fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation ("androidx.core:core-ktx:1.10.1")
    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation (project(path= ":utils"))

    //Web
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.8.1")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    //KTX & coroutines
    val lifecycle_version = "2.5.1"
    api ("androidx.core:core-ktx:1.10.1")
    api ("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")
    api ("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version")
    api ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    //Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.2.0"))
    implementation ("com.google.firebase:firebase-firestore-ktx")
    implementation ("com.google.firebase:firebase-auth-ktx")

    // ViewModel and LiveData
    api  ("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")
    api  ("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version")
    kapt ("androidx.lifecycle:lifecycle-runtime:$lifecycle_version")
    kapt ("androidx.lifecycle:lifecycle-common-java8:$lifecycle_version")
    // For Kotlin use kapt instead of kapt
    // alternately - if using Java8, use the following instead of lifecycle-compiler
    implementation( "androidx.lifecycle:lifecycle-common-java8:$lifecycle_version")


    // Tests
    androidTestImplementation ("androidx.test.ext:junit:1.1.4")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.5.0")
    androidTestImplementation ("org.mockito:mockito-core:3.4.6")
    testImplementation ("org.mockito:mockito-core:3.4.6")
    androidTestImplementation ("androidx.arch.core:core-testing:2.1.0")
    testImplementation("junit:junit:4.13.2")

// (Required) Writing and executing Unit Tests on the JUnit Platform

    //Paging Library
    val paging_version = "3.1.1"
    implementation( "androidx.paging:paging-runtime-ktx:$paging_version")

    // alternatively - without Android dependencies for testing
    testImplementation ("androidx.paging:paging-common:${paging_version}")
    // For Kotlin use paging-common-ktx


}
