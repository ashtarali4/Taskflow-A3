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
            options {
                timeout(time: 10, unit: 'MINUTES') 
            }
            steps {
                deleteDir()
                checkout scm
                
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
