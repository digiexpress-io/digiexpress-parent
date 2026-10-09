# Agent Prompt: fs-composer Component

## Setup

Read `digiexpress-parent/docs-parent/900_agent_rules.md` before starting work. All rules in that file apply to this task.

## Required Input

Before starting work, the invoker must provide the following. Do not proceed without all of them.

- **Field name** — the name of the new field in snake_case (used for the database column). The agent derives camelCase (TypeScript), PascalCase (Java interfaces), and kebab-case (file names) from this.
- **Value type** — one of: `scalar string`, `scalar number`, `array of strings`, `array of key/text objects`. Determines `TEXT` vs `JSONB` in the database, `Optional<>` vs `List<>` in Java, and the `isDirty` comparison strategy in the UI.
- **Purpose** — one sentence describing what the field stores.
- **Operation** — one of: `new` (field does not exist anywhere in the stack), `update` (field exists in the database and backend but needs frontend changes).

## Task / Goal

Using the required input and the pattern reference in `digiexpress-parent/docs-parent/116_fs_composer_add_field.md`, implement the new field across all layers in order: database schema, persistence entity, backend model and authoring, REST endpoint, frontend fetch hook, provider and context, TypeScript types, and UI component. Edit or create files at each layer. Do not write a guide — make the actual changes.

## Reference

Read `digiexpress-parent/docs-parent/116_fs_composer_add_field.md` in full before touching any files. This document describes the established patterns for every layer. Use it as the sole source of truth for naming conventions, code shapes, and ordering constraints. Do not deviate from the patterns it describes.

## Blockers

If completing any part of this task requires breaking a rule or inventing a pattern not present in `116_fs_composer_add_field.md`, do NOT proceed. Instead:
1. Stop work on the affected part.
2. Mark the task `BLOCKED`.
3. State which layer is at issue, what was attempted, and what information or decision is needed to proceed.

A partially completed task marked `BLOCKED` is correct. A fully completed task that invented patterns is not.

## Report

After completing or blocking, report:

**Status:** `COMPLETE` or `BLOCKED`

If `BLOCKED`:
- Which layer was blocked
- What was attempted
- What is needed to proceed

If `COMPLETE`:
- Files created or modified, listed by layer
- Any non-obvious decisions made (e.g. TEXT vs JSONB choice, naming derivation)
- Any pre-existing bugs or inconsistencies encountered
