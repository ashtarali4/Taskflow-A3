pipeline {
    agent {
        node {
            // Using a new workspace name to bypass the permission-locked folder
            customWorkspace "/var/lib/jenkins/workspace/Taskflow-A3-Fixed"
        }
    }

    environment {
        TEST_URL = 'http://52.87.169.55'
    }

    options {
        disableConcurrentBuilds()
    }

    stages {
        stage('Checkout Code') {
            steps {
                // Fresh checkout in the new folder
                checkout scm
            }
        }

        stage('Run Automated Tests') {
            agent {
                docker {
                    image 'markhobson/maven-chrome'
                    // Run as root inside the container for Chrome permissions
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
                    // Fix permissions so this new workspace doesn't get locked later
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
                        to: 'ashtarali720@gmail.com',
                        recipientProviders: [culprits(), developers()]
                    )
                } catch (Exception e) {
                    echo "Email failed: ${e.message}"
                }
            }
        }
    }
}
