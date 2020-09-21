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
    id("kotlin-android")
    id("kotlin-android-extensions")
    id("com.google.gms.google-services")
    id("kotlin-kapt")
}

val kotlinVersion = rootProject.extra.get("kotlinVersion")
//val getProps = rootProject.extra["getProps"] as (String) -> String


val composeVersion by extra { "1.0.0-alpha02" }


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
            buildConfigField("String", "NETATMO_MAC", getProps("netatmo.mac"))
            buildConfigField("String", "NETATMO_MODULE_MAC", getProps("netatmo.module.mac"))
            buildConfigField("String", "NETATMO_CLIENT_ID", getProps("netatmo.client.id"))
            buildConfigField("String", "NETATMO_CLIENT_SECRET", getProps("netatmo.client.secret"))
            buildConfigField("String", "NETATMO_USER_PASSWORD", getProps("netatmo.user.password"))
            buildConfigField("String", "NETATMO_USER_EMAIL", getProps("netatmo.user.email"))

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
            buildConfigField("String", "NETATMO_MAC", getProps("netatmo.mac"))
            buildConfigField("String", "NETATMO_MODULE_MAC", getProps("netatmo.module.mac"))
            buildConfigField("String", "NETATMO_CLIENT_ID", getProps("netatmo.client.id"))
            buildConfigField("String", "NETATMO_CLIENT_SECRET", getProps("netatmo.client.secret"))
            buildConfigField("String", "NETATMO_USER_PASSWORD", getProps("netatmo.user.password"))
            buildConfigField("String", "NETATMO_USER_EMAIL", getProps("netatmo.user.email"))
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
        jvmTarget = "1.8"
        useIR = true
    }

    buildFeatures {
        compose = true
        viewBinding = true
    }

    composeOptions {
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
    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    compileOnly("com.google.android.things:androidthings:+")

    implementation("com.google.android.material:material:1.2.1")

    // You also need to include the following Compose toolkit dependencies.
/*    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.ui:ui-tooling:$composeVersion")
    implementation("androidx.compose.foundation:foundation-layout:$composeVersion")
    implementation("androidx.compose.material:material:$composeVersion")
    implementation("androidx.compose.runtime:runtime-livedata:$composeVersion")*/
    implementation ("androidx.compose.ui:ui:$composeVersion")
    // Tooling supp(ort (Previews, etc.))
    implementation ("androidx.ui:ui-tooling:$composeVersion")
    // Foundation ((Border, Background, Box, Image, Scroll, shapes, animations, etc.))
    implementation ("androidx.compose.foundation:foundation:$composeVersion")
    // Material Des(ign)
    implementation ("androidx.compose.material:material:$composeVersion")
    // Material des(ign icons)
    implementation ("androidx.compose.material:material-icons-core:$composeVersion")
    implementation ("androidx.compose.material:material-icons-extended:$composeVersion")
    // Integration (with observables)
    implementation ("androidx.compose.runtime:runtime-livedata:$composeVersion")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    implementation("com.google.firebase:firebase-database:19.4.0")
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

    val workVersion = "2.4.0"
    implementation("androidx.work:work-runtime-ktx:$workVersion")

    //Room
    val roomVersion = "2.2.5"
    implementation("androidx.room:room-runtime:$roomVersion")
    kapt("androidx.room:room-compiler:${roomVersion}")
    // For Kotlin use kapt instead of kapt

    // optional - Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:$roomVersion")

    //KTX & coroutines
    implementation("androidx.core:core-ktx:1.5.0-alpha03")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.8")


    //Other Libraries
    implementation(group = "com.github.hotchemi", name = "khronos", version = "0.9.0")
    implementation("com.facebook.stetho:stetho:1.5.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("com.squareup.okhttp3:logging-interceptor:4.8.1")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
