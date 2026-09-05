pipeline {
    agent any

    options {
        disableConcurrentBuilds()
        timestamps()
    }

    environment {
        REGISTRY = 'registry.cn-hangzhou.aliyuncs.com'
        IMAGE_NAMESPACE = 'dev-cjy'
        DEPLOY_HOST = '118.178.255.26'
        DEPLOY_USER = 'deploy'
        DEPLOY_PATH = '/opt/ops-mid-platform'
    }

    stages {
        stage('Test') {
            steps {
                sh 'mvn -B test'
                dir('admin-web') {
                    sh 'npm ci'
                    sh 'npm run build'
                }
            }
        }

        stage('Build and Push Images') {
            steps {
                script {
                    def tag = "${env.BUILD_NUMBER}-${env.GIT_COMMIT.take(7)}"
                    env.BACKEND_IMAGE = "${env.REGISTRY}/${env.IMAGE_NAMESPACE}/ops-mid-platform-backend:${tag}"
                    env.FRONTEND_IMAGE = "${env.REGISTRY}/${env.IMAGE_NAMESPACE}/ops-mid-platform-frontend:${tag}"
                }
                withCredentials([usernamePassword(credentialsId: 'container-registry', usernameVariable: 'REGISTRY_USER', passwordVariable: 'REGISTRY_PASSWORD')]) {
                    sh 'echo "$REGISTRY_PASSWORD" | docker login "$REGISTRY" -u "$REGISTRY_USER" --password-stdin'
                    sh 'docker build -t "$BACKEND_IMAGE" .'
                    sh 'docker build -t "$FRONTEND_IMAGE" -f admin-web/Dockerfile .'
                    sh 'docker push "$BACKEND_IMAGE"'
                    sh 'docker push "$FRONTEND_IMAGE"'
                    sh 'docker logout "$REGISTRY"'
                }
            }
        }

        stage('Deploy') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'container-registry', usernameVariable: 'REGISTRY_USER', passwordVariable: 'REGISTRY_PASSWORD')]) {
                    sshagent(credentials: ['production-deploy-ssh']) {
                        sh 'ssh -o StrictHostKeyChecking=accept-new "$DEPLOY_USER@$DEPLOY_HOST" "mkdir -p $DEPLOY_PATH"'
                        sh 'scp deploy/docker-compose.yml deploy/nginx.conf docs/mysql-init.sql "$DEPLOY_USER@$DEPLOY_HOST:$DEPLOY_PATH/"'
                        sh 'printf "%s" "$REGISTRY_PASSWORD" | ssh "$DEPLOY_USER@$DEPLOY_HOST" "docker login \'$REGISTRY\' -u \'$REGISTRY_USER\' --password-stdin"'
                        sh 'ssh "$DEPLOY_USER@$DEPLOY_HOST" "cd $DEPLOY_PATH && BACKEND_IMAGE=\'$BACKEND_IMAGE\' FRONTEND_IMAGE=\'$FRONTEND_IMAGE\' docker compose --env-file .env pull && BACKEND_IMAGE=\'$BACKEND_IMAGE\' FRONTEND_IMAGE=\'$FRONTEND_IMAGE\' docker compose --env-file .env up -d --remove-orphans && docker logout \'$REGISTRY\'"'
                        sh 'ssh "$DEPLOY_USER@$DEPLOY_HOST" "cd $DEPLOY_PATH && docker image prune -f"'
                    }
                }
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
        }
    }
}
