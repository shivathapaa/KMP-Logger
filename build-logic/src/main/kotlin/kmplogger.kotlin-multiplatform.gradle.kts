import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
}

private val Project.libs
    get(): VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

kotlin {
    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.findLibrary("kotlinx-coroutines-test").get())
        }

        android {
            compileSdk = libs.findVersion("android-compileSdk").get().requiredVersion.toInt()
            minSdk = libs.findVersion("android-minSdk").get().requiredVersion.toInt()

            withJava()
            withHostTest {}

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

    jvm {
        testRuns["test"].executionTask.configure {
            useJUnit()
        }
    }

    js {
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
        nodejs {
            testTask {
                useMocha {
                    timeout = "60s"
                }
            }
        }
        useEsModules()
        binaries.executable()
        binaries.library()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
        nodejs {
            testTask {
                useMocha {
                    timeout = "60s"
                }
            }
        }
        binaries.executable()
        binaries.library()
    }

    iosArm64()
    iosSimulatorArm64()
    macosArm64()
    linuxX64()
    mingwX64()

    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        compilations["main"].compileTaskProvider.configure {
            compilerOptions {
                freeCompilerArgs.add("-Xexport-kdoc")
            }
        }
        compilations["test"].compileTaskProvider.configure {
            compilerOptions {
                freeCompilerArgs.add("-Xexport-kdoc")
            }
        }
    }
}