plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-android-extensions")
    id("com.google.gms.google-services")
    id("kotlin-kapt")

}


android {
    compileSdkVersion( 29)
    defaultConfig {
        applicationId ("zelgius.com.atmirror")
        minSdkVersion( 27)
        targetSdkVersion (29)
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments = mutableMapOf("room.schemaLocation" to "$projectDir/schemas")
            }
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles (getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    lintOptions {
        disable("AllowBackup", "GoogleAppIndexingWarning", "MissingApplicationIcon")
    }

    packagingOptions {
        exclude ("META-INF/DEPENDENCIES")
        exclude ("META-INF/LICENSE")
        exclude ("META-INF/LICENSE.txt")
        exclude ("META-INF/license.txt")
        exclude ("META-INF/NOTICE")
        exclude ("META-INF/NOTICE.txt")
        exclude ("META-INF/notice.txt")
        exclude ("META-INF/ASL2.0")
        exclude ("META-INF/atomicfu.kotlin_module")
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
    implementation (project(path = ":utils"))
    implementation (project(path = ":sharedThing"))
    implementation (project(":shared"))
    val lifecycleVersion = "2.2.0-alpha01"

    implementation (fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    /* implementation "com.google.android.things.contrib:driver-bmx280:0.2"*/


}
