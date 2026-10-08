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

## RAG plan left ticket ID and priority out of the chunk text

**When:** 8 Oct, while reviewing the /speckit-plan output for 002-ticket-rag-assistant

The plan had each chunk store only the raw text: the description, a comment, or the resolution notes. Ticket ID, title, status and priority were only stored as metadata on the vector row.

It looked fine until I went through the 5 questions from the assignment one by one:

- "What was the resolution for ticket TKT-1001?" The string TKT-1001 isn't anywhere in the embedded text, so similarity search has nothing to match on. It might find it, it might not.
- "Which high-priority tickets are related to payment?" The LLM can't answer this. It never sees the priority, because metadata doesn't go into the prompt unless you put it there.
- Some seed comments are just "Noted." On its own that embeds as basically nothing.

The plan's own evaluation-strategy.md expected these questions to pass, so it contradicted itself and nobody noticed. I caught it by asking Claude Code to review the plan and then checking each question by hand against the data model.

**Fix:** every chunk now starts with a header line, e.g. `Ticket TKT-1001 | Payment declined at checkout | status OPEN | priority HIGH | category Payment | section comment`, and the same header goes into the context sent to the LLM. Commit <sha>.

**Takeaway:** metadata is for filtering and citations. The model only knows what's actually in the text you embed and the prompt you send. Check each eval question against that, not against the schema.

## First live eval missed shipment tickets and dropped uncited answers

**When:** 8 Oct, first `mvn test -Dgroups=ai-eval` run for 002-ticket-rag-assistant

At similarity `0.6`, "Have we seen payment failures before?" retrieved TKT-1001 at `0.695` and grounded. "What are the common causes of shipment tracking issues?" retrieved nothing: the best shipment chunks were TKT-1007 `0.585`, TKT-1006 `0.577`, and TKT-1008 `0.577`. "Show me similar resolved tickets" also retrieved nothing; the best closed ticket, TKT-1004, scored `0.549`.

The resolution question for TKT-1001 did retrieve that ticket, then the model answered that the tickets did not contain the answer and named no id. The grounding check correctly discarded that text, so a ticket that was in the context became a no-match.

A later prompt also wrote ids it was rejecting ("TKT-1005 is not high priority"). Every `TKT-` id in the answer becomes a source, so naming a skipped ticket counted as citing it.

**Fix:** `app.rag.similarity-threshold` is `0.547`, which keeps TKT-1004 (`0.5486`, CLOSED) and drops the next chunk TKT-1005 (`0.5470`, CANCELLED). The system prompt tells the model to cite a matching ticket even when the requested section is missing, and not to write ids that do not match the question. Re-run of the eval suite passed 10/10.

**Takeaway:** log the score of the first chunk you expected, not only the chunks that passed the threshold. And treat every ticket id in the model text as a citation, including ids the model mentions in order to reject them.
