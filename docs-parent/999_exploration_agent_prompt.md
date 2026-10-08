# Agent Prompt: DigiExpress Module Documentation

## Task

Document a DigiExpress module by scanning its source code and writing a module overview file directly to `/Users/jocelyn/Development/projects/digiexpress-parent/docs-parent/`. The agent is responsible for both the research and the file creation.

## Output

Write a file named `0XX_<module>.md` in `/Users/jocelyn/Development/projects/digiexpress-parent/docs-parent/`.

Also add a one-sentence entry for the module under the `## Modules` section of `001_intro.md`.

## File naming

Files are named `NNN_<module>.md` where `NNN` is a 3-digit numeric prefix. Rules:
- No two files may share the same prefix.
- Leave gaps of at least 5 between prefixes to allow future files to be inserted in between.
- `001` is reserved for the intro. `999` is reserved for this prompt.
- Top-level modules start at round numbers (010, 100, 200, ...). Sub-modules of a top-level module occupy the range immediately following it (015, 020, 025, ...).

## File structure

```
# <Module Name>

## Purpose

<What the module does. Neutral, factual. No filler words.>

## Dependencies

<List all entries from the module's package.json `dependencies` and `peerDependencies`. Group them separately.>

## Module Relationships

<For each `@dxs-ts/*` module this module imports from, describe what it receives or uses from it. Format as a bullet list: `module-name` — what this module gets from it.>

## Backend Architecture

**Stack:** <technologies>

**Key controllers / services:**
- `ClassName` — <what it does and its route if applicable>

**Patterns:**
- <pattern name and description>

## Frontend Architecture

**Stack:** <technologies>

**Sub-module structure:**
- `module-name` — <what it contains>

**Patterns:**
- <pattern name and description>
```

## Rules

- Neutral, descriptive language only. No adjectives like "powerful", "polished", "comprehensive".
- Name actual classes, components, hooks, and files found in the code — not generic descriptions.
- If the module has no frontend, omit that section.
- If the module has no backend, omit that section.
- Keep each section to the point. No padding.

## How to scan

1. Find and read the module's `package.json` — list all `dependencies` and `peerDependencies`.
2. List the module directory structure.
3. Identify sub-modules or packages.
4. Read entry points, key service/controller classes, and 2-3 representative UI components.
5. Note all imports from other `@dxs-ts/*` modules and what is used from each.
6. Do not read every file — sample enough to understand the shape.
