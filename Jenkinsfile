pipeline {
    agent any

    environment {
        NEXUS_VERSION = "nexus3"
        NEXUS_PROTOCOL = "http"
        NEXUS_URL = "localhost:8081"
        NEXUS_REPOSITORY = "maven-snapshot"
        NEXUS_CREDENTIAL_ID = "nexus"
        PROJECT_VERSION = "2.0.0-SNAPSHOT"
    }

    stages {
        stage("Clean") {
            steps {
                sh "chmod +x ./gradlew";
                sh "./gradlew clean";
            }
        }
        stage("Build") {
            steps {
                sh "./gradlew build";
            }
            post {
                success {
                    archiveArtifacts artifacts: 'build/libs/DiscordCommands.jar', fingerprint: true
                }
            }
        }
        stage("Build ShadowJar") {
            steps {
                sh "./gradlew shadowJar";
            }
            post {
                success {
                    archiveArtifacts artifacts: 'build/libs/DiscordCommands-full.jar', fingerprint: true
                }
            }
        }
        stage("Docs") {
            steps {
                sh "./gradlew dokkaHtml";
                sh "rm -r /var/www/docs/discordcommands-v1.0.0"
                sh "mkdir /var/www/docs/discordcommands-v1.0.0"
                sh "cp -r build/discordcommands-v1.0.0 /var/www/docs/"
            }
        }
        stage("Sources") {
            steps {
                sh "./gradlew kotlinSourcesJar";
            }
            post {
                success {
                    archiveArtifacts artifacts: 'build/libs/DiscordCommands-sources.jar', fingerprint: true
                }
            }
        }
        stage("Publish") {
            steps {
                script {
                    nexusArtifactUploader(
                            nexusVersion: NEXUS_VERSION,
                            protocol: NEXUS_PROTOCOL,
                            nexusUrl: NEXUS_URL,
                            groupId: "eu.vironlab.discordcommands",
                            version: PROJECT_VERSION,
                            repository: NEXUS_REPOSITORY,
                            credentialsId: NEXUS_CREDENTIAL_ID,
                            artifacts:
                                    [
                                            [
                                                    artifactId: "DiscordCommands",
                                                    classifier: '',
                                                    file      : "build/libs/DiscordCommands.jar",
                                                    type      : "jar"
                                            ],
                                            [
                                                    artifactId: "DiscordCommands",
                                                    classifier: 'sources',
                                                    file      : "build/libs/DiscordCommands-sources.jar",
                                                    type      : "jar"
                                            ],
                                            [
                                                    artifactId: "DiscordCommands",
                                                    classifier: '',
                                                    file      : "build/pom/pom.xml",
                                                    type      : "pom"
                                            ]
                                    ]
                    );
                }
            }
        }
    }
}
