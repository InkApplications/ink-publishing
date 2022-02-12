plugins {
    id("java-gradle-plugin")
    id("org.jetbrains.kotlin.jvm") version "1.5.31"
    id("maven-publish")
    id("signing")
}

repositories {
    mavenCentral()
}

java {
    withJavadocJar()
}

val stubJavadoc by tasks.creating(Jar::class) {
    archiveClassifier.set("javadoc")
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
                if (artifacts.none { it.classifier == "javadoc"}) {
                    artifact(stubJavadoc)
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
signing {
    if (signingKeyId != null && signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
        sign(project.extensions.getByType(PublishingExtension::class.java).publications)
    }
}
