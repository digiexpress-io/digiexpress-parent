# @dxs-ts/gamut-routes

## Purpose

Defines the TanStack Router route tree for the Gamut portal. Provides the `router` instance, generated `routeTree`, page-level route components for both `public` and `secured` route hierarchies, and per-view layout components that compose primitives, API hooks, and form modules into full-page layouts. Also exports `GComponentsPropsList`, `GComponentNameToClassKey`, `GComponents`, `GComponentsProps`, `GComponentsVariants`, and `GComponentsOverrides` — the MUI theme-integration interfaces that enumerate prop and class-key types for every Gamut component across all sub-modules.

## Dependencies

### dependencies

- `@dxs-ts/gamut-cockpit-store`
- `@dxs-ts/gamut-api`
- `@dxs-ts/gamut-shell`
- `@dxs-ts/gamut-theme`
- `@dxs-ts/gamut-md`
- `@dxs-ts/gamut-primitives`
- `@dxs-ts/gamut-form-review`
- `@dxs-ts/gamut-form`

### peerDependencies

- `react`
- `react-dom`
- `@mui/material`
- `@mui/icons-material`
- `@tanstack/react-router`
- `@mui/utils`
- `@mui/system`
- `react-intl`

## Module Relationships

### Depends on

- `@dxs-ts/gamut-cockpit-store` — `CockpitStore` (called via `CockpitStore.save()` in `GRouterCockpits` on cockpit selection); `GCockpitDropdown` rendered inside the cockpit-switcher `Dialog`
- `@dxs-ts/gamut-api` — `useConfig`, `useLocale`, `useIam`, `useSite`, `usePopulateContext`; provider components `IamBackendProvider`, `SiteBackendProvider`, `DialobProvider`, `OfferProvider`, `ContractProvider`, `CommsProvider`, `BookingProvider`; namespace `SearchApi.SearchProvider`; types `SiteApi.TopicView`, `SearchApi.LinkToForm`
- `@dxs-ts/gamut-shell` — `GShell`, `GShellClassName` (used as the scroll restoration selector in `router.ts` and as the drawer anchor in layout components)
- `@dxs-ts/gamut-theme` — `GComponentsPropsList` and `GComponentNameToClassKey` exported from `g-props/g-props.ts` extend MUI's `Components` interface, enabling consuming applications to configure all Gamut component default props inside a single `ThemeOptions` object alongside `GThemeProvider`
- `@dxs-ts/gamut-primitives` — layout and navigation components (`GAppBar`, `GLayout`, `GFooter`, `GLogo`), display components (`GArticle`, `GArticleFeedback`, `GInbox`, `GContracts`, `GBookings`, `GOffers`), auth components (`GAuth`, `GAuthUn`, `GLogin`, `GLogout`), link variants (`GLinkFormLocked`, `GLinkFormUnlocked`, `GLinkHyper`, `GLinkPhone`), utility components (`GConfirm`, `GLoader`, `GLocales`), error components (`GErrorNotFound`, `GError`), type `GUserOverviewMenuView`
- `@dxs-ts/gamut-form` — `GForm`, `GFormBase`, `GFormGroup`, `GInputMultilist`, `GInputAddress`, `GInputLabel`; class keys and props types for MUI theme registration
- `@dxs-ts/gamut-form-review` — all review item components and class keys (`GFormReviewBoolean`, `GFormReviewChoice`, `GFormReviewDate`, `GFormReviewDecimal`, `GFormReviewGroup`, `GFormReviewItem`, `GFormReviewNote`, `GFormReviewPage`, `GFormReviewQuestionnaire`, `GFormReviewRowGroup`, `GFormReviewSurvey`, `GFormReviewSurveyGroup`, `GFormReviewText`, `GFormReviewTime`, `GFormReviewMultiChoice`); prop types `ItemProps`, `GroupItemProps`, `PageItemProps`, `QuestionnaireItemProps`, `SurveyProps`
- `@dxs-ts/gamut-md` — `GMarkdown`, `GMarkdownClassKey`, `GMarkdownProps` for MUI theme registration

### Consumed by

- `@dxs-ts/gamut` (umbrella) — imports `router`, `routeTree`, `GComponentsPropsList`, `GComponentNameToClassKey`, `GComponents`, `GComponentsProps`, `GComponentsVariants`, `GComponentsOverrides`; re-exports them as the public surface of the Gamut portal package

## Frontend Architecture

**Stack:** React, TanStack Router (file-based routing), MUI Material, react-intl

**Sub-module structure:**

- `routes/` — TanStack file-based route files; two top-level path segments: `public.$locale` (unauthenticated) and `secured.$locale` (authenticated). Public routes cover locale selection, login, site pages, products, offers, and offer summaries. Secured routes cover the same product/offer hierarchy plus views (`$viewId`) and inbox subjects (`$subjectId`).
- `routes/__root.tsx` — root route; mounts `IamBackendProvider`, selects `PublicSetup` or `SecuredSetup` based on `useIam().authType`, wraps with `AuthSetup`
- `routeTree.gen.ts` — auto-generated TanStack Router route tree; exported as `routeTree`, `FileRouteTypes`, `RootRouteChildren`
- `router.ts` — creates and exports the `router` instance via `createRouter`; sets `scrollRestorationBehavior: 'instant'`, `scrollRestoration: true`, and `scrollToTopSelectors` targeting `GShellClassName`
- `g-props/` — exports `GComponentsPropsList`, `GComponentNameToClassKey`, `GComponents`, `GComponentsProps`, `GComponentsVariants`, `GComponentsOverrides`; aggregates prop and class-key types for every Gamut component to enable MUI theme `components` configuration
- `g-router-cockpits/` — `GRouterCockpits` renders a `Dialog` containing `GCockpitDropdown`; activated by `Ctrl+Shift+D` keyboard shortcut when cockpit options are available; mounted unconditionally in both `SecuredSetup` and `PublicSetup`
- `g-router-secured-services/` — `GRouterSecuredServices` renders the authenticated service catalogue; uses `GShell`, `GAppBar`, `GLayout`, a permanent `Drawer` (desktop) or inline panel (mobile) with `SearchFilters` and `SearchResults`, and `GArticle` for topic content; navigates to product routes on form selection
- `g-router-product/` — `GRouterProduct` renders a full product page with `GShell`, `GLayout`, `GRouterProductBreadcrumbs`, `GRouterProductContent`, `GRouterProductActions`; state managed by `useGRouterProducState` from `g-router-product-types.ts`
- `g-router-offer/` — `GRouterOffer` renders form filling; exposes `useRouteToOffer` hook
- `g-router-offer-summary/` — `GRouterOfferSummary` renders submission confirmation with `FormFillSummaryBox`, `FormFillSummaryBoxAnon`, `FormFillSummaryGreeter`
- `g-router-offer-summary-rps/` — `FormFillRpsRating` renders post-submission RPS (net promoter) rating
- `g-router-inbox/` — `GRouterInbox` renders the messaging inbox view
- `g-router-inbox-subject/` — `GRouterInboxSubject` renders a single inbox thread
- `g-router-bookings/` — `GRouterBookings` renders the bookings list view
- `g-router-unfinished-forms/` — `GRouterUnfinishedForms` renders in-progress form submissions
- `g-router-forms-awaiting-decision/` — `GRouterFormsAwaitingDecision` renders forms pending a decision
- `g-router-forms-with-decision/` — `GRouterFormsWithDecision` renders forms with a completed decision
- `g-router-user-overview/` — `GRouterUserOverview` renders the user profile and overview panel
- `g-router-unsecured/` — `GRouterUnsecured` renders public (unauthenticated) page layouts

**Patterns:**

- File-based routing: each file in `routes/` exports a `Route` created via `createFileRoute` or `createRootRoute`; the route tree is generated into `routeTree.gen.ts` by the TanStack Router CLI
- Auth-driven provider selection: `AuthSetup` in `__root.tsx` reads `useIam().authType` and conditionally wraps the outlet in `SecuredSetup` (full provider stack) or `PublicSetup` (reduced provider stack without contracts, comms, and bookings)
- `ChooseComponent` dispatch: `secured.$locale.views.$viewId.index.tsx` maps the `$viewId` path parameter to a specific router component (`GRouterSecuredServices`, `GRouterInbox`, `GRouterBookings`, etc.) using a chain of `if/else` guards on `GUserOverviewMenuView` values
- MUI theme integration: each router component (`GRouterProduct`, `GRouterSecuredServices`, etc.) calls `useThemeProps` with its `MUI_NAME` constant; `g-props/g-props.ts` registers all component names in `GComponentsPropsList` and `GComponentNameToClassKey` for declaration merging into the MUI `Components` interface
- Utility classes pattern: each sub-module contains a `useUtilityClasses.tsx` file that defines a styled root component (e.g. `GRouterProductRoot`, `GRouterSecuredServicesRoot`) and an `OwnerState` type; styling is applied through MUI `generateUtilityClasses` and `composeClasses`
