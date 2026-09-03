plugins {
    alias(libs.plugins.kotlinMultiplatform).apply(false)
    alias(libs.plugins.composeMultiplatform).apply(false)
    alias(libs.plugins.compose.compiler).apply(false)
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.android.kotlin.multiplatform.library).apply(false)
    alias(libs.plugins.dokka).apply(false)
}

// Dokka multi-module aggregation. The root project is the aggregator: it applies Dokka and pulls
// each documented module in through the `dokka` configuration, producing one combined HTML site at
// `build/dokka/html`. Per-module settings (source links, visibility, kotlinx cross-links) live in
// the `kmplogger.dokka` convention plugin; only the site-wide identity is set here.
//
// `apply false` above keeps Dokka's classes on the root's classpath; the root then applies it
// imperatively to act as aggregator without also being documented itself.
apply(plugin = "org.jetbrains.dokka")

dependencies {
    "dokka"(project(":logger"))
    "dokka"(project(":logger-coroutines"))
}

extensions.configure<org.jetbrains.dokka.gradle.DokkaExtension> {
    moduleName.set("KMP Logger")
}