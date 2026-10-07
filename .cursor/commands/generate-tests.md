# Generate tests

If the request references specific files, generate tests only for those.

If the request references a spec file, derive tests from the behaviours and state transitions described there.

Otherwise generate tests for all uncommitted production code changes (staged, unstaged, and new untracked files). Skip test files and config-only changes.

Follow the project rules in `.cursor/rules/testing.mdc`:

- Prefer unit tests for domain and business logic; use Spring slice tests for one layer at a time.
- Name each test after the behaviour it describes.
- Structure every test as Arrange-Act-Assert.
- Cover every valid state transition and every invalid state transition with its own test.
- Keep tests independent; do not share mutable data between tests.
- Stub LLM and embedding clients; never call paid or external model APIs.
- For probabilistic AI output, use evaluation sets and acceptance thresholds — do not assert exact string matches on generated text.

Write tests only. Do not modify production code.

After writing tests, run them and report:

- Which test classes were created or updated
- The command used to run them
- Pass/fail count
- Any failures with the failing test name and error message
- If tests were not run, why

Never weaken, skip, or delete a failing test to make it pass.

End with a one-line verdict: All tests passing / N failures need attention.
