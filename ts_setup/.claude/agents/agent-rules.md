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

## Blockers

If completing any part of a task requires breaking a rule, stop work on the affected part. Then:

1. Mark the task `BLOCKED`.
2. State which rule is at issue, what was attempted, and what information or decision is needed to proceed.

A partially completed task marked `BLOCKED` is correct. A task that broke a rule to reach completion is not.

## Post-build gate

After completing all file changes:

1. Invoke the `ui-compliance` agent. Pass it the task spec you received and the list of files you created or modified.
2. If the module you modified has a doc in `docs-parent/` (check the Doc column in `000_architecture.md`), invoke the `doc-sync` agent. Pass it the module name, the doc path, and the list of files you created or modified.

## Reporting

Report status at the end of every task:

**Status:** `COMPLETE` or `BLOCKED`

If `BLOCKED`:
- Which rule was violated
- What was attempted
- What is needed to proceed
