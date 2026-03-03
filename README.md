# TeamFlow

A full-stack project management application inspired by Jira and Asana. Features a kanban board with drag-and-drop, role-based access control, team member assignment, and real-time progress tracking per project.

**Live demo:** https://teamflow-psi.vercel.app

---

## Features

- JWT-based authentication — register, login, logout
- Role-based access control (USER / ADMIN)
- Project creation, editing, and deletion with % Done progress indicator
- Kanban board with drag-and-drop (To Do / In Progress / Done)
- Task management — title, description, priority, assignee
- "Assigned to me" filter to focus on your own tasks
- Optimistic UI updates — cards move instantly without waiting for the server
- Admin panel with paginated user management
- Responsive design

## Tech Stack

**Backend**

- Java 17 + Spring Boot 3
- Spring Security + JWT (stateless)
- Spring Data JPA + PostgreSQL
- Maven
- Swagger / OpenAPI 3

**Frontend**

- React 18 + TypeScript + Vite
- TanStack React Query (server state + optimistic updates)
- Axios with JWT interceptor
- Context API for auth state
- Tailwind CSS v4
- @dnd-kit/core for drag-and-drop

**Infrastructure**

- Backend: [Render](https://render.com)
- Frontend: [Vercel](https://vercel.com)
- Database: PostgreSQL on Render

---

## Getting Started

### Prerequisites

- Java 17+
- Node.js 18+
- Docker and Docker Compose

### Installation

```bash
git clone https://github.com/vitalychernov/teamflow.git
cd teamflow
```

### Environment Variables

Copy the example config and fill in your values:

```bash
cp backend/src/main/resources/application-example.yml \
   backend/src/main/resources/application-local.yml
```

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/teamflow_dev
    username: YOUR_DB_USERNAME
    password: YOUR_DB_PASSWORD

app:
  jwt:
    secret: your-local-secret-key-min-32-characters-long
    expiration: 86400000
```

### Run Locally

```bash
# Terminal 1 — backend + database (http://localhost:8080)
docker-compose up --build

# Terminal 2 — frontend (http://localhost:5173)
cd frontend && npm install && npm run dev
```

Swagger UI: http://localhost:8080/swagger-ui.html

The database is seeded automatically on first startup with demo users and sample projects.

| Email | Password | Role |
|-------|----------|------|
| john.smith@teamflow.com | demo123 | User |
| sarah.wilson@teamflow.com | demo123 | User |
| admin@teamflow.com | admin123 | Admin |

---

## Running Tests

```bash
cd backend
mvn test
```

Test suite includes:

- **Repository tests** — `@DataJpaTest` with H2 in-memory database
- **Service tests** — Mockito unit tests for business logic
- **Controller tests** — `@WebMvcTest` with mocked services and JWT

---

## Project Structure

```
teamflow/
├── backend/
│   └── src/
│       ├── main/java/com/teamflow/
│       │   ├── config/         # SecurityConfig, DataSeeder, OpenAPI
│       │   ├── controller/     # REST controllers
│       │   ├── service/        # Business logic
│       │   ├── repository/     # Spring Data JPA repositories
│       │   ├── entity/         # JPA entities and enums
│       │   ├── dto/            # Request / Response DTOs
│       │   ├── mapper/         # Entity → DTO mappers
│       │   ├── security/       # JWT filter, UserDetailsService
│       │   └── exception/      # GlobalExceptionHandler
│       └── test/               # Repository, service, controller tests
└── frontend/
    └── src/
        ├── api/                # Axios instance, shared types
        ├── features/           # auth/, projects/, tasks/, users/
        ├── context/            # AuthContext (JWT + user state)
        ├── components/         # Spinner, ConfirmDialog
        └── pages/              # LoginPage, RegisterPage, ProjectsPage, etc.
```

---

## API Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/auth/register` | — | Register new user |
| POST | `/api/auth/login` | — | Login, returns JWT |
| GET | `/api/projects` | USER+ | List projects (paginated) |
| POST | `/api/projects` | USER+ | Create project |
| PUT | `/api/projects/{id}` | USER+ | Update project |
| DELETE | `/api/projects/{id}` | USER+ | Delete project |
| GET | `/api/projects/{id}/tasks` | USER+ | List tasks (filterable) |
| POST | `/api/projects/{id}/tasks` | USER+ | Create task |
| PUT | `/api/tasks/{id}` | USER+ | Update task |
| DELETE | `/api/tasks/{id}` | USER+ | Delete task |
| GET | `/api/users` | USER+ | List users for assignee dropdown |
| GET | `/api/admin/users` | ADMIN | Paginated user list |

Full interactive docs available at `/swagger-ui.html`.

---

## Deployment

The app deploys automatically on every push to `main`.

- **Render** (backend): connect GitHub repo → set `SPRING_PROFILES_ACTIVE=prod` and `APP_JWT_SECRET` → deploy
- **Render** (database): free PostgreSQL instance, connection URL injected via `DATABASE_URL`
- **Vercel** (frontend): connect GitHub repo → set Root Directory to `frontend` → set `VITE_API_URL` to the Render backend URL → deploy
