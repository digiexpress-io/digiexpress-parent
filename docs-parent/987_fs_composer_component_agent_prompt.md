# Agent Prompt: fs-composer Component

## Setup

Read `digiexpress-parent/docs-parent/900_agent_rules.md` before starting work. All rules in that file apply to this task.

## Required Input

Before starting work, the invoker must provide the following. Do not proceed without all of them.

- **Change type** — one of:
  - `ui_update` — a styling or UX change with no backend involvement
  - `field_change` — adding or updating a data field across the stack

If `ui_update`, also provide:
- **Description** — what the UI/UX change is and where in `fs-composer` it applies.

If `field_change`, also provide:
- **Field name** — the name of the new field in snake_case (used for the database column). The agent derives camelCase (TypeScript), PascalCase (Java interfaces), and kebab-case (file names) from this.
- **Value type** — one of: `scalar string`, `scalar number`, `array of strings`, `array of key/text objects`. Determines `TEXT` vs `JSONB` in the database, `Optional<>` vs `List<>` in Java, and the `isDirty` comparison strategy in the UI.
- **Purpose** — one sentence describing what the field stores.
- **Operation** — one of: `new` (field does not exist anywhere in the stack), `update` (field exists in the database and backend but needs frontend changes).

## Task / Goal

**If `ui_update`:** Apply the described UI/UX change directly to the relevant files in `fs-composer`. Read the affected components first to understand the existing pattern, then make the change. Do not touch backend files.

**If `field_change`:** Using the required input and the pattern reference in `digiexpress-parent/docs-parent/116_fs_composer_add_field.md`, implement the new field across all layers in order: database schema, persistence entity, backend model and authoring, REST endpoint, frontend fetch hook, provider and context, TypeScript types, and UI component. Edit or create files at each layer.

## Reference

Read `digiexpress-parent/docs-parent/116_fs_composer_add_field.md` in full before touching any files. This document describes the established patterns for every layer. Use it as the sole source of truth for naming conventions, code shapes, and ordering constraints. Do not deviate from the patterns it describes.

## Rules

- Never use hardcoded hex color values. All colors must come from `fs-theme` or `eveli-theme`. If no suitable token exists, use MUI color manipulation functions (`alpha`, `lighten`, `darken`) applied to an existing theme token.
- Never use `sx` props or inline `style` props. All styling must go through `useUtilityClasses` and the module's styled components. For portal-rendered content (Tooltip, Popover, Dialog internals) where ancestor-scoped CSS cannot reach: use a CSS `:hover` sibling approach to keep elements in the component tree, or export a named `styled('element')` from `useUtilityClasses.ts`. If a MUI component needs internal slot overrides, use `componentsProps`/`slotProps` with a `className` (not `style`), and define the class in the module's styled component.

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
