# Agent Prompt: fs-composer Cleanup

## Setup

Read `digiexpress-parent/docs-parent/900_agent_rules.md` before starting work. All rules in that file apply to this task.

## Required Input

Before starting work, the invoker must provide the following. Do not proceed without all of them.

- **Compliance report** — the full output from `982_fs_composer_compliance_agent_prompt.md` for the changes being cleaned up.
- **Files to fix** — the list of files the compliance agent reviewed.

## Task / Goal

Fix every issue listed in the compliance report. Work through the three sections in order:

1. **Coding Standards Violations** — fix each violation exactly as the report describes.
2. **Anti-patterns and Bugs** — fix each finding. If a fix requires a decision (e.g. choosing an `alt` text value), use the most specific available value from the surrounding code (e.g. `image.resourceName`, `image.name`, or `""` if purely decorative).
3. **Completion gaps** — if any requirement is listed as `PARTIAL` or `MISSING`, implement it.

Read each file before editing it. Make only the changes required by the report — do not refactor, reformat, or touch unrelated code.

## Additional Scrutiny

After fixing all report items, review the modified files for the following and fix anything found:

- **Redundant styles** — CSS properties that are set but have no visible effect (e.g. overridden immediately by a more specific rule, or duplicated in two places).
- **Unused styles** — class slots defined in `useUtilityClasses` or rules in the styled root component that are not referenced anywhere in the component files.
- **Redundant `useState`** — local state that duplicates something already tracked elsewhere, or state that is set but never read, or state that could be replaced with a derived value.
- **Overly verbose statements** — multi-step variable chains that can be expressed more directly without changing behaviour.

Only remove or simplify what is clearly redundant or unused. Do not restructure logic or change behaviour.

## Reference

- `/Users/jocelyn/Development/projects/digiexpress-parent/claude-brains/CODING_STANDARDS.md` — project-wide coding rules
- `digiexpress-parent/docs-parent/900_agent_rules.md` — DigiExpress-specific rules

## Blockers

If fixing an issue requires inventing a pattern not established in the surrounding code or reference files, do NOT proceed. Instead:
1. Stop work on the affected fix.
2. Mark the task `BLOCKED`.
3. State which report item is at issue, what was attempted, and what decision is needed to proceed.

## Report

**Status:** `COMPLETE` or `BLOCKED`

If `BLOCKED`:
- Which report item caused the block
- What was attempted
- What is needed to proceed

If `COMPLETE`:
- Each fix applied, listed by file and line
- Any report item skipped and why
