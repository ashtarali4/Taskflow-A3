pipeline {
    agent any

    environment {
        // Point tests at the permanently deployed Taskflow app on EC2
        TEST_URL = 'http://52.87.169.55'
    }

    stages {
        stage('Checkout Code') {
            steps {
                git branch: 'main', url: 'https://github.com/ashtarali4/Taskflow-A3.git'
            }
        }
        
        stage('Run Automated Tests') {
            agent {
                docker {
                    image 'markhobson/maven-chrome'
                    // Using network host so the test container can reach the EC2 host
                    args '--network host --privileged' 
                }
            }
            steps {
                dir('tests') {
                    // Execute the Maven test suite with local repo to avoid permission issues
                    sh 'mvn clean test -Dmaven.repo.local=.m2/repository -Dwdm.cachePath=.wdm'
                }
            }
        }
    }
    
    post {
        always {
            // Email the results to the committer and always CC the repo owner
            script {
                def committerEmail = sh(script: "git show -s --format='%ae' HEAD", returnStdout: true).trim()
                
                emailext (
                    subject: "Jenkins Pipeline Result: ${currentBuild.currentResult} - Taskflow",
                    body: "The Jenkins pipeline execution has finished.\n\nResult: ${currentBuild.currentResult}\n\nBuild URL: ${env.BUILD_URL}",
                    to: "${committerEmail}, ashtarali720@gmail.com"
                )
            }
        }
    }
}
