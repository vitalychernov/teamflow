# TeamFlow — Project & Task Management SaaS

> A production-ready full-stack SaaS application for project and task management, inspired by Jira/Asana.

[![Backend CI](https://github.com/vitalychernov/teamflow/actions/workflows/backend-ci.yml/badge.svg)](https://github.com/vitalychernov/teamflow/actions)
[![Frontend Deploy](https://img.shields.io/badge/frontend-vercel-black)](https://teamflow.vercel.app)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## Tech Stack

### Backend
- Java 17 + Spring Boot 3.x
- Spring Security + JWT
- Spring Data JPA + PostgreSQL
- Maven
- Swagger / OpenAPI 3

### Frontend
- React 18 + TypeScript
- React Query (TanStack Query)
- Axios + JWT interceptor
- Context API (auth state)
- Vite

### DevOps
- Docker + Docker Compose
- Render Free (backend + PostgreSQL)
- Vercel Free (frontend)
- GitHub Actions (CI/CD)

---

## Features

- [x] User registration & login (JWT)
- [x] Role-based access control (USER / ADMIN)
- [x] Project CRUD with ownership
- [x] Task CRUD with assignment
- [x] Pagination & filtering
- [x] Global exception handling
- [x] API documentation (Swagger UI)
- [x] Protected frontend routes
- [x] Dockerized deployment

---

## Project Structure

```
teamflow/
├── backend/        # Spring Boot application
├── frontend/       # React + TypeScript SPA
├── docker-compose.yml
└── README.md
```

---

## Getting Started

### Prerequisites
- Java 17+
- Node.js 18+
- Docker & Docker Compose
- PostgreSQL (or use Docker)

### Run locally with Docker Compose

```bash
cp backend/src/main/resources/application-example.yml \
   backend/src/main/resources/application-local.yml
# Edit application-local.yml with your DB credentials

docker-compose up --build
```

- Backend: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Frontend: http://localhost:5173

---

## API Overview

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/register` | Public | Register new user |
| POST | `/api/auth/login` | Public | Login, receive JWT |
| GET | `/api/projects` | USER+ | List projects (paginated) |
| POST | `/api/projects` | USER+ | Create project |
| GET | `/api/projects/{id}/tasks` | USER+ | List tasks with filters |
| POST | `/api/projects/{id}/tasks` | USER+ | Create task |
| GET | `/api/admin/users` | ADMIN | List all users |

Full API docs available at `/swagger-ui.html` when running locally.

---

## Deployment

- **Backend**: Deployed on [Render Free](https://render.com)
- **Frontend**: Deployed on [Vercel](https://vercel.com)
- **Database**: PostgreSQL on Render Free

---

## License

MIT
