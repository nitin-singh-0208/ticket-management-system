# RAG ingestion

How ticket rows become searchable vector chunks and stay current. Implementation package: `com.ticketmanagement.rag`. Rules align with `.cursor/rules/rag-vector-store.mdc`.

## Pipeline

```text
Ticket row + comments
        │
        ▼
TicketDocumentMapper  ──► List<Document> (one section per knowledge document)
        │
        ▼
Paragraph chunker     ──► ≤ ~800 tokens/chunk; ~100 token overlap if section splits
        │
        ▼
EmbeddingModel        ──► "search_document: " + chunk text
                          Ollama nomic-embed-text (768 dims)
        │
        ▼
VectorStore (PGVector) ──► vector_store table
```

## Knowledge documents

| sourceType | Source | Notes |
|------------|--------|--------|
| `description` | `tickets.description` | Always indexed. |
| `comment` | Each `comments.text` | One document per comment, oldest to newest. |
| `resolution` | `tickets.resolution_notes` | Skipped when null or blank. |

## Metadata (every chunk)

`ticketId`, `status`, `priority`, `assignee`, `category`, `sourceType`.

Metadata is copied from the ticket row at index time so filters and source display stay consistent with the relational record.

## Chunk text

Every chunk’s stored content starts with this header, then the section text:

```text
Ticket {ticketId} | {title} | status {status} | priority {priority} | category {category} | section {sourceType}
{section text}
```

The header is part of the embedding input (after the `search_document: ` prefix) and is the first line of each LLM context block. `assignee` stays in metadata only.

## nomic-embed-text prefixes

Ollama does not add task prefixes. The [nomic-embed-text-v1.5 model card](https://huggingface.co/nomic-ai/nomic-embed-text-v1.5) requires them for RAG: `search_document: ` on indexed text and `search_query: ` on questions. Apply those prefixes only on the string sent to the embedding model. Do not store them in `vector_store.content` and do not send them to the chat model. See [002-ticket-rag-assistant/research.md](002-ticket-rag-assistant/research.md).

## Chunking

Paragraph-based splitting of the section text (the header is prepended to each resulting chunk):

1. Split section text on paragraph boundaries (blank-line separated).
2. Accumulate paragraphs into a chunk until ~800 tokens, counting the header.
3. If one paragraph exceeds the limit, split with ~100 token overlap between fragments.
4. Do not merge unrelated sections (description vs comment vs resolution).

Rationale: ticket content is already structured by section; paragraph boundaries preserve meaning better than fixed token windows. The header keeps id, title, status, and priority visible to both the embedding model and the LLM.

## Triggers

| Event | Publisher | Handler |
|-------|-----------|---------|
| Ticket created | `TicketService.createTicket` | `TicketIndexer` |
| Ticket fields updated | `TicketService.updateTicket` | `TicketIndexer` |
| Status changed | `TicketService.changeStatus` | `TicketIndexer` |
| Comment added | `CommentService.addComment` | `TicketIndexer` |

`TicketChangedEvent` carries `ticketId`. Handler runs `@TransactionalEventListener(phase = AFTER_COMMIT)` and `@Async`. `@EnableAsync` is configured on the application. The listener reloads the ticket and its comments in `@Transactional(readOnly = true)`.

`TicketIndexer` and `VectorStoreBackfill` are `@ConditionalOnProperty(name = "app.rag.indexing.enabled", havingValue = "true", matchIfMissing = true)`. Tests set `app.rag.indexing.enabled: false` in `backend/src/test/resources/application.yml`.

## Re-ingest algorithm

For each event (and each ticket during backfill):

1. `vectorStore.delete(filter: ticketId == event.ticketId)` (or equivalent metadata filter).
2. Load ticket + comments from JPA inside the read-only transaction.
3. `TicketDocumentMapper.toDocuments(ticket, comments)` (header line on every chunk).
4. Embed each chunk as `search_document: ` plus stored content, then `vectorStore.add(documents)`.

Delete-then-add prevents stale chunks when text or metadata changes.

## Startup backfill

`VectorStoreBackfill` (`ApplicationRunner`):

- Count documents in `vector_store` (or call `similaritySearch` with a probe).
- If count is zero, load all tickets and run the same per-ticket index path.
- If any chunks exist, skip (no full re-index on every restart).

## Configuration

| Setting | Location | Default |
|---------|----------|---------|
| Embedding model | `spring.ai.ollama.embedding.options.model` | `nomic-embed-text` |
| Indexing switch | `app.rag.indexing.enabled` | `true` (app), `false` (tests) |
| Vector dimensions | `spring.ai.vectorstore.pgvector.dimensions` | `768` |
| PG extensions | Flyway `V3__enable_pgvector.sql` | `CREATE EXTENSION IF NOT EXISTS vector` |
| Table init | `spring.ai.vectorstore.pgvector.initialize-schema` | `true` |

## Failure behavior

- Indexing errors are logged; they do not roll back the ticket transaction (event is already committed).
- A failed async index leaves the ticket unsearchable until the next change event or manual restart backfill (if store still empty).
