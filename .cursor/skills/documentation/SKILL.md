---
name: "documentation"
description: "Use when creating or updating project documentation: specs, README, architecture decisions, or docs/ files."
---

# Documentation Guidelines

Reusable documentation guidelines. Apply them when creating or updating project documentation.

## Where docs live

- Put specifications in `spec/`.
- Put project notes in `docs/`.
- Use `README.md` for setup and overview.

## Keep docs with the change

- Update documentation in the same change as the code it describes.

## Decisions

- Record significant decisions as short ADR-style entries: context, decision, alternatives considered, consequences.

## Diagrams

- Use Mermaid for diagrams.

## Accuracy

- Every factual statement must be traceable to code, a spec, or a cited source.
- Never invent behaviour, APIs, or numbers.

## Shape

- Keep docs concise.
- Prefer tables and lists over long prose.
