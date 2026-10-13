_last updated: 13.10.2026_

# gamut-intl

## Purpose

Provides static translation message maps for the Gamut customer portal. Exports a single `messages` object containing locale-keyed records used by an i18n provider (e.g. react-intl or FormatJS `IntlProvider`) to resolve translation keys at runtime.

## Dependencies

**dependencies:** none

**peerDependencies:** none

`"private": true` — consumed only within the monorepo.

## Module Relationships

`gamut-intl` sits at the foundation layer with no `@dxs-ts/*` dependencies.

**Direct consumer: `@dxs-ts/gamut-api`**

`gamut-api` imports `messages` from `gamut-intl`. The locale selection flow (`api-locale`) holds the active locale code in `LocaleContextType`. The host app reads that code and passes `messages[locale]` to `react-intl`'s `IntlProvider`, making the translation map available to every component that calls `useIntl()` or renders `<FormattedMessage>`.

**Indirect consumers (via `react-intl` context)**

All feature components that resolve UI strings at runtime depend on the `messages` record being present in the `IntlProvider` context. This includes:

- `GAppBar`, `GLocales`, `GFooter` (`@dxs-ts/gamut-primitives`) — navigation labels and locale switcher strings
- `GForm`, `GFormBase`, `GFormBaseElement`, `GInputError` (`@dxs-ts/gamut-form`) — form labels, validation messages, step indicators
- `GFormReview`, `GFormReviewQuestionnaire`, `GFormReviewText`, `GFormReviewChoice`, `GFormReviewBoolean` (`@dxs-ts/gamut-form-review`) — read-only review labels
- `GInbox`, `GArticle`, `GOffers`, `GContracts`, `GBookings` (`@dxs-ts/gamut-primitives`) — section headings, status strings, action button labels
- `GRouterOffer`, `GRouterInbox`, `GRouterProduct` (`@dxs-ts/gamut-routes`) — route-level UI strings

None of these modules import `gamut-intl` directly; they consume translations through `useIntl()` from `react-intl`, which is seeded with `messages[locale]` at the `IntlProvider` boundary.

**Position in dependency graph**

```
gamut-intl (leaf)
    └── messages[locale] ──► gamut-api (LocaleContextType / host IntlProvider setup)
                                 └──► gamut-shell, gamut-primitives, gamut-form,
                                      gamut-form-review, gamut-routes
                                      (all consume translations via useIntl())
```

## Frontend Architecture

**Stack:** TypeScript (ESM, no framework dependency)

**Sub-module structure:**

- `en.ts` — English locale record (`en`)
- `fi.ts` — Finnish locale record (`fi`)
- `sv.ts` — Swedish locale record (`sv`)
- `bs.ts` — Bosnian locale record (`bs`)
- `my.ts` — Malay locale record (`my`)
- `index.ts` — aggregates all locale records into the exported `messages` constant

**Patterns:**

- Each locale file exports a single named `const` as a plain object literal with string keys and string values.
- Keys follow a dot-separated namespace convention: `gamut.<section>.<element>` (e.g. `gamut.forms.status.OPEN`, `gamut.inbox.title`, `gamut.rps.rating.excellent`). Keys outside the `gamut.*` namespace include `booleanValue.*`, `cust.*`, `header.*`, `footer.*`, and `dialob.*`.
- Some values use ICU-style positional placeholders: `{userName}`, `{subject}`, `{nextPageTitle}`, `{start}`, `{end}`, `{stepNumber}`, `{totalSteps}`.
- The `messages` export in `index.ts` uses the locale code as the key (`messages.en`, `messages.fi`, etc.), matching the locale identifier passed to an `IntlProvider`.
- All five locale records cover the same key set. No fallback or merge logic exists in this module; that responsibility belongs to the consumer.

## Error Handling

None. `gamut-intl` exports static object literals and performs no I/O, validation, or async operations. No error types, error boundaries, loading states, or failure hooks exist in the module.

## Bootstrapping

None. `gamut-intl` exports a single `messages` constant. The consuming app (via `gamut-api`) selects the active locale from `LocaleContextType` and passes `messages[locale]` to `react-intl`'s `IntlProvider`. No provider, context, or initialization call exists inside this module.

## TypeScript Extension Points

None. The module contains no `declare module` augmentations, no interface merging, and no extension records. No named type alias or interface is declared for the message shape; each locale file exports a plain inferred object literal.

## Known Limitations and Gotchas

- **No type contract on the message shape.** The locale exports (`en`, `fi`, `sv`, `bs`, `my`) are untyped plain object literals. TypeScript infers the shape structurally per file. No shared interface or `Record<string, string>` alias enforces that all locales carry identical keys, so a missing key in one locale file will not produce a compile error.
- **No fallback logic.** If a key is missing from the selected locale record, `react-intl` falls back to its own default behavior (typically rendering the key string). This module does not merge locale records or provide a fallback chain.
- **Placeholder values in `cust.*` and `footer.*` keys.** Several keys in `en.ts` contain placeholder contact data (e.g. `cust.phone: 'Phone: 123 456 7890'`, `cust.email: 'info@lifeDigital.com'`, `footer.column4.email: 'Email: info@digiExpress.io'`). These are not production values and must be overridden at the `IntlProvider` level by the deploying application.
- **`"version": "${project.version}"`** in `package.json` is a Maven property placeholder, not a resolved semver string. The build pipeline is expected to substitute it.
- No inline `NOTE`, `TODO`, `FIXME`, or `HACK` comments were found in any source file.

## Scope Boundaries

`gamut-intl` provides only the static translation string maps for the five supported locales (`en`, `fi`, `sv`, `bs`, `my`). It does not:

- perform locale detection, locale switching, or fallback chaining
- handle ICU message formatting, pluralization, date/number formatting, or any runtime logic
- wrap or configure `react-intl` or any other i18n library
- define translation key types or enforce key parity between locales

All of those responsibilities belong to consumers, primarily `@dxs-ts/gamut-api`.
