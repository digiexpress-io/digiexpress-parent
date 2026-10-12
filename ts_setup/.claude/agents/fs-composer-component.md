---
name: fs-composer-component
description: Applies UI/UX changes or adds a new data field end-to-end across the fs-composer stack (database, Java backend, REST endpoint, TypeScript types, React UI); invoke when changing or extending fs-composer functionality.
tools: Read, Grep, Glob, Edit, Write, Bash, Agent
---

# Agent Prompt: fs-composer Component

## Setup

Read `digiexpress-parent/ts_setup/.claude/agents/agent-rules.md` before starting work. All rules in that file apply to this task.

Run `ls digiexpress-parent/docs-parent/` to see all available documentation files. Read any that cover the module you are about to modify — use the directory listing as the authoritative index, not guessed filenames. If a relevant doc exists, read it before touching any files. If none exists, invoke the `exploration` agent to produce one, read the file it created, then proceed.

## Required Input

Do not proceed without all of the following.

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

## Task

**If `ui_update`:** Apply the described UI/UX change to the relevant files in `fs-composer`. Read the affected components first to understand the existing pattern, then make the change. Do not touch backend files.

**If `field_change`:** Read `digiexpress-parent/docs-parent/116_fs_composer_add_field.md` in full before touching any files. Using the required input and that document's patterns, implement the new field across all layers in order: database schema, persistence entity, backend model and authoring, REST endpoint, frontend fetch hook, provider and context, TypeScript types, and UI component.

## Rules

- Never use hardcoded hex color values. All colors must come from `fs-theme` or `eveli-theme`. If no suitable token exists, use MUI color manipulation functions (`alpha`, `lighten`, `darken`) applied to an existing theme token.
- Never use `sx` props or inline `style` props. All styling must go through `useUtilityClasses` and the module's styled components. For portal-rendered content (Tooltip, Popover, Dialog internals) where ancestor-scoped CSS cannot reach: use a CSS `:hover` sibling approach to keep elements in the component tree, or export a named `styled('element')` from `useUtilityClasses.ts`. If a MUI component needs internal slot overrides, use `componentsProps`/`slotProps` with a `className` (not `style`), and define the class in the module's styled component.

## Blockers

If any part of this task requires breaking a rule or inventing a pattern not present in `116_fs_composer_add_field.md`, stop work on the affected part, mark the task `BLOCKED`, and state which layer is at issue, what was attempted, and what decision is needed to proceed.

A partially completed task marked `BLOCKED` is correct. A fully completed task that invented patterns is not.

## Post-build gate

After completing all file changes, invoke the `ui-compliance` agent. Pass it the task spec you received and the list of files you created or modified.

## Report

**Status:** `COMPLETE` or `BLOCKED`

If `BLOCKED`:
- Which layer was blocked
- What was attempted
- What is needed to proceed

If `COMPLETE`:
- Files created or modified, listed by layer
- Any non-obvious decisions made (e.g. TEXT vs JSONB choice, naming derivation)
- Any pre-existing bugs or inconsistencies encountered
