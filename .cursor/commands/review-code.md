# Review code

If the request references specific files, review only those.

Otherwise review all uncommitted changes: staged (`git diff --cached`), unstaged (`git diff`), and new untracked files.

Check the changes against the project rules in `.cursor/rules/`:

- Java and Spring Boot
- API standards
- Testing

Look for:

- Correctness bugs
- Business logic in controllers
- Missing backend validation
- Inconsistent error handling
- Exposed persistence entities
- Hardcoded configuration or secrets
- Missing or weakened tests

Report each finding with:

- Severity: High, Medium, or Low
- Location: `file:line`
- The problem
- The suggested fix

Only report findings you can point to in the code. Do not speculate.

Do not modify any files. Report only.

End with a one-line verdict: Ready to commit / Needs fixes.
