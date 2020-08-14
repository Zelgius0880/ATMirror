plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-android-extensions")
    id("com.google.gms.google-services")
    id("kotlin-kapt")
}

val kotlinVersion = rootProject.extra.get("kotlinVersion")
val getProps = rootProject.extra["getProps"] as (String) -> String


val composeVersion = "0.1.0-dev14"

android {
    compileSdkVersion(29)
    defaultConfig {
        applicationId = "zelgius.com.atmirror"
        minSdkVersion(27)
        targetSdkVersion(29)
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments (mutableMapOf("room.schemaLocation" to "$projectDir/schemas".toString()))
            }
        }
    }
    buildTypes {
        getByName("debug") {
            buildConfigField("String", "DARKSKY_KEY", getProps("darkSky_key"))
            buildConfigField("double", "LATITUDE", getProps("latitude"))
            buildConfigField("double", "LONGITUDE", getProps("longitude"))

            isMinifyEnabled = false
            proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
            )
        }
        getByName("release") {
            buildConfigField("String", "DARKSKY_KEY", getProps("darkSky_key"))
            buildConfigField("double", "LATITUDE", getProps("latitude"))
            buildConfigField("double", "LONGITUDE", getProps("longitude"))
            isMinifyEnabled = false
            proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
            )
        }
    }
    lintOptions {
        disable("AllowBackup", "GoogleAppIndexingWarning", "MissingApplicationIcon")
    }

    packagingOptions {
        exclude("META-INF/DEPENDENCIES")
        exclude("META-INF/LICENSE")
        exclude("META-INF/LICENSE.txt")
        exclude("META-INF/license.txt")
        exclude("META-INF/NOTICE")
        exclude("META-INF/NOTICE.txt")
        exclude("META-INF/notice.txt")
        exclude("META-INF/ASL2.0")
        exclude("META-INF/atomicfu.kotlin_module")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }

    buildFeatures {
        compose = true
        viewBinding = true
    }

    composeOptions {
        //kotlinCompilerVersion = "1.4.0-rc"
        kotlinCompilerExtensionVersion = composeVersion
    }
}


/*
tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile).configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs += ["-Xallow-jvm-ir-dependencies", "-Xskip-prerelease-check"]
    }
}
*/


dependencies {
    implementation(project(":utils"))
    implementation(project(":inky"))
    implementation(project(":shared"))

    testImplementation("junit:junit:4.13")
    androidTestImplementation("androidx.test.ext:junit:1.1.1")
    compileOnly("com.google.android.things:androidthings:+")

    implementation("com.google.android.material:material:1.2.0")

    // You also need to include the following Compose toolkit dependencies.
/*    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.ui:ui-tooling:$composeVersion")
    implementation("androidx.compose.foundation:foundation-layout:$composeVersion")
    implementation("androidx.compose.material:material:$composeVersion")
    implementation("androidx.compose.runtime:runtime-livedata:$composeVersion")*/
    implementation ("androidx.ui:ui-core:$composeVersion")
    implementation ("androidx.ui:ui-tooling:$composeVersion")
    implementation ("androidx.ui:ui-layout:$composeVersion")
    implementation("androidx.compose:compose-runtime:$composeVersion")
    implementation ("androidx.ui:ui-material:$composeVersion")

    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    implementation("com.google.firebase:firebase-database:19.3.1")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("com.zelgius.android-libraries:ContextExtensions:1.0.0")
    implementation("com.zelgius.android-libraries:livedataextensions:1.1.0")
    implementation("com.zelgius.android-libraries:bitmap-ktx:1.0.1")

    val lifecycleVersion = "2.2.0"
    // ViewModel and LiveData
    implementation("androidx.lifecycle:lifecycle-extensions:$lifecycleVersion")
    kapt("androidx.lifecycle:lifecycle-common-java8:$lifecycleVersion")
    // For Kotlin use kapt instead of kapt
    // alternately - if using Java8, use the following instead of lifecycle-compiler
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")

    val workVersion = "1.0.1"
    implementation("android.arch.work:work-runtime-ktx:$workVersion")

    //Room
    val roomVersion = "2.2.5"
    implementation("androidx.room:room-runtime:$roomVersion")
    kapt("androidx.room:room-compiler:${roomVersion}")
    // For Kotlin use kapt instead of kapt

    // optional - Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:$roomVersion")

    //KTX & coroutines
    implementation("androidx.core:core-ktx:1.5.0-alpha01")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.8")


    //Other Libraries
    implementation(group = "com.github.hotchemi", name = "khronos", version = "0.9.0")
    implementation("com.facebook.stetho:stetho:1.5.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.code.gson:gson:2.8.6")
}
