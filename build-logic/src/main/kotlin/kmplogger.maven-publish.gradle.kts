import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.SourcesJar

plugins {
    id("com.vanniktech.maven.publish")
}

mavenPublishing {
    coordinates(
        groupId = "io.github.shivathapaa",
        artifactId = project.name,
        version = providers.gradleProperty("kmplogger.version").get()
    )

    configure(KotlinMultiplatform(sourcesJar = SourcesJar.Sources()))

    publishToMavenCentral()

    if (providers.gradleProperty("signingInMemoryKey").isPresent ||
        providers.environmentVariable("ORG_GRADLE_PROJECT_signingInMemoryKey").isPresent
    ) {
        signAllPublications()
    }

    pom {
        name.set(project.name)
        inceptionYear.set("2026")
        url.set("https://github.com/shivathapaa/KMP-Logger")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
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
            url.set("https://github.com/shivathapaa/KMP-Logger")
            connection.set("scm:git:git://github.com/shivathapaa/KMP-Logger.git")
            developerConnection.set("scm:git:ssh://github.com/shivathapaa/KMP-Logger.git")
        }
    }
}