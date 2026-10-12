---
name: agent-creation
description: Creates a new Claude Code agent prompt file by first running exploration to discover patterns, then writing a structured agent prompt to the agents directory; invoke when you need to scaffold a new agent for a task type that doesn't yet have one.
tools: Read, Grep, Glob, Edit, Write, Agent
---

# Agent Prompt: Create a New Agent Prompt

## Setup

Read `digiexpress-parent/ts_setup/.claude/agents/agent-rules.md` before starting work. All rules in that file apply to this task.

## Required Input

Do not proceed without all of the following.

- **Agent name** — a short kebab-case name (e.g. `fs-composer-field`). Used in the output filename.
- **Goal** — one sentence: what invoking this agent accomplishes. Frame it as an action (e.g. "Add a new field across all layers of the fs-composer stack").
- **Agent type** — one of:
  - `exploration` — reads source code, writes a documentation file
  - `creation` — reads source code and/or reference docs, creates or modifies code files
  - `analysis` — reads existing documentation, rewrites or annotates it in place
- **Starting points** — one or more files or directories to begin exploration from.

## Task / Goal

This agent runs in two phases.

**Phase 1 — Explore**

Run `exploration.md` against the provided starting points to determine:
- What files a real invocation would need to read as reference (patterns, conventions, existing implementations).
- What files a real invocation would need to create or modify.
- What information the invoker must supply per run (e.g. field name, component name, type) — infer this from what varies between existing instances of the same operation.
- Any ordering constraints, naming conventions, or gotchas that must be baked into the agent prompt.

Use `999_exploration_agent_prompt.md` for scanning only. The output of Phase 1 is internal findings, not a file.

**Phase 2 — Write the agent prompt**

Write a new agent prompt file to `digiexpress-parent/ts_setup/.claude/agents/` following the structure below exactly.

Before writing:
1. Read the "Already documented" table in `digiexpress-parent/ts_setup/.claude/agents/exploration.md` to identify all used prefixes in the 901–999 range.
2. Select an unused prefix between 901 and 999. Leave gaps of at least 5 between existing entries where possible.
3. Write the file as `{agent-name}.md`.
4. Add an entry for the new file to the "Already documented" table in `exploration.md` and update the "Next available prefix" line.

## Output structure

The generated agent prompt must follow this structure exactly. Omit sections marked optional if they do not apply to the agent type.

```markdown
# Agent Prompt: <Agent Name>

## Setup

Read `digiexpress-parent/ts_setup/.claude/agents/agent-rules.md` before starting work. All rules in that file apply to this task.

## Required Input                     ← omit if the agent needs no per-invocation context

Do not proceed without all of the following.

- **<field>** — <what it is and why it is needed>

## Task / Goal

<One paragraph: what the agent does, in what order, and what it produces or modifies.>

## Reference                          ← include for creation and analysis agents; omit for exploration agents

Read the following files in full before touching any files. Use them as the sole source of truth for naming conventions, code shapes, and ordering constraints. Do not deviate from the patterns they describe.

- `<path>` — <what pattern or convention this file establishes>

## What to investigate                ← include only for exploration agents

### 1. <Topic>
<What to look for and where.>

### 2. <Topic>
...

## Blockers                           ← include only for creation agents

If completing any part of this task requires breaking a rule or inventing a pattern not present in the reference files, do NOT proceed. Instead:
1. Stop work on the affected part.
2. Mark the task `BLOCKED`.
3. State which part is at issue, what was attempted, and what information or decision is needed to proceed.

A partially completed task marked `BLOCKED` is correct. A fully completed task that invented patterns is not.

## Report

After completing or blocking, report:

**Status:** `COMPLETE` or `BLOCKED`        ← omit BLOCKED branch for exploration and analysis agents

If `BLOCKED`:
- Which part was blocked
- What was attempted
- What is needed to proceed

If `COMPLETE`:
- <field relevant to this agent's output>
```

## Rules

- Omit `## Required Input` if the agent needs no per-invocation context.
- Omit `## Reference` for exploration agents — they read source directly via `## What to investigate`.
- Omit `## What to investigate` for creation and analysis agents — they read from `## Reference`.
- Omit `## Blockers` for exploration and analysis agents.
- The Reference section must list only files discovered during Phase 1 exploration — not files the invoker specified.
- Required Input must contain only what varies per invocation and cannot be baked in. File paths and patterns go in Reference, not Required Input.
- Prefix must be unique, between 901 and 999 inclusive.

## Report

After completing or blocking, report:

**Status:** `COMPLETE` or `BLOCKED`

If `BLOCKED`:
- What was attempted
- What is needed to proceed

If `COMPLETE`:
- File written and its path
- Prefix selected
- What Phase 1 exploration found (files read, key patterns identified)
- What Required Input fields were inferred and why
- Sections included and sections omitted (with reason for each omission)
