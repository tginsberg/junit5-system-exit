/*
 * MIT License
 *
 * Copyright (c) 2024 Todd Ginsberg
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import java.io.IOException
import java.net.URI

plugins {
    id("com.adarshr.test-logger") version "4.0.0"
    id("jacoco")
    id("java-library")
    id("org.barfuin.gradle.jacocolog") version "3.1.0"
    id("maven-publish")
    id("signing")
}

description = "A JUnit5 Extension to help write tests that call System.exit()"
group = "com.ginsberg"
version = file("VERSION.txt").readLines().first()

val gitBranch = gitBranch()
val junit5SystemExitVersion = if (gitBranch == "main" || gitBranch.startsWith("release/")) version.toString()
else "${gitBranch.substringAfterLast("/")}-SNAPSHOT"

val asmVersion by extra("9.7")
val junitVersion by extra("5.11.0")
val junitPlatformLauncherVersion by extra("1.11.0")

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
    withJavadocJar()
    withSourcesJar()
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.junit.jupiter:junit-jupiter-api:$junitVersion") {
        because("This library compiles against JUnit, but consumers will bring their own implementation")
    }

    implementation("org.ow2.asm:asm:$asmVersion") {
        because("Calls to System.exit() are rewritten in the agent using ASM.")
    }

    testRuntimeOnly("org.junit.platform:junit-platform-launcher:$junitPlatformLauncherVersion") {
        because("Starting in Gradle 9.0, this needs to be an explicitly declared dependency")
    }

    testImplementation("org.assertj:assertj-core:3.26.3")
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testImplementation("org.junit.platform:junit-platform-launcher:$junitPlatformLauncherVersion")
}

publishing {
    publications {
        create<MavenPublication>("junit5-system-exit") {
            from(components["java"])
            pom {
                name = "Junit5 System Exit"
                description = project.description
                version = junit5SystemExitVersion
                url = "https://github.com/tginsberg/junit5-system-exit"
                organization {
                    name = "com.ginsberg"
                    url = "https://github.com/tginsberg"
                }
                issueManagement {
                    system = "GitHub"
                    url = "https://github.com/tginsberg/junit5-system-exit/issues"
                }
                licenses {
                    license {
                        name = "The Apache License, Version 2.0"
                        url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }
                developers {
                    developer {
                        id = "tginsberg"
                        name = "Todd Ginsberg"
                        email = "todd@ginsberg.com"
                    }
                }
                scm {
                    connection = "scm:git:https://github.com/tginsberg/junit5-system-exit.git"
                    developerConnection = "scm:git:https://github.com/tginsberg/junit5-system-exit.git"
                    url = "https://github.com/tginsberg/junit5-system-exit"
                }
            }
        }
    }
    repositories {
        maven {
            url = if (version.toString().endsWith("-SNAPSHOT")) URI("https://oss.sonatype.org/content/repositories/snapshots/")
            else URI("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = System.getenv("SONATYPE_USERNAME")
                password = System.getenv("SONATYPE_TOKEN")
            }
        }
    }
}

signing {
    useInMemoryPgpKeys(System.getenv("SONATYPE_SIGNING_KEY") , System.getenv("SONATYPE_SIGNING_PASSPHRASE"))
    sign(publishing.publications["junit5-system-exit"])
}

tasks {
    jacocoTestReport {
        dependsOn(test)
        reports {
            xml.required = true
        }
    }

    jar {
        manifest {
            attributes(
                "Implementation-Title" to "Junit5 System Exit",
                "Implementation-Version" to archiveVersion,
                "Premain-Class" to "com.ginsberg.junit.exit.agent.Junit5SystemExitAgent"
            )
        }
    }

    javadoc {
        (options as CoreJavadocOptions).apply {
            addStringOption("source", rootProject.java.toolchain.languageVersion.get().toString())
            addStringOption("Xdoclint:none", "-quiet")
        }
    }

    publish {
        doLast {
            println("Project Version: $version")
            println("Publish Version: $junit5SystemExitVersion")
        }
    }

    test {
        useJUnitPlatform()
        dependsOn(jar)
        finalizedBy(jacocoTestReport)
        jvmArgumentProviders.add(CommandLineArgumentProvider {
            listOf("-javaagent:${jar.get().archiveFile.get().asFile.absolutePath}")
        })

        doLast {
            println(
                """
                Note: Several tests were skipped during this run, by design.
                      The skipped tests are run manually within other tests, to test instrumenting behavior.
                """.trimIndent()
            )
        }
    }

}

fun gitBranch(): String =
    ProcessBuilder("git rev-parse --abbrev-ref HEAD".split(" "))
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()
        .run {
            val error = errorStream.bufferedReader().readText()
            if (error.isNotEmpty()) throw IOException(error)
            inputStream.bufferedReader().readText().trim()
        }
