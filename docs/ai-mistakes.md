## Agent misread existing path and misreported its own edit (spec/ vs specs/)
- Phase: Steering files setup (review-spec command), 2026-09-29
- Prompt: add a rule to review-spec.md for checking specs/requirements.md against the source
- What the AI said: claimed the existing file used `spec/requirements.md`, overrode my explicit `specs/`, and its final summary said it used `spec/`
- Why it was wrong: the file only ever used `specs/` (see git log -p .cursor/commands/review-spec.md). Spec Kit v1.0.9 also creates specs/ folders, so `spec/` would have broken traceability
- How caught: reviewed the diff and the .specstory transcript; the AI's summary did not match the committed file
- Fix/lesson: committed `specs/` (7ddffdb); always check AI change summaries against the actual diff; never let the agent override an explicit instruction without asking
- Evidence: .specstory/history/2026-09-29_08-24-53Z-project-structure-creation.md

## Plan asserted "pay" matches "password" (ungrounded test expectation)
- Phase: /speckit-plan for 001-support-ticket-management
- What the AI produced: data-model.md and quickstart.md claimed a search for "pay" returns TKT-1011 because "password" contains "pay"
- Why it was wrong: "password" is p-a-s-s; it does not contain "pay". The quickstart check would fail or push the implementation toward wrong search logic
- How caught: manual review of the plan artefacts before /speckit-tasks
- Fix: corrected search checks in commit 9dbf6db
- Also caught in same review: the contract relied on rejecting unknown JSON properties, which Spring Boot ignores by default; made explicit in the plan
