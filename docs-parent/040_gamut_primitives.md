_last updated: 13.10.2026_

# @dxs-ts/gamut-primitives

## Purpose

Provides the concrete React component implementations for the Gamut customer portal. Each sub-module exports one or more MUI-themed components covering navigation, authentication, content display, inbox/messaging, user identity, and utility layout primitives. Components follow the MUI `useThemeProps` / `useUtilityClasses` styling pattern and expose an optional `component` prop (`GOverridableComponent`) for root element substitution.

## Quick Navigation

| Concept | File | Key Exports |
|---------|------|-------------|
| Top navigation bar | `g-app-bar/GAppBar.tsx` | `GAppBar`, `GAppBarSpacer`, `GAppBarProps`, `GNavSlotProps`, `GSearchSlotProps` |
| App bar class types | `g-app-bar/index.ts` | `GAppBarClassKey`, `GAppBarClasses` |
| Multi-variant grid layout | `g-layout/GLayout.tsx` | `GLayout`, `GLayoutProps`, `GLayoutClasses`, `GLayoutClassKey` |
| Flex wrapper | `g-flex/GFlex.tsx` | `GFlex`, `GFlexHeader`, `GFlexBody`, `GFlexHidden` |
| Authentication form wrapper | `g-auth/GAuth.tsx` | `GAuth` |
| Auth with forced-login | `g-auth-form-start/GAuthFormStart.tsx` | `GAuthFormStart`, `GAuthFormStartClassKey` |
| Auth — rep (company) | `g-auth-rep-company/GAuthRepCompany.tsx` | `GAuthRepCompany` |
| Auth — rep (person) | `g-auth-rep-person/GAuthRepPerson.tsx` | `GAuthRepPerson` |
| Auth — unauthenticated | `g-auth-un/GAuthUn.tsx` | `GAuthUn` |
| Auth — unauth rep (company) | `g-auth-un-rep-company/GAuthUnRepCompany.tsx` | `GAuthUnRepCompany` |
| Auth — unauth rep (person) | `g-auth-un-rep-person/GAuthUnRepPerson.tsx` | `GAuthUnRepPerson` |
| Article / topic view | `g-article/GArticle.tsx` | `GArticle`, `GPage` |
| Article class types | `g-article/index.ts` | `GArticleClassKey`, `GArticleClasses` |
| Article feedback widget | `g-article-feedback/GArticleFeedback.tsx` | `GArticleFeedback`, `GArticleFeedbackList`, `GArticleFeedbackTableHead`, `GArticleFeedbackTableToolbar` |
| Article feedback viewer | `g-article-feedback-viewer/GArticleFeedbackViewer.tsx` | `GArticleFeedbackViewer` |
| Inbox list | `g-inbox/GInbox.tsx` | `GInbox`, `GInboxAttachmentCount`, `GInboxProps` |
| Inbox item | `g-inbox/GInboxItem.tsx` | `GInboxItem`, `GInboxItemProps` |
| Inbox class types | `g-inbox/index.ts` | `GInboxClasses`, `GInboxClassKey` |
| Inbox messaging thread | `g-inbox-messages/index.ts` | `GInboxMessages`, `GInboxMessage`, `GInboxNewMessage`, `GInboxMessageNotAllowed`, `GInboxMessagesClassKey`, `GInboxMessagesClasses` |
| Inbox attachments | `g-inbox-attachments/GInboxAttachments.tsx` | `GInboxAttachments`, `GInboxAttachmentsProps` |
| Inbox form review | `g-inbox-form-review/GInboxFormReview.tsx` | `GInboxFormReview`, `GInboxFormReviewProps` |
| Inbox form assigned not complete | `g-inbox-form-assigned-not-complete/GInboxFormAssignedNotComplete.tsx` | `GInboxFormAssignedNotComplete` |
| Offers list | `g-offers/GOffers.tsx` | `GOffers`, `GOffersClassKey`, `GOffersClasses` |
| Offer item | `g-offers/GOfferItem.tsx` | `GOfferItem` |
| Contracts list | `g-contracts/GContracts.tsx` | `GContracts`, `GContractsClasses`, `GContractsClassKey` |
| Contract item | `g-contracts/GContractItem.tsx` | `GContractItem` |
| Bookings list | `g-bookings/GBookings.tsx` | `GBookings`, `GBookingsClassKey`, `GBookingsClasses` |
| User overview summary/detail | `g-user-overview/GUserOverview.tsx` | `GUserOverview`, `GUserOverviewClassKey`, `GUserOverviewClasses` |
| User overview detail | `g-user-overview/GUserOverviewDetail.tsx` | `GUserOverviewDetail`, `GUserOverviewDetailClassKey`, `GUserOverviewDetailClasses` |
| User account menu | `g-user-overview-menu/GUserOverviewMenu.tsx` | `GUserOverviewMenuView`, `GUserOverviewMenuClassKey`, `GUserOverviewMenuClasses` |
| Secured services search | `g-secured-services-search/GSecuredServicesSearch.tsx` | `GSecuredServicesSearch` |
| Locale selector | `g-locales/GLocales.tsx` | `GLocales`, `GLocalesClassKey`, `GLocalesClasses` |
| Login trigger | `g-login/GLogin.tsx` | `GLogin` |
| Logout trigger | `g-logout/GLogout.tsx` | `GLogout` |
| Site logo | `g-logo/GLogo.tsx` | `GLogo`, `GLogoClassKey`, `GLogoClasses` |
| Hyperlink primitive | `g-link/GLinkHyper.tsx` | `GLinkHyper` |
| Phone link | `g-link/GLinkPhone.tsx` | `GLinkPhone` |
| Info link | `g-link/GLinkInfo.tsx` | `GLinkInfo` |
| Grouped link list | `g-links/GLinks.tsx` | `GLinks` |
| Links page (article links) | `g-links-page/GLinksPage.tsx` | `GLinksPage`, `GLinksPageClassKey`, `GLinksPageClasses` |
| Article links hook | `g-links-page/useGArticleLinks.tsx` | `useGArticleLinks` |
| Popover button | `g-popover-button/GPopoverButton.tsx` | `GPopoverButton`, `GPopoverButtonClassKey`, `GPopoverButtonClasses` |
| Popover anchor hook | `g-popover-button/useAnchor.tsx` | `useAnchor` |
| Popover search | `g-popover-search/GPopoverSearch.tsx` | `GPopoverSearch`, `GPopoverSearchClassKey`, `GPopoverSearchClasses` |
| Popover topics | `g-popover-topics/GPopoverTopics.tsx` | `GPopoverTopics` |
| Confirmation dialog | `g-confirm/GConfirm.tsx` | `GConfirm`, `GConfirmClassKey`, `GConfirmClasses` |
| Loading indicator | `g-loader/GLoader.tsx` | `GLoader`, `GLoaderClassKey`, `GLoaderClasses` |
| Sort control | `g-sort/GSort.tsx` | `GSort` |
| Date display | `g-date/GDate.tsx` | `GDate` |
| Tooltip | `g-tooltip/GTooltip.tsx` | `GTooltip` |
| Divider | `g-divider/GDivider.tsx` | `GDivider` |
| Page footer | `g-footer/GFooter.tsx` | `GFooter`, `GFooterClassKey`, `GFooterClasses` |
| Generic error view | `g-error/GError.tsx` | `GError` |
| Auth failure error view | `g-error-login/GErrorLogin.tsx` | `GErrorLogin` |
| 404 error view | `g-error-not-found/GErrorNotFound.tsx` | `GErrorNotFound` |
| Missing-translation error view | `g-error-not-translated/GErrorNotTranslated.tsx` | `GErrorNotTranslated` |
| Form unavailable message | `g-form-unavailable/GFormUnavailable.tsx` | `GFormUnavailable` |

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
- `@dxs-ts/gamut` (umbrella package) — re-exports all modules including `gamut-primitives`. Not a runtime consumer beyond aggregating exports.

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

## Error Handling

`gamut-primitives` defines no validation error types, API failure types, error-boundary components, error context, or typed error classes. Error states are surfaced through dedicated display components. All error data originates from `@dxs-ts/gamut-api` context hooks (`useIam`, `useComms`, `useContracts`, `useOffers`, `useSite`); this module handles only how those states are displayed.

- `GError` — generic error page; reads `useIam` to route the "back to home" button to either the public or secured route depending on `authType`.
- `GErrorLogin` — authentication failure page; renders `GAuthFormStart` so the user can retry login directly from the error screen.
- `GErrorNotFound` — 404 state; same `authType`-conditional navigation as `GError`.
- `GErrorNotTranslated` — missing-translation state; uses `router.history.back()` rather than a home-page redirect.
- `GLoader` — loading state; renders a `CircularProgress` spinner with a `react-intl` formatted label (`gamut.loading`); used by consumers while async data is pending.
- `GInboxMessages` — if `subject`, `site`, or `contract` resolves to `undefined`, the component logs `console.error("no site / contract", ...)` and renders a plain text fallback (`...no site / contract`) instead of crashing.
- `GInboxMessageNotAllowed` — rendered in place of `GInboxNewMessage` when the contract status is `COMPLETED` or `REJECTED`, or when the exchange list is empty, preventing message submission in terminal states.

## Bootstrapping

None — leaf node. `gamut-primitives` exports only presentational components: no React context providers, no global state initialization, no setup call. Consuming applications must provide:

- `@dxs-ts/gamut-api` context providers, mounted above the component tree, supplying `useIam`, `useSite`, `useOffers`, `useContracts`, `useComms`, `useBookings`, `useLocale`, and `useSearchApi`.
- A standard MUI `ThemeProvider` with `components[MUI_NAME].defaultProps` overrides as needed.
- TanStack Router context for `GError`, `GErrorLogin`, `GErrorNotFound`, and `GErrorNotTranslated`, which call `useNavigate` or `useRouter`.
- `react-intl`'s `IntlProvider` for all components that call `useIntl`.

## TypeScript Extension Points

None. No `declare module` augmentations, interface merging, or extension records are present anywhere in the module source. MUI theme overrides follow the standard MUI runtime convention — adding entries to the `components` key of the theme object under the component's `MUI_NAME` string constant — but this is not a TypeScript extension point declared in this module.

## Common Tasks

### Add a new primitive component

- Create a new sub-directory under `modules/gamut-primitives/` using the `g-<name>` naming convention.
- Add a `useUtilityClasses.tsx` file defining `MUI_NAME`, `useUtilityClasses`, and the styled root component (`G<Name>Root`) using `generateUtilityClasses` and `useThemeProps`.
- Add a `G<Name>.tsx` file that:
  - Defines and exports a `G<Name>Props` interface including an optional `component?: GOverridableComponent<G<Name>Props>` prop.
  - Calls `useThemeProps({ props, name: MUI_NAME })` at the top of the component function.
  - Calls `useUtilityClasses()` to obtain CSS class keys.
  - Renders `props.component ?? G<Name>Root` as the root element, passing `ownerState` and `className={classes.root}`.
- Add an `index.ts` that re-exports `*` from the component file and named class-type exports (`G<Name>ClassKey`, `G<Name>Classes`) from `useUtilityClasses`.
- Add the new sub-module export to the top-level `index.ts` at `modules/gamut-primitives/index.ts`.

### Add a new prop to an existing component

- Open the target component file (e.g., `g-<name>/G<Name>.tsx`) and add the new prop to the `G<Name>Props` interface.
- If the prop requires a new CSS class, add the class key to the `generateUtilityClasses` call in `g-<name>/useUtilityClasses.tsx` and add the corresponding style rule to the styled root component in the same file.
- If the new prop needs a theme-level default, consumers add it under `components[MUI_NAME].defaultProps` in their MUI theme — no code change in this module is required for that.
- Verify that `ownerState` passed to the root element includes the new prop if the styled component's `shouldForwardProp` or style functions depend on it.

### Add a slot to a composite component

- Open the target composite component (e.g., `g-inbox/GInbox.tsx` or `g-article/GArticle.tsx`) and extend the `slots` and `slotProps` fields on the props interface with the new slot name and its props type.
- Inside the component body, resolve the slot element: `const SlotComp = props.slots?.newSlot ?? DefaultComponent`.
- Render `<SlotComp {...props.slotProps?.newSlot} />` at the appropriate position in the JSX tree.
- If `slotProps` for the new slot must be required (matching the existing pattern in `GInboxProps`), declare it without `?`; otherwise declare it optional to avoid forcing callers to supply it.

## Known Limitations and Gotchas

- **`GLinkFormUnlocked` may be dead code** (`g-link/GLinkFormUnlocked.tsx` line 33): A `//TODO Perhaps this can be deleted, but let's wait and see` comment marks the entire component as potentially unused. Consumers should not rely on it remaining in the module.
- **`GLayout` variant `fill-session-start-end` may be obsolete** (`g-layout/GLayout.tsx` line 87): `//TODO is this variant needed anymore?` annotates the `fill-session-start-end` branch. Using this variant carries a risk of removal in a future cleanup.
- **`GInboxFormReview-reviewTitle` styling is incomplete** (`g-inbox-form-review/useUtilityClasses.tsx` line 63): The `.GInboxFormReview-reviewTitle` style block carries a bare `//TODO` with no body. The slot applies `theme.typography.h1` as a placeholder; final typography intent is unresolved.
- **`GInboxMessages` silent failure on missing data**: When `subject`, `site`, or `contract` is `undefined`, the component renders a plain string `...no site / contract` instead of delegating to `GError` or throwing. This is a non-standard fallback with no i18n key.
- **`GInboxProps.slotProps` is required, not optional**: `GInboxProps.slotProps` is typed without `?`, meaning callers must always pass the full `slotProps` object even when overriding nothing. In contrast, `slots` is optional. This asymmetry is present in both `GInboxProps` and `GInboxMessagesProps`.
- **`onClick` is non-null asserted in `GInbox`**: `props.slotProps.item.onClick!` uses a non-null assertion (`g-inbox/GInbox.tsx` line 83), so omitting `slotProps.item.onClick` will cause a runtime failure, not a type error at the call site.
- **`package.json` version is a Maven placeholder**: The `"version"` field is `"${project.version}"`, not a semver string. The package is `"private": true` and version resolution happens at build time through the parent Maven project.

## Scope Boundaries

`gamut-primitives` implements the visual layer: it takes typed props and context-hook return values from `@dxs-ts/gamut-api` and renders them as MUI-styled components. It does not own:

- Data-fetching, state management, routing configuration, or authentication logic.
- API types, context providers, or global stores.
- Dialob form rendering — that belongs to `@dxs-ts/gamut-form-review`, which this module consumes.
- Page assembly or URL route definitions — those belong to `@dxs-ts/gamut-routes`.
- A theme or design tokens — consumers must supply a MUI theme.

## TanStack Integration

### TanStack Router

Four components in `gamut-primitives` use TanStack Router directly:

- `GError` — calls `useNavigate` to redirect to the home route (public or secured, conditional on `authType`).
- `GErrorLogin` — calls `useNavigate`; renders `GAuthFormStart` to allow login retry inline.
- `GErrorNotFound` — calls `useNavigate` for the same `authType`-conditional home redirect as `GError`.
- `GErrorNotTranslated` — calls `useRouter` and invokes `router.history.back()` instead of redirecting to a home route.

All four import `useNavigate` or `useRouter` directly from `@tanstack/react-router`. There is no project-level wrapper hook or abstraction.

`Link` from `@tanstack/react-router` is not used anywhere in `gamut-primitives`. The `g-link` / `g-links` / `g-links-page` sub-modules (`GLink`, `GLinks`, `GLinksPage`) use MUI `Link` (or plain anchor elements), not TanStack `Link`. Where clickable navigation elements need programmatic routing, the `onClick` handler calls the `nav` function returned by `useNavigate`.

### TanStack Query

No component calls `useQuery` directly. All data — offers, contracts, comms, bookings, IAM state, site content — arrives through provider context hooks (`useOffers`, `useContracts`, `useComms`, `useBookings`, `useIam`, `useSite`) supplied by `@dxs-ts/gamut-api`. The query lifecycle (fetching, caching, `isPending` guards) is owned by the provider layer above this module.

### TanStack Store and TanStack Query Mutations

`@tanstack/react-store` and `useMutation` are not used. `gamut-form` uses `@tanstack/react-store` for scroll-to-error ref tracking; provider components in `gamut-api` handle all write operations via plain async functions and `refetch()`.

## Open Questions

| Item | Current State | Risk | Action Needed |
|------|--------------|------|---------------|
| `GLinkFormUnlocked` may be dead code | `g-link/GLinkFormUnlocked.tsx` carries a `//TODO Perhaps this can be deleted, but let's wait and see` comment; component is exported but may have no live consumers | Consumers relying on it could break if it is removed without notice | Audit callers in `gamut-routes` and the umbrella package; delete if unused or add a deprecation notice |
| `GLayout` variant `fill-session-start-end` may be obsolete | `g-layout/GLayout.tsx` annotates the branch with `//TODO is this variant needed anymore?`; the variant is still rendered | Variant may be silently removed in a future cleanup, breaking any route that uses it | Confirm whether any route in `gamut-routes` passes `variant="fill-session-start-end"`; remove or retain and drop the TODO accordingly |
| `GInboxFormReview-reviewTitle` styling is incomplete | `g-inbox-form-review/useUtilityClasses.tsx` has a bare `//TODO` with no body; slot falls back to `theme.typography.h1` | Final typography intent is unresolved; a future theme change may expose the placeholder | Decide the intended typography token and replace the placeholder style |
| `GInboxMessages` silent failure on missing data | When `subject`, `site`, or `contract` is `undefined`, the component renders the plain string `...no site / contract` with no i18n key and no delegation to `GError` | Non-standard fallback; difficult to detect in testing and untranslatable | Replace with a proper error state: delegate to `GError` or introduce an i18n message key |
| `GInboxProps.slotProps` is required, not optional | `GInboxProps.slotProps` is typed without `?`; callers must always pass the full object even when overriding nothing; same asymmetry in `GInboxMessagesProps` | Unnecessary burden on callers; inconsistent with `slots` which is optional | Make `slotProps` (and each of its sub-keys) optional (`slotProps?: { item?: ...; ... }`) |
| `onClick` non-null assertion in `GInbox` | `props.slotProps.item.onClick!` in `g-inbox/GInbox.tsx` suppresses the type error; omitting `onClick` causes a runtime crash, not a compile-time error | Silent runtime failure; not caught by TypeScript | Either make `onClick` required in `GInboxItemProps` or remove the non-null assertion and add a runtime guard |
| `package.json` version is a Maven placeholder | `"version": "${project.version}"` in the package manifest; the package is `"private": true` | Tooling that reads the version field (e.g., bundler metadata, audit tools) will receive a literal placeholder string | Document the Maven build step that substitutes the value; no code change needed unless the package becomes public |
