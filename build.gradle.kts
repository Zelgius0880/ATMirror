import java.net.URI

// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    val kotlinVersion by extra { "1.3.72" }
    val kotlin_version by extra { "1.3.72" }

    repositories {
        google()
        jcenter()
        maven (url =  "https://dl.bintray.com/poldz123/maven/")
    }

    dependencies {
        classpath("com.android.tools.build:gradle:4.1.0-alpha09")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${kotlinVersion}")
        classpath("com.google.gms:google-services:4.3.3")
        classpath("com.google.firebase:firebase-plugins:2.0.0")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }

}

allprojects {
    repositories {
        flatDir {
            dirs ("src/main/libs")
        }

        google()
        jcenter()
        mavenCentral()

        maven("https://raw.githubusercontent.com/Zelgius0880/AndroidLibraries/master/releases")

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