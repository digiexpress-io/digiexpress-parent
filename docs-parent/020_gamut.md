# Gamut

## Purpose

Customer-facing portal frontend. Displays localised content (Stencil CMS), manages Dialob form submissions, handles IAM-gated and anonymous routes, tracks user tasks (inbox, offers, contracts, bookings), and renders form review and messaging flows. Implemented as a collection of scoped sub-modules re-exported through a single umbrella package `@dxs-ts/gamut` (source: `modules/lib-gamut`).

## Sub-modules

| Package | Source directory | Role |
|---|---|---|
| `@dxs-ts/gamut-cockpit-store` | `modules/gamut-cockpit-store` | `CockpitStore` — localStorage persistence of the active cockpit selection; exposes `window.GamutCockpit` for console use |
| `@dxs-ts/gamut-intl` | `modules/gamut-intl` | Translation message maps for `bs`, `en`, `fi`, `my`, `sv`; exported as `messages` |
| `@dxs-ts/gamut-api` | `modules/gamut-api` | React contexts and fetch factories for all backend APIs: IAM, Stencil site, Dialob, offer, contract, comms/subjects, bookings, search, map, locale, overrides, variants; entry: `ConfigProvider` / `useConfig` |
| `@dxs-ts/gamut-md` | `modules/gamut-md` | `GMarkdown` component — renders markdown strings via `react-markdown` with MUI Typography overrides |
| `@dxs-ts/gamut-shell` | `modules/gamut-shell` | `GShell` — top-level layout container; responsive toolbar height via `useMediaQuery`; `GShellProvider` / `useGShell` drawer state context |
| `@dxs-ts/gamut-theme` | `modules/gamut-theme` | `GThemeProvider` / `useGTheme` — wraps MUI `ThemeProvider`; supports primary and named secondary `ThemeOptions`; reads cockpit from `CockpitStore` to select secondary theme on init; two bundled theme option sets: `g-theme-options` and `g-theme-options-alt-1` |
| `@dxs-ts/gamut-form` | `modules/gamut-form` | Live Dialob form rendering: `GFormBase`, `GFormBaseElement`, `GFormGroup`, `GFormGroupCollapsible`, `GFormGroupDialob`, `GFormNoteValidation`, `GFormErrorVisibility`; input components `GInputAddress`, `GInputMultilist`, `GInputLabel`; uses `@tanstack/react-store` for form state |
| `@dxs-ts/gamut-form-review` | `modules/gamut-form-review` | Read-only Dialob form review: per-type display components (`GFormReviewBoolean`, `GFormReviewChoice`, `GFormReviewDate`, `GFormReviewDecimal`, `GFormReviewText`, `GFormReviewTime`, `GFormReviewMultiChoice`, `GFormReviewNote`, `GFormReviewGroup`, `GFormReviewRowGroup`, `GFormReviewPage`, `GFormReviewQuestionnaire`, `GFormReviewSurvey`, `GFormReviewSurveyGroup`) |
| `@dxs-ts/gamut-primitives` | `modules/gamut-primitives` | Shared UI building blocks: `GShell`-aware components (`GAppBar`, `GLayout`, `GFooter`, `GLogo`), auth screens (`GAuth`, `GAuthUn`, `GAuthRepPerson`, `GAuthRepCompany` and unauthenticated variants), inbox (`GInbox`, `GInboxMessages`, `GInboxAttachments`, `GInboxFormReview`), offers (`GOffers`), contracts (`GContracts`), bookings (`GBookings`), article/content (`GArticle`, `GMarkdown`-based), feedback (`GArticleFeedback`, `GArticleFeedbackViewer`), navigation helpers (`GLinks`, `GLinksPage`, `GLocales`, `GLogin`, `GLogout`), utility (`GLoader`, `GConfirm`, `GTooltip`, `GSort`, `GPopoverButton`, `GPopoverSearch`, `GPopoverTopics`) |
| `@dxs-ts/gamut-routes` | `modules/gamut-routes` | TanStack Router route tree and page-level route components; `router` instance; `GComponentsPropsList` / `GComponents` / `GComponentsOverrides` type declarations for MUI theme integration |

## Dependencies

### dependencies (`@dxs-ts/gamut`, i.e. `lib-gamut`)

- `@dxs-ts/envir-util`
- `@dxs-ts/gamut-cockpit-store`
- `@dxs-ts/gamut-api`
- `@dxs-ts/gamut-md`
- `@dxs-ts/gamut-primitives`
- `@dxs-ts/gamut-form-review`
- `@dxs-ts/gamut-form`
- `@dxs-ts/gamut-shell`
- `@dxs-ts/gamut-intl`
- `@dxs-ts/gamut-routes`
- `@dxs-ts/gamut-theme`
- `@dxs-ts/xui-datetime`

### peerDependencies

None declared at the umbrella level. Each sub-module declares its own peers:

- `react`, `react-dom`, `react-intl`, `react-markdown`
- `@mui/material`, `@mui/system`, `@mui/icons-material`, `@mui/utils`, `@mui/types`
- `@tanstack/react-router`, `@tanstack/react-query`, `@tanstack/react-store`
- `luxon`, `numbro`, `leaflet`, `react-leaflet`, `leaflet-geosearch`, `ts-md5`

## Module Relationships

### Dependency layers

**Leaf nodes** — no `@dxs-ts/*` dependencies:

| Module | What it provides |
|---|---|
| `@dxs-ts/gamut-cockpit-store` | `CockpitStore` (localStorage read/write/clear), `Cockpit` interface, `window.GamutCockpit`; no React or MUI dependency |
| `@dxs-ts/gamut-intl` | `messages` record keyed by locale code (`bs`, `en`, `fi`, `my`, `sv`); consumed by the host app's `IntlProvider` |
| `@dxs-ts/envir-util` | `VersionInfoApi`, `VersionInfoBuilder`; consumed by the umbrella `@dxs-ts/gamut` only |
| `@dxs-ts/xui-datetime` | `DatePicker`, `YearPicker`, `DateTimeFormatter`, `DateInputStateMachine`; consumed by `gamut-form` |

**Intermediate consumers:**

| Module | Depends on | What it uses from each dependency |
|---|---|---|
| `@dxs-ts/gamut-api` | `gamut-cockpit-store`, `gamut-intl` | `CockpitStore.get()` — injected into TanStack Query cache keys (`api-config/config-types.ts`, `api-offer/usePopulateContext.ts`) so queries re-fetch on cockpit change; `messages` — not used directly, but locale context in `api-locale` wraps the same locale codes |
| `@dxs-ts/gamut-theme` | `gamut-cockpit-store` | `CockpitStore.get()` at `GThemeProvider` init to select the initial entry from `secondaryThemeOptions` |
| `@dxs-ts/gamut-shell` | `gamut-api` | `useConfig` for layout configuration; exports `GShellClassName` constant consumed by `gamut-primitives` (`GAppBar`) and `gamut-routes` (`router.ts` scroll restoration) |
| `@dxs-ts/gamut-md` | `gamut-api` | `useConfig` / `GOverridableComponent` for theming; renders markdown via `react-markdown` with MUI `Typography` overrides |
| `@dxs-ts/gamut-form-review` | `gamut-api`, `gamut-md` | `useDialobReview()` (from `api-dialob`) to fetch completed session data; `GMarkdown` for rendering markdown answer text; `GFormReviewContextType` / `GFormReviewContext` distribute data to per-type leaf components (`GFormReviewText`, `GFormReviewChoice`, `GFormReviewBoolean`, etc.) without props |
| `@dxs-ts/gamut-form` | `gamut-api`, `gamut-md`, `xui-datetime` | `FormProvider` and `ActionsQueue` from `api-dialob`; `useForm(id)` reads `ActionItem` from `FormStore` (`@tanstack/react-store`); `useSlotVariant` resolves input component by `ActionItem.type`/`view`/`props`; `DatePicker`/`YearPicker` from `xui-datetime` for date/time input types |
| `@dxs-ts/gamut-primitives` | `gamut-api`, `gamut-md`, `gamut-shell`, `gamut-form-review` | Domain hooks (`useIam`, `useSite`, `useOffers`, `useContracts`, `useComms`, `useBookings`, `useSearch`) for feature data; `GMarkdown` in `GArticle`; `GShellClassName` in `GAppBar`; `GFormReview` in `GInboxFormReview` (the only point where primitives mounts a `gamut-form-review` component) |

**Top-level consumers:**

| Module | Depends on |
|---|---|
| `@dxs-ts/gamut-routes` | `gamut-cockpit-store` (`CockpitStore.save()` in `GRouterCockpits`), `gamut-api` (all domain providers composed in `SecuredSetup`/`PublicSetup` in `__root.tsx`), `gamut-shell` (`GShell`, `GShellClassName` in `router.ts`), `gamut-theme` (`GThemeProvider`), `gamut-md` (`GMarkdown`), `gamut-primitives` (page-level feature components), `gamut-form-review` (`GFormReview`), `gamut-form` (`GForm`, `GFormIterator`) |
| `@dxs-ts/gamut` (umbrella) | all of the above + `envir-util` |

### Dependency graph (acyclic)

```
envir-util ─────────────────────────────────────────────────► gamut (umbrella)
gamut-cockpit-store ──► gamut-api ──► gamut-shell ──────────► gamut-routes
                    └──► gamut-theme                       ┌──► gamut-routes
gamut-intl ──────────► gamut-api ──► gamut-md ─────────────┤
                                  └► gamut-form-review ─────┤
xui-datetime ─────────────────────────► gamut-form ─────────┤
                                  gamut-primitives ──────────┘
```

### Key cross-module contracts

- `GShellClassName` (string constant, `gamut-shell`) — applied by `GAppBar` (gamut-primitives) to the `Toolbar` for scoped layout CSS; passed by `router.ts` (gamut-routes) to `scrollToTopSelectors` in `createRouter`.
- `GFormReviewContextType` / `GFormReviewContext` (gamut-form-review) — sole data distribution mechanism in the review tree; components call `React.useContext(GFormReviewContext)` rather than receiving props.
- `ActionsQueue` (gamut-api `api-dialob`) — debounced action flush to `/portal/secured/actions`; closed over by every `*Dialob` input component in `gamut-form`.
- `GComponentsPropsList` / `GComponentNameToClassKey` (gamut-routes `g-props.ts`) — extend MUI's `Components` interface so all Gamut components can be configured via `theme.components.GXxx` in a single `ThemeOptions` object.
- `CockpitStore.get()` (gamut-cockpit-store) — read at two independent points: `GThemeProvider` init (theme selection) and `ConfigApi.mergeOptions` / `usePopulateContext` (TanStack Query cache key scoping).

## Frontend Architecture

**Stack:** React, TypeScript, MUI v5, TanStack Router, TanStack Query, react-intl, react-markdown, Leaflet

**Sub-module structure:**

- `gamut-cockpit-store` — plain class, no React; exposes `window.GamutCockpit` console API
- `gamut-intl` — static locale message maps; five locales (bs, en, fi, my, sv)
- `gamut-api` — per-domain context providers and fetch factory functions; `ConfigApi` namespace in `config-types.ts` merges host overrides into defaults at runtime
- `gamut-md` — single component `GMarkdown`; supports `div` and `span` root modes; maps h1–h6, p, li, a to MUI `Typography`
- `gamut-shell` — `GShell` / `GShellInternal`; breakpoint-aware toolbar height; `GShellProvider` exposes drawer open/close state
- `gamut-theme` — `GThemeProvider` wraps MUI `ThemeProvider`; `setThemeOptions(name)` switches between named secondary theme options; cockpit name used as key into `secondaryThemeOptions`
- `gamut-form` — `GFormBase` is the top-level Dialob form host; delegates element rendering to `GFormBaseElement` via `useSlotVariant`; `GFormGroup` / `GFormGroupCollapsible` handle question grouping; `GFormErrorVisibility` scrolls to first error
- `gamut-form-review` — stateless review components keyed by Dialob answer type; `GFormReviewContext` and `GFormReviewGroupContext` pass traversal state
- `gamut-primitives` — ~45 named components; each sub-folder contains component file, `index.ts`, and `useUtilityClasses.tsx`; covers auth UI, content display, inbox, offers, contracts, bookings, navigation, and layout utilities
- `gamut-routes` — TanStack Router file-based route tree under `routes/`; two route subtrees `public.$locale.*` and `secured.$locale.*`; root route (`__root.tsx`) bootstraps `IamBackendProvider` then branches to `PublicSetup` or `SecuredSetup` based on `iam.authType`; `GComponentsPropsList`, `GComponentNameToClassKey`, and `GComponents` interfaces in `g-props.ts` extend MUI theme typing for all Gamut components

**Patterns:**

- MUI theme integration: all Gamut components are registered in `GComponentsPropsList` and `GComponentNameToClassKey`; consumers extend the MUI `Theme` interface to enable `theme.components.GXxx` overrides
- `GOverridableComponent`: each styled component accepts an optional `component` prop to swap the root element, following the MUI `OverridableComponent` convention
- `useUtilityClasses` per component: generates BEM-style class names via MUI `generateUtilityClass` / `composeClasses`; allows `styleOverrides` in theme
- `useThemeProps`: each component calls `useThemeProps({ name: MUI_NAME })` to merge theme `defaultProps`
- Context provider nesting in `__root.tsx`: all backend data contexts are composed in `SecuredSetup` and `PublicSetup`; auth mode determines which provider tree is active
- `CockpitStore` as optional multi-tenant switch: presence of a stored cockpit activates an alternative `ThemeOptions` and a cockpit dropdown in `GRouterCockpits`

## Error Handling

**Route-level errors** — `__root.tsx` passes `GError` (from `gamut-primitives`) as the `errorComponent` prop to `createRootRoute`, and `GErrorNotFound` as `notFoundComponent`. These components render a full-page error or 404 screen with a "Go back to home" button.

**API/fetch failures** — TanStack Query is used throughout `gamut-api`. Providers such as `SiteBackendProvider` log API failures to `console.error` (e.g., "site not available", "feedback not available") but do not throw; downstream consumers receive `undefined` data. Loading state is tracked via `useQuery.isPending` (e.g., `siteQuery.isPending` in `SiteBackendProvider`).

**Dialob action queue errors** — `impl-actions-queue.ts` defines two error classes: `ActionsQueueError` and `ActionsQueueRequestError`. `ActionsQueueRequestError` is thrown on non-2xx HTTP responses during action sync. Errors are broadcast to registered listeners via `ActionsQueueErrorEvent` (type discriminants: `'SYNC'` and `'SYNC-REPEATED'`). Network errors are suppressed if `error.name === 'NetworkError'`.

**Form validation errors** — Dialob server-side validation messages are rendered in-form by `GFormNoteValidation` (styled MUI `Alert`). `GFormNoteValidationDialob` is the slot variant wired in by `useSlotVariant` when `element.type === 'note' && element.view === 'validation'`. `GFormErrorVisibility` scrolls the viewport to the first validation error on submit.

**Loading indicator** — `GLoader` (from `gamut-primitives`) renders a `CircularProgress` spinner with a localised "loading" message. It is the standard loading placeholder used by feature components while data is pending.

**Session expiry** — The `handleExpire` callback in `ConfigApi.Options` is invoked by `IamBackendProvider` on liveness failure. The default implementation calls `alert('sessionExpired')`; consuming apps are expected to override it.

**Navigation errors** — `useRouteToOffer` (`gamut-routes`) falls back to `alert('Form not available!')` when an offer route cannot be resolved (marked `TODO: polite error msgs`).

## Bootstrapping

A consuming app must compose the following providers in this order before rendering `RouterProvider`:

1. **`QueryClientProvider`** (`@tanstack/react-query`) — TanStack Query client; all `gamut-api` providers depend on it.
2. **`LocaleProvider`** (`gamut-api`) — wraps `react-intl` `IntlProvider`; must wrap everything that calls `useIntl` or `useLocale`.
3. **`GThemeProvider`** (`gamut-theme`) — wraps MUI `ThemeProvider`; reads `CockpitStore` at init to select secondary theme. Must be inside `LocaleProvider`.
4. **`ConfigProvider`** (`gamut-api`) — accepts a `ConfigApi.Options` object (all fields optional; defaults are no-op fetch factories pointing at relative `/portal/*` paths). Must override `handleExpire`, `iamLiveness`, and `staleTime` for production.
5. **`RouterProvider`** (`@tanstack/react-router`) — mounts the pre-built `router` instance exported from `gamut-routes`. The `InnerWrap` prop is used to inject the provider tree from steps 1–4 inside the router boundary.

The router must be created with `routeTree` from `gamut-routes`, `scrollRestoration: true`, and `scrollToTopSelectors: ['.${GShellClassName}']`. `IamBackendProvider`, domain providers (`SiteBackendProvider`, `DialobProvider`, `OfferProvider`, `ContractProvider`, `CommsProvider`, `BookingProvider`), and `GRouterCockpits` are composed internally by `__root.tsx` — the consuming app does not mount them directly.

The consuming app must also augment MUI's `Components` interface:

```ts
declare module "@mui/material" {
  export interface Components<Theme = unknown> extends GComponents<Theme> { }
}
```

`gamut-intl` exports a `messages` record keyed by locale code (`bs`, `en`, `fi`, `my`, `sv`). The consuming app passes the appropriate locale's message map to its `IntlProvider` (or `LocaleProvider`).

## TypeScript Extension Points

| Name | Location | Description |
|---|---|---|
| `GComponentsPropsList` | `gamut-routes/g-props.ts` | Interface listing every Gamut component's props type by component name; consumers extend MUI's `Components` interface via `GComponents<Theme>` to enable `theme.components.GXxx` overrides |
| `GComponentNameToClassKey` | `gamut-routes/g-props.ts` | Maps each Gamut component name to its class-key union; used by `GComponentsOverrides` for typed `styleOverrides` in a `ThemeOptions` object |
| `GComponents<Theme>` | `gamut-routes/g-props.ts` | Convenience type merging `GComponentsPropsList`, `GComponentNameToClassKey`, and variant support; consumed via `declare module "@mui/material" { interface Components<Theme> extends GComponents<Theme> {} }` in the host app |
| `Register` (router) | `gamut-routes/router.ts` | `declare module '@tanstack/react-router' { interface Register { router: typeof router } }` — registers the Gamut router instance for TanStack Router global type inference |
| `FileRoutesByPath` | `gamut-routes/routeTree.gen.ts` | Auto-generated `declare module '@tanstack/react-router'` augmentation produced by TanStack Router's route-tree generator; not manually edited |

No `declare module` augmentations exist within `gamut-api`, `gamut-form`, `gamut-form-review`, `gamut-md`, `gamut-shell`, `gamut-theme`, `gamut-cockpit-store`, or `gamut-intl`.

## Known Limitations and Gotchas

**Map address format is not customisable** — `api-map/_formatRaw.ts` formats geocoded addresses in European postal-code-after-street order. A `TODO` comment on line 27 marks the format as hardcoded; there is no configuration point to change the order or separator.

**Offer navigation errors use `alert()`** — `g-router-offer/useRouteToOffer.ts` falls back to `alert('Form not available!')` when the offer route cannot be resolved. The `TODO: polite error msgs` comment on line 35 confirms this is a known gap; no structured error UI is rendered.

**`GFormReviewContext` child items are not passed as props** — `GFormReviewContext.tsx` line 110 contains `// TODO: Give child items as props`. When a review item has `childrenRequired` set, children are created via recursive `createItem()` calls but not passed as explicit props to the component; the component must re-read them from context.

**`GLinkFormUnlocked` may be dead code** — Both the `useUtilityClasses` function and the `GLinkFormUnlocked` component in `g-link/GLinkFormUnlocked.tsx` carry `// TODO Perhaps this can be deleted, but let's wait and see`. The component is still exported but its necessity is unconfirmed.

**`GLayout` `fill-session-start-end` variant may be obsolete** — `GLayout.tsx` line 87 marks the `variant === 'fill-session-start-end'` branch with `//TODO is this variant needed anymore?`. The variant is still handled but its active usage is uncertain.

**`.GInboxFormReview-reviewTitle` style is incomplete** — `g-inbox-form-review/useUtilityClasses.tsx` defines `.GInboxFormReview-reviewTitle` twice: first as an empty block (marked `//TODO`), then overridden with `...theme.typography.h1`. The empty first declaration is a stub left from incomplete styling work.

**Dialob component props are strings, not booleans** — As documented in `gamut-form/DIALOB_FILL_PROPS.md`, boolean Dialob Composer props (`border`, `collapsible`) must be passed as the string `'true'`, not as native TypeScript booleans. Passing `true` directly will not activate the feature.

**`currency` prop with empty value falls back silently** — If the `currency` key is present on a Dialob text item but its value is an empty string, `GInputText` renders as a plain text input with no adornment and no currency formatting. This is documented in `DIALOB_FILL_PROPS.md` but contradicts the expectation that presence of the key activates currency mode.

**Session expiry default is a browser alert** — `ConfigApi.default_config.handleExpire` is `() => alert('sessionExpired')`. Any app that does not override `handleExpire` in `ConfigProvider` options will display a raw browser alert on session expiry.

## Scope Boundaries

Gamut is a customer portal frontend. It does not implement any backend services, authentication logic, workflow engines, or data persistence beyond `CockpitStore`'s single `localStorage` key. It does not provide a CMS authoring interface (Stencil content is consumed read-only), a form builder (Dialob forms are consumed as rendered fill-in experiences only), or an administrative dashboard. It does not expose a public REST API, WebSocket layer, or server-side rendering entry point. Theming is limited to MUI v5 `ThemeOptions`; it does not support CSS-in-JS approaches outside of MUI's `styled`/`sx` system (and `sx` props are explicitly excluded from component API by project convention). Multi-language support covers exactly five locales (`bs`, `en`, `fi`, `my`, `sv`) defined in `gamut-intl`; adding a new locale requires changes to that sub-module.

## TanStack Integration

`@dxs-ts/gamut` re-exports components from sub-modules that use TanStack Router, TanStack Query, and TanStack Store. Consuming apps take on all three packages as peer dependencies.

### Packages in use

| Package | Layer | Role |
|---|---|---|
| `@tanstack/react-router` | `gamut-routes` | File-based route tree, navigation, search param typing |
| `@tanstack/react-query` | `gamut-api` | Server state fetching inside all domain provider hooks |
| `@tanstack/react-store` | `gamut-form` | In-memory reactive `Store` instance for scroll-to-error ref tracking in `useScrollTo` |

`@tanstack/react-table` and `@tanstack/react-form` are not used.

### Required provider nesting order

1. **`QueryClientProvider`** (`@tanstack/react-query`) — outermost; all `gamut-api` providers call `useQuery` and must be descendants.
2. **`LocaleProvider`** (`gamut-api`) — wraps `react-intl` `IntlProvider`; must enclose everything that calls `useIntl` or `useLocale`.
3. **`GThemeProvider`** (`gamut-theme`) — wraps MUI `ThemeProvider`; must be inside `LocaleProvider`.
4. **`ConfigProvider`** (`gamut-api`) — supplies fetch factories and per-query `staleTime` to all domain providers.
5. **`RouterProvider`** (`@tanstack/react-router`) — innermost; mounts the pre-built `router` instance from `gamut-routes`.

`QueryClientProvider` must be outermost because `RouterProvider` and all route-mounted provider components (`IamBackendProvider`, `SiteBackendProvider`, `OfferProvider`, etc.) are descendants and all call `useQuery` internally.

### Required augmentations in the consuming app

Two `declare module` augmentations are required. Omitting either disables the corresponding layer of type inference.

**TanStack Router — `Register`:**

```ts
declare module '@tanstack/react-router' {
  interface Register {
    router: typeof router
  }
}
```

This enables global type inference for `useNavigate`, `useSearch`, `Link`, and all router hooks. The declaration also exists in `gamut-routes/router.ts` for library-level type checking; the app-level declaration is the one that takes effect at runtime.

**MUI theme — `GComponentsPropsList`:**

```ts
declare module "@mui/material" {
  export interface Components<Theme = unknown> extends GComponents<Theme> { }
}
```

This registers all Gamut components with MUI's `Components` interface so that `theme.components.GXxx` overrides are type-checked. `GComponents<Theme>` is exported from `gamut-routes/g-props.ts` and merges `GComponentsPropsList`, `GComponentNameToClassKey`, and variant support.
