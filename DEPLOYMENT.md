# Deployment Guide — Employee Management System

Automated production deployment with **Render (primary)**, **Railway (secondary)** and **Vercel (frontend)**.

## 0. Prerequisites

- GitHub repo (e.g. `your-org/employee-management-system`)
- Accounts on Render, Railway, Vercel
- A Neon project **or** Render's managed Postgres (created automatically)

## 1. Repository setup

```bash
git init
git add .
git commit -m "chore: production deployment pipeline"
git branch -M main
git remote add origin git@github.com:your-org/employee-management-system.git
git push -u origin main
```

## 2. Backend — Render (primary)

**Blueprint (recommended):**

1. Render → **New → Blueprint** → select the repo.
2. `render.yaml` creates `employee-management-system-api` + `employee-management-system-db` and wires `DATABASE_URL`.
3. Set required secrets in the **Env Groups** (they have `sync: false`):
   - `JWT_SECRET` (`openssl rand -base64 48`)
   - `ADMIN_EMAIL`, `ADMIN_PASSWORD`
   - `CORS_ALLOWED_ORIGINS` = `https://employee-management-system.vercel.app`
   - `APP_BASE_URL` = `https://employee-management-system-api.onrender.com`
4. (Optional) Copy the service **Deploy Hook** URL into GitHub secret `RENDER_DEPLOY_HOOK`.

**Neon alternative:** set `DATABASE_URL` to your Neon pooled connection string — the app converts it to a JDBC URL automatically.

## 3. Frontend — Vercel

1. **Add New Project** → import repo → framework preset **Vite**.
2. Set `VITE_API_URL` = `https://employee-management-system-api.onrender.com/api`.
3. Deploy. Pushes to `main` auto-deploy.
4. (Optional) Vercel **Deploy Hook** → GitHub secret `VERCEL_DEPLOY_HOOK`.

## 4. Backend — Railway (secondary)

1. **New Project** → Deploy from GitHub.
2. Add PostgreSQL plugin + same env vars as Render.
3. GitHub secrets `RAILWAY_TOKEN` + `RAILWAY_SERVICE` enable `railway redeploy` in CI.

## 5. Database migrations & seeds

- `backend/sql/V1__init.sql` — schema (users, departments, employees, attendances, leaves, payrolls)
- `backend/sql/seed.sql` — reference data
- Admin account — created on first start via `ADMIN_EMAIL`/`ADMIN_PASSWORD`

```bash
DATABASE_URL="postgres://user:pass@host:5432/employeemgmt" backend/scripts/migrate.sh
DATABASE_URL="postgres://user:pass@host:5432/employeemgmt" backend/scripts/seed.sh
```

## 6. GitHub Actions pipeline

`.github/workflows/ci.yml` on every push to `main`: gitleaks scan → checkstyle lint → `mvn verify` → frontend lint+build → Docker image to GHCR → Render/Railway/Vercel deploys.

### Required GitHub secrets (optional — steps skip when missing)

| Secret                 | Used for                    |
|------------------------|-----------------------------|
| `RENDER_DEPLOY_HOOK`   | Trigger Render blue deploy  |
| `RAILWAY_TOKEN`        | Railway CLI redeploy        |
| `RAILWAY_SERVICE`      | Railway service name        |
| `VERCEL_DEPLOY_HOOK`   | Trigger Vercel deploy       |

## 7. Local one-command run

```bash
docker compose up --build
```

## 8. Verify production

- `curl https://employee-management-system-api.onrender.com/api/health`
- `curl https://employee-management-system-api.onrender.com/api/ready`
- Docs: `https://employee-management-system-api.onrender.com/swagger`
- Frontend: `https://employee-management-system.vercel.app`

## 9. Portfolio URLs

```bash
# Windows
..\project_host\scripts\portfolio-urls.ps1
# Unix
../project_host/scripts/portfolio-urls.sh
```
