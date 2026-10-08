# Agent Prompt: Documentation Nitpicker

## Setup

Read `digiexpress-parent/docs-parent/900_agent_rules.md` before starting work. All rules in that file apply to this task.

## Task

Read a documentation file in `digiexpress-parent/docs-parent/` and rewrite it in place. The goal is to reduce length, increase clarity, and improve format — without losing any information.

Do not remove content. Do not add content. Restructure and reword only.

## Rules

### Wordiness

- **Redundant lead-ins** — "This module is responsible for X" → "X". The section heading already establishes context. The first sentence states the fact directly.
- **Filler phrases** — cut without replacement: "it is worth noting that", "in order to", "it should be noted that", "note that", "as mentioned above".
- **Redundant verb pairs** — "reads and consumes", "creates and instantiates", "provides and exposes" — pick the more specific verb, drop the other.
- **Over-qualification** — remove "in most cases", "generally speaking", "typically" when the statement is always true. If it is not always true, name the exception instead.
- **"Currently"** — implies temporary state. If the behaviour is stable, remove it. If it is genuinely temporary, say when it will change.

### Sentence structure

- **Sentences carrying more than one idea** joined by "and" or a semicolon → split into two sentences.
- **Passive voice** — "is used by", "is called by", "is provided to" → rewrite as active voice.
- **Weak verbs** — "plays a role in", "is involved in", "is part of" → replace with the specific verb that describes what actually happens.

### Section-level issues

- **Section content that restates the heading** — "## Purpose — The purpose of this module is to..." → remove the lead-in, start with the fact.
- **Duplicate information** — if the same fact appears in two sections, keep it in the more specific one and remove it from the other.
- **Parenthetical asides that are load-bearing** — `(see X)` or `(used by Y)` when X or Y is important → promote to its own sentence or bullet.
- **Hedging on confirmed facts** — "appears to", "seems to", "likely" when the code was read and the behaviour is confirmed → remove the hedge.

### Lists and tables

- **Single-item lists** → convert to prose.
- **Table rows where the description restates the key** — "QueryClient — the QueryClient used for queries" → rewrite the description to say what it does, not what it is.
- **Prose paragraphs with 3+ sentences** where the sentences are enumerable or parallel → convert to bullet points.
- **Bullet points only when** items are enumerable, parallel, or independently meaningful. Keep prose for genuinely connected reasoning (cause → effect, constraint → implication) where bullets would lose the relationship.
- **Never bullet a single item.**

## What NOT to do

- Do not remove technical content — named types, hooks, files, constraints, gotchas.
- Do not add new information not already in the file.
- Do not change section headings unless they are redundant with the content (e.g. a heading that duplicates the first sentence verbatim).
- Do not reorder sections.

## Output

Rewrite the file in place. Do not create a new file.

After writing, report:
- Original word count
- New word count and % reduction
- Top 3 changes made (by category from the rules above)
- Any duplicate information found and which section it was removed from
- Any prose sections converted to bullet points
