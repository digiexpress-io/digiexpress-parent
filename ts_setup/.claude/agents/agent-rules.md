_last updated: 13.10.2026_

# Agent Rules — DigiExpress Documentation

Read this file before starting any task.

## Architecture index

Read `digiexpress-parent/docs-parent/000_architecture.md` before starting any task. Use the cross-reference table to find where to look for your task, and the Doc column to locate the deep-dive doc for the module you are about to touch. If the Doc column is blank for that module, invoke the `exploration` agent to produce one before proceeding.

## Language

- Neutral, factual language only. No promotional adjectives ("powerful", "polished", "comprehensive", "robust").
- Name actual classes, hooks, files, types, and constants — not generic descriptions.
- Active voice.
- No filler phrases: "it is worth noting that", "in order to", "it should be noted that", "note that", "as mentioned above".

## Code and styling

Follow all rules in `digiexpress-parent/docs-parent/999_ui_coding_standards.md`.

## Reference fidelity

When a reference component or file is provided, match it pattern-for-pattern. Do not add logic, validation, formatting, state, event handling, or CSS that the reference does not have. If you believe something is needed that the reference does not demonstrate, stop, mark the task `BLOCKED`, and state what you think is missing and why. Do not invent a solution.

## Blockers

If completing any part of a task requires breaking a rule or adding logic not present in the reference, stop work on the affected part. Then:

1. Mark the task `BLOCKED`.
2. State which rule is at issue, what was attempted, and what information or decision is needed to proceed.

A partially completed task marked `BLOCKED` is correct. A task that broke a rule or invented patterns to reach completion is not.

## Post-build gate

Run these steps in order after completing all file changes. Do not skip or reorder them. Do not report `COMPLETE` until every applicable item is checked off.

You have the Agent tool available. Use it for each step — a self-review is never an acceptable substitute.

- [ ] **ui-compliance** — Invoke the `ui-compliance` agent using the Agent tool. Pass it the task spec and the full list of files created or modified.
  - [ ] If `FAIL`: invoke `ui-cleanup`, then invoke `ui-compliance` a second time.
  - [ ] If still `FAIL`: mark the task `BLOCKED` and stop. Do not proceed.
- [ ] **doc-sync** — Check the Doc column in `digiexpress-parent/docs-parent/000_architecture.md` for the module you modified. If a doc exists, invoke the `doc-sync` agent with the module name, doc path, and list of files. If no doc exists, check this off and skip it.

## Reporting

Report status at the end of every task:

**Status:** `COMPLETE` or `BLOCKED`

If `BLOCKED`:
- Which rule was violated
- What was attempted
- What is needed to proceed
