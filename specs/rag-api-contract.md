# RAG API contract

Grounded question-answering over indexed ticket history. OpenAPI source: [002-ticket-rag-assistant/contracts/ai.openapi.yaml](002-ticket-rag-assistant/contracts/ai.openapi.yaml).

## Endpoint

`POST /api/ai/ask`

### Request

```json
{
  "question": "Have we seen payment failures before?"
}
```

| Field | Rules |
|-------|--------|
| `question` | Required. Trimmed. 1–2000 characters. Blank or whitespace-only → HTTP 400 with field error. |

### Response (grounded)

```json
{
  "answer": "Yes. Payment failures appear in TKT-1001 (card declined at checkout) and TKT-1002 (duplicate capture).",
  "sources": [
    {
      "ticketId": "TKT-1001",
      "title": "Payment declined at checkout",
      "status": "OPEN",
      "score": 0.82,
      "snippet": "Card network returned a payment failure during checkout."
    }
  ],
  "grounded": true
}
```

### Response (no match)

```json
{
  "answer": "No relevant tickets found",
  "sources": [],
  "grounded": false
}
```

The same no-match body is returned, without calling the chat model, when retrieval returns no chunks above `app.rag.similarity-threshold`.

It is also returned **after** a chat call when the answer cites no retrieved ticket id, or cites any id that was not retrieved. In those cases the model text is discarded.

## Processing flow

1. Validate `question`.
2. Embed the question as `search_query: ` plus the question text. `vectorStore.similaritySearch` uses `topK` from `app.rag.top-k` and `similarityThreshold` from `app.rag.similarity-threshold`.
3. Log each hit’s `ticketId` and score at DEBUG (`com.ticketmanagement.rag`).
4. If results are empty → no-match response. Do not call the chat model.
5. Build the prompt: **static system prompt** (constant prefix) + context blocks that start with the same header as stored chunks (`Ticket {ticketId} | {title} | status {status} | priority {priority} | category {category} | section {sourceType}`) + user question.
6. `chatModel.call(prompt)` once.
7. Build candidate sources from retrieved chunks only: one entry per `ticketId`, keeping the highest score. Title and status come from that chunk. Snippet is up to 300 characters of section text.
8. Keep only candidates whose `ticketId` is cited in the answer.
9. If that set is empty, or the answer cites any `TKT-` id that was not retrieved, return the no-match body (`grounded: false`).
10. Otherwise return the answer, the filtered sources, and `grounded: true`.

## Grounding rules

- Answers must use only retrieved chunk text.
- A grounded answer names at least one retrieved ticket id and no other ticket id.
- `sources` never includes a ticket the retrieval step did not return.
- No follow-up tool calls, ticket creation, or notifications.

## Errors

| Status | When |
|--------|------|
| 400 | Validation failure (blank question, too long, unknown JSON property). |
| 500 | Unexpected server error (no stack trace in body). |

All errors use RFC 7807 ProblemDetail, consistent with Phase 1.

## Configuration (read-only for clients)

| Property | Default | Effect |
|----------|---------|--------|
| `app.rag.top-k` | 5 | Max chunks retrieved. |
| `app.rag.similarity-threshold` | 0.6 | Starting minimum score. Calibrate from DEBUG logs; record the value in [evaluation-strategy.md](evaluation-strategy.md). |

Clients cannot override these per request.
