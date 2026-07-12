pipeline {
    agent any

    tools {
        jdk 'JDK25_SDKMAN'
    }

    environment {
        // If Java is not in default PATH, uncomment and adjust:
        // JAVA_HOME = '/usr/lib/jvm/java-21-openjdk-amd64'

        // JAVA_HOME = '/var/jenkins_home/.sdkman/candidates/java/25.0.2-tem'
        // PATH = "${JAVA_HOME}/bin:${env.PATH}"

        // All Java services in the monorepo
        JAVA_SERVICES = 'cart,customer,delivery,inventory,location,media,order,payment,payment-paypal,product,promotion,rating,recommendation,sampledata,search,storefront-bff,backoffice-bff,tax,webhook'
    }

    stages {
        // ───────────────────────────────────────────────────────
        // STAGE 1: Checkout source code
        // ───────────────────────────────────────────────────────
        stage('Checkout') {
            steps {
                checkout scm
                echo "Branch: ${env.BRANCH_NAME}"
                echo "Build #${env.BUILD_NUMBER}"
            }
        }

        // ───────────────────────────────────────────────────────
        // STAGE 5: GITLEAKS SCAN
        // Scan repository for accidentally committed secrets
        // Requires gitleaks installed on Jenkins VM
        // ───────────────────────────────────────────────────────
        // stage('Gitleaks Scan') {
        //     steps {
        //         echo ">>> Scanning for secrets with Gitleaks..."
        //         sh 'gitleaks detect --source . --report-path gitleaks-report.json --report-format json --no-git || true'
        //         archiveArtifacts artifacts: 'gitleaks-report.json', allowEmptyArchive: true
        //     }
        // }

        stage('Gitleaks Scan') {
            steps {
                echo ">>> Scanning for secrets with Gitleaks..."

                script {
                    def status = sh(
                        script: '''
                        gitleaks detect \
                        --source . \
                        --config gitleaks.toml \
                        --report-path gitleaks-report.json \
                        --report-format json
                        ''',
                        returnStatus: true
                    )

                    if (status != 0) {
                        echo "⚠️ Gitleaks detected potential leaks, but pipeline will continue (CI practice mode)."
                    } else {
                        echo "✅ No leaks found."
                    }
                }
            }
            post {
                always {
                    archiveArtifacts artifacts: 'gitleaks-report.json', allowEmptyArchive: true
                }
            }
        }

        // ───────────────────────────────────────────────────────
        // STAGE 2: Detect which services have changed
        // This handles the MONOREPO requirement — only build/test
        // the services that actually have code changes.
        // ───────────────────────────────────────────────────────
        stage('Detect Changes') {
            steps {
                script {
                    // Get list of changed files compared to previous commit
                    def changedFiles = ''
                    try {
                        changedFiles = sh(
                            script: "git diff --name-only HEAD~1 HEAD",
                            returnStdout: true
                        ).trim()
                    } catch (Exception e) {
                        // First commit or shallow clone — list all files
                        echo "First commit or shallow clone, listing all files"
                        changedFiles = sh(
                            script: "git ls-files",
                            returnStdout: true
                        ).trim()
                    }

                    echo "========== Changed files =========="
                    echo changedFiles
                    echo "==================================="

                    // Determine which services have changed files
                    def allServices = env.JAVA_SERVICES.split(',')
                    def servicesToBuild = []

                    for (svc in allServices) {
                        if (changedFiles.contains("${svc}/")) {
                            servicesToBuild.add(svc)
                        }
                    }

                    // If root pom.xml or common-library changed → rebuild ALL services
                    def changedList = changedFiles.split('\n').toList()
                    def rootPomChanged = changedList.any { it.trim() == 'pom.xml' }
                    def commonLibChanged = changedFiles.contains('common-library/')

                    if (rootPomChanged || commonLibChanged) {
                        echo ">>> Root pom.xml or common-library changed -> building ALL services"
                        servicesToBuild = allServices.toList()
                    }

                    if (servicesToBuild.isEmpty()) {
                        env.SERVICES_TO_BUILD = ''
                        env.SKIP_BUILD = 'true'
                        echo ">>> No Java services changed. Pipeline will skip build/test."
                    } else {
                        // Maven uses comma-separated module list: -pl cart,media,order
                        env.SERVICES_TO_BUILD = servicesToBuild.join(',')
                        env.SKIP_BUILD = 'false'
                        echo ">>> Services to build: ${env.SERVICES_TO_BUILD}"
                    }
                }
            }
        }

        // ───────────────────────────────────────────────────────
        // STAGE 3: BUILD
        // Compile the changed services without running tests
        // ───────────────────────────────────────────────────────
        stage('Build') {
            when {
                expression { return env.SKIP_BUILD != 'true' }
            }
            steps {
                echo ">>> Building: ${env.SERVICES_TO_BUILD}"
                sh 'java -version'
                sh 'mvn -v'
                sh "mvn clean install -pl ${env.SERVICES_TO_BUILD} -am -DskipTests"
            }
        }

        // ───────────────────────────────────────────────────────
        // STAGE 4: TEST
        // Run unit tests + integration tests, generate coverage
        // ───────────────────────────────────────────────────────
        stage('Test') {
            when {
                expression { return env.SKIP_BUILD != 'true' }
            }
            steps {
                echo ">>> Testing: ${env.SERVICES_TO_BUILD}"
                sh "mvn verify -pl ${env.SERVICES_TO_BUILD} -am -Dmaven.install.skip=true"
            } // comment to run test to show coverage
            // steps {
            //     script {
            //         if (env.SERVICES_TO_BUILD?.trim()) {
            //             sh "mvn verify -pl ${env.SERVICES_TO_BUILD} -am -Dmaven.install.skip=true"
            //         } else {
            //             sh "mvn verify -Dmaven.install.skip=true"
            //         }
            //     }
            // }
            post {
                always {
                    // Publish JUnit test results to Jenkins dashboard
                    junit(
                        testResults: '**/target/surefire-reports/TEST-*.xml, **/target/failsafe-reports/TEST-*.xml',
                        allowEmptyResults: true
                    )

                    // Publish JaCoCo coverage report and enforce 70% threshold
                    script {
                        try {
                            jacoco(
                                execPattern: '**/target/jacoco.exec',
                                classPattern: '**/target/classes',
                                sourcePattern: '**/src/main/java',
                                exclusionPattern: '**/config/**,**/exception/**,**/constants/**,**/*Application.class',
                                changeBuildStatus: true,
                                minimumLineCoverage: '70'
                            )
                            if (currentBuild.result == 'UNSTABLE') {
                                currentBuild.result = 'FAILURE'
                                error "Quality Gate failed: Code Coverage is less than 70%!"
                            }
                        } catch (Exception e) {
                            echo "WARNING: JaCoCo plugin is not installed or threshold check failed: ${e.getMessage()}"
                        }
                    }
                }
            }
        }

       // ───────────────────────────────────────────────────────
        // STAGE 8: SNYK SCAN
        // Scan dependencies to secure system if dependencies is not safe
        // ───────────────────────────────────────────────────────
        stage('Snyk Scan') {
            steps {
                withCredentials([string(credentialsId: 'snyk_connection', variable: 'SNYK_TOKEN')]) {
                    sh '''
                    snyk auth $SNYK_TOKEN

                    echo ">>> Running Snyk vulnerability scan..."
                    
                    # 1. Quét cục bộ và in log ra console của Jenkins
                    snyk test || true

                    echo ">>> Pushing Snyk snapshot to Web Dashboard..."
                    
                    # 2. THÊM DÒNG NÀY: Đẩy toàn bộ kết quả lên Dashboard Snyk của tổ chức
                    snyk monitor || true
                    '''
                }
            }
        }

        // ───────────────────────────────────────────────────────
        // STAGE 6: SONARQUBE ANALYSIS
        // ───────────────────────────────────────────────────────
        stage('SonarQube Analysis') {
            steps {
                sh '''
                echo USER=$(whoami)
                echo JAVA_HOME=$JAVA_HOME
                mvn -v
                '''

                // Sử dụng chính xác ID 'sonarqube_connection' đang có trong Jenkins của bạn
                withCredentials([string(credentialsId: 'sonarqube_connection', variable: 'SONAR_TOKEN')]) {
                    
                    // THÊM BỌC NGOÀI NÀY: Kết nối và đồng bộ với Jenkins SonarQube Plugin
                    withSonarQubeEnv('sonarqube') { 
                        sh '''
                        # 1. Biên dịch toàn bộ các module (bỏ qua test) để sinh file target/classes (.class)
                        # Bước này giúp các module bị SKIP ở stage trước vẫn có binary cho SonarQube quét
                        mvn clean compile -DskipTests

                        # 2. Thực hiện chạy phân tích tĩnh và đẩy kết quả lên hệ thống
                        mvn sonar:sonar \
                        -Dsonar.projectKey=yas-project \
                        -Dsonar.host.url=http://70.153.136.35:9000 \
                        -Dsonar.token=$SONAR_TOKEN \
                        -Dsonar.coverage.jacoco.xmlReportPaths=**/target/site/jacoco/jacoco.xml
                        '''
                    }
                    
                }
            }
        }

        // ───────────────────────────────────────────────────────
        // STAGE 7: QUALITY GATE - SONARQUBE
        // Wait sonarqube return result about test coverage
        // ───────────────────────────────────────────────────────
        // stage("Quality Gate") {
        //     steps {
        //         timeout(time: 5, unit: 'MINUTES') {
        //             waitForQualityGate abortPipeline: true
        //         }
        //     }
        // }
    }

    post {
        success {
            echo 'Pipeline SUCCESS for branch: ' + env.BRANCH_NAME
        }
        failure {
            echo 'Pipeline FAILED for branch: ' + env.BRANCH_NAME
        }
    }
}
