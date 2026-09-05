pipeline {
    agent any

    options {
        disableConcurrentBuilds()
        timestamps()
        skipDefaultCheckout()
    }

    environment {
        // 服务器访问 GitHub 不稳定，调低低速判死阈值并让 checkout 失败自动重试
        GIT_HTTP_LOW_SPEED_LIMIT = '1000'
        GIT_HTTP_LOW_SPEED_TIME = '60'
        REGISTRY = 'crpi-bgfb2r2bqr6jxrw1.cn-hangzhou.personal.cr.aliyuncs.com'
        IMAGE_NAMESPACE = 'dev-cjy'
        DEPLOY_HOST = '118.178.255.26'
        DEPLOY_USER = 'deploy'
        DEPLOY_PATH = '/opt/ops-mid-platform'
    }

    stages {
        stage('Checkout') {
            steps {
                retry(3) {
                    checkout([
                        $class: 'GitSCM',
                        branches: [[name: '*/main']],
                        extensions: [[$class: 'CloneOption', shallow: true, depth: 1],
                                     [$class: 'CleanBeforeCheckout']],
                        userRemoteConfigs: [[url: 'https://github.com/jingyuchen465-glitch/ops-mid-platform.git']]
                    ])
                }
            }
        }
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
                    // 手动 checkout 不注入 GIT_COMMIT，直接用 git rev-parse 取提交短 SHA
                    def shortSha = sh(script: 'git rev-parse --short=7 HEAD', returnStdout: true).trim()
                    def tag = "${env.BUILD_NUMBER}-${shortSha}"
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
                withCredentials([
                    usernamePassword(credentialsId: 'container-registry', usernameVariable: 'REGISTRY_USER', passwordVariable: 'REGISTRY_PASSWORD'),
                    sshUserPrivateKey(credentialsId: 'production-deploy-ssh', keyFileVariable: 'SSH_KEY', usernameVariable: 'SSH_USER')
                ]) {
                    sh 'ssh -i "$SSH_KEY" -o StrictHostKeyChecking=accept-new "$SSH_USER@$DEPLOY_HOST" "mkdir -p $DEPLOY_PATH"'
                    sh 'scp -i "$SSH_KEY" deploy/docker-compose.yml deploy/nginx.conf docs/mysql-init.sql "$SSH_USER@$DEPLOY_HOST:$DEPLOY_PATH/"'
                    sh 'printf "%s" "$REGISTRY_PASSWORD" | ssh -i "$SSH_KEY" "$SSH_USER@$DEPLOY_HOST" "docker login $REGISTRY -u $REGISTRY_USER --password-stdin"'
                    sh 'ssh -i "$SSH_KEY" "$SSH_USER@$DEPLOY_HOST" "cd $DEPLOY_PATH && BACKEND_IMAGE=\'$BACKEND_IMAGE\' FRONTEND_IMAGE=\'$FRONTEND_IMAGE\' docker compose --env-file .env pull && BACKEND_IMAGE=\'$BACKEND_IMAGE\' FRONTEND_IMAGE=\'$FRONTEND_IMAGE\' docker compose --env-file .env up -d --remove-orphans && docker logout $REGISTRY"'
                    sh 'ssh -i "$SSH_KEY" "$SSH_USER@$DEPLOY_HOST" "cd $DEPLOY_PATH && docker image prune -f"'
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
