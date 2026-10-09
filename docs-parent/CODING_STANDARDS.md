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

---

## 4. Never edit locale TypeScript files manually

`en.ts`, `fi.ts`, etc. are generated from `intl.csv`. Never edit them directly.

All translation keys go in:
```
ts_setup/eveli-ide/intl.csv
```

Check nearby keys for the correct naming prefix before adding a new entry.

---

## 5. Never use sx props or inline style props

All styling must go through `useUtilityClasses` and the module's styled components. Never pass `sx` or `style` props to any component.

For portal-rendered content (Tooltip, Popover, Dialog internals) where ancestor-scoped CSS cannot reach: use a CSS `:hover` sibling approach to keep elements in the component tree, or export a named `styled('element')` from `useUtilityClasses.ts`. If the MUI component itself needs internal slot overrides, use `componentsProps`/`slotProps` with a `className` (not `style`), and define the class in the module's styled component.

---

## 6. When copying or re-implementing existing code, never invent anything

When re-implementing a feature from an existing codebase (e.g. migrating a component from one module to another):

- **Every line of code must be traceable to the original source.** If it wasn't in the original, it must not appear in the new version.
- **Never guess at types, props, field names, or logic.** If the original source wasn't read, go read it first.
- **Never add fields, types, or behaviors that weren't in the original** — even if they seem useful or "correct."
- **If you are unsure about any detail, stop and ask.** Do not proceed with a guess.

This rule applies to: type definitions, interface fields, optional/required markers, prop names, state shapes, function signatures, and component logic.

## 7. Never use "has" for booleans

Use "isAbc" or simply the boolean's name