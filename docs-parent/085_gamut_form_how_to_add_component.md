# How to Add a New Gamut Form Component

## Rules

1. **No webkit-specific CSS.** Never use browser-specific styling (e.g. `-webkit-*`, `-moz-*`) unless explicitly asked first.
2. **Do not deviate from existing patterns.** Do not invent new naming conventions, file structures, or styling approaches. Follow what is already in the codebase.
3. **Ask about variant props before starting.** Some components have a `variant` prop, some do not. Clarify this before writing any code.
4. **Follow the barrel export structure.** Each sub-directory has an `index.ts` that re-exports its own symbols. The top-level `gamut-form/index.ts` only exports components that are externally themeable.

---

## Overview

Each gamut-form component consists of two layers:

1. **Presentational component** (`GInputABC.tsx`) — a pure React component that receives typed props and renders the UI. It has no knowledge of Dialob. It participates in the MUI theme system via `useThemeProps`, `styled`, and `composeClasses`.

2. **Dialob bridge** (`GInputABCDialob.tsx`) — reads from `DialobApi.FormStore` (label, errors, description, value, readOnly, etc.), maps to presentational props, and writes back via `store.setAnswer`.

`GFormBaseElement` is the dispatch layer. It calls `useSlotVariant` to derive a variant key from `ActionItem.type` and `ActionItem.view`, then looks up the matching Dialob bridge in its `Slots` record and renders it.

---

## File structure

Create a new directory under `gamut-form/` named after the component (e.g., `g-input-abc/`). It must contain:

| File | Purpose |
|---|---|
| `g-input-abc-types.ts` | Props interface, classes interface, class key type. Create this file when the types are referenced from `g-props.ts`; for simpler components you may co-locate the interface in `GInputABC.tsx`. |
| `useUtilityClasses.tsx` | `MUI_NAME` constant, `OwnerState` interface, `GInputABCRoot` styled component, `useUtilityClasses` function. |
| `GInputABC.tsx` | Presentational component. Imports from `useUtilityClasses`, `g-input-base`, `g-input-error`, `g-input-label`, `g-input-adornment`. |
| `GInputABCDialob.tsx` | Dialob bridge. Props: `GFormBaseElementProps`. Reads from `formStore`, renders `<GInputABC>`. |
| `index.ts` | Re-exports everything from the above files. |

---

## Step 1: Create the props interface

**File:** `g-input-abc/g-input-abc-types.ts` (or inline in `GInputABC.tsx` for simple cases)

Required fields that every `g-input-*` component must declare:

```ts
import { OverridableStringUnion } from '@mui/types';
import { DialobApi } from '@dxs-ts/gamut-api';

// Extension hook — consumers can augment this to add custom variant keys.
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

  // Add any component-specific props here (e.g., `currency`, `datasource`).

  variant: OverridableStringUnion<
    'abc',                          // replace with your primary variant name
    GInputAbcPropsVariantOverrides
  > | undefined;

  slots?: Record<
    OverridableStringUnion<'abc', GInputAbcPropsVariantOverrides>,
    React.ElementType
  >;

  component?: React.ElementType<GInputAbcProps>;
}
```

If `g-props.ts` will reference the class key (see Step 7), also export:

```ts
export interface GInputAbcClasses {
  root: string;
}
export type GInputAbcClassKey = keyof GInputAbcClasses;
```

Naming conventions:
- Interface: `GInputAbcProps`
- Variant overrides interface: `GInputAbcPropsVariantOverrides`
- Classes interface: `GInputAbcClasses`
- Class key type: `GInputAbcClassKey`

---

## Step 2: Create useUtilityClasses

**File:** `g-input-abc/useUtilityClasses.tsx`

```ts
import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { useVariantOverride } from '@dxs-ts/gamut-api';

export const MUI_NAME = 'GInputAbc';  // PascalCase, matches the component name exactly

interface OwnerState {
  variant: string;
  disabled: boolean;
  readOnly?: boolean;
}

export const GInputAbcRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      useVariantOverride(props, styles),
    ];
  },
})<{ ownerState: OwnerState }>(({ theme, ownerState }) => {
  if (ownerState.disabled) {
    return {
      '& .MuiOutlinedInput-root.Mui-disabled': {
        backgroundColor: theme.palette.background.paper,
      },
    };
  }
  if (ownerState.readOnly) {
    return {
      '& .MuiOutlinedInput-notchedOutline': { border: 'none' },
      '& .MuiOutlinedInput-root': { backgroundColor: 'transparent' },
      '& .MuiInputBase-input': {
        cursor: 'not-allowed',
        color: theme.palette.text.primary,
        ...theme.typography.body1,
      },
    };
  }
  return {};  // default styles
});

export const useUtilityClasses = (itemId: string, variant: string | undefined) => {
  const slots = {
    root: ['root', variant, itemId],
    input: ['input'],
    // add more slots as needed
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};
```

Key points:
- `MUI_NAME` is the MUI component name string. It must be `'GInput<Abc>'` (exact PascalCase). This string is used by `useThemeProps` to look up `theme.components.GInputAbc.defaultProps` and by `styled` to generate BEM-style class names (`GInputAbc-root`, `GInputAbc-input`, etc.).
- `useVariantOverride` (from `@dxs-ts/gamut-api`) applies `theme.components.GInputAbc.styleOverrides` keyed by variant.
- `ownerState` in the styled component is the spread of the component props plus the resolved variant. Only include fields that affect styles.
- `useUtilityClasses` always includes `itemId` and `variant` in the root slot array so per-item and per-variant CSS selectors work.

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

// Sub-components for the input slot — private to this file, not exported
const AbcInput: React.FC<GInputBaseAnyProps & GInputAbcProps> = (props) => {
  const classes = useUtilityClasses(props.id, props.variant);
  // render your editable input here
  return <div className={classes.input}>...</div>;
};

const ReadOnlyAbcInput: React.FC<GInputBaseAnyProps & GInputAbcProps> = (props) => {
  const classes = useUtilityClasses(props.id, props.variant);
  // render the read-only view here
  return <div className={classes.input}>...</div>;
};
```

Key points:
- `useThemeProps({ props: initProps, name: MUI_NAME })` is always the first call. It merges `theme.components.GInputAbc.defaultProps` over `initProps`.
- `ownerState` is always `{ ...props, variant }` with the resolved default. This object is passed to `GInputAbcRoot` and determines which style branch is active.
- `GInputBase` handles the label / error / adornment layout. The component only needs to provide the four slot types and their props.
- Input sub-components (`AbcInput`, `ReadOnlyAbcInput`) are declared in the same file and are not exported.
- `as={props.component}` makes the root element replaceable via the `component` prop, following MUI's overridable component convention. There is no `GOverridableComponent` wrapper in this codebase — the `as` prop on the styled root is the mechanism used.

---

## Step 4: Create the Dialob bridge (GInputABCDialob)

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

What to read from `GFormBaseElementProps`:
- `actionItem` (`DialobApi.ActionItem`) — `id`, `value`, `required`, `readOnly`, `type`, `view`, `props` (component-specific metadata like `currency`, `variant`, `border`).
- `formStore` (`DialobApi.FormStore`) — provides `store.form.toLabel`, `toErrors`, `toDescription`, `toLabelPosition`, `toValueSet` (for list-type components). Write back via `store.setAnswer(element.id, newValue)`.
- `disabled` — passed in from the parent, reflects overall form disabled state.
- `navRef` / `navRefId` — passed through unchanged. Always render `<div ref={navRef} id={navRefId} />` as the first child for form navigation.

`element.props` carries arbitrary string metadata set in the Dialob form definition. Access it as `element.props?.someKey`. Convert booleans using `DialobApi.isTrue(element.props?.someFlag)`.

For multi-value components (e.g., multichoice), split/join the comma-separated string that `store.setAnswer` expects, as shown in `GInputMultilistDialob`.

---

## Step 5: Register the slot variant

Two files must be edited.

### 5a. `g-form-base-element/useSlotVariant.tsx`

Add the new variant key to the `GFormBaseSlotVariant` union type:

```ts
export type GFormBaseSlotVariant = (
  'text' |
  // ... existing keys ...
  'text-abc'   // add your key here
)
```

Then add a dispatch branch in `useSlotVariant`:

```ts
export function useSlotVariant(element: DialobApi.ActionItem, store: DialobApi.FormStore): SlotVariant {
  // ... existing branches ...

  // Example: element.type === 'text', element.view === 'abc'
  if (element.type === 'text' && element.view === 'abc') {
    return { variant: 'text-abc' };
  }

  // Fallback — derives variant as `type` or `type-view`
  const variant = element?.type + (element.view ? '-' + element.view : '') as any;
  return { variant };
}
```

The existing fallback at the bottom of `useSlotVariant` (`element.type + '-' + element.view`) covers simple cases where the variant key matches `type-view` directly. Only add an explicit branch when you need custom matching logic (e.g., checking `element.props`, or when `element.view` may be absent).

### 5b. `g-form-base-element/GFormBaseElement.tsx`

Import the new Dialob bridge and add it to the `Slots` record:

```ts
import { GInputAbcDialob } from '../g-input-abc';

const Slots: Record<GFormBaseSlotVariant, React.ElementType<GFormBaseElementProps>> = {
  // ... existing entries ...
  'text-abc': GInputAbcDialob,
};
```

The `Slots` record must have exactly one entry per key in `GFormBaseSlotVariant`. TypeScript will error if a key is missing.

---

## Step 6: Export from index.ts

**File:** `g-input-abc/index.ts`

```ts
export * from './GInputABC'
export * from './GInputABCDialob'
export * from './g-input-abc-types'   // only if types are in a separate file
```

**File:** `gamut-form/index.ts`

Add an export line only if the presentational component or its types need to be accessible outside the module (e.g., for theming in `g-props.ts`):

```ts
export * from './g-input-abc';
```

Components that are purely internal to the Dialob rendering pipeline (their Dialob bridge is the only consumer) do not need to be exported from the top-level `index.ts`. Check the existing entries: `g-input-text`, `g-input-boolean`, `g-input-date`, etc., are internal-only and are not in the top-level export list. Only `g-form`, `g-form-base`, `g-form-group`, `g-input-multilist`, `g-input-currency`, `g-input-label`, and `g-input-address` are exported.

---

## Step 7: Register in GComponentsPropsList (if applicable)

This step is required only when the component needs to be themeable from outside the `gamut-form` module — i.e., consumers must be able to set `theme.components.GInputAbc.defaultProps` or `styleOverrides`.

**File:** `/Users/jocelyn/Development/projects/digiexpress-parent/ts_setup/modules/gamut-routes/g-props/g-props.ts`

Three additions are required in this file:

**1. Import the types from `@dxs-ts/gamut-form`:**

```ts
import { GInputAbcClassKey, GInputAbcProps } from '@dxs-ts/gamut-form';
```

**2. Add to `GComponentsPropsList`:**

```ts
export interface GComponentsPropsList {
  // ... existing entries ...
  GInputAbc: GInputAbcProps;
}
```

**3. Add to `GComponentNameToClassKey`:**

```ts
export interface GComponentNameToClassKey {
  // ... existing entries ...
  GInputAbc: GInputAbcClassKey;
}
```

The `GComponents` interface further down the file synthesizes `defaultProps`, `styleOverrides`, and `variants` entries automatically from these two interfaces via mapped types (`GComponentsProps`, `GComponentsOverrides`, `GComponentsVariants`), so no manual addition is needed there.

---

## Step 8: Configure the Dialob form definition

The frontend dispatches to your component based on `ActionItem.type` and `ActionItem.view`, which come directly from the Dialob form definition JSON. No backend Dialob changes are needed — you only configure the form definition.

A Dialob form item looks like this:

```json
{
  "id": "myField",
  "type": "text",
  "view": "abc",
  "props": {
    "someKey": "someValue"
  }
}
```

**`type`** maps to `ActionItem.type`. Use an existing Dialob base type (`text`, `group`, `boolean`, `list`, `multichoice`, `note`, `date`, `time`, `decimal`, `integer`, `rowgroup`). You cannot invent new types — only `view` and `props` are custom.

**`view`** maps to `ActionItem.view`. This is your extension point. Set it to whatever string you added in `useSlotVariant` (e.g. `'abc'`). If you are not using `view`, leave it absent and rely on `props` keys for dispatch (as `currency` and `controlType` do for `GInputText`).

**`props`** maps to `ActionItem.props`. Set any key/value metadata your component reads via `element.props?.someKey`. Boolean props must be the string `'true'`, not a native boolean.

If your component uses `props` keys for dispatch (not `view`), document the keys in `DIALOB_FILL_PROPS.md` in `gamut-form/` following the existing table format.

---

## Step 9: Testing

There is no storybook or isolated component test fixture. Testing is done against the live demo app connected to a running backend.

**Start the dev server:**
```
pnpm start-gamut
```
Runs a Vite dev server on port `3001`, proxying API calls to `http://localhost:8080/portal`. Config: `ts_setup/vite.config.start.gamut.ts`.

**Create a test form:**
Create a Dialob form JSON in the backend containing an item with the `type`/`view`/`props` values your component expects. Forms are fetched at runtime — there are no local fixture files.

**Access the form:**
Navigate to `http://localhost:3001/secured/en/forms/{formId}` where `{formId}` is the ID of the form you created.

**What to verify:**
- The correct component renders (not the fallback).
- `onChange` writes the value back — confirm by submitting and checking the stored answer.
- `disabled` and `readOnly` states render correctly.
- The component works at all configured `variant` and `props` combinations.

---

## Checklist

- [ ] Create `g-input-abc/` directory.
- [ ] Define `GInputAbcProps` (and optionally `GInputAbcClasses` / `GInputAbcClassKey`) in `g-input-abc-types.ts` or `GInputABC.tsx`.
- [ ] Create `useUtilityClasses.tsx` with `MUI_NAME`, `GInputAbcRoot` (styled div with `ownerState`), and `useUtilityClasses`.
- [ ] Create `GInputABC.tsx` with `useThemeProps`, `ownerState`, `GInputBase` slot wiring, and private input sub-components.
- [ ] Create `GInputABCDialob.tsx` implementing `GFormBaseElementProps`, including `<div ref={navRef} id={navRefId} />`.
- [ ] Create `g-input-abc/index.ts` re-exporting all public symbols.
- [ ] Add variant key to `GFormBaseSlotVariant` union in `useSlotVariant.tsx`.
- [ ] Add dispatch branch to `useSlotVariant` if the fallback formula does not produce the correct key.
- [ ] Import `GInputAbcDialob` in `GFormBaseElement.tsx` and add entry to `Slots`.
- [ ] Add `export * from './g-input-abc'` to `gamut-form/index.ts` if the component is externally themeable.
- [ ] If externally themeable, add `GInputAbcProps`, `GInputAbcClassKey` imports and entries in `GComponentsPropsList` and `GComponentNameToClassKey` in `g-props.ts`.
- [ ] Configure a Dialob form definition with the correct `type`, `view`, and `props` to trigger your component.
- [ ] If the component introduces new `props` keys, document them in `gamut-form/DIALOB_FILL_PROPS.md`.
- [ ] Test via `pnpm start-gamut` against a backend form — verify render, onChange, disabled, and readOnly states.
