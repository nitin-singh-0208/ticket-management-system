# Implementation Plan: Support Ticket Management

**Branch**: `001-support-ticket-management` | **Date**: 2026-10-07 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/001-support-ticket-management/spec.md`

## Summary

Agents create, view, list, search, filter, edit, and comment on support tickets, and move them only through the allowed statuses. Data survives a restart. Sign-in and the assistant are out of scope.

The app is a monorepo: a Spring Boot 3.5 API in `backend/`, a React + Vite + TypeScript UI in `frontend/`, and PostgreSQL from `pgvector/pgvector:pg16` at the repo root. Tickets and comments are feature packages. Status changes go only through `PATCH /api/tickets/{ticketId}/status`, backed by a `TicketStatus` transition map. Errors are RFC 7807 problem details. The list endpoint accepts `q`, `status`, `page`, and `size`; when `page` and `size` are omitted it returns every match, and the UI has no pages.

## Technical Context

**Language/Version**: Java 21; TypeScript on the current Vite React toolchain

**Primary Dependencies**: Spring Boot 3.5 (Web, Validation, Data JPA, Flyway), PostgreSQL driver, Testcontainers; React, Vite, TypeScript, react-router. HTTP calls use a typed `fetch` client. No extra HTTP library.

**Storage**: PostgreSQL 16 via `pgvector/pgvector:pg16`. Flyway schema in `backend/src/main/resources/db/migration`. Flyway seed in `backend/src/main/resources/db/seed`. Hibernate `ddl-auto: validate`.

**Testing**: JUnit 5. `TicketStatusTest` (no Spring). `TicketStatusTransitionTest` (`@SpringBootTest`, MockMvc, Testcontainers, 25 status pairs). `TicketValidationTest` (`@WebMvcTest`).

**Target Platform**: Local developer machine. API on port 8080. UI on port 5173.

**Project Type**: Web application (backend API + frontend).

**Performance Goals**: No separate service latency target. A person can create a ticket and see its id in under 2 minutes (SC-001). The list returns every match in one response.

**Constraints**: Backend enforces validation and the state machine. No secrets beyond the published local database placeholder. No auth. No assistant, embeddings, or Spring AI in this feature. UI shows every match and does not page.

**Scale/Scope**: One agent, no accounts. About 15 seeded tickets, plus tickets created during use. Two screens flows: list/create and detail. Show-all list is the intended volume.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Gate | Pre-design | Post-design |
|------|------------|-------------|
| I. Spec-first | Pass. This plan is produced before application code. Requirements trace to `specs/001-support-ticket-management/spec.md` and `specs/requirements.md`. | Pass. No application code is added by this command. |
| II. Backend is source of truth | Pass. The transition map lives on `TicketStatus`. Disallowed pairs return 409 and do not change the row. | Pass. `allowedNextStatuses` on the detail payload is copied from that map. Field update cannot set status. |
| III. Test-first for the state machine | Pass. The 25-pair integration test and the enum unit test are required before the status behavior is treated as done. | Pass. `TicketStatusTransitionTest` covers each pair, including self-transitions. The slice writes those tests before the status endpoint is considered complete. |
| IV. Grounded AI | Pass. Not applicable. The spec excludes the assistant. | Pass. No retrieval or generation endpoint is in the contract. |
| V. Configuration over constants | Pass. `topK`, similarity threshold, and model names are not part of this feature, so they are not hardcoded here. | Pass. Unchanged. |
| VI. No secrets in git | Pass, with a local-only database placeholder. | Pass. Compose and `application.yml` may use the published placeholder `tickets` for the local container, overridable by `TICKETS_DB_PASSWORD`. Tests take a throwaway connection from Testcontainers and do not embed that password. It is not a credential for any shared or deployed system. |
| VII. Atomic commits | Pass. Slices below are one story each. | Pass. Unchanged. |
| Technology stack | Pass for this feature's scope. Java 21, Spring Boot 3.5, PostgreSQL via the PGVector image, React, Vite, and TypeScript match the constitution. Spring AI and Ollama stay out until the assistant feature. | Pass. The image is `pgvector/pgvector:pg16`. The vector extension is not created in this feature. |

No gate failed.

## Project Structure

### Documentation (this feature)

```text
specs/001-support-ticket-management/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   ├── tickets.openapi.yaml
│   └── ui.md
└── tasks.md              # /speckit-tasks — not created here
```

### Source Code (repository root)

```text
docker-compose.yml
backend/
├── pom.xml
└── src/
    ├── main/java/com/ticketmanagement/
    │   ├── TicketManagementApplication.java
    │   ├── common/
    │   │   └── ApiExceptionHandler.java
    │   ├── ticket/
    │   │   ├── TicketStatus.java
    │   │   ├── TicketPriority.java
    │   │   ├── Ticket.java
    │   │   ├── TicketRepository.java
    │   │   ├── TicketService.java
    │   │   ├── TicketController.java
    │   │   └── dto/
    │   └── comment/
    │       ├── Comment.java
    │       ├── CommentRepository.java
    │       ├── CommentService.java
    │       └── CommentController.java
    ├── main/resources/
    │   ├── application.yml
    │   ├── db/migration/V1__create_ticket_schema.sql
    │   └── db/seed/V2__seed_tickets.sql
    └── test/java/com/ticketmanagement/
        └── ticket/
            ├── TicketStatusTest.java
            ├── TicketStatusTransitionTest.java
            └── TicketValidationTest.java
frontend/
├── package.json
├── vite.config.ts
└── src/
    ├── main.tsx
    ├── App.tsx
    ├── api/tickets.ts
    └── pages/
        ├── TicketListPage.tsx
        ├── TicketCreatePage.tsx
        └── TicketDetailPage.tsx
```

**Structure Decision**: Web application. `backend/` and `frontend/` are separate projects. Backend code is packaged by feature (`ticket`, `comment`, `common`). Persistence entities stay inside those packages and are not the API payloads; controllers use DTOs. The running app enables both Flyway locations. Tests enable `db/migration` only.

### Implementation slices

Each slice is one commit. Later slices stay testable without unfinished stories. `/speckit-tasks` should keep this cut so no story becomes a multi-concern change.

| Order | Slice | Commit contains |
|-------|--------|-----------------|
| 1 | Setup | Compose, Maven and Vite skeletons, problem-detail handler, CORS for `http://localhost:5173`, Flyway `V1` empty schema, app boots. |
| 2 | User Story 1 | Create and view a ticket. Human-id sequence. Create-form field errors. |
| 3 | Seed | Flyway `V2` with the fifteen tickets in `data-model.md`, sequence set so the next id is TKT-1016. |
| 4 | User Story 2 | `TicketStatus` map, unit test, 25-pair integration test, status endpoint, screen control driven by `allowedNextStatuses`, 409 copy. Tests are written before the endpoint is treated as done. |
| 5 | User Story 4 | Field update, including on CLOSED and CANCELLED. Category and status stay unchanged. |
| 6 | User Story 5 | Append-only comments, including on CLOSED and CANCELLED. Blank comment error. |
| 7 | User Story 3 | List, `q`, status filter, creation-time order, empty "Nothing matched". The client omits `page` and `size`. |
| 8 | User Story 6 | Only what the earlier slices did not already show: restart check from the quickstart, and any field-error copy still missing on a form. Persistence itself is slice 1. |

## Complexity Tracking

No constitution violations.
