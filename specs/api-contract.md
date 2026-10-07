# API contract

Phase 1 HTTP API. Schemas, query rules, and examples are in [001-support-ticket-management/contracts/tickets.openapi.yaml](001-support-ticket-management/contracts/tickets.openapi.yaml). JSON field names are camelCase. Tickets are addressed by `ticketId`.

| Method | Path | Success |
|--------|------|---------|
| GET | `/api/tickets` | 200 |
| POST | `/api/tickets` | 201 and a `Location` header |
| GET | `/api/tickets/{ticketId}` | 200 |
| PATCH | `/api/tickets/{ticketId}` | 200 |
| PATCH | `/api/tickets/{ticketId}/status` | 200 |
| POST | `/api/tickets/{ticketId}/comments` | 201 and a `Location` header |

`GET /api/tickets` accepts `q`, `status`, `page`, and `size`. Omitting `page` and `size` returns every match. `page` without `size` is 400. When `size` is present and `page` is omitted, `page` is 0. Order is fixed: `createdAt` descending, then human-id number descending.

`PATCH /api/tickets/{ticketId}` updates title, description, priority, assignee, and resolution notes. It cannot change status or category. An omitted property stays unchanged. An explicit JSON null is 400. `resolutionNotes: ""` clears the notes. Unknown properties are rejected because `spring.jackson.deserialization.fail-on-unknown-properties=true` (Spring Boot would otherwise ignore them). See [001-support-ticket-management/plan.md](001-support-ticket-management/plan.md).

`PATCH /api/tickets/{ticketId}/status` is the only status write. A name outside the five statuses is 400. A disallowed pair, including a change to the current status, is 409 and the stored status stays as it was. Detail responses include `allowedNextStatuses` from that same map. See [state-machine.md](state-machine.md).

## Errors

Every error is an RFC 7807 problem (`application/problem+json`): `title`, `status`, `detail`, and `instance`. Validation failures are 400 and include `errors: [{ field, message }]`. An unknown ticket is 404. Bodies do not include stack traces. Rules: [001-support-ticket-management/research.md](001-support-ticket-management/research.md).
