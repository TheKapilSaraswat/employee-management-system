# Employee Management System

A production-ready HR platform: manage employees, departments, attendance, leave and payroll with role-based access (ADMIN / EMPLOYEE) and a clean MUI dashboard.

## Features

- **Employees** — searchable, paginated employee registry with departments and positions
- **Departments** — CRUD management (ADMIN only)
- **Attendance** — daily check-in / check-out records
- **Leave** — apply, approve and reject leave with annual/sick/casual balance tracking
- **Payroll** — monthly payroll generation and processing (ADMIN only)
- **Reports** — employee, attendance, leave and payroll summaries (ADMIN only)
- **JWT authentication & roles** — stateless auth with ADMIN / EMPLOYEE roles; first registered user becomes ADMIN
- **Security hardened** — secure headers (CSP), rate limiting, CORS allow-lists, BCrypt password hashing
- **Swagger/OpenAPI docs** at `/swagger`
- **Health & readiness endpoints** for orchestrators and uptime monitors
- **PostgreSQL** (production) with H2 for local development and tests
- **Automated CI/CD** — lint, test, build, containerize and deploy on every push to `main`

## Architecture

```
Browser (React/Vite on Vercel)
        │  HTTPS
        ▼
Backend API (Spring Boot on Render/Railway)
        │  JPA / HikariCP
        ▼
PostgreSQL (Neon / Render Postgres)
```

- **Frontend** — React + Vite + MUI, deployed separately to Vercel
- **Backend** — Spring Boot 3 (Java 17), stateless JWT security
- **Database** — PostgreSQL in production; embedded H2 for local development
- **Deployment** — Docker multi-stage image; GitHub Actions pipeline; Render (primary) / Railway (secondary)

## Folder Structure

```
employee-management-system/
├── backend/
│   ├── src/main/java/com/employeemgmt/
│   │   ├── config/        # Security, CORS, JWT, rate limiting, OpenAPI, DB, seeder
│   │   ├── controller/    # REST endpoints + health/readiness
│   │   ├── dto/           # Request/response contracts
│   │   ├── model/         # JPA entities
│   │   ├── repository/    # Spring Data repositories
│   │   └── service/       # Business logic
│   │   └── resources/
│   │       ├── application.properties
│   │       └── application-prod.properties
│   ├── sql/               # V1__init.sql migration + seed.sql
│   ├── scripts/           # migrate.sh / seed.sh
│   ├── checkstyle.xml     # Lint rules
│   ├── Dockerfile
│   ├── .env.example
│   └── pom.xml
├── frontend/
│   ├── src/               # React components, pages, services
│   ├── vercel.json
│   └── .env.example
├── .github/workflows/ci.yml
├── docker-compose.yml
├── render.yaml
├── .env.example
└── README.md / DEPLOYMENT.md
```

## Tech Stack

| Layer      | Technology                                        |
|------------|---------------------------------------------------|
| Backend    | Java 17, Spring Boot 3.2, Spring Security, Spring Data JPA |
| Database   | PostgreSQL (prod), H2 (dev/test), HikariCP pool   |
| Auth       | JWT (jjwt 0.12), BCrypt                           |
| API docs   | springdoc-openapi (Swagger UI)                    |
| Frontend   | React 18, Vite 5, MUI, Axios                      |
| Ops        | Docker, GitHub Actions, Render, Railway, Vercel   |

## Installation

Prerequisites: JDK 17, Maven 3.9+, Node.js 20+.

```bash
# Backend
cd backend
mvn -B clean package

# Frontend
cd frontend
npm ci
```

## Environment Variables

Copy the templates and fill in values — never commit `.env`:

```bash
cp backend/.env.example backend/.env
cp frontend/.env.example frontend/.env
cp .env.example .env          # for docker compose
```

| Variable                  | Required | Description                                        |
|---------------------------|----------|----------------------------------------------------|
| `DATABASE_URL`            | prod     | `postgres://user:pass@host:5432/db?sslmode=require` |
| `JWT_SECRET`              | prod     | ≥32 chars random string (e.g. `openssl rand -base64 48`) |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | no | Bootstraps an admin account on first start      |
| `CORS_ALLOWED_ORIGINS`    | prod     | Comma-separated browser origins                     |
| `PORT`                    | no       | Port to listen on (injected by Render/Railway)      |
| `VITE_API_URL` (frontend) | prod     | Public backend base URL, e.g. `https://<app>.onrender.com/api` |

See `backend/.env.example`, `frontend/.env.example` and `.env.example` for the full list.

## Running Locally

```bash
# Terminal 1 - backend (uses embedded H2, no database setup)
cd backend
mvn -B spring-boot:run

# Terminal 2 - frontend (Vite dev server proxies /api -> :5002)
cd frontend
npm run dev
```

Open http://localhost:5175. Backend API on http://localhost:5002.

### Local full stack with PostgreSQL (Docker)

```bash
cp .env.example .env
docker compose up --build
```

The Postgres container applies `backend/sql/*.sql` automatically on first boot.

## Deployment

Full guide in [DEPLOYMENT.md](./DEPLOYMENT.md). TL;DR:

1. **Backend → Render**: connect the GitHub repo; `render.yaml` provisions the service + managed Postgres automatically.
2. **Frontend → Vercel**: import the repo, set `VITE_API_URL`, framework is auto-detected (`vercel.json`).
3. **One command per push**: the GitHub Actions workflow lints, tests, builds the Docker image and triggers deploys.

### Live URLs (portfolio)

| What            | URL                                       |
|-----------------|-------------------------------------------|
| Frontend        | `https://employee-management-system.vercel.app` |
| Backend API     | `https://employee-management-system-api.onrender.com` |
| Swagger         | `https://employee-management-system-api.onrender.com/swagger` |
| GitHub          | `https://github.com/<org>/employee-management-system` |

Run `../project_host/scripts/portfolio-urls.ps1` (Windows) or `.sh` (Unix) after deploying to print this table.

## API Documentation

Interactive Swagger UI is exposed at:

```
https://<backend-url>/swagger
```

Raw spec: `https://<backend-url>/v3/api-docs`. Authenticated endpoints take a JWT bearer token (`POST /api/auth/login`).

## Monitoring

- `GET /api/health` — liveness probe
- `GET /api/ready` — readiness probe (validates the database connection)
- Structured JSON logs

## License

Private project.
