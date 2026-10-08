# Agent Prompt: Create a New Agent Prompt

## Setup

Read `digiexpress-parent/docs-parent/900_agent_rules.md` before starting work. All rules in that file apply to this task.

## Required Input

Before starting work, the invoker must provide the following. Do not proceed without all of them.

- **Agent name** — a short kebab-case name (e.g. `gamut-shell-component`). Used in the output filename.
- **Purpose** — one sentence: what the agent does.
- **Agent type** — one of:
  - `exploration` — reads source code, writes a documentation file
  - `creation` — reads source code and/or docs, creates or modifies code files
  - `analysis` — reads existing documentation, rewrites or annotates it in place
- **What it reads** — source directories, doc files, or rules files the agent must read to do its work.
- **What it writes** — doc files, code files, or "rewrites in place."
- **Domain-specific rules** — any rules beyond `900_agent_rules.md` that apply to this agent. Write "none" if there are none.

## Task

1. Read the "Already documented" table in `digiexpress-parent/docs-parent/999_exploration_agent_prompt.md` to identify all used prefixes in the 901–999 range.
2. Select an unused prefix between 901 and 999 (inclusive). Leave gaps of at least 5 between existing entries where possible. Suggest this prefix to the invoker and confirm before writing.
3. Write the new agent prompt file to `digiexpress-parent/docs-parent/{prefix}_{agent-name}_agent_prompt.md` using the structure below.
4. Add an entry for the new file to the "Already documented" table in `digiexpress-parent/docs-parent/999_exploration_agent_prompt.md`.

## Output structure

The generated agent prompt must follow this structure exactly. Omit sections marked optional if they do not apply.

```markdown
# Agent Prompt: <Agent Name>

## Setup

Read `digiexpress-parent/docs-parent/900_agent_rules.md` before starting work. All rules in that file apply to this task.

## Required Input                          ← include only if the agent needs context before starting

Before starting work, the invoker must provide the following. Do not proceed without all of them.

- **<field>** — <what it is and why it is needed>

## Task / Goal

<One paragraph: what the agent does, in what order, and what it produces.>

## What to investigate                     ← include only for exploration agents

### 1. <Topic>
<What to look for and where.>

### 2. <Topic>
...

## Rules                                   ← include only for domain-specific rules beyond 900_agent_rules.md

- <rule>

## Blockers                                ← include only for creation agents

If completing any part of this task requires breaking a rule, do NOT break the rule. Instead:
1. Stop work on the affected part.
2. Mark the task `BLOCKED`.
3. State which rule is at issue, what was attempted, and what information or decision is needed to proceed.

A partially completed task marked `BLOCKED` is correct. A fully completed task that broke a rule is not.

## Report

After completing or blocking, report:

**Status:** `COMPLETE` or `BLOCKED`

If `BLOCKED`:
- Which rule was violated
- What was attempted
- What is needed to proceed

If `COMPLETE`:
- <field relevant to this agent's output>
- <field relevant to this agent's output>
```

## Rules

- The output file must follow the structure above exactly — no extra sections, no reordered sections.
- Omit `## Required Input` if the agent needs no context from the invoker.
- Omit `## What to investigate` for creation and analysis agents.
- Omit `## Blockers` for exploration and analysis agents.
- Omit `## Rules` if there are no domain-specific rules beyond `900_agent_rules.md`.
- The Report section for creation agents must always include `COMPLETE` / `BLOCKED` status. For exploration and analysis agents, status is always `COMPLETE` — omit the `BLOCKED` branch.
- Prefix must be unique, between 901 and 999 inclusive, and confirmed with the invoker before writing.

## Report

After completing or blocking, report:

**Status:** `COMPLETE` or `BLOCKED`

If `BLOCKED`:
- Which rule was violated
- What was attempted
- What is needed to proceed

If `COMPLETE`:
- File written and its path
- Prefix selected and why
- Sections included and sections omitted (with reason for each omission)
