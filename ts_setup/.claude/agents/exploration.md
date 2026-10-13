---
name: exploration
description: Scans a DigiExpress module's source code and writes a structured module overview file to the agents directory; invoke when a module has no documentation entry or when its existing doc needs to be rebuilt from source.
tools: Read, Grep, Glob, Write
---
_last updated: 13.10.2026_


# Agent Prompt: DigiExpress Module Documentation

## Setup

Read `digiexpress-parent/ts_setup/.claude/agents/agent-rules.md` before starting work. All rules in that file apply to this task.

## Task

Scan a DigiExpress module's source code and write a module overview file to `digiexpress-parent/ts_setup/.claude/agents/`. The agent handles both research and file creation.

## Output

- `0XX_<module>.md` in `digiexpress-parent/ts_setup/.claude/agents/`
- One-sentence entry under `## Modules` in `digiexpress-parent/docs-parent/001_intro.md`

## File naming

`NNN_<module>.md`, where `NNN` is a 3-digit numeric prefix. Rules:
- No two files share the same prefix.
- Leave gaps of at least 5 for future insertion.
- `001` is reserved for the intro. `999` is reserved for this prompt.
- Top-level modules start at round numbers (010, 100, 200, ...). Sub-modules occupy the range immediately following (015, 020, 025, ...).

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

## Error Handling

<How errors surface in this module: validation errors, API failures, loading states. Name the actual types, hooks, or components involved.>

## Bootstrapping

<What a consuming app must do to wire up this module. List required providers, their order in the provider tree, and required configuration. State "None" if this is a leaf node.>

## TypeScript Extension Points

<List any augmentation patterns, interface merging, or extension records (e.g. GComponentsPropsList). Include name + what it extends and why.>

## Known Limitations and Gotchas

<Named gotchas with explanations. Check inline NOTE/TODO/FIXME comments, existing .md files in the module, and non-obvious constraints.>

## Scope Boundaries

<What this module explicitly does NOT do.>

```

## Rules

- Omit Frontend Architecture if the module has no frontend; omit Backend Architecture if no backend.
- Never omit "Error handling", "Bootstrapping", "Extension points", "Known limitations", or "Scope boundaries" — even if nothing was found. Write one sentence explaining *why* it does not apply (e.g. "Leaf node with no providers — no bootstrapping required." or "No async paths exist — errors surface only via synchronous throws in builder methods."). The sentence must prove the agent looked, not just assert the absence.

## Already documented

Check this list before writing to avoid duplicating work.

| Prefix | File | Module |
|---|---|---|
| 001 | `001_intro.md` | Top-level module index |
| 010 | `010_gamut.md` | Gamut — top-level overview |
| 015 | `015_envir_util.md` | `@dxs-ts/envir-util` |
| 020 | `020_gamut.md` | `@dxs-ts/gamut` — umbrella re-export barrel |
| 025 | `025_gamut_cockpit_store.md` | `@dxs-ts/gamut-cockpit-store` |
| 030 | `030_gamut_api.md` | `@dxs-ts/gamut-api` |
| 035 | `035_gamut_md.md` | `@dxs-ts/gamut-md` |
| 040 | `040_gamut_primitives.md` | `@dxs-ts/gamut-primitives` |
| 045 | `045_gamut_form_review.md` | `@dxs-ts/gamut-form-review` |
| 050 | `050_gamut_form.md` | `@dxs-ts/gamut-form` |
| 055 | `055_gamut_shell.md` | `@dxs-ts/gamut-shell` |
| 060 | `060_gamut_intl.md` | `@dxs-ts/gamut-intl` |
| 065 | `065_gamut_routes.md` | `@dxs-ts/gamut-routes` |
| 070 | `070_gamut_theme.md` | `@dxs-ts/gamut-theme` |
| 075 | `075_xui_datetime.md` | `@dxs-ts/xui-datetime` |
| 080 | `080_gamut_architecture.md` | Gamut — architecture overview and data flow |
| 090 | `090_tanstack.md` | TanStack Router + Query usage across Gamut |
| 100 | *(reserved)* | Next top-level module (metis) |
| 900 | `agent-rules.md` | Shared rules for all agents |
| 977 | `fs-composer-cleanup.md` | Agent prompt — fix issues found by the compliance agent |
| 982 | `ui-compliance.md` | Agent prompt — code compliance review for fs-composer changes |
| 987 | `fs-composer-component.md` | Agent prompt — add a new field to fs-composer end-to-end |
| 992 | `gamut-route-component.md` | Agent prompt — create a new gamut-route component |
| 995 | `agent-creation.md` | Agent prompt — create a new agent prompt |
| 996 | `gamut-primitives-component.md` | Agent prompt — create a new gamut-primitive component |
| 997 | `doc-nitpicker.md` | Agent prompt — documentation nitpicker |
| 998 | `gamut-form-component.md` | Agent prompt — create a new gamut-form component |
| 999 | `exploration.md` | This file |

**Next available prefix:** `091` (within Gamut range), `972` (agent range — freed when 972 orchestrator was removed), or `100` (next top-level module — metis).

## How to scan

1. Read `package.json` — list `dependencies` and `peerDependencies`.
2. List the directory structure; identify sub-modules or packages.
3. Read entry points, key service/controller classes, and 2-3 representative UI components.
4. Note all `@dxs-ts/*` imports and what is used from each.
5. Sample enough files to understand the shape — do not read every file.

## Report

After writing, report:
- File written (path + prefix)
- Sections where nothing was found, and the one-sentence explanation written for each
- Key gotchas or non-obvious findings worth flagging
- Whether `001_intro.md` was updated

## What to capture (beyond the template)

### Error handling patterns

Name the actual types, hooks, or components involved: validation errors, API fetch failures, loading states. Name the absence explicitly if no error UI exists.

### Host application bootstrapping

List required providers, their order in the provider tree, and required configuration. Note if the module is a leaf node.

### TypeScript extension points

Look for `declare module`, interface merging, or records like `GComponentsPropsList` / `GComponentNameToClassKey`. List each by name with a one-line description of what it extends and why.

### Known limitations and gotchas

Look for:
- Inline `NOTE:`, `TODO:`, `FIXME:`, or `HACK:` comments
- Existing `.md` files inside the module directory
- Unusual constraints (e.g. "boolean props must be passed as the string `'true'`", non-obvious required ordering)
- Props or behaviors that contradict what the name implies

List each as a named gotcha with a short explanation.

### Scope boundaries

State what this module explicitly does NOT do.

