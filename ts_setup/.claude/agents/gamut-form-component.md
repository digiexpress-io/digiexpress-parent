---
name: gamut-form-component
description: Creates a new Dialob form input component in gamut-form, including the presentational component, Dialob bridge, slot variant registration, and optional g-props registration; invoke when a new Dialob field type or view needs a custom React component.
tools: Read, Grep, Glob, Edit, Write, Bash, Agent
---
_last updated: 13.10.2026_


# Agent Prompt: Create a New Gamut Form Component

## Setup

Read `digiexpress-parent/ts_setup/.claude/agents/agent-rules.md` before starting work. All rules in that file apply to this task.

Read `digiexpress-parent/docs-parent/999_ui_coding_standards.md` before writing any code. All rules in that file apply to this task.

Run `ls digiexpress-parent/docs-parent/` to see all available documentation files. Read any that cover the module you are about to modify — use the directory listing as the authoritative index, not guessed names. If a relevant doc exists, read it in full before touching any files. If you are uncertain whether a file covers the module you are about to modify, ask before reading it or invoking the `exploration` agent — do not guess. If none exists, invoke the `exploration` agent first to produce one, read the file it created, then proceed.

## Required Input

Do not proceed without all of the following.

- **Component name** — the `g-input-*` or `g-form-*` name (e.g. `g-input-my-field`). Determines directory name, file names, `MUI_NAME`, and class key prefix.
- **Purpose** — one sentence: what kind of input or form element this is and what it captures.
- **Location in the form** — where in a Dialob form this component appears (e.g. "a text field inside a group", "a top-level page component").
- **Dialob type and view** — the `type` (base Dialob type, e.g. `text`, `boolean`) and `view` (custom extension string, e.g. `myField`) that trigger this component in the slot dispatch. Ask before proceeding if unknown.
- **Variant props** — whether the component has variant props that affect rendering or slot dispatch. Ask before proceeding if unclear.
- **Reference component** — which existing `g-input-*` component to model the structure on (e.g. `g-input-text` for simple text, `g-input-boolean` for toggle, `g-input-address` for complex multi-field).

## Task

Create a new form component in `digiexpress-parent/ts_setup/modules/gamut-form/` following the steps below exactly. Do not deviate from the patterns described. Do not invent naming conventions.

---

## Overview

Each gamut-form component has two layers:

1. **Presentational component** (`GInputABC.tsx`) — pure React, typed props, no Dialob knowledge. Participates in MUI theme via `useThemeProps`, `styled`, and `composeClasses`.
2. **Dialob bridge** (`GInputABCDialob.tsx`) — reads from `DialobApi.FormStore`, maps to presentational props, writes back via `store.setAnswer`.

`GFormBaseElement` dispatches to the bridge via `useSlotVariant`, which derives a variant key from `ActionItem.type` and `ActionItem.view`, then looks up the matching bridge in its `Slots` record.

---

## File structure

Create a directory under `gamut-form/` named after the component (e.g. `g-input-abc/`):

| File | Purpose |
|---|---|
| `g-input-abc-types.ts` | Props interface, classes interface, class key type. Use when types are referenced from `g-props.ts`; otherwise co-locate in `GInputABC.tsx`. |
| `useUtilityClasses.tsx` | `MUI_NAME`, `OwnerState` interface, `GInputAbcRoot` styled component, `useUtilityClasses` function. |
| `GInputABC.tsx` | Presentational component. Imports from `useUtilityClasses`, `g-input-base`, `g-input-error`, `g-input-label`, `g-input-adornment`. |
| `GInputABCDialob.tsx` | Dialob bridge. Props: `GFormBaseElementProps`. Reads from `formStore`, renders `<GInputABC>`. |
| `index.ts` | Barrel export. |

---

## Step 1: Create the props interface

**File:** `g-input-abc/g-input-abc-types.ts` (or inline in `GInputABC.tsx` for simple cases)

```ts
import { OverridableStringUnion } from '@mui/types';
import { DialobApi } from '@dxs-ts/gamut-api';

export interface GInputAbcPropsVariantOverrides {}

export interface GInputAbcProps {
  id: string;
  disabled: boolean;
  value: string | undefined;
  onChange: (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void;
  label: string | undefined;
  labelPosition: DialobApi.ControlLabelPosition;
  description: string | undefined;
  required: boolean;
  errors?: DialobApi.ActionError[] | undefined;
  invalid?: boolean | undefined;
  readOnly?: boolean;
  variant: OverridableStringUnion<'abc', GInputAbcPropsVariantOverrides> | undefined;
  slots?: Record<OverridableStringUnion<'abc', GInputAbcPropsVariantOverrides>, React.ElementType>;
  component?: React.ElementType<GInputAbcProps>;
}
```

If `g-props.ts` will reference the class key (Step 7), also export:

```ts
export interface GInputAbcClasses { root: string; }
export type GInputAbcClassKey = keyof GInputAbcClasses;
```

**Naming conventions:**
- Props interface: `GInputAbcProps`
- Variant overrides: `GInputAbcPropsVariantOverrides`
- Classes interface: `GInputAbcClasses`
- Class key type: `GInputAbcClassKey`

---

## Step 2: Create useUtilityClasses

**File:** `g-input-abc/useUtilityClasses.tsx`

```ts
import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { useVariantOverride } from '@dxs-ts/gamut-api';

export const MUI_NAME = 'GInputAbc';

interface OwnerState {
  variant: string;
  disabled: boolean;
  readOnly?: boolean;
}

export const GInputAbcRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (props, styles) => [styles.root, useVariantOverride(props, styles)],
})<{ ownerState: OwnerState }>(({ theme, ownerState }) => {
  if (ownerState.disabled) {
    return { '& .MuiOutlinedInput-root.Mui-disabled': { backgroundColor: theme.palette.background.paper } };
  }
  if (ownerState.readOnly) {
    return {
      '& .MuiOutlinedInput-notchedOutline': { border: 'none' },
      '& .MuiOutlinedInput-root': { backgroundColor: 'transparent' },
      '& .MuiInputBase-input': { cursor: 'not-allowed', color: theme.palette.text.primary, ...theme.typography.body1 },
    };
  }
  return {};
});

export const useUtilityClasses = (itemId: string, variant: string | undefined) => {
  const slots = { root: ['root', variant, itemId], input: ['input'] };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};
```

- `MUI_NAME` must be `'GInput<Abc>'` (exact PascalCase). Used by `useThemeProps` and `styled` for class name generation.
- `useVariantOverride` applies `styleOverrides` keyed by variant.
- `ownerState` includes only fields that affect styles.
- `useUtilityClasses` includes `itemId` and `variant` in the root slot so per-item and per-variant selectors work.

---

## Step 3: Create the presentational component

**File:** `g-input-abc/GInputABC.tsx`

```tsx
import React from 'react';
import { useThemeProps } from '@mui/material';
import { DialobApi } from '@dxs-ts/gamut-api';
import { GInputError } from '../g-input-error';
import { GInputLabel } from '../g-input-label';
import { GInputAdornment } from '../g-input-adornment';
import { GInputBase, GInputBaseAnyProps, GInputBaseProps } from '../g-input-base';
import { MUI_NAME, GInputAbcRoot, useUtilityClasses } from './useUtilityClasses';
import { GInputAbcProps } from './g-input-abc-types';

export const GInputAbc: React.FC<GInputAbcProps> = (initProps) => {
  const props = useThemeProps({ props: initProps, name: MUI_NAME });
  const { variant = 'abc', labelPosition, errors } = props;
  const classes = useUtilityClasses(props.id, variant);
  const ownerState = { ...props, variant };
  const { id, label, description } = props;
  const slots: GInputBaseProps<GInputAbcProps> = {
    id,
    slots: {
      error: GInputError,
      label: GInputLabel,
      adornment: GInputAdornment,
      input: props.readOnly ? ReadOnlyAbcInput : AbcInput,
    },
    slotProps: {
      error: { id, errors },
      input: { name: id, ...props },
      label: { id, children: label ?? '', labelPosition, required: props.required, errors: props.errors },
      adornment: { id, children: description, title: label ?? '', disabled: props.disabled },
    },
  };
  return (
    <GInputAbcRoot className={classes.root} ownerState={ownerState} as={props.component}>
      <GInputBase id={props.id} slots={slots.slots} slotProps={slots.slotProps} />
    </GInputAbcRoot>
  );
};

const AbcInput: React.FC<GInputBaseAnyProps & GInputAbcProps> = (props) => {
  const classes = useUtilityClasses(props.id, props.variant);
  return <div className={classes.input}>...</div>;
};

const ReadOnlyAbcInput: React.FC<GInputBaseAnyProps & GInputAbcProps> = (props) => {
  const classes = useUtilityClasses(props.id, props.variant);
  return <div className={classes.input}>...</div>;
};
```

- `useThemeProps({ props: initProps, name: MUI_NAME })` is always the first call.
- `ownerState` is always `{ ...props, variant }` with the resolved default.
- `GInputBase` handles label / error / adornment layout.
- Input sub-components are private to this file — not exported.
- `as={props.component}` makes the root element replaceable. No `GOverridableComponent` wrapper — `as` on the styled root is the mechanism in `gamut-form`.
- If the component must restrict input characters (e.g. numeric only), do it in `handleChange` using a regexp on `event.target.value`. Pass a synthetic event with the sanitized value to `props.onChange`. Do not use `onKeyDown` filtering.

---

## Step 4: Create the Dialob bridge

**File:** `g-input-abc/GInputABCDialob.tsx`

```tsx
import React from 'react';
import { GFormBaseElementProps } from '../g-form-base-element';
import { GInputAbc } from './GInputABC';

export const GInputAbcDialob: React.FC<GFormBaseElementProps> = ({
  disabled,
  actionItem: element,
  formStore: store,
  navRef,
  navRefId,
}) => {
  const errors = store.form.toErrors(element.id);
  const desc = store.form.toDescription(element.id);
  const labelPosition = store.form.toLabelPosition(element.id);

  function onChange(event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) {
    store.setAnswer(element.id, event.target.value);
  }

  return (
    <>
      <div ref={navRef} id={navRefId} />
      <GInputAbc
        disabled={disabled}
        id={element.id}
        label={store.form.toLabel(element.id)}
        description={desc}
        errors={errors}
        required={!!element.required}
        value={element.value}
        variant='abc'
        labelPosition={labelPosition}
        onChange={onChange}
        readOnly={element.readOnly}
      />
    </>
  );
};
```

### How the bridge fields are wired

**`element.value`** (`any` at runtime, treat as `string | undefined`)
The user's current answer. Comes from the Dialob backend via the `ActionVisitor` and is stored in `FormState.items`. Pass directly to the presentational component's `value` prop. Do not transform — the reference component defines any formatting.

**`element.props`** (`any` at runtime, treat as `Record<string, string> | undefined`)
Flat key-value metadata from the Dialob form definition JSON. Values are always strings. Booleans are encoded as the string `"true"` — check with `DialobApi.isTrue(element.props?.flag)`, not `=== true`. Numeric values are strings — parse with `parseInt`/`parseFloat` when needed. Access with optional chaining: `element.props?.key`.

The prop key used here must match the key checked in the dispatch branch in `useSlotVariant.tsx`. Both must use the same string. If the dispatch checks `element.props?.unit`, the bridge reads `element.props?.unit`.

**`variant='abc'`** (hardcoded string in the bridge)
Has nothing to do with slot dispatch. It feeds the presentational component's `useUtilityClasses` for MUI class name generation and theme override lookup. Always hardcode it to the component's default variant name — the same string used as the default in `const { variant = 'abc' } = props` in the presentational component.

**`store.form.toLabel(id)`** → `string | undefined`
**`store.form.toErrors(id)`** → `DialobApi.ActionError[]` (always an array, never undefined)
**`store.form.toDescription(id)`** → `string | undefined`
**`store.form.toLabelPosition(id)`** → `'label-left' | 'label-top'`

**`store.setAnswer(id, value)`** — queues an ANSWER action, debounced 250ms, coalesces duplicate answers for the same field. Accepts any type; text components pass `event.target.value` as a string.

**`disabled`** — reflects the overall form disabled state, not the field's own disabled flag.

**`navRef` / `navRefId`** — always render `<div ref={navRef} id={navRefId} />` as the first child of the return.

---

## Step 5: Register the slot variant

### How dispatch works

`GFormBaseElement` calls `useSlotVariant(element, store)` to get a `GFormBaseSlotVariant` key, then looks that key up in the `Slots` record. If the key is missing, `UnknownSlot` renders instead — showing `unknown = {id}` in the UI and `console.error` in the console. This is always a misconfiguration, never expected behaviour.

**Three dispatch patterns exist — read `useSlotVariant.tsx` to confirm which applies:**

1. **Props-based** (standard for `type='text'`): branch checks `element.type === 'text' && element.view === 'text' && element.props?.abc`. The props key (`abc`) must be truthy — an empty string does NOT trigger it. Branch must be placed before the generic `text` fallback.

2. **View-based**: branch checks `element.view === 'something'` with no props condition. Used for `page`, `note-validation`, `survey`.

3. **Type-only fallback** (last line of `useSlotVariant`): `element.type + (element.view ? '-' + element.view : '')`. Safe for types with no variants (`date`, `time`, `decimal`, `boolean`, etc.). Risky for `text` — a `text` field without `view: 'text'` falls into this and likely hits `UnknownSlot`.

**Props that reroute the component entirely** (adding any of these changes which class renders):

| Prop key | Value | Routes to |
|----------|-------|-----------|
| `controlType` | `'fileUpload'` | `GInputUploadDialob` |
| `currency` | any truthy string | `GInputCurrencyDialob` |
| `unit` | any truthy string | `GInputUnitDialob` |

These are checked in priority order — `controlType` first, then `currency`, then `unit`, then the generic `text` fallback. Adding `currency` to a field that also has `controlType: 'fileUpload'` has no effect; the file upload branch wins.

**Props that affect presentation only** (do NOT change which component renders):

| Prop key | Affects | Example values |
|----------|---------|----------------|
| `variant` | Boolean display mode, date picker mode, list display | `'checkbox'`, `'year'`, `'autocomplete'` |
| `style` | Note styling | `'error'`, `'warning'`, `'info'` |
| `border` | Group border | `'true'`, `'false'` |
| `columns` | Layout columns | `'2'`, `'3'` |
| `rows`, `charLimit` | TextArea sizing | `'4'`, `'200'` |

Note: `variant` inside `element.props` (presentation) is unrelated to the `variant` prop on the presentational component (CSS class generation). They are different concepts sharing the same name.

### 5a. `g-form-base-element/useSlotVariant.tsx`

Add to the `GFormBaseSlotVariant` union:

```ts
export type GFormBaseSlotVariant = (
  'text' |
  // ... existing keys ...
  'text-abc'
)
```

Add the dispatch branch using the pattern that matches your case. For a new `type='text'` component (standard pattern):

```ts
if (element.type === 'text' && element.view === 'text' && element.props?.abc) {
  return { variant: 'text-abc' };
}
```

Place it before the generic `text` branch. The prop key used here (`abc`) must be the same key the bridge reads (`element.props?.abc`) and the same key set in the Dialob form JSON (`"props": {"abc": "value"}`).

### 5b. `g-form-base-element/GFormBaseElement.tsx`

Import the bridge and add to `Slots`:

```ts
import { GInputAbcDialob } from '../g-input-abc';

const Slots: Record<GFormBaseSlotVariant, React.ElementType<GFormBaseElementProps>> = {
  // ... existing entries ...
  'text-abc': GInputAbcDialob,
};
```

`Slots` must have exactly one entry per key in `GFormBaseSlotVariant`. TypeScript errors if a key is missing.

---

## Step 6: Export from index.ts

**`g-input-abc/index.ts`:**
```ts
export * from './GInputABC'
export * from './GInputABCDialob'
export * from './g-input-abc-types'   // only if types are in a separate file
```

**`gamut-form/index.ts`** — add only if the component must be accessible outside the module for theming:
```ts
export * from './g-input-abc';
```

Components used only internally do not need a top-level export.

---

## Step 7: Register in GComponentsPropsList (if applicable)

Required only when the component must be themeable from outside `gamut-form`.

**File:** `digiexpress-parent/ts_setup/modules/gamut-routes/g-props/g-props.ts`

Three additions:

**1. Import:**
```ts
import { GInputAbcClassKey, GInputAbcProps } from '@dxs-ts/gamut-form';
```

**2. `GComponentsPropsList`:**
```ts
GInputAbc: GInputAbcProps;
```

**3. `GComponentNameToClassKey`:**
```ts
GInputAbc: GInputAbcClassKey;
```

`GComponents` is synthesized automatically from these two interfaces — no manual addition needed there.

---

## Step 8: Configure the Dialob form definition

No backend Dialob changes needed — configure the form definition JSON only.

### Dispatch is determined by type + view + props together

`useSlotVariant` reads `element.type`, `element.view`, and `element.props` to select a component. A misconfigured field renders `UnknownSlot` — visible as `unknown = {id}` in the UI with a `console.error`. Getting this wrong is silent until runtime.

**Standard pattern for new `type='text'` components:**

```json
{
  "id": "myField",
  "type": "text",
  "view": "text",
  "props": { "abc": "someValue" }
}
```

The prop key (`abc`) must be the same string in all three places: the dispatch branch in `useSlotVariant`, the bridge's `element.props?.abc` read, and the form JSON `"props"` object. If any of the three differ, a different component renders (or `UnknownSlot`).

**Existing dispatch-triggering props — do not reuse these keys:**

| JSON props key | Triggers |
|---|---|
| `"controlType": "fileUpload"` | `GInputUploadDialob` |
| `"currency": "<symbol>"` | `GInputCurrencyDialob` |
| `"unit": "<symbol>"` | `GInputUnitDialob` |

Adding one of these accidentally to a field you are building will silently reroute it to the wrong component.

**Type reference:**
- `type` — must be an existing Dialob base type: `text`, `group`, `boolean`, `list`, `multichoice`, `note`, `date`, `time`, `decimal`, `integer`, `rowgroup`. Do not invent new types.
- `props` values are always strings. Booleans must be `"true"` / `"false"`, not native booleans. Numbers must be string-encoded (`"2"`, not `2`).

Document all new `props` keys in `gamut-form/DIALOB_FILL_PROPS.md`.

---

## Step 9: Testing

No storybook or fixture files exist. Test against the live demo app.

**Start the dev server:**
```
pnpm start-gamut
```
Runs on port `3001`, proxying to `http://localhost:8080/portal`. Config: `ts_setup/vite.config.start.gamut.ts`.

Create a Dialob form in the backend with the correct `type`/`view`/`props`. Navigate to `http://localhost:3001/secured/en/forms/{formId}`.

**Verify:**
- Correct component renders (not the fallback `UnknownSlot`).
- `onChange` writes back correctly.
- `disabled` and `readOnly` states render correctly.
- All configured variants and `props` combinations work.

---

## Checklist

- [ ] Create `g-input-abc/` directory
- [ ] `GInputAbcProps` (and optionally `GInputAbcClasses` / `GInputAbcClassKey`) in `g-input-abc-types.ts` or `GInputABC.tsx`
- [ ] `useUtilityClasses.tsx` — `MUI_NAME`, `GInputAbcRoot`, `useUtilityClasses`
- [ ] `GInputABC.tsx` — `useThemeProps`, `ownerState`, `GInputBase` slot wiring, private input sub-components
- [ ] `GInputABCDialob.tsx` — implements `GFormBaseElementProps`, includes `<div ref={navRef} id={navRefId} />`
- [ ] `g-input-abc/index.ts` — barrel export
- [ ] Variant key added to `GFormBaseSlotVariant` union in `useSlotVariant.tsx`
- [ ] Dispatch branch added to `useSlotVariant` if fallback formula does not produce the correct key
- [ ] `GInputAbcDialob` imported and added to `Slots` in `GFormBaseElement.tsx`
- [ ] `export * from './g-input-abc'` added to `gamut-form/index.ts` if externally themeable
- [ ] `GInputAbcProps` and `GInputAbcClassKey` added to `g-props.ts` if externally themeable
- [ ] Dialob form definition configured with correct `type`, `view`, and `props`
- [ ] New `props` keys documented in `gamut-form/DIALOB_FILL_PROPS.md` if applicable
- [ ] Tested via `pnpm start-gamut` — render, onChange, disabled, readOnly verified

---

## Before finishing

Do not report `COMPLETE` until every applicable item is checked off. Use the Agent tool for each agent invocation — a self-review is never an acceptable substitute.

- [ ] Run `corepack pnpm build-gamut` and fix all TypeScript errors before proceeding.
- [ ] **ui-compliance** — Invoke the `ui-compliance` agent. Pass it the task spec and the full list of files created or modified.
  - [ ] If `FAIL`: invoke `ui-cleanup`, then invoke `ui-compliance` a second time.
  - [ ] If still `FAIL`: mark the task `BLOCKED` and stop.
- [ ] **doc-sync** — Check the Doc column in `digiexpress-parent/docs-parent/000_architecture.md` for the module you modified. If a doc exists, invoke the `doc-sync` agent with the module name, doc path, and list of files. If no doc exists, check this off and skip it.

## Report

After completing or blocking, report:

**Status:** `COMPLETE` or `BLOCKED`

If `BLOCKED`:
- Which rule was violated
- What was attempted
- What is needed to proceed

If `COMPLETE`:
- Files created and their paths
- Dialob `type` + `view` values used
- Reference component used and why
- Slot variant key registered in `useSlotVariant`
- Whether `GComponentsPropsList` registration was done
- Any non-obvious wiring decisions made
