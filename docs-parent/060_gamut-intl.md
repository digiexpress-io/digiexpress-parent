# gamut-intl

## Purpose

Provides static translation message maps for the Gamut customer portal. Exports a single `messages` object containing locale-keyed records used by an i18n provider (e.g. react-intl or FormatJS `IntlProvider`) to resolve translation keys at runtime.

## Dependencies

**dependencies:** none

**peerDependencies:** none

The module declares no runtime or peer dependencies. It is `"private": true` and consumed only within the monorepo.

## Module Relationships

`gamut-intl` sits at the foundation layer — it has no `@dxs-ts/*` dependencies and is consumed by modules above it.

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

`gamut-intl` is a pure data leaf. No module in the graph below `gamut-api` imports it directly.

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

- Each locale file exports a single named `const` typed as a plain object literal with string keys and string values. No type alias or interface is declared; the shape is inferred structurally.
- Keys follow a dot-separated namespace convention: `gamut.<section>.<element>` (e.g. `gamut.forms.status.OPEN`, `gamut.inbox.title`, `gamut.rps.rating.excellent`). Keys outside the `gamut.*` namespace include `booleanValue.*`, `cust.*`, `header.*`, `footer.*`, and `dialob.*`.
- Some values use ICU-style positional placeholders: `{userName}`, `{subject}`, `{nextPageTitle}`, `{start}`, `{end}`, `{stepNumber}`, `{totalSteps}`.
- The `messages` export in `index.ts` uses the locale code as the key (`messages.en`, `messages.fi`, etc.), matching the locale identifier passed to an `IntlProvider`.
- All five locale records cover the same key set. No fallback or merge logic exists in this module; that responsibility is left to the consumer.
