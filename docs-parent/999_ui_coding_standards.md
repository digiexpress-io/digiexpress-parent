_last updated: 13.10.2026_

# Coding Standards

Follow these rules exactly. No exceptions.

---

## 1. Always use braces for conditionals, and return on its own line

Every `if`, `else if`, and `else` block must use `{}`. Never write a single-line return inline with the condition.

**Wrong:**
```ts
if (!tab) return prev;
```

**Correct:**
```ts
if (!tab) {
  return prev;
}
```

Applies to .ts/.tsx files in this project.

---

## 2. [fs-composer] Never import icons directly from MUI — use FsIcons

All icons must come from `FsIcons` via `../fs-theme`. Never import from `@mui/icons-material` directly.

```ts
import { FsIcons } from '../fs-theme';
// <FsIcons.Close />, <FsIcons.Search />, etc.
```

If the icon isn't in `FsIcons` yet, add it there first.

---

## 3. Follow existing patterns — never invent new ones

Before writing code, check the surrounding files for established naming and structural patterns. Match them exactly. If you're unsure, **ask**.

When re-implementing a feature from an existing codebase (e.g. migrating a component from one module to another):

- **Every line of code must be traceable to the original source.** If it wasn't in the original, it must not appear in the new version.
- **Never guess at types, props, field names, or logic.** If the original source wasn't read, go read it first.
- **Never add fields, types, or behaviors that weren't in the original** — even if they seem useful or "correct."
- **If you are unsure about any detail, stop and ask.** Do not proceed with a guess.

This rule applies to: type definitions, interface fields, optional/required markers, prop names, state shapes, function signatures, and component logic.

---

## 4. Never edit locale TypeScript files manually

`en.ts`, `fi.ts`, etc. are generated from `intl.csv`. Never edit them directly.

All translation keys go in:
```
ts_setup/eveli-ide/intl.csv
```

Check nearby keys for the correct naming prefix before adding a new entry.

No raw literal UI strings — every user-facing string goes through `useIntl().formatMessage({ id: '...' })`.

---

## 5. Never use sx props, inline style props, or CSS grid

All styling must go through `useUtilityClasses` and the module's styled components. Never pass `sx` or `style` props to any component.

Never use `display: grid` — use `flex` with fixed widths instead.

For portal-rendered content (Tooltip, Popover, Dialog internals) where ancestor-scoped CSS cannot reach: use a CSS `:hover` sibling approach to keep elements in the component tree, or export a named `styled('element')` from `useUtilityClasses.ts`. If the MUI component itself needs internal slot overrides, use `componentsProps`/`slotProps` with a `className` (not `style`), and define the class in the module's styled component.

---

## 6. When copying or re-implementing existing code, never invent anything

When re-implementing a feature from an existing codebase (e.g. migrating a component from one module to another):

- **Every line of code must be traceable to the original source.** If it wasn't in the original, it must not appear in the new version.
- **Never guess at types, props, field names, or logic.** If the original source wasn't read, go read it first.
- **Never add fields, types, or behaviors that weren't in the original** — even if they seem useful or "correct."
- **If you are unsure about any detail, stop and ask.** Do not proceed with a guess.

This rule applies to: type definitions, interface fields, optional/required markers, prop names, state shapes, function signatures, and component logic.

---

## 7. Never use "has" for booleans

All boolean variable and prop names use an `is` prefix (`isOpen`, `isError`, `isLoading`). Never `has`.

---

## 8. useUtilityClasses rules

Slot names must not be MUI reserved global-state names: `active`, `checked`, `completed`, `disabled`, `error`, `expanded`, `focused`, `focusVisible`, `open`, `readOnly`, `required`, `selected`. Using any of these causes `generateUtilityClass` to silently return `Mui-{slot}` instead of `{MUI_NAME}-{slot}`, so the stylesheet selector never matches.

`useUtilityClasses` and `useOwnerState` must never be in the same file.

`useUtilityClasses.ts`, styled roots, and `ownerState` types are internal to the folder — never re-exported from `index.ts`.

Never define a `Classes` interface or `ClassKey` type alias in `useUtilityClasses.ts` — this codebase does not use them. The `slots` object is untyped.

---

## 9. Component folder structure

Every component folder must contain:
- `ComponentName.tsx` — the component
- `useUtilityClasses.ts` — styling only
- `index.ts` — narrow barrel

Optional (only when applicable):
- `ComponentNameProps.ts` — props interface (when the interface is non-trivial)
- `useCreateOwnerState.ts` — create-flow state (only when the component manages create-flow form state)
- `useUpdateOwnerState.ts` — update-flow state (only when the component manages update-flow form state)
- `useOwnerState.ts` — derived/computed state for stateless components (only when needed)

Do **not** create `useCreateOwnerState` / `useUpdateOwnerState` for read-only display components.

---

## 10. Barrel (index.ts) rules

- Only re-export the component(s) and their public prop/option types.
- Never re-export `useUtilityClasses`, styled roots, or `ownerState` types.
- Cross-folder imports must go through the target folder's `index.ts` barrel — never a direct path into an internal file.
- Cross-module imports must use the `@dxs-ts/*` alias, not relative `../` paths. Same-folder relative imports (`./Component`) are exempt.

---

## 11. State management

A component's local state must go through a dedicated `useXxxOwnerState` hook — never raw `useState` calls in the component body.

Any `.filter()`, `.sort()`, `.map()`, grouping, or branching-on-a-field computation that produces what the component renders belongs in the hook, returned as an already-derived value. It must never appear inline in the component body above the `return`.

Purely presentational per-item formatting calls made directly inside JSX (e.g. `intl.formatMessage(...)`) are fine inline.

---

## 12. TypeScript rules

- No `null` — use `undefined`. Exception: `useRef<T>(null)` is the correct React 19 initialisation pattern; the declared type must be `React.RefObject<T | null>`, not `React.RefObject<T>`.
- Underscore-prefixed types/interfaces (e.g. `_XxxState`, `_XxxStateProps`, `_XxxOwnerState`) are module-local — never export them.
- Descriptive parameter names only — no single-character names (`v`, `e`, `k`, etc.).
- No unused declarations — any type alias, interface, variable, or constant defined in a file but never referenced within that file or re-exported for external use is a violation.

---

## 13. Dependency and command restrictions

- Never add a new npm dependency without stopping, stating the reason, and waiting for explicit confirmation.
- Never run npm/build commands.
- Never run git commands.
