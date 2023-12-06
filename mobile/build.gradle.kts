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
    compileSdk = 34
    namespace = "zelgius.com.atmirror.mobile"

    defaultConfig {
        applicationId = "zelgius.com.atmirror.mobile"
        minSdk = 27
        targetSdk = 34
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
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }

    namespace = "zelgius.com.atmirror.mobile"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

val navVersion = "2.7.2"
val pagingVersion = "3.2.1"

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
    implementation(fileTree(mapOf("dir" to "src/main/libs", "include" to listOf("*.jar", "*.aar"))))

    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation(project(path = ":shared"))
    implementation(project(path = ":utils"))
    implementation(project(path = ":swipeToDelete"))
    implementation(project(path = ":ShapeRipple"))
    implementation("com.zelgius.android-libraries:ContextExtensions:1.0.0")
    implementation("com.zelgius.android-libraries:DialogExtensions:1.0.0")
    implementation("com.zelgius.android-libraries:view-helper-extensions:1.0.3")
    implementation(project(":colorpicker"))

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    //Jetpack
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")

    //Material
    implementation("com.google.android.material:material:1.9.0")

    // Java language implementation
    implementation("androidx.navigation:navigation-fragment:$navVersion")
    implementation("androidx.navigation:navigation-ui:$navVersion")
    implementation("androidx.fragment:fragment:1.6.1")

    // Kotlin
    implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navVersion")

//Paging Library
    implementation ("androidx.paging:paging-runtime-ktx:$pagingVersion")


    // alternatively - without Android dependencies for testing
    implementation("androidx.paging:paging-common:$pagingVersion")
    testImplementation("androidx.paging:paging-runtime:$pagingVersion")
    // For Kotlin use paging-common-ktx

    // FirebaseUI for Cloud Firestore
    implementation ("com.firebaseui:firebase-ui-firestore:8.0.1")


    // Compose
    val composeBom = platform("androidx.compose:compose-bom:2023.10.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)
    // Material Design 3
    implementation("androidx.compose.material3:material3")
    // Android Studio Preview support
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.activity:activity-compose:1.8.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.compose.runtime:runtime-livedata")


    // Test only
    androidTestImplementation("com.squareup.retrofit2:retrofit:2.9.0")
    androidTestImplementation("com.squareup.retrofit2:converter-gson:2.9.0")
    androidTestImplementation("com.google.code.gson:gson:2.9.0")
    androidTestImplementation("com.squareup.okhttp3:logging-interceptor:4.8.1")
}
