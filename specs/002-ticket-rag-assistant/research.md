# Research: Ticket RAG Assistant

## Spring AI version and Spring Boot compatibility

- **Decision**: Use **Spring AI 1.1.8** (`spring-ai-bom:1.1.8`) on the **1.1.x** line. Do **not** use Spring AI 2.x (targets Spring Boot 4).
- **Rationale**: The project parent is Spring Boot 3.5.16. Spring AI documents Boot 3.4.x and 3.5.x support on the 1.x reference docs. The Spring AI repository compatibility matrix maps **1.1.x → Spring Boot 3.5.x** and **2.x → Spring Boot 4.x**. Maven Central lists `spring-ai-bom` 1.1.8 as the latest 1.1.x release (June 2026).
- **Alternatives considered**: Spring AI 1.0.x (also documents Boot 3.5 support but 1.1.x is the maintained 1.x line for Boot 3.5); Spring AI 2.0.x (rejected — Boot 4 only).
- **Sources**:
  - [Getting Started (Spring AI 1.1)](https://docs.spring.io/spring-ai/reference/1.1/getting-started.html) — "Spring AI supports Spring Boot 3.4.x and 3.5.x"; BOM import pattern.
  - [spring-projects/spring-ai README — Spring Boot Version Compatibility](https://github.com/spring-projects/spring-ai#spring-boot-version-compatibility) — 1.1.x ↔ 3.5.x, 2.x ↔ 4.x.
  - [Maven Central: spring-ai-bom](https://repo1.maven.org/maven2/org/springframework/ai/spring-ai-bom/) — 1.1.8 latest in 1.1.x line.

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>org.springframework.ai</groupId>
      <artifactId>spring-ai-bom</artifactId>
      <version>1.1.8</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

## Starter artifacts

- **Decision**:
  - `org.springframework.ai:spring-ai-starter-vector-store-pgvector` — PGVector `VectorStore`
  - `org.springframework.ai:spring-ai-starter-model-ollama` — Ollama `ChatModel` and `EmbeddingModel` (single starter covers both)
- **Rationale**: Official 1.1 reference docs name these starter artifact IDs. Starter modules replaced older non-starter module names in Spring AI 1.1 auto-configuration.
- **Alternatives considered**: Manual `spring-ai-pgvector-store` + `spring-ai-ollama` wiring without starters (more boilerplate, same runtime).
- **Sources**:
  - [PGvector Vector Store (Spring AI 1.1)](https://docs.spring.io/spring-ai/reference/1.1/api/vectordbs/pgvector.html) — `spring-ai-starter-vector-store-pgvector`.
  - [Ollama Chat (Spring AI 1.1)](https://docs.spring.io/spring-ai/reference/1.1/api/chat/ollama-chat.html) — `spring-ai-starter-model-ollama`.
  - [Ollama Embeddings (Spring AI 1.1)](https://docs.spring.io/spring-ai/reference/1.1/api/embeddings/ollama-embeddings.html) — same Ollama starter for embeddings.

## Configuration properties (verified)

### PGVector

| Property | Purpose | Project value |
|----------|---------|---------------|
| `spring.ai.vectorstore.pgvector.dimensions` | Embedding column width | `768` (nomic-embed-text) |
| `spring.ai.vectorstore.pgvector.distance-type` | Similarity metric | `COSINE_DISTANCE` (default) |
| `spring.ai.vectorstore.pgvector.index-type` | ANN index | `HNSW` (default) |
| `spring.ai.vectorstore.pgvector.initialize-schema` | Create `vector_store` table | `true` (schema init is opt-in since 1.1) |

Source: [PGvector configuration properties](https://docs.spring.io/spring-ai/reference/1.1/api/vectordbs/pgvector.html#configuration-properties).

### Ollama chat

| Property | Purpose | Project value |
|----------|---------|---------------|
| `spring.ai.ollama.base-url` | Ollama server | `http://localhost:11434` |
| `spring.ai.model.chat` | Enable chat auto-config | `ollama` |
| `spring.ai.ollama.chat.options.model` | Chat model name | `llama3.1:8b` |
| `spring.ai.ollama.chat.options.temperature` | Sampling temperature | `0` |

Source: [Ollama Chat — Base Properties and Chat Properties](https://docs.spring.io/spring-ai/reference/1.1/api/chat/ollama-chat.html).

### Ollama embeddings

| Property | Purpose | Project value |
|----------|---------|---------------|
| `spring.ai.model.embedding` | Enable embedding auto-config | `ollama` |
| `spring.ai.ollama.embedding.options.model` | Embedding model name | `nomic-embed-text` |

Source: [Ollama Embeddings — Embedding Properties](https://docs.spring.io/spring-ai/reference/1.1/api/embeddings/ollama-embeddings.html).

### Application RAG settings (project-specific, not Spring AI)

| Property | Purpose | Project value |
|----------|---------|---------------|
| `app.rag.top-k` | Retrieval `topK` | `5` |
| `app.rag.similarity-threshold` | Minimum similarity score | `0.6` (starting value; calibrate from eval — see below) |
| `app.rag.indexing.enabled` | Register `TicketIndexer` and `VectorStoreBackfill` | `true` in the running app; `false` in `backend/src/test/resources/application.yml` |
| `app.rag.chat-model` | Mirror of chat model name for docs/tests | `llama3.1:8b` |
| `app.rag.embedding-model` | Mirror of embedding model name | `nomic-embed-text` |

Retrieval uses `VectorStore.similaritySearch(SearchRequest.builder().query(...).topK(...).similarityThreshold(...).build())` — threshold and topK come from `app.rag`, not hardcoded. Source: [PGvector similarity search example](https://docs.spring.io/spring-ai/reference/1.1/api/vectordbs/pgvector.html).

### Other Spring settings

- `spring.jpa.open-in-view=false` — disable OSIV for API-only backend.
- Flyway `V3__enable_pgvector.sql` in `db/migration` runs `CREATE EXTENSION IF NOT EXISTS vector` (and `uuid-ossp` / `hstore` if the PGVector starter requires them) before the vector store table is created. Version numbers are shared with `db/seed`; V1 schema, V2 seed, V3 vector extension.

## Models

- **Decision**: Embeddings via Ollama `nomic-embed-text` (768 dimensions). Chat via Ollama `llama3.1:8b` at temperature `0`.
- **Rationale**: Constitution and assignment require local Ollama models. `nomic-embed-text` is a common local embedding model at 768 dims, within PGVector HNSW limits (≤ 2000 dims per Spring AI PGVector docs). Temperature `0` maximizes determinism for grounded answers and eval repeatability.
- **Alternatives considered**: Cloud OpenAI embeddings/chat (cost, privacy, network dependency); larger local models (latency, RAM).
- **Prerequisite**: `ollama pull nomic-embed-text` and `ollama pull llama3.1:8b` before running the app or `@Tag("ai-eval")` tests.

## nomic-embed-text task prefixes

- **Decision**: Prefixes not used: PgVectorStore embeds Document text internally, so asymmetric prefixes would need a custom EmbeddingModel wrapper; deferred, retrieval quality checked via threshold calibration.
- **Rationale**: The model card recommends `search_document: ` and `search_query: ` prefixes, but `PgVectorStore` embeds `Document` text internally, so those asymmetric prefixes cannot be applied without a custom `EmbeddingModel` wrapper. Store and query text are embedded as-is; retrieval quality is checked later via threshold calibration.
- **Alternatives considered**: A custom `EmbeddingModel` wrapper that prepends the nomic task prefixes. Deferred.
- **Sources**:
  - [nomic-ai/nomic-embed-text-v1.5 model card](https://huggingface.co/nomic-ai/nomic-embed-text-v1.5) — "the text prompt must include a task instruction prefix"; RAG example uses `search_document: ` and `search_query: `.
  - [Ollama library: nomic-embed-text](https://ollama.com/library/nomic-embed-text) — examples pass the text with no prefix; no prefix is documented.
  - [Nomic Embed Text API](https://docs.nomic.ai/reference/api/embed-text-v-1-embedding-text-post) — `task_type` of `search_document` or `search_query`; "Using `nomic-embed-text` with other libraries requires you to use a prefix."

## Similarity threshold

- **Decision**: Ship `app.rag.similarity-threshold=0.6` as a starting value. `RagAskService` logs each retrieved chunk’s `ticketId` and score at DEBUG. After the first `@Tag("ai-eval")` run, replace 0.6 with the calibrated value and record the reasoning in [evaluation-strategy.md](../evaluation-strategy.md).
- **Rationale**: 0.6 is a reasonable cosine cutoff before real score distributions are known. Calibrating from logged scores avoids locking in a guess that drops in-scope tickets or lets out-of-scope questions through.

## Ingestion and re-ingestion

- **Decision**: `TicketDocumentMapper` maps each ticket into `Document` chunks (one section per knowledge document: description, each comment, resolution notes). Every chunk’s stored content starts with the header line `Ticket {ticketId} | {title} | status {status} | priority {priority} | category {category} | section {sourceType}`, then the section text, so id, title, status, and priority are embedded. Paragraph-based splitting per `.cursor/rules/rag-vector-store.mdc` (~800 token max, ~100 token overlap only when a section must split). Metadata on every chunk: `ticketId`, `status`, `priority`, `assignee`, `category`, `sourceType` (`description` | `comment` | `resolution`). `TicketIndexer` listens to `TicketChangedEvent` via `@TransactionalEventListener(phase = AFTER_COMMIT)` and `@Async`. The reload of the ticket and its comments runs in `@Transactional(readOnly = true)`. `@EnableAsync` is configured so the listener actually runs off the request thread. Handler deletes all chunks for `ticketId`, then adds fresh documents. Both `TicketIndexer` and `VectorStoreBackfill` are `@ConditionalOnProperty(name = "app.rag.indexing.enabled", havingValue = "true", matchIfMissing = true)`. `TicketService.createTicket`, `updateTicket`, `changeStatus`, and `CommentService.addComment` publish the event after successful commit. Startup `ApplicationRunner` backfills all tickets when the vector store has zero documents.
- **Rationale**: The header puts identity fields in the embedding and in the text the LLM reads, so a question about status or priority can match even when those words are absent from the section body. Delete-then-add guarantees no stale chunks (RAG-05, constitution). AFTER_COMMIT avoids indexing uncommitted data. A read-only transaction on the async thread loads the committed row without holding a write lock. Async keeps ticket API latency unchanged. The property switch keeps Phase 1 tests from indexing when Ollama is stopped.
- **Alternatives considered**: Incremental chunk diffing (complex, error-prone); synchronous indexing on request thread (slow writes); metadata-only identity fields (invisible to the embedding model and to the LLM).

## Ask flow

- **Decision**: `POST /api/ai/ask` embeds the question as `search_query: ` plus the question, then `similaritySearch` with configured `topK` and `similarityThreshold`. Log each hit’s ticket id and score at DEBUG. If results are empty, return `{ answer: "No relevant tickets found", sources: [], grounded: false }` **without** calling `ChatModel`. Otherwise build the prompt: static system instructions (constant prefix for prompt caching per NFR-10) + user question + context blocks that use the same chunk header line as stored content. After generation, build `sources` from retrieved chunks only: deduplicate by `ticketId` (keep the highest score), then keep only ids cited in the answer. If the answer cites no retrieved id, or cites any id that was not retrieved, return the no-match body with `grounded: false` instead of an answer the sources cannot support.
- **Rationale**: Constitution principle IV (Grounded AI). Skipping the LLM on empty retrieval prevents fabrication. Replacing an uncited or mis-cited answer with the no-match message prevents a plausible answer that points at the wrong ticket.
- **Alternatives considered**: Dropping unretrieved ids and still returning `grounded: true` (leaves an answer that named a ticket the retrieval never saw); agentic multi-step flows (out of scope per RAG-13).

## Testing strategy

- **Decision**: Unit tests with stubbed `ChatModel` and `VectorStore` for deterministic paths: empty retrieval skips the LLM; an answer that cites no retrieved id returns no-match; an answer that cites an id that was not retrieved returns no-match; re-ingest does delete then add. Test isolation is concrete: `app.rag.indexing.enabled=false` in `backend/src/test/resources/application.yml`, and `@ConditionalOnProperty` on `TicketIndexer` and `VectorStoreBackfill`. Full application contexts that still need the model beans declare `@MockitoBean EmbeddingModel` and `@MockitoBean ChatModel`. Default `mvn test` must pass with Ollama stopped. One `@Tag("ai-eval")` class with the five PDF questions and five out-of-scope questions (O1–O5), excluded from the default build via Surefire `excludedGroups`.
- **Rationale**: Disabling indexing stops startup backfill and event listeners from calling Ollama during Phase 1 tests. Mockito beans satisfy Spring AI auto-configuration without a live server. The eval suite validates real models on demand.
- **Alternatives considered**: `@Profile` exclusion of every AI auto-config class (brittle across Spring AI upgrades); Testcontainers Ollama in every build (slow, flaky on CI).

## Chunking strategy (summary)

- **Decision**: Paragraph-based chunking for ticket sections.
- **Rationale**: Ticket text is naturally segmented (description, comments, resolution). Paragraph boundaries preserve semantic units better than fixed token windows. Fixed-size splitting can cut sentences mid-thought. Semantic splitting adds embedding cost and complexity without benefit at this scale (~15 seeded tickets).
- **Detail**: See [architecture.md](../architecture.md#chunking-strategy) and [rag-ingestion.md](../rag-ingestion.md).

## Embedding model choice (summary)

- **Decision**: Local Ollama `nomic-embed-text`.
- **Rationale**: Zero per-token cloud cost; low latency on localhost; ticket text never leaves the machine (privacy); quality sufficient for support-ticket similarity at this volume. Cloud models (e.g. OpenAI `text-embedding-3-small`) offer slightly better retrieval on noisy corpora but add API cost, network dependency, and data egress.
- **Detail**: See [architecture.md](../architecture.md#embedding-model-choice).

## Prompt caching

- **Decision**: Keep the full system prompt (role, grounding rules, citation format) as a `static final String` constant prepended unchanged to every `Prompt`. Only the retrieved context block and user question vary per request.
- **Rationale**: NFR-10 and `.cursor/rules/rag-vector-store.mdc`. Stable prefix allows providers that support prompt caching to cache the instruction block; even with local Ollama, a constant prefix keeps behavior predictable and testable.
