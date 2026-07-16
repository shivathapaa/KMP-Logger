plugins {
    id("kmplogger.kotlin-multiplatform")
    id("kmplogger.maven-publish")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // `api`, not `implementation`: LogContext and LogContextElement are part of this
            // module's public API, so consumers must be able to name them transitively.
            api(project(":logger"))
            api(libs.kotlinx.coroutines.core)
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