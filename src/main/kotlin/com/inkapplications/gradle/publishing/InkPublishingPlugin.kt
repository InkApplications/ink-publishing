/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package com.inkapplications.gradle.publishing

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.jvm.tasks.Jar
import org.gradle.plugins.signing.SigningExtension
import org.gradle.plugins.signing.SigningPlugin

/**
 * Applies Maven Publishing and signing plugins with reasonable defaults.
 */
class InkPublishingPlugin: Plugin<Project> {
    override fun apply(target: Project) {
        target.version = when (target.properties["version"]?.toString()) {
            null, "unspecified", "" -> "1.0-SNAPSHOT"
            else -> target.properties["version"].toString()
        }
        target.pluginManager.apply(MavenPublishPlugin::class.java)

        target.extensions.configure(PublishingExtension::class.java) {
            it.repositories.maven {
                it.name = "Build"
                it.url = target.uri(target.layout.buildDirectory.dir("repo"))
            }
            val mavenUser = target.findProperty("mavenUser")?.toString()
            val mavenPassword = target.findProperty("mavenPassword")?.toString()
            if (mavenUser != null && mavenPassword != null) {
                it.repositories.maven {
                    it.name = "MavenCentral"
                    it.url = target.uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                    it.credentials {
                        it.username = mavenUser
                        it.password = mavenPassword
                    }
                }
            }
        }
        val stubJavaDoc = target.tasks.register("stubJavaDoc", Jar::class.java) {
            it.archiveClassifier.set("javadoc")
        }
        target.afterEvaluate {
            target.extensions.configure(PublishingExtension::class.java) {
                it.publications.all {
                    (it as? MavenPublication)?.run {
                        if (artifacts.none { it.classifier == "javadoc" }) {
                            artifact(stubJavaDoc.get())
                        }
                    }
                }
            }
        }
        target.pluginManager.apply(SigningPlugin::class.java)
        target.extensions.configure(SigningExtension::class.java) {
            val signingKey = target.findProperty("signingKey")?.toString()
            val signingKeyId = target.findProperty("signingKeyId")?.toString()
            val signingPassword = target.findProperty("signingPassword")?.toString()
            if (signingKeyId != null && signingKey != null && signingPassword != null) {
                it.useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
                it.sign(target.extensions.getByType(PublishingExtension::class.java).publications)
            }
        }
    }
}
