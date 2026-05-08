pipeline {
    agent any

    environment {
        // We will configure the test to look at the host port exposed by docker-compose
        TEST_URL = 'http://localhost:8081'
    }

    stages {
        stage('Checkout Code') {
            steps {
                // You will need to change this URL to your new repository URL once you push it!
                git branch: 'main', url: 'https://github.com/ashtarali4/Taskflow-A3.git'
            }
        }
        
        stage('Deploy Pipeline Test Environment') {
            steps {
                // Bring down any existing instances first
                sh 'sudo docker compose -f docker-compose.ci.yml down || true'
                // Bring up the containerized application
                sh 'sudo docker compose -f docker-compose.ci.yml up -d'
                
                // Wait for the application to be fully up (Vite and FastAPI can take a few seconds)
                sleep time: 10, unit: 'SECONDS'
            }
        }
        
        stage('Run Automated Tests') {
            agent {
                docker {
                    image 'markhobson/maven-chrome'
                    // Using network host so the test container can reach the frontend exposed on localhost:8081
                    args '--network host' 
                }
            }
            steps {
                dir('tests') {
                    // Execute the Maven test suite
                    sh 'mvn clean test'
                }
            }
        }
    }
    
    post {
        always {
            // Clean up the environment after tests
            sh 'sudo docker compose -f docker-compose.ci.yml down || true'
            
            // Email the results
            script {
                // In a real environment, you'd use the committer's email. 
                // For the assignment, we explicitly CC the instructor as requested.
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
