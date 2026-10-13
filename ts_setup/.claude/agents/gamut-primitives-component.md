---
name: gamut-primitives-component
description: Creates a new reusable React component in gamut-primitives, including props interface, styled root, utility classes, optional data hook integration, barrel export, and g-props registration; invoke when a new UI primitive is needed in the gamut design system.
tools: Read, Grep, Glob, Edit, Write, Bash, Agent
---
_last updated: 13.10.2026_


# Agent Prompt: Create a New Gamut Primitive Component

## Setup

Read `digiexpress-parent/ts_setup/.claude/agents/agent-rules.md` before starting work. All rules in that file apply to this task.

Read `digiexpress-parent/docs-parent/999_ui_coding_standards.md` before writing any code. All rules in that file apply to this task.

Run `ls digiexpress-parent/docs-parent/` to see all available documentation files. Review the filenames and read any that appear to cover the module you are about to modify — do not search by guessed name, use the directory listing as the authoritative index. If a relevant doc exists, read it in full before touching any files. If you are uncertain whether a file covers the module you are about to modify, ask before reading it or invoking the `exploration` agent — do not guess. If none exists, invoke the `exploration` agent first to produce one, read the file it created, then proceed.

## Required Input

Do not proceed without all of the following.

- **Component name** — the `G`-prefixed name (e.g. `GMyWidget`). Determines directory name, file names, `MUI_NAME`, and class key prefix.
- **Purpose** — one sentence: what it displays and why. Determines which data hooks to use and what props to expose.
- **Location in the UI** — which page, view, or route it appears on. Determines layout assumptions and whether navigation is needed.
- **Data source** — which `gamut-api` hook provides its data (`useIam`, `useOffers`, `useComms`, `useContracts`, `useSite`, `useLocale`), or "none" if purely presentational.
- **User interactions** — what happens on user action (navigation via `useNavigate`, a callback prop, nothing).

## Task

Create a new primitive component in `digiexpress-parent/ts_setup/modules/gamut-primitives/` following the steps below exactly. Do not deviate from the patterns described. Do not invent naming conventions.

---

## Component families

Use a component in the same family as the new component for reference. If nothing fits, use `GLoader` (minimal, one slot, `GOverridableComponent`) as the baseline.

| Family prefix | Sub-directories | What it covers |
|---|---|---|
| `GAppBar` | `g-app-bar` | Top navigation bar |
| `GArticle` | `g-article`, `g-article-feedback`, `g-article-feedback-viewer` | Knowledge-base article display and feedback |
| `GAuth` | `g-auth`, `g-auth-form-start`, `g-auth-rep-company`, `g-auth-rep-person`, `g-auth-un`, `g-auth-un-rep-company`, `g-auth-un-rep-person` | Authentication flows |
| `GBookings` | `g-bookings` | Booking list |
| `GContracts` | `g-contracts` | Contract list |
| `GError` | `g-error`, `g-error-login`, `g-error-not-found`, `g-error-not-translated` | Error pages — single-file, no data |
| `GFlex` | `g-flex` | Variant-based flex layout primitives |
| `GInbox` | `g-inbox`, `g-inbox-attachments`, `g-inbox-form-review`, `g-inbox-messages` | Inbox listing and sub-views — complex, domain hook |
| `GLayout` | `g-layout` | Structured page layout with named column and row variants |
| `GLink` | `g-link` | Typed link variants |
| `GLoader` | `g-loader` | Loading spinner — standard layout, `GOverridableComponent` |
| `GLocales` | `g-locales` | Locale selector |
| `GOffers` | `g-offers` | Offer listing |

---

## Directory and file structure

Create the component in its own sub-directory under `modules/gamut-primitives/`. The directory name uses kebab-case with the `g-` prefix (e.g. `g-my-widget`).

**Minimal layout (single-file, no data):**
```
g-my-widget/
  GMyWidget.tsx   ← component, props interface, useUtilityClasses, styled root all in one file
  index.ts
```
Reference: `g-error/GError.tsx`.

**Standard layout (extracted utility classes):**
```
g-my-widget/
  GMyWidget.tsx
  useUtilityClasses.tsx
  index.ts
```
Reference: `g-loader/`, `g-layout/`.

**Complex layout (sub-components + domain hook):**
```
g-my-widget/
  GMyWidget.tsx
  GMyWidgetItem.tsx
  useUtilityClasses.tsx
  utils.tsx
  index.ts
```
Reference: `g-inbox/`.

**Naming rules:**
- Component file: `PascalCase` matching the exported component name (`GMyWidget.tsx`).
- Utility classes file: always `useUtilityClasses.tsx` when extracted.
- Data/logic file: always `utils.tsx` when extracted.
- `MUI_NAME` constant: exact string matching the component name (`'GMyWidget'`).

---

## Step 1: Create the props interface

Define a plain TypeScript `interface` in the component file. Each sub-component's props live in its own file.

**Naming convention:** `G<ComponentName>Props` (exported).

**Fields to consider:**
- `children?: React.ReactNode` — if the component accepts child content.
- `component?: GOverridableComponent<GMyWidgetProps>` — if root element substitution is needed (see Step 3).
- `slots?` / `slotProps?` — if sub-components must be replaceable (see Step 4).
- `variant?` — string union for structural variants (see Step 4).

**Example:**
```ts
import { GOverridableComponent } from '@dxs-ts/gamut-api';

export interface GMyWidgetProps {
  label: string;
  onClick: () => void;
  component?: GOverridableComponent<GMyWidgetProps>;
}
```

**Class key types** — export from `useUtilityClasses.tsx` (or inline for single-file):
```ts
export interface GMyWidgetClasses { root: string; }
export type GMyWidgetClassKey = keyof GMyWidgetClasses;
```

---

## Step 2: Create useUtilityClasses

Extract to `useUtilityClasses.tsx` when the component has more than one CSS slot or shares styling with sub-components.

**Pattern:**
```ts
import { generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';

export const MUI_NAME = 'GMyWidget';

export const useUtilityClasses = () => {
  const slots = { root: ['root'], label: ['label'] };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};
```

When a variant prop affects class composition, pass `ownerState`:
```ts
export const useUtilityClasses = (ownerState: GMyWidgetProps) => {
  const slots = { root: ['root', ownerState.variant] };
  // ...
};
```

---

## Step 3: Create the component

**Structure:**
1. Destructure `initProps`.
2. Call `useThemeProps` to merge MUI theme defaults.
3. Call `useUtilityClasses`.
4. Resolve root: `const Root = props.component ?? GMyWidgetRoot`.
5. Build `ownerState` from `props`.
6. Render `<Root className={classes.root} ownerState={ownerState}>`.

**Example:**
```tsx
import React from 'react';
import { useThemeProps } from '@mui/material';
import { GMyWidgetRoot, MUI_NAME, useUtilityClasses } from './useUtilityClasses';
import { GOverridableComponent } from '@dxs-ts/gamut-api';

export interface GMyWidgetProps {
  label: string;
  component?: GOverridableComponent<GMyWidgetProps>;
}

export const GMyWidget: React.FC<GMyWidgetProps> = (initProps) => {
  const props = useThemeProps({ props: initProps, name: MUI_NAME });
  const classes = useUtilityClasses();
  const ownerState = { ...props };
  const Root = props.component ?? GMyWidgetRoot;
  return <Root className={classes.root} ownerState={ownerState}>{props.label}</Root>;
};
```

**`GOverridableComponent` contract:**
```ts
export type OverrideProps<T> = { ownerState: T; className: string; children?: React.ReactNode };
export type GOverridableComponent<T> = React.ElementType<OverrideProps<T>>;
```
The styled root must be typed as `styled('div')<{ ownerState: GMyWidgetProps }>`. Omit `component` from props when root substitution is not required.

**Styled root:**
```ts
export const GMyWidgetRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => [styles.root, styles.label],
})<{ ownerState: GMyWidgetProps }>(({ theme }) => ({
  display: 'flex',
  '.GMyWidget-label': { color: theme.palette.text.primary },
}));
```

Child slot styles go as nested `.{MUI_NAME}-{slotName}` selectors inside the styled root. Never use `sx` props.

---

## Step 4: Slots and variants (if applicable)

**Slots** — use when a sub-component must be replaceable by callers:
```ts
// Props interface:
slots?: { item?: React.ElementType<GMyWidgetItemProps> };
slotProps?: { item?: Partial<GMyWidgetItemProps> };

// Component body:
const ItemSlot = props.slots?.item ?? GMyWidgetItem;
```

**Variants** — use `if`/`else if` blocks, not a lookup map:
```ts
if (props.variant === 'compact') { return <Root ownerState={themeProps} className={classes.root}>...</Root>; }
return <Root ownerState={themeProps} className={classes.root}>...</Root>;
```

Pass `variant` to `useUtilityClasses` so it appends as an additional class on `root`.

Reference: `g-layout/GLayout.tsx` (five variants), `g-flex/GFlex.tsx` (three variants), `g-inbox/GInbox.tsx` (slots).

---

## Step 5: Context and data (if applicable)

No component may own its own `createContext`/`useContext`. All data comes from `@dxs-ts/gamut-api` hooks.

| Hook | Provides |
|---|---|
| `useIam()` | `{ authType, user }` — authentication type and current user |
| `useLocale()` | `{ locale }` — active locale string |
| `useOffers()` | `{ getLocalisedOfferName, getOffer }` — offer data |
| `useContracts()` | `{ getContract }` — contract data by ID |
| `useComms()` | `{ subjects, sortOrder, getSubject, markViewed }` — inbox subjects |
| `useSite()` | `{ site }` — site content (topics, pages) |
| `useNavigate()` from `@tanstack/react-router` | Programmatic navigation |

**Navigation pattern:**
```ts
import { useNavigate } from '@tanstack/react-router';
const nav = useNavigate();
nav({ params: { locale }, to: '/public/$locale' });
```
Route path strings must match routes declared in `gamut-routes`. Never invent new paths.

**i18n:** use `useIntl()` from `react-intl` and `intl.formatMessage({ id: 'gamut.<key>' })`. Never write static strings in JSX. Add translation keys to `intl.csv` only — never edit `en.ts`, `fi.ts`, or other generated locale files.

For complex components, extract all hook calls and data transformation into a `useMyWidgetItems()` function in `utils.tsx`. Reference: `g-inbox/utils.tsx`.

---

## Step 6: Export from index.ts

**Sub-directory `index.ts`:**
```ts
// Standard:
export * from './GMyWidget';
export type { GMyWidgetClasses, GMyWidgetClassKey } from './useUtilityClasses';

// Complex (with sub-components):
export * from './GMyWidget';
export * from './GMyWidgetItem';
export type { GMyWidgetClasses, GMyWidgetClassKey } from './useUtilityClasses';
```

**Top-level `modules/gamut-primitives/index.ts`** — append one line in alphabetical order within the family group:
```ts
export * from './g-my-widget'
```

---

## Step 7: Register in GComponentsPropsList

Register every component that uses `useThemeProps` or that a theme author might want to override. Omit purely structural helpers with no `MUI_NAME`.

Four additions required in `modules/gamut-routes/g-props/g-props.ts`:

1. **Import:**
```ts
import { GMyWidgetClassKey, GMyWidgetProps } from '@dxs-ts/gamut-primitives';
```

2. **`GComponentsPropsList`:**
```ts
GMyWidget: GMyWidgetProps;
```

3. **`GComponentNameToClassKey`:**
```ts
GMyWidget: GMyWidgetClassKey;
```

4. **`GComponents`:**
```ts
GMyWidget?: {
  defaultProps?: GComponentsProps['GMyWidget'];
  styleOverrides?: GComponentsOverrides<Theme>['GMyWidget'];
  variants?: GComponentsVariants['GMyWidget'];
};
```

---

## Checklist

- [ ] Create `modules/gamut-primitives/g-my-widget/` directory
- [ ] `GMyWidget.tsx` — props interface + component function
- [ ] `useUtilityClasses.tsx` — `MUI_NAME`, class interfaces, `useUtilityClasses`, styled root (extract when multiple slots or shared styling)
- [ ] `utils.tsx` — domain hook if data/logic is non-trivial
- [ ] Sub-component files (e.g. `GMyWidgetItem.tsx`) if compound structure is needed
- [ ] All CSS inside styled root using `.GMyWidget-<slotName>` nested selectors — no `sx` props
- [ ] `useThemeProps({ props: initProps, name: MUI_NAME })` as first operation in component function
- [ ] `ownerState` built from resolved `props` and passed to `<Root ownerState={ownerState}>`
- [ ] `component?: GOverridableComponent<GMyWidgetProps>` only if root substitution is required
- [ ] Slots/slotProps only if sub-components must be replaceable
- [ ] Boolean props prefixed with `is` (never `has`)
- [ ] Descriptive parameter names (never single-character names)
- [ ] `undefined` instead of `null` throughout
- [ ] No `display: grid` — use `flex` with fixed widths
- [ ] Sub-directory `index.ts` exporting component, sub-components, and class types
- [ ] Top-level `modules/gamut-primitives/index.ts` updated
- [ ] Four additions to `modules/gamut-routes/g-props/g-props.ts`
- [ ] Translation keys added to `intl.csv`

---

## Post-build gate

After completing all file changes, invoke the `ui-compliance` agent. Pass it the task spec you received and the list of files you created or modified.

## Report

After completing or blocking, report:

**Status:** `COMPLETE` or `BLOCKED`

If `BLOCKED`:
- Which rule was violated
- What was attempted
- What is needed to proceed

If `COMPLETE`:
- Files created and their paths
- Which family reference component was used
- Which `gamut-api` hooks were used (or "none")
- Whether `GComponentsPropsList` registration was done
- Any non-obvious decisions made during implementation
