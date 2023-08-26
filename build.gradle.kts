plugins {
    id("java-gradle-plugin")
    id("org.jetbrains.kotlin.jvm") version "1.9.10"
    id("maven-publish")
    id("signing")
}

repositories {
    mavenCentral()
}

java {
    withJavadocJar()
    withSourcesJar()
}

gradlePlugin {
    val publishing by plugins.creating {
        id = "com.inkapplications.publishing"
        implementationClass = "com.inkapplications.gradle.publishing.InkPublishingPlugin"
    }
}

publishing {
    repositories {
        maven {
            name = "Build"
            url = uri(layout.buildDirectory.dir("repo"))
        }

        val mavenUser: String? by project
        val mavenPassword: String? by project
        if (mavenUser != null && mavenPassword != null) {
            maven {
                name = "MavenCentral"
                url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                credentials {
                    username = mavenUser
                    password = mavenPassword
                }
            }
        }
    }
}

afterEvaluate {
    extensions.configure(PublishingExtension::class.java) {
        publications {
            withType<MavenPublication> {
                val publication = this
                if (artifacts.none { it.classifier == "javadoc" }) {
                    val stubJavadoc = project.tasks.register("${publication.name}StubJavadocJar", Jar::class) {
                        archiveClassifier.set("javadoc")
                        // Each archive name should be distinct, to avoid implicit dependency issues.
                        // https://youtrack.jetbrains.com/issue/KT-46466
                        archiveBaseName.set("${archiveBaseName.get()}-${publication.name}-javadoc")
                    }
                    artifact(stubJavadoc)
                }
                if (artifacts.none { it.classifier == "sources" }) {
                    val stubSources = project.tasks.register("${publication.name}StubSourcesJar", Jar::class) {
                        archiveClassifier.set("sources")
                        // Each archive name sources be distinct, to avoid implicit dependency issues.
                        // https://youtrack.jetbrains.com/issue/KT-46466
                        archiveBaseName.set("${archiveBaseName.get()}-${publication.name}-sources")
                    }
                    artifact(stubSources)
                }
                pom {
                    name.set("Ink Publishing")
                    description.set("Boilerplate Gradle Publishing Config")
                    url.set("https://github.com/inkapplications/ink-publishing")
                    licenses {
                        license {
                            name.set("MIT")
                            url.set("https://choosealicense.com/licenses/mit/")
                        }
                    }
                    developers {
                        developer {
                            id.set("reneevandervelde")
                            name.set("Renee Vandervelde")
                            email.set("Renee@InkApplications.com")
                        }
                    }
                    scm {
                        connection.set("scm:git:https://github.com/InkApplications/ink-publishing.git")
                        developerConnection.set("scm:git:ssh://git@github.com:InkApplications/ink-publishing.git")
                        url.set("https://github.com/InkApplications/ink-publishing")
                    }
                }
            }
        }
    }
}

val signingKey: String? by project
val signingKeyId: String? by project
val signingPassword: String? by project
fun shouldSign() = signingKeyId != null && signingKey != null && signingPassword != null

signing {
    if (shouldSign()) {
        useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
    }
}

publishing.publications.configureEach {
    if (shouldSign()) {
        signing.sign(project.extensions.getByType(PublishingExtension::class.java).publications)
    }
}
