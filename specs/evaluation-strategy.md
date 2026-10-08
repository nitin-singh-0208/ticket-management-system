# Evaluation strategy

How to measure RAG quality for the ticket assistant. Automated eval lives in `RagAskEvalTest` (`@Tag("ai-eval")`), excluded from default `mvn test`. Requires Ollama with `nomic-embed-text` and `llama3.1:8b` and a running Postgres with seeded tickets.

## In-scope questions (from assignment PDF)

Expected source tickets are derived from seed data in `backend/src/main/resources/db/seed/V2__seed_tickets.sql`. Scores are indicative; exact ranking varies with embeddings.

| # | Question | Expected source tickets | Pass criteria |
|---|----------|-------------------------|---------------|
| Q1 | Have we seen payment failures before? | TKT-1001, TKT-1002, TKT-1003, TKT-1004, TKT-1005 (Payment category; at least TKT-1001, TKT-1002 with "payment failure" / failure language) | `grounded: true`; ≥1 Payment ticket in `sources`; answer mentions payment failures |
| Q2 | What was the resolution for ticket TKT-1001? | TKT-1001 (description/comment only — no resolution notes on seed row) | `grounded: true`; TKT-1001 in `sources`; answer does not invent resolution text absent from indexed content |
| Q3 | What are the common causes of shipment tracking issues? | TKT-1006, TKT-1007, TKT-1008, TKT-1009, TKT-1010 | `grounded: true`; ≥1 Shipment ticket in `sources`; answer references tracking/shipment causes from retrieved snippets |
| Q4 | Show me similar resolved tickets. | TKT-1003, TKT-1008, TKT-1013 (RESOLVED); may also surface CLOSED (TKT-1004, TKT-1009, TKT-1014) | `grounded: true`; cited tickets have status RESOLVED or CLOSED |
| Q5 | Which high-priority tickets are related to payment? | TKT-1001, TKT-1002 (HIGH + Payment) | `grounded: true`; sources ⊆ {TKT-1001, TKT-1002, TKT-1003, TKT-1004, TKT-1005} and include only HIGH priority payment-related rows |

## Out-of-scope questions (no-match)

| # | Question | Expected behavior |
|---|----------|-------------------|
| O1 | What is the weather in Paris today? | `grounded: false`, `answer` = `No relevant tickets found`, `sources: []`. No LLM call when retrieval is empty. |
| O2 | What is the stock price of Apple? | Same as O1 |
| O3 | How do I bake sourdough bread? | Same as O1 |
| O4 | What is the capital of France? | Same no-match body. The answer must not be "Paris" or any other capital invented from general knowledge. |
| O5 | What was the resolution for TKT-9999? | Same no-match body. TKT-9999 is not in the seed. The answer must not invent a resolution or cite a different ticket as if it were TKT-9999. |

## Metrics

| Metric | Definition | Target |
|--------|------------|--------|
| **Retrieval hit rate** | For Q1–Q5, fraction where ≥1 expected ticket id appears in retrieval results (before generation) | ≥ 80% per question over 3 runs |
| **Citation precision** | For grounded responses, fraction of `sources[].ticketId` that were both retrieved and cited in the answer | 100%. An answer that cites no retrieved id, or cites an id that was not retrieved, is a no-match (`grounded: false`), not a trimmed answer. |
| **No-match correctness** | For O1–O5, fraction where `grounded` is false, `sources` is empty, and `answer` is exactly `No relevant tickets found` | 100% |
| **Grounded answer rate** | For Q1–Q5, fraction with `grounded: true` | 100% when Ollama and index are healthy |
| **Hallucination rate** | Manual/automated check: answer facts not present in any retrieved snippet | 0% in acceptance |

## Similarity threshold

| | |
|--|--|
| Starting value | `0.6` in `app.rag.similarity-threshold` |
| Calibrated value | Not yet measured. The first `@Tag("ai-eval")` run has not been executed, so `0.6` stays in `application.yml` until that run. |
| How to calibrate | Set `logging.level.com.ticketmanagement.rag=DEBUG`. Each retrieval logs `ticketId` and score. Run `mvn test -Dgroups=ai-eval` against the seeded index. |

After that run, replace the row above with the chosen number and the reasoning, using the logged scores:

- Lower the threshold only if an in-scope question (Q1–Q5) misses an expected ticket whose score sits just under `0.6`.
- Raise the threshold if an out-of-scope question (O1–O5) retrieves chunks and the model then produces a grounded answer.
- Keep `0.6` when in-scope questions stay grounded and out-of-scope questions stay no-match.

Do not invent a calibrated number before those scores exist.

## Running eval

```bash
cd backend
mvn test -Dgroups=ai-eval
```

## Documenting mistakes

When eval or manual testing surfaces a wrong citation, fabricated fact, or missed retrieval, record it in [docs/ai-mistakes.md](../docs/ai-mistakes.md) (AC-23).

## Unit-test coverage (default build)

Without Ollama:

| Test | Proves |
|------|--------|
| `RagAskServiceTest` no-match | Empty `VectorStore` → no `ChatModel` interaction; no-match body |
| `RagAskServiceTest` cites nothing retrieved | Retrieval returns TKT-1001; model answer cites no `TKT-` id → `grounded: false`, no-match answer, empty `sources` |
| `RagAskServiceTest` cites an id not retrieved | Retrieval returns TKT-1001; model answer cites TKT-9999 (alone or alongside TKT-1001) → `grounded: false`, no-match answer, empty `sources` |
| `TicketIndexerTest` | Re-ingest calls delete then add for the same `ticketId`; reload uses a read-only transaction |
| `TicketDocumentMapperTest` | Paragraph chunking; every chunk starts with the header line; metadata present |

Phase 1 tests load `backend/src/test/resources/application.yml` (`app.rag.indexing.enabled: false`). Full-context tests that need model beans use `@MockitoBean EmbeddingModel` and `@MockitoBean ChatModel`. `mvn test` passes with Ollama stopped.
