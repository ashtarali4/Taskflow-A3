// Jenkins CI/CD Pipeline for Taskflow - Stabilized on 2026-05-08
pipeline {
    agent {
        node {
            label ""
            customWorkspace "/var/lib/jenkins/workspace/Taskflow-A3-FINAL"
        }
    }

    triggers {
        pollSCM('* * * * *')
    }

    environment {
        TEST_URL = 'http://52.87.169.55'
    }

    options {
        disableConcurrentBuilds()
        skipDefaultCheckout()
    }

    stages {
        stage('Force Cleanup & Checkout') {
            steps {
                script {
                    // Force remove any existing containers with conflicting names
                    sh 'docker rm -f taskflow_db taskflow_backend taskflow_frontend || true'
                    // Stop any existing containers and clean workspace
                    sh 'docker compose down || true'
                    sh 'docker run --rm -u 0:0 -v /var/lib/jenkins/workspace/Taskflow-A3-FINAL:/ws -w /ws markhobson/maven-chrome /bin/sh -c "rm -rf ./*"'
                }
                checkout scm
            }
        }
        stage('Deploy Application') {
            steps {
                script {
                    echo "Bringing the deployment up as per assignment requirements..."
                    sh 'docker compose up -d --build'
                    echo "Waiting for services to stabilize..."
                    sh 'sleep 20' // Give Postgres and Backend time to start
                }
            }
        }
        stage('Automated Selenium Tests') {
            options {
                timeout(time: 10, unit: 'MINUTES') 
            }
            steps {
                script {
                    docker.image('markhobson/maven-chrome').inside('-u 0:0 --network host --privileged --shm-size=2g') {
                        dir('tests') {
                            sh 'mvn clean test -Dmaven.repo.local=.m2/repository -Dwdm.cachePath=.wdm -Dsurefire.useFile=false'
                        }
                    }
                }
            }
        }
    }

    post {
        always {
            script {
                try {
                    // Manually get the committer email from git to bypass Jenkins security restrictions
                    def committerEmail = sh(script: "git show -s --format=%ae HEAD", returnStdout: true).trim()
                    echo "Sending results to: ashtarali720@gmail.com and ${committerEmail}"
                    
                    emailext (
                        subject: "Taskflow Build ${env.BUILD_NUMBER}: ${currentBuild.currentResult}",
                        body: "Build ${env.BUILD_NUMBER} finished with status ${currentBuild.currentResult}. Check console: ${env.BUILD_URL}",
                        to: "ashtarali720@gmail.com, ${committerEmail}",
                        recipientProviders: [culprits(), developers()]
                    )
                } catch (Exception e) {
                    echo "Email failed or git not available: ${e.message}"
                }
            }
        }
    }
}
