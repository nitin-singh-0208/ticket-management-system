# Graph Report - ticket-management-system  (2026-10-07)

## Corpus Check
- 105 files · ~129,232 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 13 file(s) not represented in the graph (top: (none) 8, .mdc 4, .toml 1)

## Summary
- 816 nodes · 1381 edges · 55 communities (42 shown, 13 thin omitted)
- Extraction: 87% EXTRACTED · 13% INFERRED · 0% AMBIGUOUS · INFERRED: 181 edges (avg confidence: 0.88)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `b605c936`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- ApiExceptionHandler.java
- state-machine.md
- UpdateTicketRequest
- common.sh
- package.json
- Tasks: [FEATURE NAME]
- speckit-analyze/SKILL.md
- compilerOptions
- User Scenarios & Testing *(mandatory)*
- Execution Steps
- Research: Support Ticket Management
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
- TicketValidationTest.java
- WebConfig
- speckit-clarify/SKILL.md
- speckit-implement/SKILL.md
- speckit-constitution/SKILL.md
- ValidUpdateTicket.java
- speckit-taskstoissues/SKILL.md
- [CHECKLIST TYPE] Checklist: [FEATURE NAME]
- tickets.ts
- ai-mistakes.md
- 2026-09-30 07:46:29Z
- generate-tests.md
- review-code.md
- review-rag-output.md
- review-spec.md
- 2026-09-29_08-24-53Z-create-the-following-project.md
- 2026-10-07_17-30-01Z-support-ticket-phase-1.md
- com.ticketmanagement:ticket-management
- TicketStatus
- TicketController.java
- Comment.java
- 2026-10-07_17-54-00Z-support-ticket-implementation-phases.md
- TicketValidationTest
- TicketStatusTest
- TicketStatusTransitionTest.java
- Quickstart: Support Ticket Management
- Requirements
- Specification Quality Checklist: Support Ticket Management
- Architecture
- Data model

## God Nodes (most connected - your core abstractions)
1. `TicketValidationTest` - 37 edges
2. `Ticket` - 34 edges
3. `TicketStatus` - 27 edges
4. `ApiExceptionHandler` - 26 edges
5. `UpdateTicketRequest` - 20 edges
6. `TicketStatusTransitionTest` - 18 edges
7. `TicketPriority` - 17 edges
8. `TicketService` - 17 edges
9. `Tasks: Support Ticket Management` - 17 edges
10. `Comment` - 16 edges

## Surprising Connections (you probably didn't know these)
- `Constitution Check` --references--> `TicketStatusTransitionTest`  [INFERRED]
  specs/001-support-ticket-management/plan.md → backend/src/test/java/com/ticketmanagement/ticket/TicketStatusTransitionTest.java
- `2026-10-07 17:10:37Z` --references--> `FieldError`  [INFERRED]
  .specstory/history/2026-10-07_17-10-37Z-phase-1-setup-implementation.md → frontend/src/api/tickets.ts
- `Parallel opportunities` --references--> `TicketDetailPage()`  [INFERRED]
  specs/001-support-ticket-management/tasks.md → frontend/src/pages/TicketDetailPage.tsx
- `Notes` --references--> `ApiExceptionHandler`  [INFERRED]
  specs/001-support-ticket-management/tasks.md → backend/src/main/java/com/ticketmanagement/common/ApiExceptionHandler.java
- `Tests for User Story 1` --references--> `ApiExceptionHandler`  [INFERRED]
  specs/001-support-ticket-management/tasks.md → backend/src/main/java/com/ticketmanagement/common/ApiExceptionHandler.java

## Import Cycles
- None detected.

## Communities (55 total, 13 thin omitted)

### Community 0 - "ApiExceptionHandler.java"
Cohesion: 0.11
Nodes (27): ApiExceptionHandler, FieldErrorItem, Override, TicketManagementApplication, com.fasterxml.jackson.databind.exc.InvalidFormatException, invalidnullexception, jakarta.servlet.http.HttpServletRequest, jsonmappingexception (+19 more)

### Community 1 - "state-machine.md"
Cohesion: 0.27
Nodes (3): API contract, Errors, State machine

### Community 2 - "UpdateTicketRequest"
Cohesion: 0.05
Nodes (49): UpdateTicketRequest, Comment, Data Model: Support Ticket Management, Human ID sequence, List query, Priority, Seed, Status transitions (+41 more)

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

### Community 10 - "Research: Support Ticket Management"
Cohesion: 0.15
Nodes (13): Concurrent edits, CORS, Database image, Human ID allocation, List query versus "no pages", Local database password, Monorepo layout, Performance and scale (+5 more)

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
Cohesion: 0.09
Nodes (11): CreateTicketRequest, TicketSummary, Ticket, TicketPriority, HIGH, LOW, MEDIUM, jsonsetter (+3 more)

### Community 20 - "Implementation Plan: Support Ticket Management"
Cohesion: 0.22
Nodes (9): Complexity Tracking, Constitution Check, Documentation (this feature), Implementation Plan: Support Ticket Management, Implementation slices, Project Structure, Source Code (repository root), Summary (+1 more)

### Community 21 - "Documentation Guidelines"
Cohesion: 0.25
Nodes (7): Accuracy, Decisions, Diagrams, Documentation Guidelines, Keep docs with the change, Shape, Where docs live

### Community 22 - "speckit-checklist/SKILL.md"
Cohesion: 0.25
Nodes (7): Anti-Examples: What NOT To Do, Checklist Purpose: "Unit Tests for English", Example Checklist Types & Sample Items, Execution Steps, Post-Execution Checks, Pre-Execution Checks, User Input

### Community 23 - "TicketValidationTest.java"
Cohesion: 0.15
Nodes (15): autowired, CommentValidationTest, get, hassize, is, mockitobean, nullvalue, org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest (+7 more)

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

### Community 28 - "ValidUpdateTicket.java"
Cohesion: 0.20
Nodes (12): Override, UpdateTicketRequestValidator, ValidUpdateTicket, elementtype, jakarta.validation.Constraint, jakarta.validation.ConstraintValidator, jakarta.validation.ConstraintValidatorContext, jakarta.validation.Payload (+4 more)

### Community 29 - "speckit-taskstoissues/SKILL.md"
Cohesion: 0.40
Nodes (4): Outline, Post-Execution Checks, Pre-Execution Checks, User Input

### Community 30 - "[CHECKLIST TYPE] Checklist: [FEATURE NAME]"
Cohesion: 0.40
Nodes (4): [Category 1], [Category 2], [CHECKLIST TYPE] Checklist: [FEATURE NAME], Notes

### Community 31 - "tickets.ts"
Cohesion: 0.09
Nodes (39): addComment(), ApiError, changeStatus(), CommentResponse, CreateCommentRequest, createTicket(), CreateTicketRequest, FieldError (+31 more)

### Community 44 - "TicketStatus"
Cohesion: 0.07
Nodes (29): assertequals, assertfalse, asserttrue, StatusChangeRequest, StatusChangeNotAllowedException, allowedNext(), TicketStatus, CANCELLED (+21 more)

### Community 45 - "TicketController.java"
Cohesion: 0.06
Nodes (38): arraylist, Comment, CommentController, CommentRepository, CommentService, CommentResponse, CreateCommentRequest, InvalidListQueryException (+30 more)

### Community 48 - "Comment.java"
Cohesion: 0.20
Nodes (13): column, enumerated, enumtype, eventtype, fetchtype, generated, generatedvalue, generationtype (+5 more)

### Community 51 - "TicketStatusTest"
Cohesion: 0.15
Nodes (6): TicketStatusTest, org.junit.jupiter.params.provider.Arguments, Tests, AI / retrieval evaluation, Frontend, Test strategy

### Community 52 - "TicketStatusTransitionTest.java"
Cohesion: 0.20
Nodes (12): assertnotnull, TicketStatusTransitionTest, container, dockerimagename, jsonpath, mediatype, org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc, org.springframework.boot.test.context.SpringBootTest (+4 more)

### Community 56 - "Quickstart: Support Ticket Management"
Cohesion: 0.25
Nodes (8): Backend, Create, edit, comment, Frontend, Prerequisites, Quickstart: Support Ticket Management, Restart, Seeded list, Status

### Community 57 - "Requirements"
Cohesion: 0.25
Nodes (7): Acceptance criteria, Functional requirements, Non-functional requirements, RAG and assistant, Requirements, Spec layout, State machine

### Community 59 - "Specification Quality Checklist: Support Ticket Management"
Cohesion: 0.29
Nodes (5): Content Quality, Feature Readiness, Notes, Requirement Completeness, Specification Quality Checklist: Support Ticket Management

### Community 60 - "Architecture"
Cohesion: 0.33
Nodes (6): Architecture, Chunking strategy, Decisions, Embedding model choice, RAG pipeline, Shape

### Community 61 - "Data model"
Cohesion: 0.50
Nodes (4): Comment, Data model, Search and list, Ticket

## Knowledge Gaps
- **305 isolated node(s):** `common.sh script`, `com.ticketmanagement:ticket-management`, `LOW`, `MEDIUM`, `HIGH` (+300 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 375 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **13 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `TicketValidationTest` connect `TicketValidationTest` to `ApiExceptionHandler.java`, `UpdateTicketRequest`, `TicketController.java`, `TicketStatusTest`, `Implementation Plan: Support Ticket Management`, `TicketStatusTransitionTest.java`, `TicketValidationTest.java`, `Quickstart: Support Ticket Management`, `tickets.ts`?**
  _High betweenness centrality (0.082) - this node is a cross-community bridge._
- **Why does `ApiExceptionHandler` connect `ApiExceptionHandler.java` to `UpdateTicketRequest`, `TicketStatus`, `TicketValidationTest`, `TicketStatusTest`, `TicketValidationTest.java`?**
  _High betweenness centrality (0.065) - this node is a cross-community bridge._
- **Why does `Test strategy` connect `TicketStatusTest` to `ApiExceptionHandler.java`, `state-machine.md`, `TicketValidationTest`, `TicketStatusTransitionTest.java`, `TicketValidationTest.java`?**
  _High betweenness centrality (0.055) - this node is a cross-community bridge._
- **Are the 7 inferred relationships involving `TicketValidationTest` (e.g. with `Technical Context` and `Backend`) actually correct?**
  _`TicketValidationTest` has 7 INFERRED edges - model-reasoned connections that need verification._
- **Are the 7 inferred relationships involving `ApiExceptionHandler` (e.g. with `Notes` and `Tests for User Story 1`) actually correct?**
  _`ApiExceptionHandler` has 7 INFERRED edges - model-reasoned connections that need verification._
- **What connects `common.sh script`, `com.ticketmanagement:ticket-management`, `LOW` to the rest of the system?**
  _305 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `ApiExceptionHandler.java` be split into smaller, more focused modules?**
  _Cohesion score 0.1073170731707317 - nodes in this community are weakly interconnected._