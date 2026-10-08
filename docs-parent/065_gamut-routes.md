# @dxs-ts/gamut-routes

## Purpose

Defines the TanStack Router route tree for the Gamut portal. Exports the `router` instance, generated `routeTree`, page-level route components for both `public` and `secured` route hierarchies, and per-view layout components that compose primitives, API hooks, and form modules into full-page layouts. Also exports `GComponentsPropsList`, `GComponentNameToClassKey`, `GComponents`, `GComponentsProps`, `GComponentsVariants`, and `GComponentsOverrides` — the MUI theme-integration interfaces that enumerate prop and class-key types for every Gamut component across all sub-modules.

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
- `@dxs-ts/gamut-shell` — `GShell`, `GShellClassName` (scroll restoration selector in `router.ts` and drawer anchor in layout components)
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

## Error Handling

Route-level errors are handled by two components registered on the root route in `routes/__root.tsx`:

- `GErrorNotFound` (from `@dxs-ts/gamut-primitives`) — assigned to `notFoundComponent`; renders for unmatched routes
- `GError` (from `@dxs-ts/gamut-primitives`) — assigned to `errorComponent`; renders for thrown errors caught by TanStack Router's error boundary

Additional error rendering occurs inline within route components:

- `GErrorNotTranslated` — rendered in `public.$locale.pages.$pageId.tsx` when a page ID is not found in the current locale's site views
- `GErrorNotFound` — rendered in `secured.$locale.views.$viewId.index.tsx` when the `$viewId` parameter does not match any known `GUserOverviewMenuView` value
- `GErrorLogin` — rendered in `public.$locale.login.tsx`

Authorization failures are handled in `RouteComponent` (`routes/__root.tsx`) via `IamBackendProvider`'s `onAuthorizationFail` callback, which navigates to `/public/$locale/login`.

`SearchResults.tsx` in `g-router-secured-services/` reads `backend.loading` from `SearchApi.useBackendSearch` and conditionally renders a `CircularProgress` (MUI) with `aria-busy`. No global loading primitive is mounted at the route level; individual router components delegate loading state to their backing primitives and API hooks.

Form validation is delegated to `@dxs-ts/gamut-form` and `@dxs-ts/gamut-api`. No validation error types are defined in this module.

## Bootstrapping

A consuming application must wrap the TanStack `RouterProvider` with the following providers, in this order:

1. `QueryClientProvider` (from `@tanstack/react-query`) — required by all API hooks; must be the outermost wrapper
2. `LocaleProvider` (from `@dxs-ts/gamut`) — provides locale context consumed by `useLocale` and `react-intl`
3. MUI theme provider (e.g. `ThemeProvider`) — required for `useThemeProps` calls in each router component
4. `ConfigProvider` (from `@dxs-ts/gamut`) — provides the configuration object consumed by `useConfig`; must receive `handleExpire`, `iamLiveness`, and `staleTime` options

The `router` instance is registered for type safety via `declare module '@tanstack/react-router' { interface Register { router: typeof router } }` in `router.ts`. The consuming application must extend the MUI `Components` interface with `GComponents<Theme>` to enable theme `components` configuration for all Gamut components:

```ts
declare module "@mui/material" {
  export interface Components<Theme = unknown> extends GComponents<Theme> { }
}
```

All backend-specific providers (`IamBackendProvider`, `SiteBackendProvider`, `DialobProvider`, `OfferProvider`, `ContractProvider`, `CommsProvider`, `BookingProvider`) are mounted internally by the root route and its `SecuredSetup`/`PublicSetup` components — the consuming application does not mount them directly.

## TypeScript Extension Points

- `declare module '@tanstack/react-router' { interface Register { router: typeof router } }` in `router.ts` — registers the `router` instance for full type inference across TanStack Router hooks and link components
- `declare module '@tanstack/react-router' { interface FileRoutesByPath { ... } }` in `routeTree.gen.ts` — auto-generated; maps every route path string to its ID, preloader, and parent route types; not manually edited
- `GComponentsPropsList` in `g-props/g-props.ts` — interface that consuming apps extend via `declare module "@mui/material" { interface Components<Theme> extends GComponents<Theme> {} }` to register all Gamut component prop types in the MUI theme `components` object
- `GComponentNameToClassKey` in `g-props/g-props.ts` — maps each Gamut component name to its class-key union type, enabling typed `styleOverrides` in MUI theme configuration
- `GComponents<Theme>` in `g-props/g-props.ts` — the concrete interface passed to the MUI module augmentation; aggregates `defaultProps`, `styleOverrides`, and `variants` entries for every registered component

No interface merging for custom route context or search params is performed within this module.

## Known Limitations and Gotchas

- **`useRouteToOffer` falls back to `alert()`** — `g-router-offer/useRouteToOffer.ts` line 35 contains `// TODO: polite error msgs` above an `alert('Form not available!')` call. This fires when an offer has no `productId` and no usable `otherLocales` entry, producing a native browser alert with no styling or i18n.
- **`productId` substitution with `'__search_topic'`** — in `useRouteToOffer.ts`, when an assigned offer has no `productId`, the literal string `'__search_topic'` is injected as the `productId` route parameter. This is a sentinel value, not a real product identifier.
- **`routeTree.gen.ts` is auto-generated** — this file must not be edited manually; it is regenerated by the TanStack Router CLI. Any manual change will be overwritten on the next generation run.
- **Auth-type detection is binary** — `AuthSetup` in `routes/__root.tsx` branches only on `iam.authType === 'ANON'`; any non-`ANON` value activates `SecuredSetup` with the full provider stack. There is no intermediate partially-authenticated state.
- **`ChooseComponent` dispatch is stringly typed at the call site** — `secured.$locale.views.$viewId.index.tsx` casts `viewId` as `any` before passing it to `ChooseComponent`, which accepts `GUserOverviewMenuView`. Unrecognized `$viewId` values render `GErrorNotFound` silently without logging.
- **`GComponentsPropsList` contains no `GRouterOfferSummaryRps` entry** — `g-router-offer-summary-rps/` (`FormFillRpsRating`) exists as a sub-module but its props and class-key types are not registered in `GComponentsPropsList` or `GComponentNameToClassKey`, so it cannot be configured via MUI theme `components`.
- **`GInboxMessageNotAllowed` class-key mapped to `GInboxMessagesClassKey`** — in `GComponentNameToClassKey`, the entry `GInboxMessageNotAllowed: GInboxMessagesClassKey` reuses the messages class key rather than a dedicated one. This appears to be an oversight; the prop type correctly uses `GInboxMessageNotAllowedProps`.

## Scope Boundaries

Owns: the TanStack Router route tree, the `router` instance, route-level layout components that compose primitives and API hooks into full-page views, and the MUI theme-integration interfaces (`GComponentsPropsList`, `GComponentNameToClassKey`, `GComponents`, `GComponentsProps`, `GComponentsVariants`, `GComponentsOverrides`).

Does not own: authentication logic, API fetch implementations, form rendering engines, theming tokens, markdown rendering, or i18n message catalogs — those belong to `@dxs-ts/gamut-api`, `@dxs-ts/gamut-form`, `@dxs-ts/gamut-theme`, `@dxs-ts/gamut-md`, and `@dxs-ts/gamut-intl` respectively. Does not expose a standalone application entry point; the consuming application mounts `RouterProvider` and the outer provider stack.

## TanStack Integration

### Packages Used

| Package | Role |
|---|---|
| `@tanstack/react-router` | File-based routing, `router` instance, search param typing, navigation |

`@tanstack/react-query` and `@tanstack/react-store` are used by `@dxs-ts/gamut-api` and `@dxs-ts/gamut-form` respectively. `gamut-routes` does not call `useQuery`, `useMutation`, or `Store` itself.

`@tanstack/react-table` and `@tanstack/react-form` are not used in any Gamut module.

### File-Based Routing

All route files live under `routes/` and use `createFileRoute`. The root route (`routes/__root.tsx`) uses `createRootRoute`. No code-based `createRoute` is used anywhere in this module.

File naming follows TanStack Router's dot-separator convention for nested routes. Example path segments: `public.$locale`, `secured.$locale.views.$viewId`. The TanStack Router CLI reads these files and writes the assembled tree into `routeTree.gen.ts` via `._addFileChildren()` calls. `routeTree.gen.ts` must not be edited manually — any manual change is overwritten on the next generation run.

### `Register` Interface Augmentation

The `Register` augmentation is declared in `router.ts`:

```ts
declare module '@tanstack/react-router' {
  interface Register {
    router: typeof router
  }
}
```

This wires up the `router` instance so that TanStack Router hooks (`useNavigate`, `useSearch`, `Link`, etc.) infer types from the full route tree without requiring explicit generic parameters. `routeTree.gen.ts` separately augments `FileRoutesByPath` inside the same module declaration — that augmentation is auto-generated and maps every route path string to its ID, preloader, and parent route types.

### Search Param Typing

One route uses `validateSearch`: `routes/secured.$locale.views.$viewId.index.tsx`.

```ts
export interface ViewRouteSearchParams {
  topicId?: string | undefined;
}

export const Route = createFileRoute('/secured/$locale/views/$viewId/')({
  component: Component,
  validateSearch: (search: Record<string, unknown>): ViewRouteSearchParams =>
    parseViewRouteSearchParams(search),
})
```

The param is consumed inside the component via `Route.useSearch()`. `zodSearchValidator` is not used. No other route in this module defines `validateSearch`; adding search params to any other route requires adding it explicitly.

### Error and Not-Found Components

`GErrorNotFound` and `GError` (both from `@dxs-ts/gamut-primitives`) are registered on the root route in `routes/__root.tsx`:

```ts
export const Route = createRootRoute({
  component: RouteComponent,
  notFoundComponent: GErrorNotFound,
  errorComponent: GError,
})
```

`GErrorNotFound` handles unmatched routes. `GError` handles thrown errors caught by TanStack Router's error boundary. No child route defines its own `errorComponent` or `notFoundComponent`.

### Navigation Pattern

`useNavigate` is imported directly from `@tanstack/react-router` at each call site. There is no wrapper hook or abstraction. It appears in:

- `routes/__root.tsx` — redirects to `/public/$locale/login` on auth failure via `IamBackendProvider`'s `onAuthorizationFail` callback
- `routes/secured.$locale.forms.$formId.index.tsx` and `routes/secured.$locale.forms.$formId.review.tsx` — cancel navigation
- `g-router-offer/GRouterOffer.tsx` — navigates to summary after form completion, or to home on cancel
- `g-router-secured-services/Nav.tsx` — breadcrumb navigation between secured views using MUI `Link` with `onClick` calling the `nav` function returned by `useNavigate`

`Navigate` (component redirect) is used in `routes/index.tsx` to redirect from `/` to either the public or secured landing based on `authType`. TanStack `Link` is not used; breadcrumbs use MUI `Link` with imperative navigation.

### What This Module Does Not Use

- **No `loader` or `beforeLoad`** — no route file defines either option. All data fetching starts when provider components mount inside route components, not in route lifecycle hooks.
- **No router context** — `createRouter` in `router.ts` has no `context` option set. `QueryClient` is not in the router context and is not accessible from any route lifecycle.
- **No `zodSearchValidator`** — search param parsing in `secured.$locale.views.$viewId.index.tsx` uses a plain function, not a Zod schema validator.
- **No child-route error boundaries** — `errorComponent` and `notFoundComponent` are defined only on the root route.
- **No TanStack `Link`** — all navigable elements use MUI `Link` with imperative `useNavigate` calls.
