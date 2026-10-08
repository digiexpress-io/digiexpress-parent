# Agent Prompt: Gamut Form Component — How to Add a New Component

## Setup

Read `digiexpress-parent/docs-parent/900_agent_rules.md` before starting work. All rules in that file apply to this task.

## Required Input

Before starting work, the invoker must provide the following. Do not proceed without all five.

- **Component name** — the `g-input-*` or `g-form-*` name (e.g. `g-input-my-field`). Determines directory name, file names, `MUI_NAME`, and class key prefix.
- **Purpose** — one sentence: what kind of input or form element this is and what it captures. This determines the Dialob `type` and `view` values and what props to expose.
- **Location in the form** — where in a Dialob form this component appears (e.g. "a text field inside a group", "a top-level page component"). This determines whether it needs page-level or item-level Dialob props.
- **Dialob type and view** — the `type` (base Dialob type, e.g. `text`, `boolean`) and `view` (custom extension string, e.g. `myField`) that trigger this component in the slot dispatch. If unknown, ask before proceeding.
- **Variant props** — whether the component has variant props (e.g. `multiline`, `currency`) that affect rendering or slot dispatch. Ask before proceeding if unclear.
- **Reference component** — which existing `g-input-*` component to model the structure on (e.g. `g-input-text` for simple text, `g-input-boolean` for toggle, `g-input-address` for complex multi-field).

## Goal

Explore the `@dxs-ts/gamut-form` module and produce step-by-step instructions for adding a new `g-input-*` or `g-form-*` component. The output is a developer guide, not just a description — it should be specific enough that a developer can follow it without reading source code.

## Location

`digiexpress-parent/ts_setup/modules/gamut-form/`

## What to investigate

### 1. Module structure

- List all sub-directories inside `gamut-form/`.
- Identify which are `g-input-*`, which are `g-form-*`, and what other files exist (e.g. `package.json`, `index.ts`, shared utilities).
- Note which sub-modules are exported from the top-level `index.ts` and which are internal-only.

### 2. Per-component file pattern

For each component sub-directory, identify the standard set of files. Read at least two `g-input-*` components in full (pick one simple and one complex). For each, document:
- What files exist and what each one contains
- The component's props interface and where it lives
- The styled root component and how `useUtilityClasses` is structured
- How `useThemeProps` is used
- How `GOverridableComponent` is applied (if at all)
- What `ownerState` contains and how it flows into the styled root

### 3. CSS / styling pattern

- How are utility classes defined? (`generateUtilityClass`, `composeClasses`)
- How does the styled root component receive `ownerState`?
- How are `styleOverrides` in the MUI theme wired up?
- What is the naming convention for the MUI component name (`MUI_NAME`)?

### 4. Dialob bridge pattern (`GInputABCDialob`)

- Read at least two `*Dialob.tsx` files.
- Document what props it reads from the Dialob form store (`useForm`, `ActionItem`).
- Document how it maps Dialob state to the presentational component's props.
- Document how it writes back to Dialob (onChange → `ActionsQueue` or equivalent).
- Note any slot/variant registration in `GFormBaseElement`.

### 5. Slot/variant registration

- Read `GFormBaseElement` and `useSlotVariant`.
- Document how a new component registers itself as a slot variant.
- List all existing variant keys and how the dispatch logic works.

### 6. Index/export registration

- Document what needs to be added to `index.ts` when a new component is created.
- Note if anything else needs to be updated (e.g. `GComponentsPropsList` in `gamut-routes`).

## Output

Write the output to `digiexpress-parent/docs-parent/085_gamut_form_how_to_add_component.md`.

The document must be a step-by-step guide with the following structure:

```
# How to Add a New Gamut Form Component

## Overview
<Brief description of what a gamut-form component consists of and how it fits into the Dialob rendering pipeline.>

## File structure
<Exact list of files to create, with their purpose.>

## Step 1: Create the props interface
<What to define, where, naming conventions, example.>

## Step 2: Create useUtilityClasses
<Exact pattern, class names, MUI_NAME convention, example.>

## Step 3: Create the presentational component
<Props, useThemeProps, GOverridableComponent, ownerState, styled root, example.>

## Step 4: Create the Dialob bridge (GInputABCDialob)
<What to read from useForm/ActionItem, how to map to presentational props, how to write back, example.>

## Step 5: Register the slot variant
<Where to add the variant key, how useSlotVariant dispatches it, example.>

## Step 6: Export from index.ts
<What to add and where.>

## Step 7: Register in GComponentsPropsList (if applicable)
<Whether this is needed and how.>

## Checklist
<Bullet list of everything that must be done when adding a new component.>
```

## Rules

- Every step must name actual files, functions, types, and constants from the codebase — not generic placeholders.
- Include short code snippets where the pattern is non-obvious.

## Report

After completing or blocking, report:

**Status:** `COMPLETE` or `BLOCKED`

If `BLOCKED`:
- Which rule was violated
- What was attempted
- What is needed to proceed

If `COMPLETE`:
- Files written and their paths
- Dialob `type` + `view` values used
- Reference component used and why
- Slot variant key registered in `useSlotVariant`
- Whether `GComponentsPropsList` registration is required
- Any non-obvious wiring decisions made
