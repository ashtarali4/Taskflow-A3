pipeline {
    agent {
        node {
            label ""
            customWorkspace "/var/lib/jenkins/workspace/Taskflow-A3-Fixed"
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
        stage('Checkout Code') {
            steps {
                deleteDir()
                checkout scm
            }
        }

        stage('Run Automated Tests') {
            agent {
                docker {
                    image 'markhobson/maven-chrome'
                    // Force this stage to use the same fixed workspace
                    customWorkspace "/var/lib/jenkins/workspace/Taskflow-A3-Fixed"
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
                    // Fix permissions so this workspace remains accessible
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
