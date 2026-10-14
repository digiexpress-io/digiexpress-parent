_last updated: 14.10.2026_

# `@dxs-ts/eveli-intl`

## Purpose

Provides static translation message maps for three locales (en, fi, sv), exported as a single `messages` object keyed by locale code for use with react-intl's `IntlProvider` in the Eveli worker portal.

## Dependencies

No `dependencies` or `peerDependencies` declared in `package.json`. The module contains no runtime imports — it exports plain TypeScript object literals only.

## Module Relationships

This module has no `@dxs-ts/*` imports. It is a leaf node that exports data, not logic.

## Frontend Architecture

**Stack:** TypeScript only — no React, no MUI, no hooks.

**Sub-module structure:**
- `en.ts` — English message map (~330 keys)
- `fi.ts` — Finnish message map (same key set)
- `sv.ts` — Swedish message map (same key set)
- `index.ts` — re-exports `messages = { en, fi, sv }`

**Patterns:**
- Each locale file exports a plain object literal with string keys following dot-notation namespacing (e.g. `task.status.new`, `menu.tasks`, `error.valueRequired`).
- Some values use react-intl ICU placeholders (e.g. `{count}`, `{from}`, `{to}`, `{cause}`, `{fileName}`, `{minLength}`, `{max}`, `{tag}`).

## Error Handling

No async paths, no runtime errors, and no components exist in this module. Errors surface only if a consumer references an undefined key — react-intl renders the key string as fallback. The `LocaleProvider` in `@dxs-ts/eveli-api` suppresses react-intl `onError` callbacks by passing `onError={() => {}}`.

## Bootstrapping

Leaf node with no providers — no bootstrapping required from this module. Consuming code passes the `messages` export to `LocaleProvider` (in `@dxs-ts/eveli-api`), which merges it with optional caller-supplied overrides before passing the result to react-intl's `IntlProvider`.

## TypeScript Extension Points

No `declare module`, interface merging, or extension records exist in this module. The exported `messages` object is typed as a plain inferred TypeScript record with no named interface.

## Known Limitations and Gotchas

- **Generated source — do not edit directly.** The comment in `100_eveli_modules.md` and the `eveli-ide/intl.csv` reference in that doc indicate the `en.ts`, `fi.ts`, and `sv.ts` files are generated from a CSV source (`eveli-ide/intl.csv`). Edits to these files will be overwritten by the generator.
- **Three locales only.** `gamut-intl` covers five locales (en, fi, sv, bs, my). `eveli-intl` covers only en, fi, sv. `LocaleProvider` in `eveli-api` hard-codes `['en', 'fi', 'sv']` as the supported set and falls back to `'en'` for any other locale.
- **No TypeScript key union.** Message keys are plain string literals. There is no generated union type or record that would give compile-time errors for missing or misspelled keys.
- **Locale detection reads the URL path.** `LocaleProvider` resolves the initial locale by scanning `window.location.pathname` for a path segment after `secured` or `public`. If neither segment appears, it falls back to `navigator.language`. This means locale selection is implicit and tied to URL structure.

## Scope Boundaries

This module does not implement locale detection, locale persistence, or the `IntlProvider` wrapper — those live in `@dxs-ts/eveli-api`'s `LocaleProvider`. It does not contain React components, hooks, or any runtime logic. It does not cover Gamut portal translations (handled by `@dxs-ts/gamut-intl`).
