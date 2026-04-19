plugins {
    id("kmplogger.kotlin-multiplatform")
    id("kmplogger.maven-publish")
}

kotlin {
    sourceSets {
        androidLibrary {
            namespace = "io.github.shivathapaa.logger"
        }
    }
}

mavenPublishing {
    pom {
        name.set("KMP Logger")
        description.set(
            "Kotlin Multiplatform logger - A lightweight, structured logging library for Kotlin Multiplatform. Supports Android, iOS, macOS,\n" +
                    "watchOS, tvOS, JVM, JS (Node.js & Browser), Wasm/JS, Linux, and MinGW."
        )
    }
}