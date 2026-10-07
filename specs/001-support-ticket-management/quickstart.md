# Quickstart: Support Ticket Management

Validates the Phase 1 ticket app against [spec.md](spec.md). Field rules and the transition table are in [data-model.md](data-model.md). Request and response shapes are in [contracts/tickets.openapi.yaml](contracts/tickets.openapi.yaml). Screen behavior is in [contracts/ui.md](contracts/ui.md).

## Prerequisites

- Java 21
- Node.js with npm
- Docker, able to pull `pgvector/pgvector:pg16`

From the repository root:

```bash
docker compose up -d
```

The compose file starts PostgreSQL as database `tickets` with user `tickets`. The local password default is `tickets`, overridable with `TICKETS_DB_PASSWORD`. That default is only for this local container.

## Backend

```bash
cd backend
mvn test
mvn spring-boot:run
```

`mvn test` is the required check. It runs:

- `TicketStatusTest` on the transition map, without Spring
- `TicketStatusTransitionTest`, a `@SpringBootTest` plus Testcontainers parameterized test of all 25 status pairs
- `TicketValidationTest`, a `@WebMvcTest` that expects 400 field errors and no service call

The app listens on port 8080 and applies Flyway schema `V1` and seed `V2`. Tests apply `V1` only, so an empty test database still issues TKT-1001 first.

Expected `mvn test` result: the five allowed status pairs return 200 and the twenty other pairs return 409 with the previous status still stored. Blank required fields return 400 with one error per field.

## Frontend

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173/tickets`. The API allows that origin.

## Seeded list

The running app, not the test database, contains TKT-1001 through TKT-1015. The list shows TKT-1015 first. There is no page control, and every seeded row is visible before a filter is applied.

| Check | Action | Expected |
|-------|--------|----------|
| Order | Open `/tickets` | TKT-1015 is the first row. TKT-1001 is the last. |
| Substring | Search `pay` | TKT-1001 appears. A login ticket whose title contains "password", such as TKT-1011, also appears. |
| Exclusive word | Search `checkout` | Only TKT-1001 appears. |
| Phrase | Search `payment failure` | TKT-1001 appears. A ticket that does not contain that sequence does not. |
| Word order | Search `failure payment` | TKT-1001 does not appear on the strength of the reversed words. |
| Non-search field | Search `reissued` | TKT-1004 is not listed. That word is only in its resolution notes. |
| Status | Filter `OPEN` | Only OPEN tickets remain, still newest first. |
| Empty | Search `zzzz-no-such-ticket` | The list is empty and the screen says "Nothing matched". |

## Create, edit, comment

| Check | Action | Expected |
|-------|--------|----------|
| Next id | Create a ticket with title, description, priority, assignee, and category | 201, `Location` points at the new ticket, status is OPEN, id is TKT-1016 on a fresh seed. |
| Blank field | Submit the create form with title left blank | The ticket is not created. The title field is named in the error. |
| Edit | Change the assignee on TKT-1004 and save | Assignee updates. Status stays CLOSED. Category and ticket id stay the same. The row does not jump to the top of the list. |
| Comment | Add a comment on TKT-1004 | The comment appears with a time, after older comments. Status stays CLOSED. |
| Blank comment | Submit an empty comment | No comment is added. The screen says the comment needs text. |

## Status

Use a ticket whose `allowedNextStatuses` includes the target. The control offers only those values.

| Check | Action | Expected |
|-------|--------|----------|
| Allow | Move an OPEN ticket to IN_PROGRESS | Status becomes IN_PROGRESS. |
| Refuse | Request CLOSED on an OPEN ticket (API call; the screen does not offer it) | 409, detail says the change is not allowed, status stays OPEN. |
| Terminal | Open TKT-1004 or TKT-1005 | No next status is offered. |

Calling `PATCH /api/tickets/{ticketId}` with a `status` property returns 400 and does not change status.

## Restart

Stop the backend process and start it again. Do not remove the Docker volume. TKT-1001 through TKT-1016, including the edit and comment above, are still present. Create one more ticket and confirm its id is TKT-1017.
