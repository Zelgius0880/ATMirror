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

plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.gms.google-services")
    id("kotlin-kapt")
}
val kotlinVersion = rootProject.extra.get("kotlinVersion") as String
//val getProps = rootProject.extra["getProps"] as (String) -> String


android {
    compileSdk = 33

    defaultConfig {
        applicationId = "zelgius.com.atmirror.mobile"
        minSdk = 27
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("debug") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        viewBinding = true
    }
}

val navVersion = "2.3.0"
val pagingVersion = "3.1.1"

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
    implementation(fileTree(mapOf("dir" to "src/main/libs", "include" to listOf("*.jar", "*.aar"))))
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("androidx.core:core-ktx:1.9.0-alpha05")
    implementation("androidx.appcompat:appcompat:1.4.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation(project(path = ":shared"))
    implementation(project(path = ":utils"))
    implementation(project(path = ":swipeToDelete"))
    implementation(project(path = ":ShapeRipple"))
    implementation("com.zelgius.android-libraries:livedataextensions:1.1.0")
    implementation("com.zelgius.android-libraries:ContextExtensions:1.0.0")
    implementation("com.zelgius.android-libraries:DialogExtensions:1.0.0")
    implementation("com.zelgius.android-libraries:view-helper-extensions:1.0.3")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")

    //Jetpack
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.4.1")

    //Material
    implementation("com.google.android.material:material:1.7.0-alpha02")

    // Java language implementation
    implementation("androidx.navigation:navigation-fragment:$navVersion")
    implementation("androidx.navigation:navigation-ui:$navVersion")
    implementation("androidx.fragment:fragment:1.4.1")

    // Kotlin
    implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navVersion")

//Paging Library
    implementation ("androidx.paging:paging-runtime-ktx:$pagingVersion")


    // alternatively - without Android dependencies for testing
    testImplementation("androidx.paging:paging-common:$pagingVersion")
    // For Kotlin use paging-common-ktx

    // FirebaseUI for Cloud Firestore
    //implementation "com.firebaseui:firebase-ui-firestore:6.2.1"
}
