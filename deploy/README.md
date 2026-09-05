# Jenkins + Docker deployment

This project is deployed as four containers: the Vue/Nginx frontend, the Spring Boot backend, MySQL, and Redis. Only Nginx publishes a host port; `/api/*` and `/mcp` are reverse-proxied to the backend.

## One-time server setup

Use a non-root deploy account for Jenkins. On a fresh Ubuntu/Debian host:

```bash
sudo apt-get update
sudo apt-get install -y docker.io docker-compose-plugin
sudo useradd --create-home --shell /bin/bash deploy
sudo usermod -aG docker deploy
sudo mkdir -p /opt/ops-mid-platform
sudo chown -R deploy:deploy /opt/ops-mid-platform
```

Copy `deploy/.env.example` to `/opt/ops-mid-platform/.env`, replace all placeholders, and keep it mode `600`. Jenkins copies `mysql-init.sql` alongside the Compose file; the database init script runs only when the MySQL volume is first created. Apply later `docs/mysql-migrate-*.sql` files explicitly when needed.

The Jenkins credentials should contain:

- `container-registry`: registry username/password;
- `production-deploy-ssh`: an SSH private key whose public key is in `/home/deploy/.ssh/authorized_keys`.

Set `REGISTRY`, `IMAGE_NAMESPACE`, and the deploy account values in `Jenkinsfile`, then create a Multibranch or Pipeline job pointed at this repository. The Jenkins agent needs Docker, Maven, and an SSH Agent plugin.

The pipeline logs the deployment host into the registry before `docker compose pull`; this is required for private registries. For Docker Hub, set `REGISTRY=docker.io` and use the namespace/repository names that actually exist. Never commit `.env` or registry passwords.

## First manual smoke test

```bash
cd /opt/ops-mid-platform
docker compose --env-file .env pull
docker compose --env-file .env up -d
docker compose ps
curl -f http://127.0.0.1/ || exit 1
```

Open `http://<server-ip>/` for the admin console. The backend listens internally on `8090`; the MCP endpoint is `http://<server-ip>/mcp`.

## Firewall and HTTPS

Allow only SSH and HTTP/HTTPS (`22`, `80`, `443`) in the cloud security group. Do not expose `3306`, `6379`, or `8090`. Put the server behind a domain and add TLS with Certbot/Nginx before exposing it to the Internet.
