<!--
Sync Impact Report
- Version change: unversioned template → 1.0.0
- Modified principles:
  - [PRINCIPLE_1_NAME] → I. Spec-First
  - [PRINCIPLE_2_NAME] → II. Backend Is Source of Truth
  - [PRINCIPLE_3_NAME] → III. Test-First for Deterministic Logic
  - [PRINCIPLE_4_NAME] → IV. Grounded AI
  - [PRINCIPLE_5_NAME] → V. Configuration over Constants
- Added principles: VI. No Secrets in Git; VII. Atomic Commits
- Added sections: Technology Stack; Binding Project Rules
- Removed sections: none (placeholder section titles replaced)
- Follow-up TODOs: none
-->

# AI-Powered Support Ticket Management System Constitution

## Core Principles

### I. Spec-First

No production code MUST be written without an approved spec, plan, and tasks.
Every requirement MUST trace to `specs/requirements.md`.

Rationale: Unapproved work and untraced requirements break the spec as the
source of truth.

### II. Backend Is Source of Truth

Validation and the ticket state machine MUST be enforced on the server.
Allowed transitions are:

- OPEN → IN_PROGRESS → RESOLVED → CLOSED
- OPEN → CANCELLED
- IN_PROGRESS → CANCELLED

Every other transition MUST be rejected with HTTP 409.

Rationale: Clients cannot be trusted to enforce workflow or validation rules.

### III. Test-First for Deterministic Logic

Every valid state transition and every invalid state transition MUST have its
own integration test before that behaviour is treated as complete.

Rationale: The state machine is deterministic, so each transition can and MUST
be asserted exactly.

### IV. Grounded AI

Answers MUST come only from retrieved ticket context and MUST cite ticket IDs.
When retrieval is empty, the system MUST return an explicit
"No relevant tickets found" and MUST NOT call the LLM.
The flow MUST be a single retrieve-then-generate step. Agentic actions are
prohibited.

Rationale: Ungrounded or multi-step generation invents ticket facts.

### V. Configuration over Constants

`topK`, the similarity threshold, and model names MUST live in
`application.yml`. They MUST NOT be hardcoded.

Rationale: Retrieval and model settings change by environment without code
edits.

### VI. No Secrets in Git

Secrets, credentials, tokens, and private keys MUST NOT be committed.

Rationale: The repository is not a secret store.

### VII. Atomic Commits

Each commit MUST contain one logical change and MUST use a conventional
commit message.

Rationale: Small, conventional commits stay reviewable and revertable.

## Technology Stack

The project MUST use:

- Java 21
- Spring Boot 3.5
- Spring AI
- PostgreSQL with PGVector, run via Docker Compose
- Ollama with `nomic-embed-text` and `llama3.1:8b`
- React, Vite, and TypeScript

## Binding Project Rules

Rules in `.cursor/rules/` are binding. Implementation and reviews MUST comply
with them.

## Governance

This constitution supersedes conflicting local practice. Amendments MUST be
recorded in this file, versioned semantically, and checked for compliance
before merge.

Versioning policy:

- MAJOR: backward-incompatible governance or principle removal or redefinition
- MINOR: a new principle or section, or materially expanded guidance
- PATCH: clarifications, wording, and other non-semantic refinements

Compliance review: every change MUST be checked against these principles and
against `.cursor/rules/` before it is accepted. Runtime development guidance
lives in `.cursor/rules/`.

**Version**: 1.0.0 | **Ratified**: 2026-10-07 | **Last Amended**: 2026-10-07
