# Test strategy

Phase 1 backend tests. How to run them and what the seeded app should show are in [001-support-ticket-management/quickstart.md](001-support-ticket-management/quickstart.md). The required cases are in [001-support-ticket-management/plan.md](001-support-ticket-management/plan.md).

| Test | Style | Proves |
|------|--------|--------|
| `TicketStatusTest` | JUnit, no Spring | The enum map matches [state-machine.md](state-machine.md) |
| `TicketStatusTransitionTest` | `@SpringBootTest`, MockMvc, Testcontainers (`pgvector/pgvector:pg16`) | All 25 status pairs, and an unknown `ticketId` returns 404 |
| `TicketValidationTest` | `@WebMvcTest` | Field errors are 400 and the service is not called. `PATCH /api/tickets/{ticketId}` with `status` is 400 |
| `CommentValidationTest` | `@WebMvcTest(CommentController.class)` | A blank comment is 400 and the service is not called |
| `ApiExceptionHandler` | Covered in `TicketValidationTest` | An unknown property, an explicit null, or a bad enum is a ProblemDetail 400 that names the property |

The transition test inserts each source status through the repository, then calls the status endpoint and reads the row back. Five pairs return 200 and the requested status. The other twenty return 409 and the previous status. The pair matrix is in [001-support-ticket-management/data-model.md](001-support-ticket-management/data-model.md).

Tests apply Flyway `db/migration` only, so the first id in an empty database is TKT-1001. The running app also applies `db/seed`.

Manual checks in the quickstart cover list order, `pay` matching only TKT-1001 and TKT-1005, the phrase `payment failure`, a reversed phrase, a word that exists only in resolution notes, create, edit, comment, a refused status, and data still present after a restart.

## Frontend

No automated tests in Phase 1. UI error display (field errors, 409 `detail`, 404 `detail`) is verified manually via [001-support-ticket-management/quickstart.md](001-support-ticket-management/quickstart.md). That check maps to AC-13, "UI shows meaningful errors," in [requirements.md](requirements.md).

## AI / retrieval evaluation

Phase 2 – TBD (stubbed-model unit tests, pgvector integration test, `@Tag("ai-eval")` real-model suite excluded from default build)
