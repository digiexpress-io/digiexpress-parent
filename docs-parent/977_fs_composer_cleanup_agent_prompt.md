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
