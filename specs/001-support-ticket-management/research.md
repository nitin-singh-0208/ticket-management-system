# Research: Support Ticket Management

## Monorepo layout

- **Decision**: Two projects at the repository root, `backend/` (Maven) and `frontend/` (Vite), plus root `docker-compose.yml`. Java packages are `com.ticketmanagement.ticket`, `com.ticketmanagement.comment`, and `com.ticketmanagement.common`.
- **Rationale**: The feature is a web app with a REST boundary. Feature packages keep the ticket workflow, comment append, and shared error handling apart without a third deployable. The user asked for this layout.
- **Alternatives considered**: A single Gradle project; layer packages (`controller`, `service`, `repository`) across features. Layer packages were rejected because the user required feature packages and the two features are small enough to own their own controller, service, and repository.

## Database image

- **Decision**: `pgvector/pgvector:pg16` in Compose and in Testcontainers. Phase 1 does not enable the `vector` extension and does not add Spring AI or Ollama.
- **Rationale**: The constitution requires PostgreSQL with PGVector for the project. This feature's spec leaves the assistant out of scope. Using the Phase 2 image now avoids a later database swap. Leaving the extension unused keeps this feature's schema limited to tickets and comments.
- **Alternatives considered**: `postgres:16` now and a second image later; H2 for tests. H2 was rejected because status and search tests must run against the same SQL behavior as the app (case-insensitive `ILIKE`, sequences).

## Schema ownership

- **Decision**: Flyway owns the schema. Hibernate `ddl-auto` is `validate`. Schema migration: `backend/src/main/resources/db/migration/V1__create_ticket_schema.sql`. Seed migration: `backend/src/main/resources/db/seed/V2__seed_tickets.sql`. The running app uses both locations. Tests set `spring.flyway.locations` to `classpath:db/migration` only.
- **Rationale**: The user required a Flyway seed. Keeping the seed in its own Flyway location lets create-ticket tests start from an empty database, where the first id is still TKT-1001. The seeded app used in the quickstart already contains TKT-1001 through TKT-1015, and the next created ticket is TKT-1016.
- **Alternatives considered**: One migration that both creates and seeds. That would make the "no tickets exist → TKT-1001" test false unless every test deleted the seed first.

## Human ID allocation

- **Decision**: A PostgreSQL sequence `ticket_number_seq` starts at 1001. The public id is `TKT-` plus that number, stored in `public_id` and used in URLs. The seed inserts TKT-1001 through TKT-1015 and then `setval`s the sequence to 1015 so the next `nextval` is 1016. Tickets are not deleted, so numbers are not reused. The sequence lives in PostgreSQL, so a restart continues it.
- **Rationale**: Matches the spec: first id TKT-1001, then the next integer, stable across restarts.
- **Alternatives considered**: A UUID public id; computing the max id plus one in application code. Max-plus-one races if two creates run together. The sequence is atomic.

## Status changes

- **Decision**: `TicketStatus` holds the allowed-transition map. The only write path is `PATCH /api/tickets/{ticketId}/status`. `PATCH /api/tickets/{ticketId}` has no `status` or `category` property, and unknown JSON properties are rejected with 400. Allowed targets are also returned on the ticket detail as `allowedNextStatuses`, and the screen renders that list.
- **Rationale**: The constitution requires the server to enforce the state machine. One enum map is the source of the 25-pair test, the 409 rule, and the choices on screen. A second copy in the UI can drift.
- **Alternatives considered**: Letting the field-update PATCH accept `status`. Rejected because the user forbade it and a combined write makes "notes saved but status rejected" ambiguous.

## List query versus "no pages"

- **Decision**: `GET /api/tickets` accepts `q`, `status`, `page`, and `size`. When `page` and `size` are both omitted, the response `content` contains every match, `page` is 0, `size` equals `totalElements`, and `totalPages` is 1 (or 0 when nothing matches). The UI never sends `page` or `size` and has no page controls. When `size` is sent, it is a 1-based window size with a maximum of 100, and `page` defaults to 0 if omitted. `page` without `size` is 400. Sort is fixed: `createdAt` descending, then human-id number descending. There is no `sort` parameter.
- **Rationale**: The spec requires every match on one screen. The user and the API standard also require these query parameter names. The default response satisfies the spec. Optional `page` and `size` exist for the contract without adding a pager to the UI.
- **Alternatives considered**: Always paging with Spring's default size of 20. That would hide the 21st match and fail the spec. Removing `page` and `size` would ignore the requested contract.

## Keyword search

- **Decision**: Trim `q`. A blank or omitted `q` applies no text filter. Otherwise match `title` or `description` with a case-insensitive contiguous substring (`ILIKE` after escaping `%`, `_`, and `\`). Do not split the keyword into words. Do not search comments, resolution notes, category, assignee, or the human id.
- **Rationale**: "pay" must match "payment", and "payment failure" must match that sequence only. Escaping stops a keyword of `%` from matching every ticket.
- **Alternatives considered**: Full-text search; matching whole words only. Whole words contradict the spec. Full-text search is unnecessary at this scale.

## Concurrent edits

- **Decision**: Last save wins. There is no version column and no `ETag`.
- **Rationale**: The spec does not define a conflict rule. This feature has no accounts. Adding 409 on a version mismatch would be a new user-visible rule.
- **Alternatives considered**: Optimistic locking. Rejected for the reason above. It can be added later without changing the public fields.

## Errors

- **Decision**: Every error is an RFC 7807 `ProblemDetail`. Enable MVC problem details. One `@ControllerAdvice` in `common` supplies the body. Validation failures are 400 and include `errors: [{ field, message }]`. Unknown tickets are 404. A status value that is not one of the five names is 400. A known status that is not an allowed next status, including a change to the current status, is 409 and the stored status is unchanged. Responses never include stack traces.
- **Rationale**: The API standard requires one error shape, field details on validation, 400, 404, and 409. Spring Boot 3 already speaks ProblemDetail.
- **Alternatives considered**: A custom `{ code, message }` envelope. Rejected because the user required ProblemDetail.

## Validation limits

- **Decision**: Trim text inputs. After trim, title, description, priority, assignee, and category are required on create. Title and assignee max 200. Category max 100. Description, resolution notes, and comment text max 5000. Priority is `LOW`, `MEDIUM`, or `HIGH`. The UI labels those Low, Medium, and High. Resolution notes may be omitted or empty. On field update, an omitted property is left unchanged; a present required property that is blank is 400; `resolutionNotes: ""` clears the notes.
- **Rationale**: These limits and the blank-means-missing rule are in the spec. Token-style priority values match the status tokens already used in the spec (`OPEN`, `IN_PROGRESS`).

## Seed themes

- **Decision**: Fifteen tickets, five each for payment failures, shipment tracking, and login issues. Statuses and priorities are mixed. Several have comments and resolution notes. Created times are distinct so the list order is TKT-1015 first through TKT-1001 last. Details are in `data-model.md`.
- **Rationale**: The user asked for about fifteen realistic rows in those themes so list, search, and detail can be tried without hand entry. Distinct timestamps make the creation-time order obvious in the demo.

## Tests

- **Decision**: Three backend test layers, and no extra frontend test framework in this feature.
  - `TicketStatusTest`: plain JUnit on the enum map. No Spring.
  - `TicketStatusTransitionTest`: `@SpringBootTest` with `@AutoConfigureMockMvc` and Testcontainers using `pgvector/pgvector:pg16`. One `@ParameterizedTest` with 25 cases (5 sources × 5 targets). Each case inserts a ticket already in the source status through the repository, calls `PATCH /api/tickets/{ticketId}/status`, then reads the row back. Five cases expect 200 and the target status. Twenty cases expect 409 and the original status.
  - `TicketValidationTest`: `@WebMvcTest` for create, field update, and comment. Blank and whitespace-only required fields return 400 with one `errors` entry per field, and the service mock is not called.
- **Rationale**: The constitution requires an integration test per valid and per invalid transition. The user named these three styles. Seeding the source status through the repository keeps a broken happy-path transition from hiding a failure that starts at `CLOSED` or `CANCELLED`.
- **Alternatives considered**: One Spring test that only walks the five happy paths. That does not cover the twenty refusals. A browser test suite was not requested.

## Local database password

- **Decision**: Compose and `application.yml` use a local-only default database name, user, and password of `tickets`, overridable with `TICKETS_DB_PASSWORD`. Tests do not use that password; Testcontainers supplies a throwaway connection through `@ServiceConnection`.
- **Rationale**: The app has to start from the quickstart without a secret store. The default is only for the developer’s local container and is not a credential for a deployed environment.
- **Alternatives considered**: A password committed as if it were production. Rejected. Requiring a manual `.env` before the first boot was rejected because the quickstart would fail closed for a local demo.

## CORS

- **Decision**: The API allows `http://localhost:5173` only.
- **Rationale**: Vite’s default origin needs to call the API in local development. An allow-all origin is wider than this feature needs.

## Performance and scale

- **Decision**: No separate latency or throughput target. The spec’s timing goal is a person creating a ticket in under two minutes. The list returns every match in one query. An index on `(created_at DESC, ticket_number DESC)` supports that order. Leading-wildcard `ILIKE` is acceptable for the seeded set and a single-agent queue.
- **Rationale**: Nothing in the spec asks for a numeric service-level objective. Inventing one would not change the design.
- **Alternatives considered**: A trigram index for search. Unnecessary until the table is large enough for the sequential scan to matter.
