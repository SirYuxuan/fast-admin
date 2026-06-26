# fast-admin Docker deployment

This deployment is intended for a private Mac mini with Docker.

## Server env file

Create an env file on the Mac mini, for example:

```bash
mkdir -p "$HOME/fast-admin"
cp deploy/.env.example "$HOME/fast-admin/.env"
```

Edit the file and set the real database and Redis passwords. Do not commit that file.

## Manual deploy

```bash
export FAST_ADMIN_ENV_FILE="$HOME/fast-admin/.env"
docker compose -p fast-admin -f deploy/docker-compose.yml up -d --build
```

The web entrypoint listens on `WEB_HTTP_PORT` and proxies `/api/*` to the backend container.

## Cloudflare Tunnel

The Mac mini exposes the web entrypoint through the existing Cloudflare Tunnel:

```text
fa-admin.oofo.cc -> http://localhost:${WEB_HTTP_PORT}
```

Keep the real tunnel config on the Mac mini at `/usr/local/etc/cloudflared/config.yml`.
Do not commit tunnel credentials or tokens.

## GitHub self-hosted runner

Install a GitHub Actions self-hosted runner on the Mac mini, give it the label `macmini`, and set this environment variable for the runner service:

```bash
FAST_ADMIN_ENV_FILE=$HOME/fast-admin/.env
```

After that, pushes to `main` deploy with `.github/workflows/deploy.yml`.
