pipeline {
    agent {
        node {
            label ""
            customWorkspace "/var/lib/jenkins/workspace/Taskflow-A3-FINAL"
        }
    }

    environment {
        TEST_URL = 'http://52.87.169.55'
    }

    options {
        disableConcurrentBuilds()
        skipDefaultCheckout()
    }

    stages {
        stage('Automated Selenium Tests') {
            steps {
                // 1. Clear everything
                deleteDir()
                // 2. Download code
                checkout scm
                
                // 3. Run tests inside the Docker container
                script {
                    docker.image('markhobson/maven-chrome').inside('-u 0:0 --network host --privileged --shm-size=2g') {
                        dir('tests') {
                            sh 'mvn clean test -Dmaven.repo.local=.m2/repository -Dwdm.cachePath=.wdm'
                        }
                    }
                }
            }
            post {
                always {
                    // Fix permissions so we can clean up next time
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
