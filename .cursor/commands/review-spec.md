# Review spec

If the request references specific specs files, review only those.

Otherwise review all files in `specs/`.

Check each requirement for:

- Ambiguity
- Missing edge cases
- Missing error cases
- Contradictions with other spec files
- Statements that cannot be tested

Check that each requirement traces back to `specs/requirements.md`. Flag anything in the spec that has no source requirement (scope creep).

When reviewing `specs/requirements.md` itself, check it against the original requirements source provided in the request. Flag any source requirement that is missing, and any requirement not present in the source.

Flag any invented numbers, APIs, or behaviour that are not backed by requirements or an explicit decision.

Report each finding with:

- Severity: High, Medium, or Low
- Location: file and section
- The problem
- A suggested rewrite

Only report findings you can point to in the text. Do not speculate.

Do not modify any files. Report only.

End with a one-line verdict: Ready for planning / Needs revision.
