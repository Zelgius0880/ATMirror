plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlin-android-extensions")
    id("kotlin-kapt")

}
val kotlinVersion = rootProject.extra.get("kotlinVersion")


val getProps = rootProject.extra["getProps"] as (String) -> String


android {
    compileSdkVersion(29)


    defaultConfig {
        minSdkVersion(27)
        targetSdkVersion(29)
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }

}


tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = freeCompilerArgs + listOf("-Xallow-jvm-ir-dependencies", "-Xskip-prerelease-check")
    }
}


dependencies {
    api(project(path= ":utils"))
    api(project(":shared"))

    val lifecycleVersion = "2.2.0"

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    /* implementation "com.google.android.things.contrib:driver-bmx280:0.2"*/

    api("androidx.legacy:legacy-support-v4:1.0.0")
    api("androidx.appcompat:appcompat:1.1.0")
    api("com.google.firebase:firebase-database:19.3.1")
    api("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    api("com.zelgius.android-libraries:ContextExtensions:1.0.0")


    //UI
    api("com.google.android.material:material:1.1.0")
    api("androidx.constraintlayout:constraintlayout:1.1.3")

    // ViewModel and LiveData
    api("androidx.lifecycle:lifecycle-extensions:$lifecycleVersion")
    kapt("androidx.lifecycle:lifecycle-runtime:$lifecycleVersion")
    kapt("androidx.lifecycle:lifecycle-common-java8:$lifecycleVersion")
    // For Kotlin use kapt instead of kapt
    // alternately - if using Java8, use the following instead of lifecycle-compiler
    api("androidx.lifecycle:lifecycle-common-java8:$lifecycleVersion")
    api ("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")

    val workVersion = "1.0.1"
    api ("android.arch.work:work-runtime-ktx:$workVersion")

    //Room
    val roomVersion = "2.2.5"
    api("androidx.room:room-runtime:$roomVersion")
    kapt("androidx.room:room-compiler:${roomVersion}")
    // For Kotlin use kapt instead of kapt

    // optional - Kotlin Extensions and Coroutines support for Room
    api("androidx.room:room-ktx:$roomVersion")

    //KTX & coroutines
    api("androidx.core:core-ktx:1.5.0-alpha01")
    api("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.8")

    // Tests
    testImplementation("androidx.room:room-testing:$roomVersion")
    testImplementation("junit:junit:4.13")
    androidTestImplementation("androidx.test.ext:junit:1.1.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
    androidTestImplementation("org.mockito:mockito-core:3.4.4")
    testImplementation("org.mockito:mockito-core:3.4.4")
    androidTestImplementation("androidx.arch.core:core-testing:2.1.0")


    //Other Libraries
    api("com.jjoe64:graphview:4.2.2")
    api(group = "com.github.hotchemi", name=  "khronos", version= "0.9.0")
    api("com.facebook.stetho:stetho:1.5.1")
    api("com.squareup.retrofit2:retrofit:2.9.0")
    api("com.squareup.retrofit2:converter-gson:2.9.0")
    api("com.google.code.gson:gson:2.8.6")

    implementation("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")

}
