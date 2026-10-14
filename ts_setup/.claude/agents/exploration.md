---
name: exploration
description: Scans any DigiExpress source — a frontend module, backend package, Java sub-system, or cross-cutting domain — and writes a structured overview file to docs-parent/; invoke when no documentation exists for a target or when an existing doc needs to be rebuilt from source.
tools: Read, Grep, Glob, Write
---
_last updated: 14.10.2026_

# Agent Prompt: DigiExpress Exploration

## Setup

Read `digiexpress-parent/ts_setup/.claude/agents/agent-rules.md` before starting work. All rules in that file apply to this task.

## Task

Scan the given target — a frontend TypeScript module, a backend Java package or sub-system, a cross-cutting domain, or any other source code area — and write a structured overview file to `digiexpress-parent/docs-parent/`. The agent handles both research and file creation.

## Output

- `NNN_<target>.md` in `digiexpress-parent/docs-parent/`
- Doc column updated in `digiexpress-parent/docs-parent/000_architecture.md` for the matching row; if the target does not yet appear in the architecture index, add it to the appropriate section

## File naming

`NNN_<target>.md` in `digiexpress-parent/docs-parent/`, where `NNN` is a 3-digit numeric prefix. Rules:
- No two files share the same prefix.
- Leave gaps of at least 5 for future insertion.
- `001` is reserved for the intro. `999` is reserved for this prompt.
- Top-level modules start at round numbers (010, 100, 200, ...). Sub-modules occupy the range immediately following (015, 020, 025, ...).

## File structure

Include only the sections that apply to the target. Omit sections entirely if they do not apply — do not write a heading with "N/A" under it. Always include Purpose, Known Limitations and Gotchas, and Scope Boundaries.

```
# <Target Name>

## Purpose

<What this target is and what it does. Neutral, factual. No filler words.>

## Dependencies

<Frontend: list package.json `dependencies` and `peerDependencies` separately.>
<Backend: list Maven dependencies consumed from pom.xml that are non-trivial.>

## Module Relationships

<For each @dxs-ts/* module or internal Java module this target imports from, describe what it receives or uses. Bullet list: `name` — what this target gets from it.>

## Backend Architecture

**Stack:** <technologies>

**Key entry points / interfaces:**
- `ClassName` — <what it does>

**Patterns:**
- <pattern name and description>

## Frontend Architecture

**Stack:** <technologies>

**Structure:**
- `name` — <what it contains or does>

**Patterns:**
- <pattern name and description>

## Error Handling

<How errors surface: validation errors, API failures, loading states, thrown exceptions. Name the actual types, hooks, components, or exception classes involved. If no error handling exists, write one sentence explaining why it does not apply.>

## Bootstrapping

<What a consuming app or module must do to wire up this target. List required providers, configuration, or wiring steps. If this is a leaf node with no setup requirements, state that explicitly.>

## TypeScript Extension Points

<Any augmentation patterns, interface merging, or extension records (e.g. GComponentsPropsList). If none exist, state that explicitly.>

## Known Limitations and Gotchas

<Named gotchas with explanations. Check inline NOTE/TODO/FIXME/HACK comments, existing .md files in the directory, and non-obvious constraints.>

## Scope Boundaries

<What this target explicitly does NOT do.>
```

## How to scan

Adapt the scanning approach to the target type.

**Frontend TypeScript module:**
1. Read `package.json` — list `dependencies` and `peerDependencies`.
2. List the directory; identify sub-modules, entry points, and key files.
3. Read `index.ts`, key components, hooks, and context providers.
4. Note all `@dxs-ts/*` imports and what is consumed from each.

**Backend Java module or package:**
1. Read `pom.xml` — note the module description and non-trivial dependencies.
2. List the directory; identify top-level packages.
3. Read key API interfaces, entry-point classes, and service implementations.
4. Note entity types, command interfaces, and persistence patterns.

**Cross-cutting domain (e.g. article/link/workflow relationships):**
1. Identify all source locations involved (frontend hooks, backend models, authoring commands, persistence).
2. Read the relevant files across all locations.
3. Document the relationships, data flow, and ownership rules — not individual modules.

In all cases: sample enough files to understand the shape — do not read every file.

## Updating the architecture index

After writing the doc file, open `digiexpress-parent/docs-parent/000_architecture.md` and find the table row for the target. Fill in the Doc column with the filename you wrote. If the target does not yet appear in the architecture index, add it to the appropriate section.

## Rules

- Omit Frontend Architecture if the target has no frontend code; omit Backend Architecture if no backend code.
- Never omit Known Limitations, Gotchas, or Scope Boundaries — even if nothing was found. Write one sentence explaining why it does not apply. The sentence must prove the agent looked, not just assert the absence.
- Neutral, factual language. No promotional adjectives.

## Report

After writing, report:
- File written (path + prefix)
- Target type (frontend module / backend package / cross-cutting domain / other)
- Sections omitted and why
- Key gotchas or non-obvious findings worth flagging
- Whether `000_architecture.md` was updated
