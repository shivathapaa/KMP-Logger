import com.android.build.api.variant.HostTestBuilder
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.composeMultiplatform)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

android {
    namespace = "sample.app"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "sample.app.androidApp"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    testOptions {
        // AGP 9.5 registers a Compose Preview runfiles task for every variant; configuring it
        // fails unless the variant's unit tests include the compiled Android resources.
        unitTests.isIncludeAndroidResources = true
    }
}

androidComponents {
    beforeVariants { variant ->
        // The same task also needs a unit-test component, which AGP only creates for debug by
        // default, so anything that realizes every task (such as `./gradlew tasks`) fails on release.
        variant.hostTests[HostTestBuilder.UNIT_TEST_TYPE]?.enable = true
    }
}

dependencies {
    implementation(libs.runtime)
    implementation(libs.ui)
    implementation(libs.foundation)
    implementation(libs.androidx.activityCompose)

    implementation(project(":sample:composeApp")) {
        exclude(group = "org.jetbrains.runtime", module = "jbr-api")
    }
}