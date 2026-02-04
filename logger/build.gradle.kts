import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.SourcesJar
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.maven.publish)
}

group = "io.github.shivathapaa"
version = "1.2.1"

kotlin {
    sourceSets {
        androidLibrary {
            namespace = "io.github.shivathapaa.logger"
            compileSdk = libs.versions.android.compileSdk.get().toInt()
            minSdk = libs.versions.android.minSdk.get().toInt()

            withJava() // enable java compilation support

            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_11)
            }

            packaging {
                resources {
                    excludes += "/META-INF/{AL2.0,LGPL2.1}"
                }
            }
        }
    }
    jvm()
    js {
        browser()
        nodejs()
        useEsModules()
        binaries.executable()
        binaries.library()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        nodejs()
        binaries.executable()
        binaries.library()
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    macosX64()
    macosArm64()
    linuxX64()
    mingwX64()

    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        compilations["main"].compileTaskProvider.configure {
            compilerOptions {
                freeCompilerArgs.add("-Xexport-kdoc")
            }
        }
    }
}

mavenPublishing {
    coordinates(group.toString(), "logger", version.toString())
    configure(KotlinMultiplatform(sourcesJar = SourcesJar.Sources()))

    pom {
        name = "KMP Logger"
        description = "Kotlin Multiplatform logger"
        inceptionYear = "2026"
        url = "https://github.com/shivathapaa/KMP-Logger"

        licenses {
            license {
                name = "MIT"
                url = "https://opensource.org/licenses/MIT"
            }
        }

        developers {
            developer {
                id.set("shivathapaa")
                name.set("Shiva Thapa")
                email.set("query.shivathapaa.dev@gmail.com")
                url.set("https://github.com/shivathapaa/")
            }
        }

        scm {
            url = "https://github.com/shivathapaa/KMP-Logger"
        }
    }

    publishToMavenCentral()

    signAllPublications()
}
