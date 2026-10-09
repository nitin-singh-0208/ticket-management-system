# Graph Report - ticket-management-system  (2026-10-08)

## Corpus Check
- 140 files · ~168,024 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 13 file(s) not represented in the graph (top: (none) 8, .mdc 4, .toml 1)

## Summary
- 1211 nodes · 2429 edges · 93 communities (70 shown, 23 thin omitted)
- Extraction: 84% EXTRACTED · 16% INFERRED · 0% AMBIGUOUS · INFERRED: 394 edges (avg confidence: 0.89)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `137a5f42`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- ApiExceptionHandler
- test-strategy.md
- .assignee
- common.sh
- package.json
- Tasks: [FEATURE NAME]
- speckit-analyze/SKILL.md
- compilerOptions
- User Scenarios & Testing *(mandatory)*
- Execution Steps
- TicketIndexerTest.java
- Core Principles
- Feature Specification: [FEATURE NAME]
- speckit-plan/SKILL.md
- speckit-specify/SKILL.md
- speckit-tasks/SKILL.md
- Core Principles
- Implementation Plan: [FEATURE]
- UI contract: Support Ticket Management
- Ticket
- Implementation Plan: Support Ticket Management
- Documentation Guidelines
- speckit-checklist/SKILL.md
- RagControllerTest.java
- WebConfig
- speckit-clarify/SKILL.md
- speckit-implement/SKILL.md
- speckit-constitution/SKILL.md
- UpdateTicketRequest
- speckit-taskstoissues/SKILL.md
- [CHECKLIST TYPE] Checklist: [FEATURE NAME]
- RagAskEvalTest.java
- RagAskServiceTest.java
- 2026-09-30 07:46:29Z
- generate-tests.md
- review-code.md
- review-rag-output.md
- review-spec.md
- 2026-09-29_08-24-53Z-create-the-following-project.md
- Tasks: AI Assistant Over Ticket History
- 2026-10-07_17-30-01Z-support-ticket-phase-1.md
- com.ticketmanagement:ticket-management
- RagAskEvalTest
- TicketStatus
- Tasks: Support Ticket Management
- Research: Ticket RAG Assistant
- RagProperties
- 2026-10-07_17-54-00Z-support-ticket-implementation-phases.md
- org.junit.jupiter.api.Test
- TicketStatusTest
- TicketStatusTransitionTest.java
- TicketIndexer.java
- RAG API contract
- Data Model: Ticket RAG Assistant
- TicketController.java
- Requirements
- Specification Quality Checklist: Support Ticket Management
- Architecture
- Data model
- Quickstart: Ticket RAG Assistant
- Evaluation strategy
- Plan complete
- Specification Quality Checklist: AI Assistant Over Ticket History
- 2026-10-07_18-26-01Z-rag-ai-assistant-over.md
- 2026-10-08_16-39-27Z-ticket-rag-assistant-setup.md
- 2026-10-08_16-44-08Z-ticket-rag-assistant-setup.md
- TicketController
- Comment
- org.springframework.ai.vectorstore.VectorStore
- TicketService.java
- Research: Support Ticket Management
- ReadOnlyCapableTransactionManager
- TicketRepository
- tickets.ts
- RagAskService
- org.springframework.http.ResponseEntity
- .ticketId
- Quickstart: Support Ticket Management
- User Scenarios & Testing *(mandatory)*
- Completion Report
- RAG ingestion
- RagAskServiceTest
- allowedNext
- Data Model: Support Ticket Management
- AskResponse
- TicketManagementApplication.java
- Implementation Plan: AI Assistant Over Ticket History
- ai-mistakes.md
- 2026-09-29 08:24:53Z
- 2026-10-08_17-11-45Z-ticket-rag-assistant-implementation.md

## God Nodes (most connected - your core abstractions)
1. `Ticket` - 47 edges
2. `TicketValidationTest` - 37 edges
3. `ApiExceptionHandler` - 29 edges
4. `TicketStatus` - 28 edges
5. `TicketRepository` - 25 edges
6. `TicketDocumentMapper` - 24 edges
7. `RagAskService` - 23 edges
8. `RagAskServiceTest` - 23 edges
9. `UpdateTicketRequest` - 22 edges
10. `RagAskEvalTest` - 22 edges

## Surprising Connections (you probably didn't know these)
- `Chunking strategy` --references--> `TicketDocumentMapper`  [INFERRED]
  specs/architecture.md → backend/src/main/java/com/ticketmanagement/rag/TicketDocumentMapper.java
- `Key technical decisions` --references--> `TicketDocumentMapper`  [INFERRED]
  .specstory/history/2026-10-07_18-32-18Z-spring-ai-rag-plan.md → backend/src/main/java/com/ticketmanagement/rag/TicketDocumentMapper.java
- `Tests written first (confirmed failing)` --references--> `allowedNext()`  [INFERRED]
  .specstory/history/2026-10-07_18-05-59Z-ticket-status-transition-enforcement.md → backend/src/main/java/com/ticketmanagement/ticket/TicketStatus.java
- `Phase 6 — `feat(comment): append comments on a ticket` (`8f59719`)` --references--> `CommentValidationTest`  [INFERRED]
  .specstory/history/2026-10-07_18-05-59Z-ticket-status-transition-enforcement.md → backend/src/test/java/com/ticketmanagement/comment/CommentValidationTest.java
- `Parallel opportunities` --references--> `RagAskServiceTest`  [INFERRED]
  .specstory/history/2026-10-08_15-49-19Z-rag-ask-implementation-phases.md → backend/src/test/java/com/ticketmanagement/rag/RagAskServiceTest.java

## Import Cycles
- None detected.

## Communities (93 total, 23 thin omitted)

### Community 0 - "ApiExceptionHandler"
Cohesion: 0.14
Nodes (21): ApiExceptionHandler, FieldErrorItem, Override, com.fasterxml.jackson.databind.exc.InvalidFormatException, invalidnullexception, jsonmappingexception, methodargumenttypemismatchexception, ordered (+13 more)

### Community 2 - ".assignee"
Cohesion: 0.32
Nodes (12): Ticket, Keyword search, Implementation for User Story 1, Implementation for User Story 3, Implementation for User Story 4, Phase 2: User Story 1 - Create and view a ticket (Slice 2) (Priority: P1) MVP, Phase 5: User Story 4 - Correct ticket details (Slice 5) (Priority: P2), Tests for User Story 1 (+4 more)

### Community 3 - "common.sh"
Cohesion: 0.13
Nodes (29): check-prerequisites.sh script, check_dir(), check_file(), find_specify_root(), format_speckit_command(), get_current_branch(), get_feature_paths(), get_invoke_separator() (+21 more)

### Community 4 - "package.json"
Cohesion: 0.08
Nodes (24): dependencies, react, react-dom, react-router, devDependencies, @types/react, @types/react-dom, typescript (+16 more)

### Community 5 - "Tasks: [FEATURE NAME]"
Cohesion: 0.07
Nodes (26): Dependencies & Execution Order, Format: `[ID] [P?] [Story] Description`, Implementation for User Story 1, Implementation for User Story 2, Implementation for User Story 3, Implementation Strategy, Incremental Delivery, MVP First (User Story 1 Only) (+18 more)

### Community 6 - "speckit-analyze/SKILL.md"
Cohesion: 0.08
Nodes (25): 1. Initialize Analysis Context, 2. Load Artifacts (Progressive Disclosure), 3. Build Semantic Models, 4. Detection Passes (Token-Efficient Analysis), 5. Severity Assignment, 6. Produce Compact Analysis Report, 7. Provide Next Actions, 8. Offer Remediation (+17 more)

### Community 7 - "compilerOptions"
Cohesion: 0.11
Nodes (17): compilerOptions, allowImportingTsExtensions, isolatedModules, jsx, lib, module, moduleDetection, moduleResolution (+9 more)

### Community 8 - "User Scenarios & Testing *(mandatory)*"
Cohesion: 0.12
Nodes (17): Assumptions, Clarifications, Edge Cases, Feature Specification: Support Ticket Management, Functional Requirements, Key Entities *(include if feature involves data)*, Measurable Outcomes, Requirements *(mandatory)* (+9 more)

### Community 9 - "Execution Steps"
Cohesion: 0.12
Nodes (15): 1. Initialize Convergence Context, 2. Load Artifacts (Progressive Disclosure), 3. Build the Intent Inventory, 4. Assess the Codebase and Classify Findings, 5. Assign Severity, 6. Present the In-Session Findings Summary, 7. Append Convergence Tasks (or report converged), 8. Provide Next Actions (Handoff) (+7 more)

### Community 10 - "TicketIndexerTest.java"
Cohesion: 0.11
Nodes (19): ReadOnlyReload, Reindex, WhenIndexingDisabled, doanswer, document, filter, inorder, method (+11 more)

### Community 11 - "Core Principles"
Cohesion: 0.15
Nodes (12): AI-Powered Support Ticket Management System Constitution, Binding Project Rules, Core Principles, Governance, I. Spec-First, II. Backend Is Source of Truth, III. Test-First for Deterministic Logic, IV. Grounded AI (+4 more)

### Community 12 - "Feature Specification: [FEATURE NAME]"
Cohesion: 0.15
Nodes (12): Assumptions, Edge Cases, Feature Specification: [FEATURE NAME], Functional Requirements, Key Entities *(include if feature involves data)*, Measurable Outcomes, Requirements *(mandatory)*, Success Criteria *(mandatory)* (+4 more)

### Community 13 - "speckit-plan/SKILL.md"
Cohesion: 0.18
Nodes (10): Completion Report, Done When, Key rules, Mandatory Post-Execution Hooks, Outline, Phase 0: Outline & Research, Phase 1: Design & Contracts, Phases (+2 more)

### Community 14 - "speckit-specify/SKILL.md"
Cohesion: 0.18
Nodes (10): Completion Report, Done When, For AI Generation, Mandatory Post-Execution Hooks, Outline, Pre-Execution Checks, Quick Guidelines, Section Requirements (+2 more)

### Community 15 - "speckit-tasks/SKILL.md"
Cohesion: 0.18
Nodes (10): Checklist Format (REQUIRED), Completion Report, Done When, Mandatory Post-Execution Hooks, Outline, Phase Structure, Pre-Execution Checks, Task Generation Rules (+2 more)

### Community 16 - "Core Principles"
Cohesion: 0.18
Nodes (10): Core Principles, Governance, [PRINCIPLE_1_NAME], [PRINCIPLE_2_NAME], [PRINCIPLE_3_NAME], [PRINCIPLE_4_NAME], [PRINCIPLE_5_NAME], [PROJECT_NAME] Constitution (+2 more)

### Community 17 - "Implementation Plan: [FEATURE]"
Cohesion: 0.22
Nodes (8): Complexity Tracking, Constitution Check, Documentation (this feature), Implementation Plan: [FEATURE], Project Structure, Source Code (repository root), Summary, Technical Context

### Community 18 - "UI contract: Support Ticket Management"
Cohesion: 0.22
Nodes (7): Comments, Create, Detail and edit, List, Routes, UI contract: Support Ticket Management, UI flow

### Community 19 - "Ticket"
Cohesion: 0.07
Nodes (18): CreateCommentRequest, TicketNotFoundException, TicketDocumentMapper, CreateTicketRequest, StatusChangeRequest, TicketDetail, TicketChangedEvent, Ticket (+10 more)

### Community 20 - "Implementation Plan: Support Ticket Management"
Cohesion: 0.22
Nodes (9): Complexity Tracking, Constitution Check, Documentation (this feature), Implementation Plan: Support Ticket Management, Implementation slices, Project Structure, Source Code (repository root), Summary (+1 more)

### Community 21 - "Documentation Guidelines"
Cohesion: 0.25
Nodes (7): Accuracy, Decisions, Diagrams, Documentation Guidelines, Keep docs with the change, Shape, Where docs live

### Community 22 - "speckit-checklist/SKILL.md"
Cohesion: 0.25
Nodes (7): Anti-Examples: What NOT To Do, Checklist Purpose: "Unit Tests for English", Example Checklist Types & Sample Items, Execution Steps, Post-Execution Checks, Pre-Execution Checks, User Input

### Community 23 - "RagControllerTest.java"
Cohesion: 0.15
Nodes (19): autowired, CommentValidationTest, RagControllerTest, get, hassize, is, jsonpath, mediatype (+11 more)

### Community 24 - "WebConfig"
Cohesion: 0.43
Nodes (5): Override, WebConfig, org.springframework.context.annotation.Configuration, org.springframework.web.servlet.config.annotation.CorsRegistry, org.springframework.web.servlet.config.annotation.WebMvcConfigurer

### Community 25 - "speckit-clarify/SKILL.md"
Cohesion: 0.29
Nodes (6): Completion Report, Done When, Mandatory Post-Execution Hooks, Outline, Pre-Execution Checks, User Input

### Community 26 - "speckit-implement/SKILL.md"
Cohesion: 0.29
Nodes (6): Completion Report, Done When, Mandatory Post-Execution Hooks, Outline, Pre-Execution Checks, User Input

### Community 27 - "speckit-constitution/SKILL.md"
Cohesion: 0.33
Nodes (5): Outline, Post-Execution Checks, Pre-Execution Checks, Scope Guard, User Input

### Community 28 - "UpdateTicketRequest"
Cohesion: 0.08
Nodes (25): UpdateTicketRequest, Override, UpdateTicketRequestValidator, ValidUpdateTicket, TicketPriority, HIGH, LOW, MEDIUM (+17 more)

### Community 29 - "speckit-taskstoissues/SKILL.md"
Cohesion: 0.40
Nodes (4): Outline, Post-Execution Checks, Pre-Execution Checks, User Input

### Community 30 - "[CHECKLIST TYPE] Checklist: [FEATURE NAME]"
Cohesion: 0.40
Nodes (4): [Category 1], [Category 2], [CHECKLIST TYPE] Checklist: [FEATURE NAME], Notes

### Community 31 - "RagAskEvalTest.java"
Cohesion: 0.16
Nodes (14): assertequals, assertfalse, asserttrue, com.fasterxml.jackson.databind.ObjectMapper, duration, enumset, field, httprequest (+6 more)

### Community 32 - "RagAskServiceTest.java"
Cohesion: 0.12
Nodes (16): any, argumentcaptor, assistantmessage, generation, java.util.regex.Pattern, linkedhashset, matcher, mock (+8 more)

### Community 39 - "Tasks: AI Assistant Over Ticket History"
Cohesion: 0.08
Nodes (24): Commit rule, Dependencies & Execution Order, Eval and calibration, Format: `[ID] [P?] [Story] Description`, Implementation for Foundational, Implementation for Setup, Implementation for User Story 3, Implementation Strategy (+16 more)

### Community 45 - "TicketStatus"
Cohesion: 0.17
Nodes (9): TicketSummary, StatusChangeNotAllowedException, TicketStatus, CANCELLED, CLOSED, IN_PROGRESS, OPEN, RESOLVED (+1 more)

### Community 46 - "Tasks: Support Ticket Management"
Cohesion: 0.07
Nodes (27): Commit rule, Dependencies and execution order, Format: `[ID] [P?] [Story] Description`, Implementation for Seed, Implementation for Setup, Implementation for User Story 5, Implementation for User Story 6, Implementation strategy (+19 more)

### Community 47 - "Research: Ticket RAG Assistant"
Cohesion: 0.12
Nodes (17): Application RAG settings (project-specific, not Spring AI), Ask flow, Chunking strategy (summary), Configuration properties (verified), Embedding model choice (summary), Models, nomic-embed-text task prefixes, Ollama chat (+9 more)

### Community 48 - "RagProperties"
Cohesion: 0.19
Nodes (3): Indexing, RagProperties, org.springframework.boot.context.properties.ConfigurationProperties

### Community 51 - "TicketStatusTest"
Cohesion: 0.16
Nodes (5): TicketStatusTest, org.junit.jupiter.params.ParameterizedTest, org.junit.jupiter.params.provider.Arguments, org.junit.jupiter.params.provider.MethodSource, Tests

### Community 52 - "TicketStatusTransitionTest.java"
Cohesion: 0.27
Nodes (11): assertnotnull, RagContextSmokeTest, TicketStatusTransitionTest, container, dockerimagename, org.springframework.ai.embedding.EmbeddingModel, org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc, org.springframework.boot.test.context.SpringBootTest (+3 more)

### Community 53 - "TicketIndexer.java"
Cohesion: 0.19
Nodes (14): TicketIndexer, Override, VectorStoreBackfill, filterexpressionbuilder, loggerfactory, org.slf4j.Logger, org.springframework.boot.ApplicationArguments, org.springframework.boot.ApplicationRunner (+6 more)

### Community 54 - "RAG API contract"
Cohesion: 0.22
Nodes (9): Configuration (read-only for clients), Endpoint, Errors, Grounding rules, Processing flow, RAG API contract, Request, Response (grounded) (+1 more)

### Community 55 - "Data Model: Ticket RAG Assistant"
Cohesion: 0.22
Nodes (8): Ask request, Backfill, Chunk metadata (every document), Data Model: Ticket RAG Assistant, Knowledge document → chunk mapping, Re-ingestion invariant, Retrieval configuration (`app.rag`), Vector document (Spring AI `Document`)

### Community 56 - "TicketController.java"
Cohesion: 0.24
Nodes (11): CommentController, AskRequest, RagController, org.springframework.web.bind.annotation.PostMapping, org.springframework.web.bind.annotation.RequestMapping, org.springframework.web.bind.annotation.RestController, pathvariable, requestbody (+3 more)

### Community 57 - "Requirements"
Cohesion: 0.25
Nodes (7): Acceptance criteria, Functional requirements, Non-functional requirements, RAG and assistant, Requirements, Spec layout, State machine

### Community 59 - "Specification Quality Checklist: Support Ticket Management"
Cohesion: 0.33
Nodes (5): Content Quality, Feature Readiness, Notes, Requirement Completeness, Specification Quality Checklist: Support Ticket Management

### Community 60 - "Architecture"
Cohesion: 0.29
Nodes (7): Architecture, Chunking strategy, Decisions, Embedding model choice, Prompt caching, RAG pipeline, Shape

### Community 61 - "Data model"
Cohesion: 0.50
Nodes (4): Comment, Data model, Search and list, Ticket

### Community 62 - "Quickstart: Ticket RAG Assistant"
Cohesion: 0.25
Nodes (8): AI evaluation suite (optional, requires Ollama), Ask API (curl), Backend, Frontend, Prerequisites, Quickstart: Ticket RAG Assistant, Re-ingestion, Regression

### Community 63 - "Evaluation strategy"
Cohesion: 0.29
Nodes (7): Documenting mistakes, Evaluation strategy, In-scope questions (from assignment PDF), Metrics, Out-of-scope questions (no-match), Running eval, Similarity threshold

### Community 64 - "Plan complete"
Cohesion: 0.29
Nodes (6): 2026-10-07 18:32:18Z, Constitution check, Generated artifacts, Key technical decisions, Next step, Plan complete

### Community 65 - "Specification Quality Checklist: AI Assistant Over Ticket History"
Cohesion: 0.33
Nodes (5): Content Quality, Feature Readiness, Notes, Requirement Completeness, Specification Quality Checklist: AI Assistant Over Ticket History

### Community 69 - "TicketController"
Cohesion: 0.16
Nodes (5): InvalidListQueryException, TicketPage, TicketController, TicketSpecifications, org.springframework.web.bind.annotation.GetMapping

### Community 70 - "Comment"
Cohesion: 0.13
Nodes (16): Comment, CommentResponse, column, enumerated, enumtype, eventtype, fetchtype, generated (+8 more)

### Community 71 - "org.springframework.ai.vectorstore.VectorStore"
Cohesion: 0.24
Nodes (6): Config, org.springframework.ai.vectorstore.VectorStore, org.springframework.beans.factory.ObjectProvider, org.springframework.context.annotation.Bean, org.springframework.transaction.annotation.EnableTransactionManagement, org.springframework.transaction.PlatformTransactionManager

### Community 72 - "TicketService.java"
Cohesion: 0.27
Nodes (7): arraylist, linkedhashmap, list, org.springframework.data.jpa.domain.Specification, page, pagerequest, predicate

### Community 73 - "Research: Support Ticket Management"
Cohesion: 0.15
Nodes (13): Concurrent edits, CORS, Database image, Human ID allocation, List query versus "no pages", Local database password, Monorepo layout, Performance and scale (+5 more)

### Community 74 - "ReadOnlyCapableTransactionManager"
Cohesion: 0.33
Nodes (5): Override, ReadOnlyCapableTransactionManager, org.springframework.transaction.support.AbstractPlatformTransactionManager, org.springframework.transaction.support.DefaultTransactionStatus, TransactionDefinition

### Community 75 - "TicketRepository"
Cohesion: 0.32
Nodes (10): CommentRepository, CommentService, TicketRepository, TicketService, optional, org.springframework.context.ApplicationEventPublisher, org.springframework.data.jpa.repository.JpaRepository, org.springframework.data.jpa.repository.JpaSpecificationExecutor (+2 more)

### Community 76 - "tickets.ts"
Cohesion: 0.05
Nodes (59): askQuestion(), AskRequest, AskResponse, AskSource, addComment(), ApiError, changeStatus(), CommentResponse (+51 more)

### Community 78 - "org.springframework.http.ResponseEntity"
Cohesion: 0.47
Nodes (4): jakarta.servlet.http.HttpServletRequest, org.springframework.http.ProblemDetail, org.springframework.http.ResponseEntity, org.springframework.web.bind.annotation.ExceptionHandler

### Community 79 - ".ticketId"
Cohesion: 0.14
Nodes (18): TicketIndexerTest, Ask response, Source entry, Sources and citation rules, Implementation for User Story 1, Implementation for User Story 2, Phase 3: User Story 1 — Ingestion and re-index (Priority: P1) 🎯 MVP, Phase 4: User Story 2 — Ask endpoint (Priority: P1) (+10 more)

### Community 80 - "Quickstart: Support Ticket Management"
Cohesion: 0.25
Nodes (8): Backend, Create, edit, comment, Frontend, Prerequisites, Quickstart: Support Ticket Management, Restart, Seeded list, Status

### Community 81 - "User Scenarios & Testing *(mandatory)*"
Cohesion: 0.17
Nodes (12): Edge Cases, Feature Specification: AI Assistant Over Ticket History, Functional Requirements, Key Entities *(include if feature involves data)*, Measurable Outcomes, Requirements *(mandatory)*, Success Criteria *(mandatory)*, User Scenarios & Testing *(mandatory)* (+4 more)

### Community 82 - "Completion Report"
Cohesion: 0.18
Nodes (10): 2026-10-08 15:49:19Z, Completion Report, Independent test criteria, Next Actions, Parallel opportunities, Required items covered, Specification Analysis Report, Structure (matches `plan.md`) (+2 more)

### Community 83 - "RAG ingestion"
Cohesion: 0.20
Nodes (10): Chunk text, Chunking, Configuration, Failure behavior, Metadata (every chunk), nomic-embed-text prefixes, Pipeline, RAG ingestion (+2 more)

### Community 85 - "allowedNext"
Cohesion: 0.67
Nodes (4): allowedNext(), Implementation for User Story 2, Phase 4: User Story 2 - Move a ticket through its allowed statuses (Slice 4) (Priority: P1), Tests for User Story 2

### Community 86 - "Data Model: Support Ticket Management"
Cohesion: 0.25
Nodes (8): Comment, Data Model: Support Ticket Management, Human ID sequence, List query, Priority, Seed, Status transitions, Validation summary

### Community 88 - "TicketManagementApplication.java"
Cohesion: 0.43
Nodes (5): TicketManagementApplication, org.springframework.boot.autoconfigure.SpringBootApplication, org.springframework.boot.context.properties.EnableConfigurationProperties, org.springframework.scheduling.annotation.EnableAsync, springapplication

### Community 89 - "Implementation Plan: AI Assistant Over Ticket History"
Cohesion: 0.22
Nodes (9): Complexity Tracking, Constitution Check, Documentation (this feature), Implementation Plan: AI Assistant Over Ticket History, Implementation slices (3 user stories), Project Structure, Source Code (repository root), Summary (+1 more)

### Community 90 - "ai-mistakes.md"
Cohesion: 0.40
Nodes (4): Agent misread existing path and misreported its own edit (spec/ vs specs/), First live eval missed shipment tickets and dropped uncited answers, Plan asserted "pay" matches "password" (ungrounded test expectation), RAG plan left ticket ID and priority out of the chunk text

## Knowledge Gaps
- **398 isolated node(s):** `common.sh script`, `com.ticketmanagement:ticket-management`, `LOW`, `MEDIUM`, `HIGH` (+393 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 493 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **23 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `TicketDetailPage()` connect `tickets.ts` to `.assignee`, `Ticket`, `UpdateTicketRequest`, `Tasks: Support Ticket Management`?**
  _High betweenness centrality (0.064) - this node is a cross-community bridge._
- **Why does `Test strategy` connect `.ticketId` to `ApiExceptionHandler`, `test-strategy.md`, `org.junit.jupiter.api.Test`, `TicketStatusTest`, `TicketStatusTransitionTest.java`, `RagControllerTest.java`?**
  _High betweenness centrality (0.044) - this node is a cross-community bridge._
- **Why does `2026-10-07 16:12:31Z` connect `.assignee` to `ApiExceptionHandler`, `tickets.ts`, `.ticketId`, `org.junit.jupiter.api.Test`, `TicketStatusTransitionTest.java`, `allowedNext`, `RagControllerTest.java`?**
  _High betweenness centrality (0.042) - this node is a cross-community bridge._
- **Are the 7 inferred relationships involving `TicketValidationTest` (e.g. with `Technical Context` and `Backend`) actually correct?**
  _`TicketValidationTest` has 7 INFERRED edges - model-reasoned connections that need verification._
- **Are the 8 inferred relationships involving `ApiExceptionHandler` (e.g. with `Notes` and `Tests for User Story 1`) actually correct?**
  _`ApiExceptionHandler` has 8 INFERRED edges - model-reasoned connections that need verification._
- **What connects `common.sh script`, `com.ticketmanagement:ticket-management`, `LOW` to the rest of the system?**
  _398 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `ApiExceptionHandler` be split into smaller, more focused modules?**
  _Cohesion score 0.14285714285714285 - nodes in this community are weakly interconnected._