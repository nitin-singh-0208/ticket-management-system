# Architecture

Phase 1 is a local web app for support tickets. Sign-in and the assistant are out of scope. Source: [001-support-ticket-management/plan.md](001-support-ticket-management/plan.md) and [001-support-ticket-management/research.md](001-support-ticket-management/research.md).

## Shape

| Piece | Choice |
|-------|--------|
| API | Java 21, Spring Boot 3.5, port 8080 |
| UI | React, Vite, TypeScript, react-router, port 5173 |
| Database | `pgvector/pgvector:pg16` via root `docker-compose.yml` |
| Packages | `com.ticketmanagement.ticket`, `comment`, and `common` |

The UI calls the API with a typed `fetch` client. CORS allows `http://localhost:5173` only. The `vector` extension, Spring AI, and Ollama are not part of Phase 1. The PGVector image is in place so Phase 2 does not need a different database.

## Decisions

**Monorepo.** Context: the feature needs an HTTP API and a browser UI. Decision: `backend/` (Maven) and `frontend/` (Vite), plus Compose at the repo root. Alternatives considered: one Gradle project. Consequences: each side builds and runs on its own; they meet only over HTTP.

**Feature packages.** Context: tickets and comments are the only Phase 1 features. Decision: each feature owns its controller, service, and repository. `common` owns the problem-detail handler. Alternatives considered: packages split by layer. Consequences: status rules stay in `ticket` and are not copied into the UI.

**Schema ownership.** Context: data must survive a restart, and tests need an empty database. Decision: Flyway owns the schema (`ddl-auto: validate`). `db/migration` is V1. `db/seed` is V2. Version numbers are shared, so the next migration is V3. Tests run V1 only. Alternatives considered: Hibernate-generated schema, or one migration that also seeds. Consequences: the seeded app starts at TKT-1001 through TKT-1015; an empty test database still issues TKT-1001 first.

## RAG pipeline

Phase 2 – TBD

## Chunking strategy

Phase 2 – TBD

## Embedding model choice

Phase 2 – TBD
