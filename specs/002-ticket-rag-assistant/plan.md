# Implementation Plan: AI Assistant Over Ticket History

**Branch**: `002-ticket-rag-assistant` | **Date**: 2026-10-08 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/002-ticket-rag-assistant/spec.md`

## Summary

Agents ask natural-language questions over ticket history and receive answers grounded only in indexed tickets, with cited sources or an honest no-match message. Ticket description, comments, and resolution notes are embedded into PGVector via Spring AI and Ollama; re-ingestion runs on every ticket change and on startup when the store is empty.

Stack additions: Spring AI **1.1.8** (not 2.x), `spring-ai-starter-vector-store-pgvector`, `spring-ai-starter-model-ollama`, Flyway V3 for the `vector` extension, `app.rag` retrieval settings (`top-k=5`, `similarity-threshold=0.6`), and an Ask AI React page.

## Technical Context

**Language/Version**: Java 21; TypeScript on the existing Vite React toolchain

**Primary Dependencies**: Spring Boot 3.5.16; Spring AI BOM 1.1.8; `spring-ai-starter-vector-store-pgvector`; `spring-ai-starter-model-ollama`; existing Spring Web, Validation, Data JPA, Flyway, Testcontainers

**Storage**: PostgreSQL 16 (`pgvector/pgvector:pg16`). Ticket tables unchanged (V1 schema, V2 seed). Flyway V3 enables PG extensions. Spring AI PGVector `vector_store` table (768-dim embeddings, cosine distance, HNSW index).

**Models**: Ollama `nomic-embed-text` (768 dims) for embeddings; Ollama `llama3.1:8b` at temperature `0` for chat. Models must be pulled locally before running the app or `ai-eval` tests.

**Testing**: JUnit 5. Stubbed `ChatModel` / `VectorStore` unit tests for ingestion and ask logic. `app.rag.indexing.enabled=false` in `backend/src/test/resources/application.yml` (`@ConditionalOnProperty` on `TicketIndexer` and `VectorStoreBackfill`). `@SpringBootTest` contexts that need model beans use `@MockitoBean EmbeddingModel` and `@MockitoBean ChatModel`. Default `mvn test` passes with Ollama stopped. `@Tag("ai-eval")` class for five in-scope PDF questions and five out-of-scope questions (O1–O5), excluded from default `mvn test`.

**Target Platform**: Local developer machine. API 8080, UI 5173, Ollama 11434, Postgres via Compose.

**Project Type**: Web application (backend API + frontend).

**Performance Goals**: Grounded answers for in-scope questions in under 10 seconds (SC-001). Re-ingestion visible within one async cycle (~30 s local).

**Constraints**: Single retrieve-then-generate flow; no agentic actions. Empty retrieval must not call the LLM. An answer that cites no retrieved id, or cites an id that was not retrieved, returns the no-match body with `grounded: false`. `topK` and similarity threshold in `application.yml` under `app.rag`, not hardcoded. `0.6` is the starting threshold; DEBUG logs of retrieval scores feed calibration in `specs/evaluation-strategy.md`. `spring.jpa.open-in-view=false`. Ticket state machine and existing REST endpoints unchanged.

**Scale/Scope**: ~15 seeded tickets plus user-created tickets. One new API endpoint (`POST /api/ai/ask`). One new UI page (Ask AI). Three implementation user stories.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Gate | Pre-design | Post-design |
|------|------------|-------------|
| I. Spec-first | Pass. Spec at `specs/002-ticket-rag-assistant/spec.md`; requirements trace to `specs/requirements.md` RAG items. | Pass. Design artifacts in this feature folder and top-level `specs/rag-*.md`. |
| II. Backend is source of truth | Pass. Ask validation and grounding enforced server-side. Ticket SM untouched. | Pass. No client-side grounding bypass. |
| III. Test-first for deterministic logic | Pass. Unit tests for no-match path, citation filter, re-ingest delete+add planned before feature complete. Phase 1 SM tests remain. | Pass. Test matrix in [quickstart.md](quickstart.md) and [../test-strategy.md](../test-strategy.md). |
| IV. Grounded AI | Pass. Retrieve → generate only when chunks exist; explicit no-match; cite ticket IDs; static system prompt. | Pass. Uncited or mis-cited answers become the no-match response. Documented in [research.md](research.md) and [../rag-api-contract.md](../rag-api-contract.md). |
| V. Configuration over constants | Pass. `app.rag.top-k`, `app.rag.similarity-threshold`, model names in YAML. | Pass. Spring AI Ollama/PGVector properties externalized per research.md. |
| VI. No secrets in git | Pass. Ollama is local; no API keys. | Pass. Unchanged. |
| VII. Atomic commits | Pass. Three user-story slices below. | Pass. Unchanged. |
| Technology stack | Pass. Java 21, Boot 3.5, Spring AI 1.1.x, PGVector, Ollama, React. | Pass. Spring AI 2.x explicitly excluded. |

No gate failed.

## Project Structure

### Documentation (this feature)

```text
specs/002-ticket-rag-assistant/
├── plan.md              # This file
├── research.md          # Spring AI version, starters, properties (with doc URLs)
├── data-model.md        # Vector chunks, events, ask DTOs
├── quickstart.md        # Ollama + backfill + ask validation
├── contracts/
│   └── ai.openapi.yaml  # POST /api/ai/ask
└── tasks.md             # /speckit-tasks — not created here

specs/
├── architecture.md      # RAG pipeline, chunking, embedding choice, prompt caching
├── rag-ingestion.md
├── rag-api-contract.md
├── evaluation-strategy.md
└── test-strategy.md     # AI section updated
```

### Source Code (repository root)

```text
docker-compose.yml
backend/
├── pom.xml                          # spring-ai-bom 1.1.8, starters
└── src/
    ├── main/java/com/ticketmanagement/
    │   ├── ticket/
    │   │   ├── TicketService.java   # publish TicketChangedEvent
    │   │   └── event/
    │   │       └── TicketChangedEvent.java
    │   ├── comment/
    │   │   └── CommentService.java  # publish TicketChangedEvent
    │   ├── TicketManagementApplication.java  # @EnableAsync
    │   └── rag/
    │       ├── RagProperties.java           # app.rag.* including indexing.enabled
    │       ├── TicketDocumentMapper.java    # header line + paragraph chunks
    │       ├── TicketIndexer.java           # AFTER_COMMIT, @Async, read-only reload
    │       ├── VectorStoreBackfill.java     # startup when store empty
    │       ├── RagAskService.java           # retrieve → optional generate
    │       ├── RagController.java           # POST /api/ai/ask
    │       └── dto/
    │           ├── AskRequest.java
    │           └── AskResponse.java
    ├── main/resources/
    │   ├── application.yml          # app.rag, spring.ai.*, open-in-view=false
    │   └── db/migration/
    │       └── V3__enable_pgvector.sql
    └── test/
        ├── resources/application.yml        # app.rag.indexing.enabled: false
        └── java/com/ticketmanagement/rag/
            ├── TicketDocumentMapperTest.java
            ├── TicketIndexerTest.java
            ├── RagAskServiceTest.java       # stubbed models; both citation failures
            └── RagAskEvalTest.java          # @Tag("ai-eval")
frontend/
└── src/
    ├── api/ai.ts
    ├── pages/AskAiPage.tsx
    └── App.tsx                      # /ask route + nav link
```

**Structure Decision**: New `rag` feature package alongside `ticket`, `comment`, and `common`. Domain events live under `ticket/event` and are published from existing services without changing HTTP contracts. Frontend adds one page and one API module.

### Implementation slices (3 user stories)

| Order | User story | Commit contains |
|-------|------------|-----------------|
| 1 | **Ingestion and re-index** | Spring AI deps, `application.yml`, test `application.yml` with `app.rag.indexing.enabled=false`, `@EnableAsync`, Flyway V3, `RagProperties`, `TicketDocumentMapper` (header line; `search_document: ` only on the embedding input), `TicketChangedEvent`, event publication, `TicketIndexer` (`@ConditionalOnProperty`, `@Async`, `@Transactional(readOnly = true)` reload), `VectorStoreBackfill` (`@ConditionalOnProperty`), mapper/indexer unit tests. `@MockitoBean` for `EmbeddingModel` and `ChatModel` on full-context tests that need them. |
| 2 | **Ask endpoint** | `RagAskService` (`search_query: ` on the query embedding; DEBUG score logs), `RagController`, DTOs, static system prompt, context blocks with the same header, sources from retrieved chunks filtered to cited ids, no-match when the answer cites nothing retrieved or cites an id that was not retrieved. `RagAskServiceTest` covers empty retrieval (no LLM call) and both citation failures. OpenAPI contract. |
| 3 | **Ask AI UI** | `AskAiPage`, `api/ai.ts`, route and nav, no-match and source links to ticket detail. Manual quickstart checks. |

## Complexity Tracking

No constitution violations.
