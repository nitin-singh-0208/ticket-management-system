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

Phase 2 adds a grounded assistant over ticket history. Detail: [002-ticket-rag-assistant/plan.md](002-ticket-rag-assistant/plan.md), [rag-ingestion.md](rag-ingestion.md), [rag-api-contract.md](rag-api-contract.md).

```text
┌─────────────┐     TicketChangedEvent      ┌──────────────────┐
│ TicketService│ ─────────────────────────► │  TicketIndexer   │
│CommentService│   (AFTER_COMMIT, @Async)    │  delete + add    │
└─────────────┘                              └────────┬─────────┘
                                                      │
┌─────────────┐     startup if store empty            ▼
│  All tickets │ ◄────────────────────────── TicketDocumentMapper
└─────────────┘                              (paragraph chunks + metadata)
                                                      │
                                                      ▼
                                            Ollama nomic-embed-text
                                                      │
                                                      ▼
                                            PGVector vector_store

User question ──► POST /api/ai/ask ──► similaritySearch(topK, threshold)
                                              │
                         empty ◄──────────────┴──────────────► chunks
                           │                                      │
                           ▼                                      ▼
              "No relevant tickets found"              static system prompt
              (no LLM call)                            + [TKT-xxxx] context
                                                       + llama3.1:8b (temp 0)
                                                              │
                                                              ▼
                                                    answer + cited sources
```

| Piece | Choice |
|-------|--------|
| Framework | Spring AI **1.1.8** BOM (1.x line; not 2.x / Boot 4) |
| Vector store | `spring-ai-starter-vector-store-pgvector`; Flyway V3 enables `vector` extension |
| Models | Ollama `nomic-embed-text` (768 dims), `llama3.1:8b` (temperature 0) |
| Retrieval config | `app.rag.top-k=5`, `app.rag.similarity-threshold=0.6` |
| Flow | Single retrieve → generate; no agentic actions |
| Re-ingest | Delete all chunks for `ticketId`, then index current ticket content |

## Chunking strategy

Ticket text is split **paragraph-based**, not fixed-size or semantic.

| Approach | Fit for ticket data | Decision |
|----------|---------------------|----------|
| **Paragraph-based** | Description, comments, and resolution notes are separate sections; paragraphs are natural semantic units. | **Chosen.** One knowledge document per section; split on paragraph boundaries; max ~800 tokens per chunk; ~100 token overlap only when one section must fragment. |
| Fixed-size | Splits mid-sentence across comment boundaries; loses section context. | Rejected for primary strategy. |
| Semantic | Extra embedding calls per split; overkill for ~15–50 tickets. | Rejected. |

Implementation: `TicketDocumentMapper` per [rag-ingestion.md](rag-ingestion.md) and `.cursor/rules/rag-vector-store.mdc`.

## Embedding model choice

| Option | Cost | Latency | Quality | Privacy | Decision |
|--------|------|---------|---------|---------|----------|
| **Ollama `nomic-embed-text` (local)** | Free after model download | Low on localhost (~ms–low s per batch) | Good for short support text and keyword overlap | Ticket text never leaves the machine | **Chosen** |
| Cloud (e.g. OpenAI embeddings) | Per-token API cost | Network round-trip | Often stronger on noisy/long corpora | Data sent to third party | Rejected for this assignment |

768 dimensions fit PGVector HNSW limits and match `nomic-embed-text` output. Chat uses local `llama3.1:8b` at temperature 0 for deterministic grounded answers.

## Prompt caching

The assistant system prompt (role, grounding rules, citation requirements) is a **constant prefix** — a `static final String` prepended unchanged to every `Prompt`. Only the retrieved `[TKT-xxxx]` context blocks and the user question vary per request.

This satisfies NFR-10: providers that cache stable prompt prefixes avoid re-processing unchanged instructions; with local Ollama it keeps prompts testable and consistent. Do not embed dynamic ticket metadata in the system prompt.
