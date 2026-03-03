# TeamFlow

TeamFlow is a full-stack project management application inspired by Jira and Asana. It features a kanban board with drag-and-drop task management, role-based access control, team member assignment, and a real-time progress indicator per project.

## Key Features

The application supports user registration and JWT-based authentication, project creation with completion tracking, and a three-column kanban board (To Do / In Progress / Done) with drag-and-drop powered by @dnd-kit. Tasks include priority levels, optional descriptions, and assignee management. An "Assigned to me" filter lets users focus on their own work. Admin users have access to a separate user management panel. All UI updates are optimistic — cards move instantly without waiting for the server.

## Technology Foundation

**Backend**: Java 17 with Spring Boot 3, Spring Security and JWT authentication, Spring Data JPA with PostgreSQL, Maven, and Swagger / OpenAPI 3 for API documentation.

**Frontend**: React 18 and TypeScript with Vite, TanStack React Query for server state and optimistic updates, Axios with a JWT interceptor, Context API for auth state, Tailwind CSS v4, and @dnd-kit/core for drag-and-drop.

**Infrastructure**: Docker and Docker Compose for local development, Render Free for backend and PostgreSQL, Vercel for the frontend, and GitHub Actions for CI.

## Getting Started

Prerequisites: Java 17+, Node.js 18+, Docker and Docker Compose.

```bash
git clone https://github.com/vitalychernov/teamflow.git
cd teamflow
docker-compose up --build
```

- Frontend: http://localhost:5173
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

The database is seeded automatically on first startup with demo users and sample projects.

## Demo Credentials

| Email | Password | Role |
|-------|----------|------|
| john.smith@teamflow.com | demo123 | User |
| sarah.wilson@teamflow.com | demo123 | User |
| admin@teamflow.com | admin123 | Admin |

## Testing

Backend tests cover the repository layer with `@DataJpaTest`, the service layer with Mockito unit tests, and the controller layer with `@WebMvcTest`. Run them with:

```bash
cd backend && mvn test
```

## Deployment

The backend is deployed on [Render Free](https://render.com) with a managed PostgreSQL database. The frontend is deployed on [Vercel](https://vercel.com) with automatic deploys on every push to `main`.

**Live demo**: https://teamflow-psi.vercel.app
