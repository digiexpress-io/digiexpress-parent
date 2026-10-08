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
