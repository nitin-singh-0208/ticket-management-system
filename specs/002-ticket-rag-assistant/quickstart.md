# Quickstart: Ticket RAG Assistant

Validates Phase 2 RAG against [spec.md](spec.md). Contracts: [contracts/ai.openapi.yaml](contracts/ai.openapi.yaml) and [../rag-api-contract.md](../rag-api-contract.md). Ingestion rules: [../rag-ingestion.md](../rag-ingestion.md).

## Prerequisites

- Java 21, Node.js with npm, Docker
- [Ollama](https://ollama.com/) running locally on port 11434
- Models pulled:

```bash
ollama pull nomic-embed-text
ollama pull llama3.1:8b
```

From the repository root:

```bash
docker compose up -d
```

## Backend

```bash
cd backend
mvn test
mvn spring-boot:run
```

`mvn test` runs Phase 1 tests plus RAG unit tests. It does **not** run `@Tag("ai-eval")` tests, and it must pass with Ollama stopped.

Test isolation:

- `backend/src/test/resources/application.yml` sets `app.rag.indexing.enabled: false`, so `TicketIndexer` and `VectorStoreBackfill` are not created (`@ConditionalOnProperty`).
- `@SpringBootTest` classes that load model beans declare `@MockitoBean EmbeddingModel` and `@MockitoBean ChatModel`.

On first startup with seeded tickets, backfill indexes all tickets into PGVector (watch logs for indexer/backfill). Each stored chunk starts with `Ticket {ticketId} | {title} | status … | priority … | category … | section …`. Embeddings use the `search_document: ` prefix; queries use `search_query: `. Flyway applies V1 schema, V2 seed, and V3 `vector` extension.

Expected `application.yml` highlights:

- `spring.jpa.open-in-view=false`
- `app.rag.top-k=5`, `app.rag.similarity-threshold=0.6` (starting value)
- `app.rag.indexing.enabled=true` in the running app
- `spring.ai.vectorstore.pgvector.dimensions=768`
- `spring.ai.ollama.embedding.options.model=nomic-embed-text`
- `spring.ai.ollama.chat.options.model=llama3.1:8b`
- `spring.ai.ollama.chat.options.temperature=0`

Set `logging.level.com.ticketmanagement.rag=DEBUG` while calibrating. Each retrieval logs ticket id and score. After the eval run, update the threshold and [../evaluation-strategy.md](../evaluation-strategy.md).

## Ask API (curl)

```bash
curl -s -X POST http://localhost:8080/api/ai/ask \
  -H 'Content-Type: application/json' \
  -d '{"question":"Have we seen payment failures before?"}' | jq
```

| Check | Question | Expected |
|-------|----------|----------|
| Payment history | Have we seen payment failures before? | `grounded: true`; sources include payment tickets (e.g. TKT-1001, TKT-1002). |
| Specific ticket | What was the resolution for ticket TKT-1001? | `grounded: true` if indexed content matches; cites TKT-1001. (TKT-1001 has no resolution notes — answer may use description/comment.) |
| Shipment causes | What are the common causes of shipment tracking issues? | `grounded: true`; sources from Shipment category (TKT-1006–TKT-1010). |
| Resolved similar | Show me similar resolved tickets. | `grounded: true`; cited tickets have status RESOLVED or CLOSED. |
| High-priority payment | Which high-priority tickets are related to payment? | `grounded: true`; sources are HIGH priority Payment tickets (TKT-1001, TKT-1002). |
| No match | What is the weather in Paris today? | `grounded: false`, `answer` exactly `No relevant tickets found`, `sources: []`. |
| No match | What is the capital of France? | Same no-match body. The answer must not be "Paris". |
| Missing ticket | What was the resolution for TKT-9999? | Same no-match body. Must not invent a resolution. |
| Blank question | `{"question":"   "}` | HTTP 400 validation error. |

## Re-ingestion

1. Note an answer to a payment question.
2. Update TKT-1001 description via `PATCH /api/tickets/TKT-1001` with new unique phrase.
3. Wait a few seconds for async indexing.
4. Ask a question that should match only the new phrase.
5. Confirm the answer reflects the update.

## Frontend

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173/ask`. Submit a question, verify answer and clickable sources. Submit an out-of-scope question and confirm the no-match message.

## AI evaluation suite (optional, requires Ollama)

```bash
cd backend
mvn test -Dgroups=ai-eval
```

Runs [../evaluation-strategy.md](../evaluation-strategy.md) scenarios against live models. Excluded from default CI.

## Regression

Phase 1 quickstart checks in [../001-support-ticket-management/quickstart.md](../001-support-ticket-management/quickstart.md) must still pass after RAG is enabled.
