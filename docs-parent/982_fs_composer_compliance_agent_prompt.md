# Agent Prompt: fs-composer Code Compliance

## Setup

Read `digiexpress-parent/docs-parent/900_agent_rules.md` before starting work. All rules in that file apply to this task.

## Required Input

Before starting work, the invoker must provide the following. Do not proceed without all of them.

- **Task specification** — the original description of what the previous agent was asked to do (copy the user's request verbatim).
- **Files modified** — the list of files the previous agent created or changed.

## Task / Goal

Review the modified files against three criteria, in order:

1. **Completion** — Did the previous agent fulfil the task specification? Check each stated requirement against the actual code changes. Flag any requirement that is missing, incomplete, or implemented differently than specified.

2. **Coding standards** — Check every modified file against the rules in `digiexpress-parent/docs-parent/claude-brains/CODING_STANDARDS.md` and `digiexpress-parent/docs-parent/900_agent_rules.md`. Flag every violation with the file path, line number, rule broken, and what the correct form should be.

3. **Anti-patterns and bugs** — Identify any bugs, logic errors, or anti-patterns in the modified files regardless of whether they relate to the task. Include pre-existing issues only if they are in code the previous agent touched.

## Reference

Read the following files in full before reviewing any code:

- `/Users/jocelyn/Development/projects/digiexpress-parent/claude-brains/CODING_STANDARDS.md` — project-wide coding rules
- `digiexpress-parent/docs-parent/900_agent_rules.md` — DigiExpress-specific rules (CSS, barrel exports, language)

## Report

**Status:** `PASS` or `FAIL`

### Completion

| Requirement | Status | Notes |
|-------------|--------|-------|
| `<requirement from spec>` | DONE / PARTIAL / MISSING | `<detail>` |

### Coding Standards Violations

List each violation as:
- `<file>:<line>` — Rule: `<rule name>` — Found: `<what is there>` — Expected: `<correct form>`

If none: "No violations found."

### Anti-patterns and Bugs

List each finding as:
- `<file>:<line>` — `<description of issue and why it is a problem>`

If none: "No issues found."
