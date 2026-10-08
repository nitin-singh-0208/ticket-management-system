---
description: "Implementation tasks for AI assistant over ticket history (RAG)"
---

# Tasks: AI Assistant Over Ticket History

**Input**: Design documents from `/specs/002-ticket-rag-assistant/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md

**Tests**: Included. `plan.md`, `quickstart.md`, and `specs/test-strategy.md` require unit tests before implementation in each user-story phase. `@Tag("ai-eval")` integration tests are optional and excluded from default `mvn test`.

**Organization**: Five implementation phases after this file — Setup, Foundational, then exactly three user-story phases matching `plan.md` slices (ingestion, ask endpoint, Ask AI UI). Each user-story phase is one conventional commit. Do not commit between tasks inside a phase.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependency on an incomplete task)
- **[Story]**: User story label (`[US1]`, `[US2]`, `[US3]`). Setup and Foundational have none.
- Every task includes a file path.

## Path Conventions

- Backend: `backend/src/main/java/com/ticketmanagement/`, `backend/src/main/resources/`, `backend/src/test/java/com/ticketmanagement/`
- Frontend: `frontend/src/`
- Specs: `specs/002-ticket-rag-assistant/`, `specs/evaluation-strategy.md`, `docs/ai-mistakes.md`

## Commit rule

Finish all tasks in a phase, then make **one commit** for that phase, then start the next phase. Suggested messages are on each checkpoint. Phase 1 and Phase 2 may be committed separately or combined into a single `chore: rag infrastructure` commit if preferred; Phases 3–5 must each be exactly one commit.

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Add Spring AI dependencies, PGVector extension migration, and base RAG configuration so later phases can build on a bootable app.

**Independent Test**: `docker compose up -d` starts Postgres. `mvn -f backend/pom.xml compile` succeeds. Flyway applies V1, V2, and V3. `application.yml` declares `app.rag.*` and `spring.ai.*` without hardcoded retrieval logic in Java.

**Commit**: `chore: add spring ai dependencies and pgvector migration`

### Implementation for Setup

- [X] T001 [P] Update `backend/pom.xml`: import `spring-ai-bom` **1.1.8** (not 2.x) in `dependencyManagement`; add `org.springframework.ai:spring-ai-starter-vector-store-pgvector` and `org.springframework.ai:spring-ai-starter-model-ollama`. Keep existing Boot 3.5.16 parent and Phase 1 starters unchanged.
- [X] T002 [P] Create `backend/src/main/resources/db/migration/V3__enable_pgvector.sql` with `CREATE EXTENSION IF NOT EXISTS vector` (and `uuid-ossp` / `hstore` if required by the PGVector starter). Version numbers remain shared: V1 schema, V2 seed, V3 vector extension.
- [X] T003 [P] Update `backend/src/main/resources/application.yml`: set `spring.jpa.open-in-view=false`; add `spring.ai.vectorstore.pgvector.dimensions=768`, `initialize-schema=true`, `distance-type=COSINE_DISTANCE`; add `spring.ai.model.chat=ollama`, `spring.ai.ollama.base-url=http://localhost:11434`, `spring.ai.ollama.chat.options.model=llama3.1:8b`, `spring.ai.ollama.chat.options.temperature=0`; add `spring.ai.model.embedding=ollama`, `spring.ai.ollama.embedding.options.model=nomic-embed-text`; add `app.rag.top-k=5`, `app.rag.similarity-threshold=0.6`, `app.rag.indexing.enabled=true`, `app.rag.chat-model=llama3.1:8b`, `app.rag.embedding-model=nomic-embed-text`.
- [X] T004 Add `@EnableAsync` to `backend/src/main/java/com/ticketmanagement/TicketManagementApplication.java` so async event listeners run off the request thread.

**Checkpoint**: Backend compiles with Spring AI on the classpath. Commit this phase before Phase 2.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Configuration binding, test isolation so default builds never call Ollama, and Surefire tagging for the eval suite. **No user story work until this phase completes.**

**Note**: `backend/src/test/resources/application.yml` replaces (does not merge with) `backend/src/main/resources/application.yml`. Keep `spring.ai.vectorstore.pgvector.initialize-schema` unset in the test file so no embedding call happens at context startup.

**Independent Test**: With Ollama stopped, `mvn -f backend/pom.xml verify` passes. `backend/src/test/resources/application.yml` sets `app.rag.indexing.enabled: false`. No `TicketIndexer` or `VectorStoreBackfill` beans exist in the test context.

**Commit**: `chore: rag test isolation and eval surefire config`

### Implementation for Foundational

- [X] T005 Create `backend/src/main/java/com/ticketmanagement/rag/RagProperties.java` as `@ConfigurationProperties(prefix = "app.rag")` with fields `topK`, `similarityThreshold`, `indexing.enabled` (default `true`), `chatModel`, `embeddingModel`. Register via `@EnableConfigurationProperties` on the application class or a `@Configuration` in the `rag` package.
- [X] T006 Update `backend/src/test/resources/application.yml`: keep `spring.flyway.locations: classpath:db/migration` (no seed); add `app.rag.indexing.enabled: false` so `TicketIndexer` and `VectorStoreBackfill` are not created under `@ConditionalOnProperty(name = "app.rag.indexing.enabled", havingValue = "true", matchIfMissing = true)`.
- [X] T007 [P] Configure Surefire in `backend/pom.xml` to exclude `@Tag("ai-eval")` from the default test run (`<groups>!ai-eval</groups>` or equivalent `excludedGroups`). Document in a comment that `mvn test -Dgroups=ai-eval` runs the live-model suite.
- [X] T008 [P] Create a minimal `@SpringBootTest` smoke class at `backend/src/test/java/com/ticketmanagement/rag/RagContextSmokeTest.java` (or extend an existing full-context test) with `@MockitoBean EmbeddingModel` and `@MockitoBean ChatModel` so Spring AI auto-configuration loads without a live Ollama server when indexing is disabled.
- [X] T009 Run `mvn -f backend/pom.xml verify` with Ollama actually stopped (`pkill ollama` or quit the app) and confirm success. This is the gate for all later phases: default verify must not require embeddings or chat.

**Checkpoint**: Foundation ready — user story implementation can begin. Commit this phase before Phase 3.

---

## Phase 3: User Story 1 — Ingestion and re-index (Priority: P1) 🎯 MVP

**Goal**: Ticket description, each comment, and resolution notes become searchable PGVector chunks with metadata. Re-ingestion runs on every ticket change (delete-then-add per `ticketId`) and on startup when the store is empty.

**Independent Test**: Start the app with Ollama and Postgres. Create or update a ticket, add a comment, or change status; after async indexing, chunks for that ticket exist in `vector_store` with the correct header and metadata. Re-ingest removes stale chunks. Empty store on startup triggers backfill.

**Commit**: `feat: index tickets into pgvector on change and startup`

### Tests for User Story 1

> Write these first. They must fail before `TicketDocumentMapper`, `TicketIndexer`, and `VectorStoreBackfill` exist.

- [X] T010 [P] [US1] Create `backend/src/test/java/com/ticketmanagement/rag/TicketDocumentMapperTest.java`. Assert paragraph-based chunking: max ~800 tokens per chunk, ~100 token overlap only when a single section exceeds the limit. Assert every chunk `content` starts with the header line exactly `Ticket {ticketId} | {title} | status {status} | priority {priority} | category {category} | section {sourceType}` (e.g. `Ticket TKT-1001 | Payment declined at checkout | status OPEN | priority HIGH | category Payment | section description`), then a newline, then section text. Assert metadata on every chunk: `ticketId`, `status`, `priority`, `assignee`, `category`, `sourceType` (`description`, `comment`, or `resolution`). Assert `resolution` documents are skipped when `resolution_notes` is null or blank after trim. Embed stored content as-is (no `search_document: ` prefix).
- [X] T011 [US1] Create `backend/src/test/java/com/ticketmanagement/rag/TicketIndexerTest.java` with stubbed `VectorStore` and `EmbeddingModel`. Assert re-ingest for a `ticketId` calls delete-by-filter for that id then `add` fresh documents. Assert the listener reloads ticket and comments inside `@Transactional(readOnly = true)`. Assert `TicketIndexer` is not loaded when `app.rag.indexing.enabled=false` (use `@SpringBootTest` with test `application.yml`). Add a test that `TicketService.updateTicket` and `CommentService.addComment` publish `TicketChangedEvent` (e.g. `@RecordApplicationEvents`).

### Implementation for User Story 1

- [X] T012 [P] [US1] Create `backend/src/main/java/com/ticketmanagement/ticket/event/TicketChangedEvent.java` with field `ticketId` (public id, e.g. `TKT-1001`).
- [X] T013 [US1] Publish `TicketChangedEvent` after successful commit from `backend/src/main/java/com/ticketmanagement/ticket/TicketService.java` in `createTicket`, `updateTicket`, and `changeStatus`, and from `backend/src/main/java/com/ticketmanagement/comment/CommentService.java` in `addComment`. Use `ApplicationEventPublisher`; do not change HTTP contracts or the ticket state machine.
- [X] T014 [US1] Implement `backend/src/main/java/com/ticketmanagement/rag/TicketDocumentMapper.java`: one knowledge document per section (description, each comment, resolution notes); header line as above; paragraph chunking per `specs/architecture.md` and `.cursor/rules/rag-vector-store.mdc`; metadata on every chunk; embed stored content as-is (no `search_document: ` prefix).
- [X] T015 [US1] Implement `backend/src/main/java/com/ticketmanagement/rag/TicketIndexer.java` with `@ConditionalOnProperty(name = "app.rag.indexing.enabled", havingValue = "true", matchIfMissing = true)`, `@TransactionalEventListener(phase = AFTER_COMMIT)`, `@Async`, delete-all-chunks-for-`ticketId` then add mapped documents, reload ticket and comments in `@Transactional(readOnly = true)`.
- [X] T016 [US1] Implement `backend/src/main/java/com/ticketmanagement/rag/VectorStoreBackfill.java` with the same `@ConditionalOnProperty`; implement `ApplicationRunner` that indexes all tickets when `vector_store` has zero rows, skipping when any chunks exist.
- [X] T017 [US1] Re-run `mvn -f backend/pom.xml verify` with Ollama stopped; confirm `TicketIndexerTest` and `TicketDocumentMapperTest` pass and no test triggers live embedding or backfill.

**Checkpoint**: Ingestion and re-index work against a live stack. Commit this phase alone before Phase 4.

---

## Phase 4: User Story 2 — Ask endpoint (Priority: P1)

**Goal**: `POST /api/ai/ask` returns grounded answers with cited sources, or the explicit no-match body. Single retrieve-then-generate flow; empty retrieval skips the LLM; mis-cited or uncited answers become no-match.

**Independent Test**: With indexed tickets and Ollama running, `curl` the five PDF example questions from `quickstart.md` and get `grounded: true` with sources. Out-of-scope questions return `answer` exactly `No relevant tickets found`, `grounded: false`, `sources: []`. Blank question returns HTTP 400. With Ollama stopped, `RagAskServiceTest` passes via stubs.

**Commit**: `feat: add grounded ask endpoint over ticket history`

### Tests for User Story 2

> Write these first. They must fail before `RagAskService` and `RagController` exist.

- [X] T018 [P] [US2] Create `backend/src/test/java/com/ticketmanagement/rag/RagAskServiceTest.java` with stubbed `VectorStore` and `ChatModel`. **Empty retrieval**: stub `similaritySearch` to return an empty list; assert `ChatModel` is never called; assert response is `answer` = `No relevant tickets found`, `grounded` = `false`, `sources` = `[]`.
- [X] T019 [P] [US2] In `backend/src/test/java/com/ticketmanagement/rag/RagAskServiceTest.java`, add **answer cites nothing retrieved**: retrieval returns chunks for TKT-1001; stub `ChatModel` to return an answer with no `TKT-` id; assert `grounded` = `false`, no-match answer, empty `sources`, model text is not returned.
- [X] T020 [P] [US2] In `backend/src/test/java/com/ticketmanagement/rag/RagAskServiceTest.java`, add **answer cites id not retrieved**: retrieval returns TKT-1001 only; stub `ChatModel` to cite TKT-9999 (alone or with TKT-1001); assert `grounded` = `false`, no-match answer, empty `sources` — do not trim the bad id and return partial answer.
- [X] T021 [P] [US2] In `backend/src/test/java/com/ticketmanagement/rag/RagAskServiceTest.java`, add **sources deduplicated per ticket**: retrieval returns multiple chunks for TKT-1001 (scores 0.9 and 0.7) and one for TKT-1002; model answer cites both; assert `sources` has exactly two entries (one per `ticketId`), TKT-1001 keeps score 0.9 and snippet from the higher-scoring chunk, `snippet` excludes the header line and is at most 300 characters.
- [X] T022 [P] [US2] Create `backend/src/test/java/com/ticketmanagement/rag/RagControllerTest.java` as `@WebMvcTest(controllers = RagController.class)` with `@MockitoBean RagAskService`. Assert `POST /api/ai/ask` with blank or whitespace-only `question` returns 400 ProblemDetail before the service runs; valid body delegates to the service.

### Implementation for User Story 2

- [X] T023 [P] [US2] Create `backend/src/main/java/com/ticketmanagement/rag/dto/AskRequest.java` (`question`: required, trimmed, max 2000, blank → validation error) and `backend/src/main/java/com/ticketmanagement/rag/dto/AskResponse.java` (`answer`, `sources` with `ticketId`, `title`, `status`, `score`, `snippet`, `grounded`) matching `specs/002-ticket-rag-assistant/contracts/ai.openapi.yaml`.
- [X] T024 [US2] Implement `backend/src/main/java/com/ticketmanagement/rag/RagAskService.java`: embed the raw question as-is (no `search_query: ` prefix); `similaritySearch` with `topK` and `similarityThreshold` from `RagProperties` (never hardcoded); **log each retrieved chunk's `ticketId` and score at DEBUG** (`logging.level.com.ticketmanagement.rag=DEBUG`); empty retrieval → no-match without `ChatModel`; otherwise build prompt with static system constant + context blocks whose first line is the same chunk header as stored content + user question; after generation, parse cited `TKT-` ids, build `sources` from retrieved chunks deduplicated by `ticketId` (highest score), filtered to cited ids only; uncited or mis-cited → no-match body.
- [X] T025 [US2] Implement `backend/src/main/java/com/ticketmanagement/rag/RagController.java` at `POST /api/ai/ask`: validate `AskRequest`, call `RagAskService`, return 200 with `AskResponse` or 400 for blank question per `specs/rag-api-contract.md`.
- [X] T026 [US2] Re-run `mvn -f backend/pom.xml verify` with Ollama stopped; confirm all `RagAskServiceTest` and `RagControllerTest` cases pass.

**Checkpoint**: Ask API is contract-complete and unit-tested. Commit this phase alone before Phase 5.

---

## Phase 5: User Story 3 — Ask AI UI (Priority: P2)

**Goal**: Agents open an Ask AI page, submit a question, read the grounded answer or no-match message, and click source tickets to open ticket detail.

**Independent Test**: With backend and frontend running, navigate to `http://localhost:5173/ask`, submit an in-scope question, see answer and clickable sources linking to `/tickets/{ticketId}`. Submit an out-of-scope question and see `No relevant tickets found` with no source list. Submit whitespace-only question and see client-side validation without calling the API.

**Commit**: `feat: add ask ai page with sources and no-match handling`

### Tests for User Story 3

> No frontend test framework in this project. Validation is manual per `quickstart.md`; list checks as tasks.

- [ ] T027 [US3] Manual check: open `http://localhost:5173/ask` with backend and Ollama running; submit "Have we seen payment failures before?" and confirm answer text, `grounded` sources with title and status, and each source link opens the correct ticket detail page (`frontend/src/pages/TicketDetailPage.tsx` route).

### Implementation for User Story 3

- [ ] T028 [P] [US3] Create `frontend/src/api/ai.ts` with `askQuestion(question: string)` calling `POST http://localhost:8080/api/ai/ask`; types match `specs/002-ticket-rag-assistant/contracts/ai.openapi.yaml` (`AskRequest`, `AskResponse`, `AskSource`).
- [ ] T029 [US3] Create `frontend/src/pages/AskAiPage.tsx`: question input and submit; display answer; when `grounded` is true, list sources with title and status as links to `/tickets/{ticketId}`; when `grounded` is false, show `No relevant tickets found` prominently with no source list; reject empty or whitespace-only questions on the client with a clear message and do not call the API.
- [ ] T030 [US3] Register route `/ask` and an "Ask AI" nav link in `frontend/src/App.tsx`.
- [ ] T031 [US3] Manual check per `specs/002-ticket-rag-assistant/quickstart.md`: no-match question ("What is the weather in Paris today?"), blank-question UX, and regression that Phase 1 ticket flows still work.

**Checkpoint**: Ask AI UI complete. Commit this phase alone before Phase 6.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Live-model evaluation, similarity-threshold calibration, and documentation. Does not block MVP (Phases 3–5).

**Independent Test**: `mvn -f backend/pom.xml test -Dgroups=ai-eval` passes with Ollama and seeded index. `specs/evaluation-strategy.md` records the calibrated threshold with reasoning from DEBUG retrieval scores.

**Commit**: `test: add ai-eval suite and calibrate similarity threshold`

### Eval and calibration

- [ ] T032 [P] Create `backend/src/test/java/com/ticketmanagement/rag/RagAskEvalTest.java` as a plain JUnit 5 test tagged `@Tag("ai-eval")` with no Spring context. It calls the running app (`http://localhost:8080/api/ai/ask`, base URL overridable via system property) using `java.net.http.HttpClient`. Cover five in-scope questions (Q1–Q5) and five out-of-scope questions (O1–O5) from `specs/evaluation-strategy.md`; assert pass criteria (grounded, sources, no-match body). Excluded from default `mvn test`. Prerequisites: `docker compose up`, backend running with seed and indexing enabled, Ollama running.
- [ ] T033 Run `mvn -f backend/pom.xml test -Dgroups=ai-eval` against that running app. Set `logging.level.com.ticketmanagement.rag=DEBUG` on the backend and record each question's response and retrieval scores (from backend DEBUG logs) for T034.
- [ ] T034 **Threshold calibration**: from DEBUG logs, choose `app.rag.similarity-threshold` — lower only if an in-scope question misses an expected ticket just under 0.6; raise if an out-of-scope question retrieves chunks and grounds; keep 0.6 if both sides pass. Update `backend/src/main/resources/application.yml` with the chosen value and record the number and reasoning in `specs/evaluation-strategy.md` (replace the "Not yet measured" row).
- [ ] T035 [P] If eval or manual testing surfaced a wrong citation, fabricated fact, or missed retrieval, record it in `docs/ai-mistakes.md` per AC-23.

**Checkpoint**: Eval suite and calibrated threshold documented. Commit this phase when eval is green.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately.
- **Foundational (Phase 2)**: Depends on Setup — **blocks all user stories**.
- **User Story 1 (Phase 3)**: Depends on Foundational — ingestion must exist before ask can retrieve.
- **User Story 2 (Phase 4)**: Depends on Foundational; practically needs indexed data for manual curl checks, but unit tests stub the store.
- **User Story 3 (Phase 5)**: Depends on User Story 2 (API must exist).
- **Polish (Phase 6)**: Depends on Phases 3–5.

### User Story Dependencies

```text
Setup → Foundational → US1 (ingestion) → US2 (ask API) → US3 (Ask AI UI) → Polish (eval)
```

- **US1**: No dependency on US2/US3. Delivers searchable index.
- **US2**: Logically follows US1 for end-to-end manual testing; unit tests are independent.
- **US3**: Requires US2 endpoint.

### Within Each User Story Phase

1. Tests first — must fail before implementation.
2. Domain events / mapper before indexer.
3. Service before controller (US2).
4. API module before page (US3).
5. `mvn -f backend/pom.xml verify` with Ollama stopped after US1 and US2.

### Parallel Opportunities

- **Phase 1**: T001, T002, T003 in parallel.
- **Phase 2**: T007, T008 in parallel.
- **Phase 3**: T010 parallel with early T012; T012–T014 parallel where files differ.
- **Phase 4**: T018–T022 all parallel (different test methods/files); T023 parallel with T024 start.
- **Phase 5**: T028 parallel with page skeleton.
- **Phase 6**: T032, T035 in parallel.

---

## Parallel Example: User Story 2

```bash
# Launch all RagAskServiceTest cases together (separate @Test methods, same file):
Task T018: empty retrieval → no ChatModel call
Task T019: answer cites nothing → grounded=false
Task T020: cites unretrieved id → grounded=false
Task T021: sources deduplicated per ticket

# Then implement service and controller:
Task T024: RagAskService.java (DEBUG score logging, header in LLM context)
Task T025: RagController.java
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (verify gate with Ollama stopped)
3. Complete Phase 3: User Story 1 — ingestion and re-index
4. **STOP and VALIDATE**: Index a ticket change; inspect `vector_store` chunks and header format
5. Demo retrieval manually if needed before building the ask endpoint

### Incremental Delivery

1. Setup + Foundational → test isolation proven (`mvn verify`, Ollama stopped)
2. US1 → indexed ticket history (one commit)
3. US2 → grounded ask API (one commit)
4. US3 → Ask AI page (one commit)
5. Polish → ai-eval + threshold calibration (one commit)

### Suggested commit sequence

| Order | Phase | Suggested message |
|-------|-------|-------------------|
| 1 | Setup | `chore: add spring ai dependencies and pgvector migration` |
| 2 | Foundational | `chore: rag test isolation and eval surefire config` |
| 3 | US1 | `feat: index tickets into pgvector on change and startup` |
| 4 | US2 | `feat: add grounded ask endpoint over ticket history` |
| 5 | US3 | `feat: add ask ai page with sources and no-match handling` |
| 6 | Polish | `test: add ai-eval suite and calibrate similarity threshold` |

---

## Notes

- Spring AI **1.1.8** only; do not upgrade to 2.x (Boot 4).
- `app.rag.top-k` and `app.rag.similarity-threshold` must never be hardcoded in Java.
- Chunk header in tests, stored content, and LLM context must match: `Ticket {ticketId} | {title} | status {status} | priority {priority} | category {category} | section {sourceType}`.
- Default build safety: `app.rag.indexing.enabled=false` in test YAML + `@ConditionalOnProperty` on `TicketIndexer` and `VectorStoreBackfill` + `@MockitoBean` model beans in full-context tests.
- Ticket state machine and existing REST endpoints remain unchanged.
