pipeline {
    // Defines where the pipeline will execute. 'any' uses the available Jenkins executor on your Azure VM.
    agent any

    // Define global variables to be used across different stages
    environment {
        APP_NAME = "YAS-System"
        JAVA_HOME = "/usr/lib/jvm/java-21-openjdk-amd64" // Adjust path if necessary
    }

    stages {
        stage('Initialize') {
            steps {
                echo "--- Initializing Build for ${env.APP_NAME} ---"
                echo "Branch: ${env.BRANCH_NAME}"
                echo "Workspace: ${env.WORKSPACE}"
            }
        }

        stage('Checkout Source') {
            steps {
                // Automatically pulls the latest code from the branch that triggered the build
                checkout scm
            }
        }

        stage('System Check') {
            steps {
                echo "--- Verifying Environment Capabilities ---"
                sh 'java -version'
                // Check for Maven Wrapper; if not found, verify if Maven is installed
                sh '[ -f mvnw ] && echo "Maven Wrapper found" || mvn -version'
            }
        }

        stage('Build & Unit Test') {
            steps {
                echo "--- Starting Application Build ---"
                /* In a Monorepo, you can target specific services:
                   Example for Maven: sh './mvnw clean package -DskipTests'
                */
                sh 'echo "Running build commands here..."'
            }
        }

        stage('Quality Gate') {
            steps {
                echo "--- Static Code Analysis (SonarQube) ---"
                sh 'echo "SonarScanner execution would happen here..."'
            }
        }

        stage('Dockerize') {
            steps {
                echo "--- Building Docker Images ---"
                /*
                   Example: sh 'docker build -t yas-product-service:latest ./product-service'
                */
                sh 'echo "Docker build commands would happen here..."'
            }
        }
    }

    // Post-execution block handles cleanup or notifications
    post {
        always {
            echo "--- Cleaning up Workspace ---"
            // cleanWs() // Uncomment this to wipe the workspace after each build
        }
        success {
            echo "Build successful! Notifying the team..."
        }
        failure {
            echo "Build failed. Please check the console output for errors."
        }
    }
}
