# Agent Workflows

_Last updated: 14.10.2026_

This document describes how agents are invoked in DigiExpress — both by humans and by other agents — and what chain of calls follows. It is intended for humans who want to understand what happens when they ask an agent to do something.

---

## How to read this document

Each workflow starts with a **trigger** (who or what initiates it), followed by the **chain** of agents called in sequence. Conditions are shown inline:

- `→` means "always calls next"
- `→ [if FAIL]` means "only calls next if the previous step failed"
- `→ [if doc exists]` means "only calls next if the condition is met"
- `✋ BLOCKED` means the chain stops and a human decision is required

---

## Workflow 1: Build a UI component in fs-composer

**Trigger:** Human asks to create or modify a component in `modules/fs-composer/`.

**Agent invoked:** `fs-composer-component`

**What it does:** Applies a UI/UX change or adds a new data field end-to-end across the fs-composer stack.

**Chain:**

```
Human
  └── fs-composer-component
        ├── [if no module doc exists] → exploration
        │     └── writes NNN_module.md to docs-parent/
        │     └── updates 000_architecture.md Doc column
        ├── runs corepack pnpm build-eveli (TypeScript build)
        │     └── [if errors] → fixes errors, re-runs build
        ├── → ui-compliance
        │     ├── [if PASS] → doc-sync [if doc exists]
        │     └── [if FAIL] → ui-cleanup
        │           └── → ui-compliance (second pass)
        │                 ├── [if PASS] → doc-sync [if doc exists]
        │                 └── [if FAIL] ✋ BLOCKED — human decision required
        └── → doc-sync [if module doc exists in 000_architecture.md]
              └── patches stale sections in the module's doc file
```

---

## Workflow 2: Audit existing code for standards violations

**Trigger:** Human asks to audit a directory or file list for coding standards compliance.

**Agent invoked:** `ui-compliance` (Mode 2 — existing-code audit)

**What it does:** Reads the target files and checks them against the coding standards. No build step.

**Chain:**

```
Human
  └── ui-compliance
        ├── [if PASS] → done, report returned to human
        └── [if FAIL] → ui-cleanup
              └── fixes all listed violations
              └── → ui-compliance (second pass, optional — human decides)
```

---

## Workflow 3: Document a module or sub-system

**Trigger:** Human asks to explore and document a module, backend package, or cross-cutting domain. Also triggered automatically by `fs-composer-component` when no module doc exists.

**Agent invoked:** `exploration`

**What it does:** Scans source code and writes a structured overview file to `docs-parent/`.

**Chain:**

```
Human (or fs-composer-component)
  └── exploration
        └── writes NNN_target.md to docs-parent/
        └── updates Doc column in 000_architecture.md
        └── done — no further agents called
```

---

## Workflow 4: Update a module doc after code changes

**Trigger:** Called automatically by building agents (e.g. `fs-composer-component`) after completing work and after `ui-compliance` has passed, if the module has an existing doc. Not typically triggered directly by humans.

**Agent invoked:** `doc-sync`

**What it does:** Re-reads the changed files and patches only the stale sections of the module doc. Always runs after `ui-compliance` passes — never before — so the doc reflects the final state of the code, not an intermediate state that may still change.

**Chain:**

```
fs-composer-component (or other building agent)
  └── → ui-compliance
        ├── [if FAIL] → ui-cleanup → ui-compliance (second pass)
        │     └── [if still FAIL] ✋ BLOCKED — doc-sync does not run
        └── [if PASS] → doc-sync [if module doc exists]
              └── patches stale sections in NNN_module.md
              └── updates _last updated_ date
              └── done — no further agents called
```

---

## Workflow 5: Create a new agent prompt

**Trigger:** Human asks to scaffold a new agent for a task type that doesn't yet have one.

**Agent invoked:** `agent-creation`

**What it does:** Explores existing agent patterns, writes a new structured agent prompt file to `.claude/agents/`, then runs `doc-nitpicker` on the result to tighten the language before it is used.

**Chain:**

```
Human
  └── agent-creation
        └── [reads existing agent prompts to discover patterns]
        └── writes new agent prompt to .claude/agents/
        └── → doc-nitpicker
              └── rewrites the new agent prompt in place (reduce length, improve clarity)
              └── done — no further agents called
```

---

## Workflow 6: Tighten an existing doc file

**Trigger:** Human asks to reduce length and improve clarity of a doc file.

**Agent invoked:** `doc-nitpicker`

**What it does:** Rewrites a single documentation file in place — reduces redundancy, removes filler, improves clarity without removing content.

**Chain:**

```
Human
  └── doc-nitpicker
        └── rewrites target doc file in place
        └── done — no further agents called
```

---

## Workflow 7: Build a new Gamut primitives component

**Trigger:** Human asks to create a new reusable React component in `gamut-primitives`.

**Agent invoked:** `gamut-primitives-component`

**What it does:** Creates the component file, props interface, styled root, utility classes, barrel export, and g-props registration.

**Chain:**

```
Human
  └── gamut-primitives-component
        ├── [if no module doc exists] → exploration
        ├── → ui-compliance
        │     ├── [if PASS] → doc-sync [if doc exists]
        │     └── [if FAIL] → ui-cleanup
        │           └── → ui-compliance (second pass)
        │                 ├── [if PASS] → doc-sync [if doc exists]
        │                 └── [if FAIL] ✋ BLOCKED
        └── → doc-sync [if module doc exists]
```

---

## Workflow 8: Build a new Gamut form component

**Trigger:** Human asks to create a new Dialob form input component in `gamut-form`.

**Agent invoked:** `gamut-form-component`

**What it does:** Creates the presentational component, Dialob bridge, slot variant registration, and optional g-props registration.

**Chain:** Same structure as Workflow 7, replacing `gamut-primitives-component` with `gamut-form-component`.

---

## Workflow 9: Build a new Gamut route component

**Trigger:** Human asks to create a new page-level route component in `gamut-routes`.

**Agent invoked:** `gamut-route-component`

**What it does:** Creates the route component, utility classes, optional types file, barrel export, g-props registration, and TanStack Router route file wiring.

**Chain:** Same structure as Workflow 7, replacing `gamut-primitives-component` with `gamut-route-component`.

---

## Agent summary

| Agent | Triggered by | Triggers |
|-------|-------------|---------|
| `fs-composer-component` | Human | `exploration` (conditional), `ui-compliance`, `doc-sync` (conditional) |
| `gamut-primitives-component` | Human | `exploration` (conditional), `ui-compliance`, `doc-sync` (conditional) |
| `gamut-form-component` | Human | `exploration` (conditional), `ui-compliance`, `doc-sync` (conditional) |
| `gamut-route-component` | Human | `exploration` (conditional), `ui-compliance`, `doc-sync` (conditional) |
| `ui-compliance` | Building agents, Human | `ui-cleanup` (if FAIL) |
| `ui-cleanup` | `ui-compliance` | nothing |
| `doc-sync` | Building agents | nothing |
| `exploration` | Human, building agents | nothing |
| `doc-nitpicker` | Human | nothing |
| `agent-creation` | Human | `doc-nitpicker` |
