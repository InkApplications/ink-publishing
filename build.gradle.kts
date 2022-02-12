plugins {
    id("java-gradle-plugin")
    id("org.jetbrains.kotlin.jvm") version "1.5.31"
    id("maven-publish")
}

repositories {
    mavenCentral()
}

gradlePlugin {
    val publishing by plugins.creating {
        id = "com.inkapplications.publishing"
        implementationClass = "com.inkapplications.gradle.publishing.InkPublishingPlugin"
    }
}
