pipeline {
    agent none

    environment {
        TEST_URL = 'http://52.87.169.55'
    }

    options {
        disableConcurrentBuilds()
    }

    stages {
        stage('Emergency Workspace Cleanup') {
            agent {
                docker {
                    image 'markhobson/maven-chrome'
                    // Using root user to force delete files that Jenkins cannot touch
                    args '-u 0:0 --privileged'
                }
            }
            steps {
                sh 'rm -rf ./* || true'
                sh 'rm -rf .[a-zA-Z0-9]* || true'
            }
        }

        stage('Checkout Code') {
            agent any
            steps {
                checkout scm
            }
        }

        stage('Run Automated Tests') {
            agent {
                docker {
                    image 'markhobson/maven-chrome'
                    args '-u 0:0 --network host --privileged --shm-size=2g' 
                }
            }
            steps {
                dir('tests') {
                    sh 'mvn clean test -Dmaven.repo.local=.m2/repository -Dwdm.cachePath=.wdm'
                }
            }
            post {
                always {
                    // Fix permissions so Jenkins can clean up next time
                    sh 'chmod -R 777 . || true'
                }
            }
        }
    }

    post {
        always {
            script {
                try {
                    emailext (
                        subject: "Taskflow Build ${env.BUILD_NUMBER}: ${currentBuild.currentResult}",
                        body: "Build ${env.BUILD_NUMBER} finished with status ${currentBuild.currentResult}. Check console: ${env.BUILD_URL}",
                        to: 'ashtarali720@gmail.com'
                    )
                } catch (Exception e) {
                    echo "Email failed: ${e.message}"
                }
            }
        }
    }
}
