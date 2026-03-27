# CodePulse — Real-time Code Quality Dashboard

A self-hosted platform that connects to your GitHub repos via webhooks, analyzes code quality on every push, stores historical metrics, and displays trends on a React dashboard. Think of it as your own mini-SonarQube — focused, lightweight, and built to showcase software engineering skills.

## Features

- **Real-time Analysis**: Every push to GitHub triggers automated code quality analysis
- **Multi-language Support**: Analyzes Java, Python, and JavaScript/TypeScript codebases
- **Composite Health Score**: 0-100 score based on complexity, duplication, code smells, and test coverage
- **Trend Tracking**: Historical metrics visualization with interactive charts
- **File Hotspot Detection**: Identifies the most complex files in your codebase
- **Configurable Thresholds**: Per-repository quality gates with alert generation
- **Async Processing**: Redis-backed job queue decouples webhook receipt from analysis
- **Comprehensive Testing**: 80%+ code coverage enforced via JaCoCo in CI

## Architecture

```
GitHub Webhook (push event)
        |
        v
+------------------+
|  Spring Boot API  | <-- REST endpoints for dashboard
|  (Port 8080)      |
+--------+---------+
         |
         v
+------------------+
|   Redis Queue     | <-- Decouples webhook receipt from analysis
|   (Port 6379)     |
+--------+---------+
         |
         v
+------------------+
|  Analysis Worker  | <-- Picks jobs from queue, clones repo, analyzes
|  (Spring async)   |
+--------+---------+
         |
         v
+------------------+
|   PostgreSQL      | <-- Stores repos, commits, metrics history
|   (Port 5432)     |
+--------+---------+
         |
         v
+------------------+
|  React Dashboard  | <-- Displays trends, alerts, per-file breakdown
|  (Port 3000)      |
+------------------+
```

## Tech Stack

| Tech | Purpose |
|------|---------|
| Java 17 | Backend language |
| Spring Boot 3.2 | REST API framework |
| Spring Data JPA | ORM for database operations |
| Spring Security | Webhook signature verification (HMAC SHA-256) |
| Redis | Async message queue for job processing |
| PostgreSQL | Relational DB for metrics history |
| Flyway | Database migration management |
| JUnit 5 + Mockito | Unit & integration testing |
| Testcontainers | Integration tests with real DB/Redis |
| JaCoCo | Code coverage reporting (80% enforced) |
| React 18 | Frontend dashboard |
| Recharts | Metrics visualization charts |
| Docker + Compose | Containerized deployment |
| GitHub Actions | CI/CD pipeline |
| Swagger/OpenAPI | API documentation |

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- Node.js 20+
- Docker & Docker Compose
- Git

### Running with Docker (recommended)

```bash
git clone https://github.com/Falgunisharma72/codepulse.git
cd codepulse
docker compose up --build
```

The app will be available at:
- **Dashboard**: http://localhost:3000
- **API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html

### Running Locally (development)

1. Start PostgreSQL and Redis:
```bash
docker compose up postgres redis
```

2. Run the Spring Boot backend:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

3. Run the React frontend:
```bash
cd frontend
npm install
npm start
```

### Running Tests

```bash
# Backend tests
mvn clean verify

# Coverage report (generated at target/site/jacoco/index.html)
mvn jacoco:report

# Frontend tests
cd frontend && npm test -- --coverage --watchAll=false
```

## API Documentation

Full API documentation is available at `/swagger-ui.html` when the application is running.

### Key Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/webhooks/github` | GitHub webhook receiver |
| POST | `/api/repositories` | Register a new repo |
| GET | `/api/repositories` | List all tracked repos |
| GET | `/api/repositories/{id}/metrics/latest` | Latest metrics snapshot |
| GET | `/api/repositories/{id}/metrics/trend` | Historical trend data |
| GET | `/api/repositories/{id}/metrics/hotspots` | Top 10 complex files |
| GET | `/api/repositories/{id}/alerts` | Active quality alerts |
| GET | `/api/dashboard/summary` | Dashboard overview |

## Design Patterns & Key Decisions

- **Strategy Pattern**: Language-specific analyzers (`JavaAnalyzer`, `PythonAnalyzer`, `JavaScriptAnalyzer`) implement a common `CodeAnalyzer` interface
- **Async Processing**: Webhooks return `202 Accepted` immediately; analysis happens asynchronously via Redis queue
- **HMAC SHA-256 Verification**: Webhook signatures are verified for security
- **Flyway Migrations**: Database schema managed through version-controlled migrations (not Hibernate auto-DDL)
- **Composite Health Score**: Weighted formula considering complexity, duplication, code smells, and test coverage

## Future Improvements

- GitHub OAuth integration for user authentication
- Support for Go, Rust, and C# analyzers
- Webhook retry mechanism with exponential backoff
- Email/Slack notifications for critical alerts
- AST-based analysis for more accurate metrics
- Pull request status checks integration
- Multi-branch analysis comparison
