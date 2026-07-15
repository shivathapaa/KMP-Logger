plugins {
    id("kmplogger.kotlin-multiplatform")
    id("kmplogger.maven-publish")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":logger"))
            implementation(libs.kotlinx.coroutines.core)
        }
        android {
            namespace = "io.github.shivathapaa.logger.coroutines"
        }
    }
}

mavenPublishing {
    pom {
        name.set("KMP Logger Coroutines")
        description.set("Coroutines support for KMP Logger - safe LogContext propagation across suspension points")
    }
}