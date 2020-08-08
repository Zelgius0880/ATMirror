// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    val kotlinVersion by extra { "1.4.0-rc" }

    repositories {
        google()
        jcenter()
        maven (url =  "https://dl.bintray.com/poldz123/maven/")
        maven ( "https://dl.bintray.com/kotlin/kotlin-eap/" )
    }

    dependencies {
        classpath("com.android.tools.build:gradle:4.2.0-alpha07")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${kotlinVersion}")
        classpath("com.google.gms:google-services:4.3.3")
        classpath("com.google.firebase:firebase-plugins:2.0.0")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }

}

allprojects {
    repositories {
        google()
        jcenter()
        mavenCentral()
        maven(url = "https://jitpack.io")

        maven("https://raw.githubusercontent.com/Zelgius0880/AndroidLibraries/master/releases")
        maven ( "https://dl.bintray.com/kotlin/kotlin-eap/" )
    }

}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}


val getProps by extra {
    fun(propName: String): String {
        val propsFile = rootProject.file("local.properties")
        return if (propsFile.exists()) {
            val props = java.util.Properties()
            props.load(java.io.FileInputStream(propsFile))
            props[propName] as String
        } else {
            ""
        }
    }
}

/*
DependencyHandleScope.kotlinProject
 */