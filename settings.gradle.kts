rootProject.name = "KMP-Logger"

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

include(":logger")
include(":logger-coroutines")

include(":sample:androidApp")
include(":sample:composeApp")
include(":sample:terminalApp")