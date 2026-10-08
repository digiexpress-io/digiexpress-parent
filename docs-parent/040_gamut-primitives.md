# @dxs-ts/gamut-primitives

## Purpose

Provides the concrete React component implementations for the Gamut customer portal. Each sub-module exports one or more MUI-themed components covering navigation, authentication, content display, inbox/messaging, user identity, and utility layout primitives. Components follow the MUI `useThemeProps` / `useUtilityClasses` styling pattern and expose an optional `component` prop (`GOverridableComponent`) for root element substitution.

## Dependencies

### dependencies

- `@dxs-ts/gamut-api`
- `@dxs-ts/gamut-md`
- `@dxs-ts/gamut-shell`
- `@dxs-ts/gamut-form-review`

### peerDependencies

- `react`
- `react-dom`
- `react-intl`
- `react-markdown`
- `react-leaflet`
- `@mui/material`
- `@mui/system`
- `@mui/icons-material`
- `@mui/utils`
- `@mui/types`
- `@tanstack/react-router`
- `luxon`
- `numbro`
- `leaflet`

## Module Relationships

### Dependencies (what `@dxs-ts/gamut-primitives` uses)

- `@dxs-ts/gamut-api` — domain type namespaces (`SiteApi`, `OfferApi`, `ContractApi`, `CommsApi`, `IamApi`, `SearchApi`); context hooks (`useIam`, `useSite`, `useOffers`, `useContracts`, `useComms`, `useBookings`, `useLocale`); styling utilities (`GOverridableComponent`, `useVariantOverride`); auth helper `IamForcedLogin`.
- `@dxs-ts/gamut-shell` — `GShellClassName` constant applied to the `Toolbar` in `GAppBar` so that `GShellRoot`'s scoped CSS selectors can compute toolbar-height layout offsets.
- `@dxs-ts/gamut-md` — `GMarkdown` component used in `g-article` and related sub-modules to render markdown strings as MUI-styled elements.
- `@dxs-ts/gamut-form-review` — `GFormReview` component and `GFormReviewProps` type used in `g-inbox-form-review` to render submitted Dialob form data inside an inbox item. This is the only point in `gamut-primitives` where a `gamut-form-review` component is directly mounted; the `formId` is sourced from the `OfferApi.Offer` associated with the selected inbox item.

### Consumers (what depends on `@dxs-ts/gamut-primitives`)

- `@dxs-ts/gamut-routes` — the sole direct consumer. Route components in `gamut-routes` compose primitives components to build every page: `GAppBar` and `GLayout` / `GFooter` / `GFlex` for shell structure; `GArticle` in public routes; `GInbox`, `GOffers`, `GContracts`, `GBookings` in secured routes; `GAuth*` family for authentication flows; `GLocales`, `GLogin`, `GLogout`, `GLogo` in navigation; `GError*` variants for error states; `GUserOverview` and `GUserOverviewMenu` for identity display.
- `@dxs-ts/gamut` (umbrella package) — re-exports all modules including `gamut-primitives`; not a runtime consumer beyond aggregating exports.

## Frontend Architecture

**Stack:** React, MUI Material, MUI System, react-intl, TanStack Router, Luxon, Numbro, Leaflet / react-leaflet

**Sub-module structure:**

- `g-app-bar` — `GAppBar`, `GAppBarSpacer`: top navigation bar with logo, locale switcher, login/logout, optional nav and search slots; reads IAM state to switch between `GLogin` and `GLogout`.
- `g-layout` — `GLayout`: multi-variant grid layout (`toolbar-n-rows-2-columns`, `secured-1-row-2-columns`, `secured-1-row-1-column`, `secured-1-row-1-column-small`, `fill-session-start-end`) with named slots (`left`, `right`, `center`, `topTitle`, `breadcrumbs`).
- `g-flex` — `GFlex`, `GFlexHeader`, `GFlexBody`, `GFlexHidden`: lightweight flex wrapper with `variant` discriminant; `GFlexHidden` accepts a `hiddenOn` breakpoint predicate.
- `g-auth` — `GAuth`: form wrapper (`<form action method onSubmit>`) with MUI theming applied.
- `g-auth-form-start` — `GAuthFormStart`: extends `GAuth` with forced-login support; calls `IamForcedLogin` on submit when `forced` is set and `authType === 'ANON'`, then reloads IAM state before invoking `onSubmit`.
- `g-auth-rep-company` — authentication form for representative acting as a company.
- `g-auth-rep-person` — authentication form for representative acting as a person.
- `g-auth-un` — unauthenticated entry form.
- `g-auth-un-rep-company` — unauthenticated representative (company) form.
- `g-auth-un-rep-person` — unauthenticated representative (person) form.
- `g-article` — `GArticle`: renders a `SiteApi.TopicView` using slottable sub-components `GPage`, `GLinksPage`, `GArticleFeedback`; accepts overrides for `page`, `pageLinks`, `pageBottom`, `pageFeedback`, and `pageAdditionalInfo` slots.
- `g-article-feedback` — `GArticleFeedback`: article feedback submission widget.
- `g-article-feedback-viewer` — `GArticleFeedbackViewer`: displays aggregated article feedback.
- `g-inbox` — `GInbox`, `GInboxItem`, `GInboxAttachmentCount`: tabular inbox list driven by `OfferApi.Offer` data from `useInboxItems`; supports slotted `item`, `attachment`, and `formReview` overrides.
- `g-inbox-messages` — messaging thread view within an inbox item.
- `g-inbox-attachments` — attachment list view within an inbox item.
- `g-inbox-form-review` — embeds `GFormReview` (from `@dxs-ts/gamut-form-review`) to display a submitted Dialob form inside the inbox.
- `g-inbox-form-assigned-not-complete` — state view for inbox items where a form has been assigned but not yet completed.
- `g-offers` — `GOffers`, `GOfferItem`: renders available offers from `useOffers`.
- `g-contracts` — contract list using `ContractApi` and `useContracts`.
- `g-bookings` — bookings list using `useBookings`.
- `g-user-overview` — `GUserOverview`, `GUserOverviewDetail`: summary and detail view of authenticated user identity.
- `g-user-overview-menu` — `GUserOverviewMenuView`: dropdown menu for user account actions.
- `g-secured-services-search` — search input for secured services using `SearchApi`.
- `g-locales` — `GLocales`: locale/language selector; reads current locale, emits `onClick(newLocale)`.
- `g-login` — `GLogin`: login trigger button.
- `g-logout` — `GLogout`: logout trigger button.
- `g-logo` — `GLogo`: site logo with `variant` and optional `onClick`.
- `g-link` / `g-links` / `g-links-page` — `GLink`, `GLinks`, `GLinksPage`: hyperlink primitives and grouped link lists for site navigation.
- `g-popover-button` — `GPopoverButton`: button that opens a `Popover`.
- `g-popover-search` — `GPopoverSearch`: popover-wrapped search field with anchor management (`useAnchor`).
- `g-popover-topics` — `GPopoverTopics`: popover for browsing site topics.
- `g-confirm` — `GConfirm`: confirmation dialog.
- `g-loader` — `GLoader`: loading indicator.
- `g-sort` — `GSort`: sort control.
- `g-date` — `GDate`: date display component (uses Luxon).
- `g-tooltip` — `GTooltip`: MUI tooltip wrapper.
- `g-divider` — `GDivider`: section divider.
- `g-footer` — `GFooter`: page footer.
- `g-error` — `GError`: generic error display.
- `g-error-login` — `GErrorLogin`: error state for authentication failures.
- `g-error-not-found` — `GErrorNotFound`: 404 error view.
- `g-error-not-translated` — `GErrorNotTranslated`: error view for missing translations.
- `g-form-unavailable` — `GFormUnavailable`: message shown when a form is not available.

**Patterns:**

- Every component calls `useThemeProps({ props, name: MUI_NAME })` to allow theme-level prop overrides via `components[MUI_NAME].defaultProps`.
- Styling is produced by a per-sub-module `useUtilityClasses` hook following the MUI `generateUtilityClasses` convention; styled root components are defined as `G<Name>Root` in the same `useUtilityClasses.tsx` file.
- The `component` prop (`GOverridableComponent<Props>`) on each public component allows the root element to be replaced with an arbitrary component while preserving `ownerState` passing.
- `slots` / `slotProps` props on composite components (e.g., `GInbox`, `GArticle`) allow consumers to inject or replace inner components without forking the parent.
- Authentication state (`authType`, `useIam`) determines conditional rendering of login/logout controls and identity labels throughout nav components.
