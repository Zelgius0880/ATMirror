import com.google.protobuf.gradle.*
import java.io.FileInputStream
import java.util.Properties
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.protobuf

val getProps by extra {
    fun(propName: String): String {
        val propsFile = rootProject.file("local.properties")
        return if (propsFile.exists()) {
            val props = Properties()
            props.load(FileInputStream(propsFile))
            (props[propName] as String?)?: ""
        } else {
            ""
        }
    }
}

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.google.gms.google-services")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id ("com.google.protobuf") version "0.9.0"
}

val kotlinVersion = rootProject.extra.get("kotlinVersion")
//val getProps = rootProject.extra["getProps"] as (String) -> String


val composeVersion by extra { "1.2.0-rc02" }


android {
    compileSdk = 34
    namespace = "zelgius.com.atmirror.things"
    defaultConfig {
        applicationId = "zelgius.com.atmirror"
        minSdk = 27
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments(mutableMapOf("room.schemaLocation" to "$projectDir/schemas".toString()))
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
    packagingOptions {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/license.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/notice.txt",
                "META-INF/ASL2.0",
                "META-INF/atomicfu.kotlin_module"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        viewBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.7"
    }
    lint {
        disable += setOf("AllowBackup", "GoogleAppIndexingWarning", "MissingApplicationIcon")
    }
}


dependencies {
    implementation(project(":utils"))
    implementation(project(":inky"))
    implementation(project(":shared"))
    implementation("androidx.compose.ui:ui-tooling-preview:1.3.1")
    implementation("androidx.hilt:hilt-common:1.0.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.4")
    compileOnly("com.google.android.things:androidthings:1.0")

    implementation("com.google.android.material:material:1.7.0")

    // You also need to include the following Compose toolkit dependencies.
    implementation( "androidx.activity:activity-compose:1.6.1")
    implementation("androidx.compose.ui:ui:$composeVersion")
    // Foundation ((Border, Background, Box, Image, Scroll, shapes, animations, etc.))
    implementation("androidx.compose.foundation:foundation:$composeVersion")
    // Material Des(ign)
    implementation("androidx.compose.material:material:$composeVersion")
    // Material des(ign icons)
    implementation("androidx.compose.material:material-icons-core:$composeVersion")
    implementation("androidx.compose.material:material-icons-extended:$composeVersion")
    // Integration (with observables)
    implementation("androidx.compose.runtime:runtime-livedata:$composeVersion")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    implementation("com.google.firebase:firebase-database:20.1.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("com.zelgius.android-libraries:ContextExtensions:1.0.0")
    implementation("com.zelgius.android-libraries:livedataextensions:1.1.0")
    implementation("com.zelgius.android-libraries:bitmap-ktx:1.0.1")
    debugImplementation("androidx.compose.ui:ui-tooling:1.3.1")

    val lifecycleVersion = "2.2.0"
    // ViewModel and LiveData
    implementation("androidx.lifecycle:lifecycle-extensions:$lifecycleVersion")
    kapt("androidx.lifecycle:lifecycle-common-java8:$lifecycleVersion")
    // For Kotlin use kapt instead of kapt
    // alternately - if using Java8, use the following instead of lifecycle-compiler
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")

    val workVersion = "2.7.1"
    implementation("androidx.work:work-runtime-ktx:$workVersion")

    //Room
    val roomVersion = "2.2.5"
    implementation("androidx.room:room-runtime:$roomVersion")
    kapt("androidx.room:room-compiler:${roomVersion}")
    // For Kotlin use kapt instead of kapt

    // optional - Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:$roomVersion")

    //KTX & coroutines
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    //hilt
    implementation("com.google.dagger:hilt-android:2.44")
    kapt("com.google.dagger:hilt-android-compiler:2.44")
    kapt ("androidx.hilt:hilt-compiler:1.0.0")
    implementation("androidx.hilt:hilt-work:1.0.0")

    // Datastore
    implementation("androidx.datastore:datastore:1.1.0-alpha04")
    implementation ( "com.google.protobuf:protobuf-javalite:3.21.7")

    //Other Libraries
    implementation(group = "com.github.hotchemi", name = "khronos", version = "0.9.0")
    implementation("com.facebook.stetho:stetho:1.6.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("com.squareup.okhttp3:logging-interceptor:4.8.1")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_17
}

kapt {
    correctErrorTypes = true
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.0.0-rc-2"
    }

    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                id("java") {
                    option("lite")
                }
            }
        }
    }
}