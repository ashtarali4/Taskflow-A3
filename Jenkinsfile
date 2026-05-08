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
                    // Use Docker to delete root-owned files that Jenkins can't touch
                    sh 'docker run --rm -u 0:0 -v /var/lib/jenkins/workspace/Taskflow-A3-FINAL:/ws -w /ws markhobson/maven-chrome /bin/sh -c "rm -rf ./*"'
                }
                checkout scm
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
