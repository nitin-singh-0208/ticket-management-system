# Requirements

Source: `docs/c2-assignment-ticket-management-system.pdf`.

Each item below is stated in that PDF. Phase tags classify it as the ticket application or the RAG assistant.

## Spec layout

The PDF shows `spec/`. This project uses `specs/` because GitHub Spec Kit v1.0.9 creates feature folders under `specs/NNN-feature/`. Top-level files are the assignment's spec artefacts. Feature folders hold Spec Kit outputs.

The PDF lists these spec artefacts:

- `requirements.md`
- `architecture.md`
- `data-model.md`
- `api-contract.md`
- `state-machine.md`
- `rag-ingestion.md`
- `rag-api-contract.md`
- `evaluation-strategy.md`
- `ui-flow.md`
- `test-strategy.md`

## Functional requirements

| ID | Phase | Requirement |
|----|-------|-------------|
| FR-01 | [Phase 1: App] | Create a ticket. |
| FR-02 | [Phase 1: App] | List tickets. |
| FR-03 | [Phase 1: App] | View ticket details. |
| FR-04 | [Phase 1: App] | Update title, description, priority and assignee. |
| FR-05 | [Phase 1: App] | Add comments. |
| FR-06 | [Phase 1: App] | Search tickets by keyword. |
| FR-07 | [Phase 1: App] | Filter tickets by status. |
| FR-08 | [Phase 1: App] | Persist data in a database. |
| FR-09 | [Phase 1: App] | Validate input at the backend. |
| FR-10 | [Phase 1: App] | Display meaningful errors in the UI. |
| FR-11 | [Phase 2: RAG] | Provide a natural-language question-answering endpoint over ticket history, grounded strictly in real ticket data. |
| FR-12 | [Phase 2: RAG] | Cite the specific ticket(s) used to produce any assistant answer. |
| FR-13 | [Phase 2: RAG] | Explicitly indicate when no relevant tickets are found, rather than fabricate an answer. |

## State machine

The backend must enforce the following transitions.

| ID | Phase | Requirement |
|----|-------|-------------|
| SM-01 | [Phase 1: App] | OPEN → IN_PROGRESS → RESOLVED → CLOSED |
| SM-02 | [Phase 1: App] | OPEN → CANCELLED |
| SM-03 | [Phase 1: App] | IN_PROGRESS → CANCELLED |
| SM-04 | [Phase 1: App] | Invalid transitions must be rejected. |

The PDF gives these invalid examples: CLOSED → OPEN, RESOLVED → OPEN, CANCELLED → OPEN.

## RAG and assistant

| ID | Phase | Requirement |
|----|-------|-------------|
| RAG-01 | [Phase 2: RAG] | Implement the flow: Support Tickets → Create Knowledge Documents → Chunk → Generate Embeddings → Vector Store → User Question → Similarity Search → Relevant Tickets → LLM + Context → Grounded Answer → Ticket Sources. |
| RAG-02 | [Phase 2: RAG] | The assistant should answer: "Have we seen payment failures before?"; "What was the resolution for ticket TKT-1001?"; "What are the common causes of shipment tracking issues?"; "Show me similar resolved tickets."; "Which high-priority tickets are related to payment?" |
| RAG-03 | [Phase 2: RAG] | Convert ticket information (description, comments, resolution notes) into searchable knowledge documents. |
| RAG-04 | [Phase 2: RAG] | Include metadata: ticketId, status, priority, assignee, category. |
| RAG-05 | [Phase 2: RAG] | Re-ingest / refresh embeddings when a ticket is updated or closed. Do not let the knowledge base go stale. |
| RAG-06 | [Phase 2: RAG] | Create `POST /api/ai/ask`. |
| RAG-07 | [Phase 2: RAG] | Request body: `{ "question": "What caused previous payment failures?" }` |
| RAG-08 | [Phase 2: RAG] | Document the chunking strategy for ticket data (paragraph-based vs. fixed-size vs. semantic splitting). This may come from brainstorming and spec analysis in the IDE. |
| RAG-09 | [Phase 2: RAG] | Top-K and similarity threshold must be configurable, not hardcoded. |
| RAG-10 | [Phase 2: RAG] | Document the embedding model choice (local via Ollama vs. cloud) and the cost/latency/quality tradeoff considered. This may come from brainstorming and spec analysis in the IDE. |
| RAG-11 | [Phase 2: RAG] | The assistant must only answer from retrieved ticket context. It must not fall back on general LLM knowledge for support-specific questions. |
| RAG-12 | [Phase 2: RAG] | If no tickets are relevant, say so explicitly rather than produce a fabricated but plausible-sounding answer. |
| RAG-13 | [Phase 2: RAG] | Use a single retrieval → generate flow, not an autonomous agent. The assistant answers one question with one grounded response. It does not independently create tickets, send notifications, or chain into other tools. That is out of scope. |

## Non-functional requirements

The PDF requires the system to be built using the following.

| ID | Phase | Requirement |
|----|-------|-------------|
| NFR-01 | [Phase 1: App] | Java 21 |
| NFR-02 | [Phase 1: App] | Spring Boot |
| NFR-03 | [Phase 2: RAG] | Spring AI |
| NFR-04 | [Phase 1: App] | PostgreSQL/H2 |
| NFR-05 | [Phase 2: RAG] | An embedding model |
| NFR-06 | [Phase 2: RAG] | A vector store (e.g. PGVector or Chroma) |
| NFR-07 | [Phase 1: App] | REST API |
| NFR-08 | [Phase 1: App] | React/Next.js or equivalent frontend |
| NFR-09 | [Phase 1: App] | Cursor / GitHub Copilot / Kiro |
| NFR-10 | [Phase 2: RAG] | Use prompt caching for the static portions of the assistant's system prompt (instructions, guardrail text) so repeated questions don't re-pay for unchanged context. |

## Acceptance criteria

Verbatim from the PDF. The solution is complete when:

| ID | Phase | Criterion |
|----|-------|-----------|
| AC-01 | [Phase 1: App] | Ticket can be created from UI. |
| AC-02 | [Phase 1: App] | Tickets can be listed. |
| AC-03 | [Phase 1: App] | Ticket details can be viewed. |
| AC-04 | [Phase 1: App] | Ticket fields can be updated. |
| AC-05 | [Phase 1: App] | Assignee can be changed. |
| AC-06 | [Phase 1: App] | Comments can be added. |
| AC-07 | [Phase 1: App] | Search works. |
| AC-08 | [Phase 1: App] | Status filter works. |
| AC-09 | [Phase 1: App] | Valid status transitions work. |
| AC-10 | [Phase 1: App] | Invalid status transitions are rejected by backend. |
| AC-11 | [Phase 1: App] | Data survives application restart. |
| AC-12 | [Phase 1: App] | Backend validation works. |
| AC-13 | [Phase 1: App] | UI shows meaningful errors. |
| AC-14 | [Phase 1: App] | State-machine integration tests pass. |
| AC-15 | [Phase 2: RAG] | Ticket data is converted into embeddings and stored in a vector store. |
| AC-16 | [Phase 2: RAG] | POST /api/ai/ask returns a grounded, ticket-sourced answer for in-scope questions. |
| AC-17 | [Phase 2: RAG] | The response cites the specific ticket ID(s) used to generate it. |
| AC-18 | [Phase 2: RAG] | Out-of-scope / no-match questions return an honest "no relevant tickets found" response, not a fabricated answer. |
| AC-19 | [Phase 2: RAG] | Chunking strategy and embedding model choice are documented and justified in architecture.md. |
| AC-20 | [Phase 2: RAG] | Re-ingestion happens when a ticket is updated - embeddings do not go stale. |
| AC-21 | [Phase 2: RAG] | Retrieval parameters (top-K, similarity threshold) are configurable, not hardcoded. |
| AC-22 | [Phase 1: App] | No secrets are committed. |
| AC-23 | [Phase 2: RAG] | At least one meaningful AI mistake - in code or in a RAG answer - was caught and documented during development. |
