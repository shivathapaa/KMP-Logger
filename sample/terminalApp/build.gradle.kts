plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    listOf(
        macosArm64(),
        linuxX64(),
        mingwX64(),
    ).forEach {
        it.binaries.executable {
            entryPoint = "main"
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":logger"))
            implementation(project(":logger-coroutines"))
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
