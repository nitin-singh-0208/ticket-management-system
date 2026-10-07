# Feature Specification: AI Assistant Over Ticket History

**Feature Branch**: `002-ticket-rag-assistant`

**Created**: 2026-10-07

**Status**: Draft

**Input**: User description: "AI assistant over ticket history covering the [Phase 2: RAG] items in specs/requirements.md. Ingestion: each ticket's description, comments and resolution notes become knowledge documents with metadata (ticketId, status, priority, assignee, category), embedded and stored in PGVector. Re-ingest on ticket create/update/comment/status change so the knowledge base never goes stale; backfill on startup when the store is empty. Ask: POST /api/ai/ask {\"question\"} → {answer, sources:[{ticketId,title,status,score,snippet}], grounded}. Answers only from retrieved tickets, never general LLM knowledge; cites the ticket IDs used; returns an explicit \"No relevant tickets found\" when nothing is relevant instead of fabricating. Must handle the 5 example questions in the PDF. Single retrieve→generate, no agentic actions. Top-K and similarity threshold are configurable, not hardcoded. UI: an \"Ask AI\" page with question input, answer, and clickable source tickets; shows the no-match message clearly. Re-ingestion hooks: publish a domain event from TicketService.createTicket, updateTicket, changeStatus and CommentService.addComment; do not change the ticket state machine or existing endpoints."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Ask a grounded question about ticket history (Priority: P1)

A support agent types a natural-language question and receives an answer drawn only from relevant tickets, with links to the tickets that informed the answer.

**Why this priority**: This is the core value of the assistant—turning ticket history into searchable institutional knowledge without inventing facts.

**Independent Test**: With several tickets already indexed (including payment and shipment examples), submit each of the five assignment example questions and confirm every response is grounded, cites ticket IDs, and lists clickable sources—or returns the honest no-match message when nothing applies.

**Acceptance Scenarios**:

1. **Given** tickets about payment failures exist in the knowledge base, **When** the agent asks "Have we seen payment failures before?", **Then** the response answers from those tickets, `grounded` is true, and `sources` lists the ticket IDs used (FR-11, FR-12, RAG-02, RAG-11, RAG-06, AC-16, AC-17).
2. **Given** ticket TKT-1001 has resolution notes, **When** the agent asks "What was the resolution for ticket TKT-1001?", **Then** the response summarizes that ticket's resolution from its indexed content and cites TKT-1001 in `sources` (FR-11, FR-12, RAG-02, RAG-11, AC-16, AC-17).
3. **Given** tickets about shipment tracking exist, **When** the agent asks "What are the common causes of shipment tracking issues?", **Then** the response groups causes found across matching tickets and cites each contributing ticket ID (FR-11, FR-12, RAG-02, RAG-11, AC-16, AC-17).
4. **Given** resolved tickets with similar content exist, **When** the agent asks "Show me similar resolved tickets.", **Then** the response lists or summarizes those resolved tickets and every cited ticket in `sources` has status RESOLVED or CLOSED (FR-11, FR-12, RAG-02, RAG-11, AC-16, AC-17).
5. **Given** high-priority tickets related to payment exist, **When** the agent asks "Which high-priority tickets are related to payment?", **Then** the response names only tickets whose indexed metadata shows High priority and payment-related content, with matching ticket IDs in `sources` (FR-11, FR-12, RAG-02, RAG-04, RAG-11, AC-16, AC-17).
6. **Given** a question is submitted, **When** the system answers, **Then** the response body includes `answer`, `sources` (each with `ticketId`, `title`, `status`, `score`, and `snippet`), and `grounded` set to true when retrieval succeeded (RAG-06, RAG-07, AC-16).
7. **Given** retrieved ticket context is used, **When** the answer is produced, **Then** the answer text names the ticket ID(s) it relied on and does not present facts absent from retrieved content (FR-12, RAG-11, RAG-13, AC-17).
8. **Given** a question is submitted, **When** the system processes it, **Then** it performs exactly one retrieval followed by one generation step with no follow-up tool calls, ticket creation, or notifications (RAG-01, RAG-13, AC-16).

---

### User Story 2 - Receive an honest answer when nothing matches (Priority: P1)

When no ticket content is relevant enough to answer a question, the agent sees a clear no-match message instead of a plausible but invented answer.

**Why this priority**: Fabricated answers destroy trust in support history and violate the grounding requirement.

**Independent Test**: Ask a question about a topic with zero related tickets (or below the similarity threshold) and confirm the response states that no relevant tickets were found, `grounded` is false, and `sources` is empty.

**Acceptance Scenarios**:

1. **Given** no indexed ticket content is similar enough to the question, **When** the agent asks any support question, **Then** `answer` is the explicit message "No relevant tickets found", `grounded` is false, and `sources` is an empty array (FR-13, RAG-12, AC-18).
2. **Given** retrieval returns no chunks above the configured similarity threshold, **When** a question is submitted, **Then** the system does not call the language model and returns the no-match response (RAG-11, RAG-12, AC-18).
3. **Given** the agent is on the Ask AI page, **When** a no-match response is returned, **Then** the page shows the no-match message prominently and does not show fabricated ticket sources (FR-13, RAG-12, AC-18).

---

### User Story 3 - Keep the knowledge base current (Priority: P1)

Whenever ticket content changes, the assistant's searchable knowledge reflects the latest description, comments, and resolution notes without manual re-indexing.

**Why this priority**: Stale embeddings make answers wrong even when the ticket record is correct.

**Independent Test**: Create or update a ticket, add a comment, or change status; then ask a question that should only be answerable from the new content and confirm the fresh information appears.

**Acceptance Scenarios**:

1. **Given** a new ticket is created, **When** indexing completes, **Then** its description is searchable and carries metadata `ticketId`, `status`, `priority`, `assignee`, and `category` (RAG-01, RAG-03, RAG-04, AC-15).
2. **Given** a ticket's description, priority, assignee, or resolution notes are updated, **When** re-ingestion completes, **Then** old chunks for that ticket are replaced and answers reflect the new content (RAG-05, AC-20).
3. **Given** a comment is added to a ticket, **When** re-ingestion completes, **Then** the comment text is searchable as part of that ticket's knowledge (RAG-03, RAG-05, AC-15, AC-20).
4. **Given** a ticket's status changes through an allowed transition, **When** re-ingestion completes, **Then** indexed metadata shows the new status and answers that filter by status use the updated value (RAG-04, RAG-05, AC-20).
5. **Given** re-ingestion runs for a ticket, **When** it finishes, **Then** all prior chunks for that `ticketId` are removed before new ones are stored so no orphaned stale chunks remain (RAG-05, AC-20).
6. **Given** the vector store is empty on application startup, **When** the application starts, **Then** all existing tickets are indexed automatically (backfill) (RAG-01, AC-15).
7. **Given** ticket create, update, status change, or comment add occurs, **When** the operation succeeds, **Then** a domain event is published so indexing can run without altering the ticket state machine or existing HTTP endpoints (RAG-05, AC-20).

---

### User Story 4 - Use the Ask AI page (Priority: P2)

A support agent opens a dedicated Ask AI screen, submits a question, reads the answer, and opens source tickets from the response.

**Why this priority**: The API alone does not deliver value; agents need a simple place to ask questions during their workflow.

**Independent Test**: Navigate to Ask AI, submit a question with known matching tickets, click a source ticket, and land on that ticket's detail view.

**Acceptance Scenarios**:

1. **Given** the agent opens the application, **When** they navigate to Ask AI, **Then** they see a question input, a submit action, and space for the answer and sources (RAG-06, AC-16).
2. **Given** the agent enters a question and submits, **When** a grounded answer returns, **Then** the page shows the answer text and a list of source tickets with title and status (FR-11, AC-16, AC-17).
3. **Given** source tickets are shown, **When** the agent clicks one, **Then** they are taken to that ticket's detail view (AC-16, AC-17).
4. **Given** a no-match response is returned, **When** the page renders, **Then** "No relevant tickets found" is shown clearly and no source list is presented (FR-13, RAG-12, AC-18).
5. **Given** the agent submits an empty or whitespace-only question, **When** they try to ask, **Then** the page explains that a question is required and does not call the assistant (FR-09, AC-12).

---

### Edge Cases

- The vector store is empty on first startup with existing tickets in the database. Backfill indexes every ticket before the assistant accepts questions (RAG-01, AC-15).
- A ticket is updated several times in quick succession. The knowledge base ends up consistent with the latest ticket state; intermediate states may be skipped (RAG-05, AC-20).
- A question references a human ticket ID that does not exist. The response is the no-match message unless other retrieved content is relevant (RAG-12, AC-18).
- A question matches content only in a comment or resolution notes, not the description. The answer still cites the ticket and the snippet reflects the matching section (RAG-03, AC-16).
- Retrieval returns chunks from the same ticket multiple times. Sources list each ticket once with the best-scoring snippet (AC-17).
- Similarity scores fall below the configured threshold for all chunks. The no-match response is returned and the language model is not invoked (RAG-09, RAG-12, AC-18, AC-21).
- The question is very long (up to the allowed limit). The system still returns a grounded answer or the no-match message without error (AC-16).
- A ticket is CLOSED or CANCELLED but its description or notes are later edited. Re-ingestion updates the knowledge base even though status cannot change further (RAG-05, AC-20).
- No tickets exist in the system. Any question returns the no-match message (RAG-12, AC-18).
- Operators change `topK` or the similarity threshold in configuration. Retrieval behaviour changes without code changes (RAG-09, AC-21).

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST convert each ticket's description, every comment, and resolution notes into searchable knowledge documents (source RAG-03, AC-15).
- **FR-002**: Each indexed chunk MUST carry metadata: `ticketId`, `status`, `priority`, `assignee`, and `category` (source RAG-04, AC-15).
- **FR-003**: The system MUST generate embeddings for knowledge documents and store them in a vector store so similarity search can find relevant ticket content (source RAG-01, FR-11, AC-15).
- **FR-004**: On ticket create, ticket field update, status change, and comment add, the system MUST delete all existing chunks for that `ticketId` and re-index the ticket's current content so the knowledge base does not go stale (source RAG-05, AC-20).
- **FR-005**: When the vector store contains no chunks at application startup, the system MUST backfill by indexing all existing tickets (source RAG-01, AC-15).
- **FR-006**: The system MUST expose `POST /api/ai/ask` accepting `{ "question": "<text>" }` and returning `{ "answer": "<text>", "sources": [{ "ticketId", "title", "status", "score", "snippet" }], "grounded": <boolean> }` (source RAG-06, RAG-07, AC-16).
- **FR-007**: The assistant MUST answer only from retrieved ticket context and MUST NOT supplement answers with general language-model knowledge about support topics (source FR-11, RAG-11, AC-16).
- **FR-008**: Every grounded answer MUST cite the specific ticket ID(s) used, both in the `sources` array and within the answer text (source FR-12, AC-17).
- **FR-009**: When no chunks meet the similarity threshold, the system MUST return `answer`: "No relevant tickets found", `grounded`: false, and an empty `sources` array, and MUST NOT invoke the language model (source FR-13, RAG-12, AC-18).
- **FR-010**: The question-answering flow MUST be a single retrieval step followed by a single generation step with no autonomous agent behaviour—no ticket creation, notifications, or chained tool calls (source RAG-13, AC-16).
- **FR-011**: Retrieval `topK` and similarity threshold MUST be configurable at deploy time and MUST NOT be hardcoded in application logic (source RAG-09, AC-21).
- **FR-012**: The system MUST successfully handle the five assignment example questions when matching ticket data exists: payment failures, resolution for TKT-1001, shipment tracking causes, similar resolved tickets, and high-priority payment tickets (source RAG-02, AC-16).
- **FR-013**: Ticket create (`createTicket`), ticket update (`updateTicket`), status change (`changeStatus`), and comment add (`addComment`) MUST each publish a domain event that triggers re-ingestion without modifying the ticket state machine or existing REST endpoints (source RAG-05, AC-20).
- **FR-014**: The UI MUST provide an Ask AI page with question input, answer display, and clickable source tickets that navigate to ticket details (source FR-11, AC-16, AC-17).
- **FR-015**: The Ask AI page MUST display the no-match message clearly when `grounded` is false (source FR-13, RAG-12, AC-18).
- **FR-016**: The system MUST reject blank or whitespace-only questions with a validation error before calling the assistant (source FR-09, AC-12).
- **FR-017**: Chunking strategy and embedding model choice MUST be documented and justified in `specs/architecture.md` (source RAG-08, RAG-10, AC-19).
- **FR-018**: At least one meaningful AI mistake discovered during development MUST be recorded in project documentation (source AC-23).

### Key Entities *(include if feature involves data)*

- **Knowledge document**: A searchable unit derived from one ticket section—description, a single comment, or resolution notes. It holds the text content and metadata (`ticketId`, `status`, `priority`, `assignee`, `category`). Multiple documents may exist per ticket after chunking.
- **Embedding chunk**: A vector representation of a knowledge document (or a paragraph-sized fragment of one) stored in the vector store for similarity search.
- **Ask request**: A natural-language question submitted by an agent through the API or Ask AI page.
- **Ask response**: The assistant output comprising an answer, ranked source tickets with relevance scores and snippets, and a `grounded` flag indicating whether retrieval found relevant tickets.
- **Ticket change event**: A domain event emitted after a successful ticket create, update, status change, or comment add, signalling that the affected ticket must be re-indexed.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: For each of the five assignment example questions, when matching ticket data exists, the agent receives a grounded answer with at least one cited ticket ID in under 10 seconds.
- **SC-002**: 100% of responses where `grounded` is true include every ticket ID referenced in the answer text in the `sources` array.
- **SC-003**: 100% of questions with no chunks above the similarity threshold return "No relevant tickets found" with `grounded` false and an empty `sources` array, with no language-model call made.
- **SC-004**: After a ticket description, comment, resolution notes, or status is changed, a question answerable only from the new content returns the updated information within one re-ingestion cycle (under 30 seconds in local development).
- **SC-005**: After application restart with an empty vector store but existing tickets, backfill completes and the assistant can answer in-scope questions without manual intervention.
- **SC-006**: Changing configured `topK` or similarity threshold alters which tickets are retrieved without redeploying application code.
- **SC-007**: On the Ask AI page, an agent can submit a question, read the answer, and open a cited ticket's detail view in under 1 minute on first attempt.
- **SC-008**: Zero assistant responses in acceptance testing invent ticket facts not present in retrieved snippets.

## Assumptions

- The Phase 1 ticket application is complete. Tickets, comments, statuses, and resolution notes already exist and persist across restarts.
- Authentication and role-based access are out of scope. Any user who can open the application may use Ask AI and see all indexed tickets.
- The vector store is PGVector backed by the same PostgreSQL database used for ticket persistence.
- Embeddings are produced locally using Ollama `nomic-embed-text` (768 dimensions). Answer generation uses Ollama `llama3.1:8b`.
- Chunking is paragraph-based with a maximum of approximately 800 tokens per chunk and approximately 100 tokens overlap only when a single section must be split (source RAG-08).
- Each knowledge document is tagged with a `sourceType` metadata field (`description`, `comment`, or `resolution`) in addition to the required ticket metadata.
- Re-ingestion is triggered by domain events published from `TicketService.createTicket`, `TicketService.updateTicket`, `TicketService.changeStatus`, and `CommentService.addComment`. Event handlers perform delete-then-index asynchronously; slight delay before search reflects changes is acceptable.
- Backfill on startup runs only when the vector store has zero chunks, not on every restart.
- Default retrieval settings are `topK` = 5 and similarity threshold = 0.7, overridable in `application.yml`.
- The static system prompt (instructions and guardrails) is kept constant for prompt-caching compatibility (source NFR-10).
- Questions may be up to 2,000 characters. The API returns HTTP 400 for blank questions.
- `score` in sources is the similarity score from retrieval (0–1 scale, higher is more relevant).
- `snippet` is a short excerpt (up to 300 characters) from the highest-scoring matching chunk for that ticket.
- Title is included in sources from the live ticket record, not from the chunk metadata alone.
- The ticket state machine, existing REST endpoints, and Phase 1 UI behaviour remain unchanged.
- Chunking strategy and embedding model tradeoffs are documented in `specs/architecture.md` during planning/implementation (AC-19).
- AI mistakes found during development are logged in `docs/ai-mistakes.md` (AC-23).
