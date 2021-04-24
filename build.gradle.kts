import kotlin.collections.*

buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        jcenter()
    }
    dependencies {
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.4.20")
        classpath("com.github.jengelman.gradle.plugins:shadow:6.1.0")
    }
}

//Define Plugins
plugins {
    id("java")
    id("maven")
    id("com.github.johnrengelman.shadow") version "6.1.0"
    kotlin("jvm") version "1.4.32"
    kotlin("kapt") version "1.4.32"
    id("org.jetbrains.dokka") version "1.4.20"
}


//Define Repositorys
repositories {
    mavenCentral()
    jcenter()
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.vironlab.eu/repository/snapshot/")
    maven("https://m2.dv8tion.net/releases/")
}

//Define Version and Group
group = findProperty("group").toString()
version = findProperty("version").toString()


//Configure build of docs
tasks.dokkaHtml.configure {
    outputDirectory.set(File(rootProject.buildDir.path, "discordcommands-v1.0.0"))
}

//Define Dependencies for all Modules
dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-serialization")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3")
    implementation("com.google.inject:guice:5.0.1")
    implementation("net.dv8tion:JDA:4.2.1_256")
}

tasks {
    //Set the Name of the Sources Jar
    kotlinSourcesJar {
        archiveFileName.set("${rootProject.name}-sources.jar")
        doFirst {
            //Set Manifest
            manifest {
                attributes["Implementation-Title"] = rootProject.name
                attributes["Implementation-Version"] = findProperty("version").toString()
                attributes["Specification-Version"] = findProperty("version").toString()
                attributes["Implementation-Vendor"] = "VironLab.eu"
                attributes["Built-By"] = System.getProperty("user.name")
                attributes["Build-Jdk"] = System.getProperty("java.version")
                attributes["Created-By"] = "Gradle ${gradle.gradleVersion}"
            }
        }
    }

    shadowJar {
        //Set the Name of the Output File
        archiveFileName.set("${rootProject.name}-full.jar")

        exclude("META-INF/**")

        doFirst {
            //Set Manifest
            manifest {
                attributes["Implementation-Title"] = rootProject.name
                attributes["Implementation-Version"] = findProperty("version").toString()
                attributes["Specification-Version"] = findProperty("version").toString()
                attributes["Implementation-Vendor"] = "VironLab.eu"
                attributes["Built-By"] = System.getProperty("user.name")
                attributes["Build-Jdk"] = System.getProperty("java.version")
                attributes["Created-By"] = "Gradle ${gradle.gradleVersion}"
            }
        }
    }

    jar {
        archiveFileName.set("${rootProject.name}.jar")
        doFirst {
            //Set Manifest
            manifest {
                attributes["Implementation-Title"] = rootProject.name
                attributes["Implementation-Version"] = findProperty("version").toString()
                attributes["Specification-Version"] = findProperty("version").toString()
                attributes["Implementation-Vendor"] = "VironLab.eu"
                attributes["Built-By"] = System.getProperty("user.name")
                attributes["Build-Jdk"] = System.getProperty("java.version")
                attributes["Created-By"] = "Gradle ${gradle.gradleVersion}"
            }
        }
        doLast {
            //Generate the Pom file for the Repository
            maven.pom {
                withGroovyBuilder {
                    "project" {
                        groupId = "eu.vironlab.discordcommands"
                        artifactId = rootProject.name
                        version = findProperty("version").toString()
                        this.setProperty("inceptionYear", "2021")
                        "licenses" {
                            "license" {
                                setProperty("name", "General Public License (GPL v3.0)")
                                setProperty("url", "https://www.gnu.org/licenses/gpl-3.0.txt")
                                setProperty("distribution", "repo")
                            }
                        }
                        "developers" {
                            "developer" {
                                setProperty("id", "Infinity_dev")
                                setProperty("name", "Florin Dornig")
                                setProperty("email", "infinitydev@vironlab.eu")
                            }
                        }
                    }
                }

            }.writeTo("build/pom/pom.xml")
        }
    }

    withType<JavaCompile> {
        this.options.encoding = "UTF-8"
    }

}


