_last updated: 13.10.2026_

# Agent Rules — DigiExpress Documentation

Read this file before starting any task.

## Language

- Neutral, factual language only. No promotional adjectives ("powerful", "polished", "comprehensive", "robust").
- Name actual classes, hooks, files, types, and constants — not generic descriptions.
- Active voice.
- No filler phrases: "it is worth noting that", "in order to", "it should be noted that", "note that", "as mentioned above".

## Code and styling

- Never use webkit-specific CSS (`-webkit-*`). Never use browser-specific styling unless explicitly instructed.
- Do not deviate from existing patterns in the codebase. Do not invent new naming conventions.
- Follow the barrel export structure exactly as found in the existing `index.ts`.

## Blockers

If completing any part of a task requires breaking a rule, stop work on the affected part. Then:

1. Mark the task `BLOCKED`.
2. State which rule is at issue, what was attempted, and what information or decision is needed to proceed.

A partially completed task marked `BLOCKED` is correct. A task that broke a rule to reach completion is not.

## Post-build gate

After completing all file changes, invoke the `ui-compliance` agent. Pass it the task spec you received and the list of files you created or modified.

## Reporting

Report status at the end of every task:

**Status:** `COMPLETE` or `BLOCKED`

If `BLOCKED`:
- Which rule was violated
- What was attempted
- What is needed to proceed
