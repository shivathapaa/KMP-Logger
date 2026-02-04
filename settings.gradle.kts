rootProject.name = "KMP-Logger"

pluginManagement {
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
include(":sample:androidApp")
include(":sample:composeApp")
include(":sample:terminalApp")