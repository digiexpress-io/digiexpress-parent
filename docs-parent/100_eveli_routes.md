_last updated: 14.10.2026_

# `@dxs-ts/eveli-routes`

## Purpose

Full-page route tree and layout shell for the Eveli worker application. Owns the TanStack Router route hierarchy, the `EveliApp` layout container, per-route page components, and the backend-provider wrappers (`AnyTaskRoute`, `AnyCockpitRoute`, `AnyContractRoute`, `AnyLedgerRoute`) that connect each domain composer to Eveli's REST API via `@dxs-ts/envir-fetch`.

## Quick Navigation

| Concept | File | Key Exports |
|---------|------|-------------|
| Router instance | `router.ts` | `router`, `routeTree`, `FileRouteTypes`, `RootRouteChildren` |
| Root route | `routes/__root.tsx` | — (file-based route) |
| Auth guard | `routes/secured.$locale.tsx` | — (file-based route) |
| Worker shell | `routes/secured.$locale.worker.tsx` | — (file-based route); filesystem bypass logic lives here |
| Default redirect | `routes/secured.$locale.index.tsx` | — (file-based route) |
| Login page | `routes/public.$locale.auth.tsx` | — (file-based route) |
| Dashboard | `routes/secured.$locale.worker.dashboard.index.tsx` | — (file-based route) |
| Task list/detail | `routes/secured.$locale.worker.tasks.tsx` + `tasks.$taskId.index.tsx` | — (file-based routes) |
| Filesystem route | `routes/secured.$locale.worker.filesystem.index.tsx` | — (file-based route) |
| Publications route | `routes/secured.$locale.publications.index.tsx` | — (file-based route) |
| App shell component | `eveli-app/EveliApp.tsx` | `EveliApp`, `ContainerProps` |
| Toolbar + Secondary | `eveli-setup/index.tsx` | `EveliSetup` (`EveliSetup.Toolbar`, `EveliSetup.Secondary`) |
| Task backend wiring | `eveli-any-task-route/AnyTaskRoute.tsx` | `AnyTaskRoute` |
| Cockpit backend wiring | `eveli-any-cockpit-route/AnyCockpitRoute.tsx` | `AnyCockpitRoute` |
| Contract backend wiring | `eveli-any-contract-route/AnyContractRoute.tsx` | `AnyContractRoute` |
| Ledger backend wiring | `eveli-any-ledger-route/AnyLedgerRoute.tsx` | `AnyLedgerRoute` |
| Dashboard data builder | `eveli-task-stats/WithDashboardData.ts` | `WithDashboardData`, `withDs`, `DashboardEvent`, `DashboardGroupEvent` |
| Dashboard component | `eveli-task-stats/EveliTaskStats.tsx` | `EveliTaskStats` |
| Queue list | `eveli-task-queue/find-all-queues/FindAllQueues.tsx` | `FindAllQueues`, `FindAllQueuesProps` |
| Message list | `eveli-task-queue/find-all-messages/FindAllMessages.tsx` | `FindAllMessages` |
| Delivery list | `eveli-task-queue/find-all-deliveries/FindAllDeliveries.tsx` | `FindAllDeliveries` |
| Publications table | `eveli-publications/EveliPublicationsTable.tsx` | `PublicationsTable` |
| User profile form | `eveli-user-profile/UserProfile.tsx` | `UserProfile` |
| Process execution table | `eveli-proc-execution/EveliProcExecution.tsx` | `EveliProcExecution` |
| Task activity table | `eveli-task-activity/EveliTaskActivity.tsx` | `EveliTaskActivity` |
| User activity table | `eveli-user-activity/EveliUserActivity.tsx` | `EveliUserActivity` |
| In-house form list | `eveli-in-house/EveliInHouse.tsx` | `EveliInHouse` |
| In-house form fill | `eveli-in-house/InHouseFill.tsx` | `InHouseFill` |
| Dialob review (session) | `dialob-review/DialobReview.tsx` | `DialobReview` |
| Dialob review (form-based) | `dialob-review/DialobReviewBasedOnForm.tsx` | `DialobReviewBasedOnForm` |
| Dialob review types | `dialob-review/dialob-review-types.ts` | `DialobReviewProps` |
| Customer ratings | `eveli-customer-ratings/CustomerRatings.tsx` | `CustomerRatings` |

## Dependencies

```
@dxs-ts/gamut-api
@dxs-ts/gamut-form
@dxs-ts/gamut-shell
@dxs-ts/gamut-theme
@dxs-ts/gamut-form-review
@dxs-ts/stencil-api
@dxs-ts/stencil-routes
@dxs-ts/wrench-api
@dxs-ts/wrench-routes
@dxs-ts/tagomi-api
@dxs-ts/tagomi-routes
@dxs-ts/envir-fetch
@dxs-ts/task-api
@dxs-ts/eveli-api
@dxs-ts/eveli-primitives
@dxs-ts/fs-api
@dxs-ts/fs-composer
@dxs-ts/user-profile
@dxs-ts/task-feedback
@dxs-ts/task-composer-v2
@dxs-ts/contract-api
@dxs-ts/contract-composer
@dxs-ts/cockpit-api
@dxs-ts/cockpit-composer
@dxs-ts/ledger-api
@dxs-ts/ledger-composer
@dxs-ts/xui-table
@dxs-ts/xui-datetime
@dxs-ts/fs-nav
```

**peerDependencies:**

```
react
react-intl
@mui/material
@mui/icons-material
@mui/utils
@mui/system
@tanstack/react-router
@tanstack/react-table
@tanstack/react-query
@monaco-editor/react
yaml
luxon
notistack
recharts
```

## Module Relationships

- `@dxs-ts/eveli-api` — `useIam`, `useLocale`, `useConfig`, `IconbarProvider`, `EveliTenantFeatureEnabled`, `EveliPermissions`, `useFeedbackBackend`, `useTenantConfigFeatures`, `useTenantConfig`, `useQueue`, `PublicationApi`, `ProcExecutionApi`, `EveliHealthTaskActivity`, `EveliHealthUserActivity`, `mapIamRole`
- `@dxs-ts/eveli-primitives` — `EveliApp` shell CSS class constants, `EveliShell`, `useEveliShell`, `EveliLogin`, `EveliShellExplorer`, `EveliLogo`, `EveliError`, `EveliErrorNotFound`, `EveliSpinner`, `EveliLocales`, `EveliPermissions`, `useEveliPermissions`, `CancelButton`, `EveliBatchView`
- `@dxs-ts/envir-fetch` — `useFetch` for all REST API calls throughout route components; keys follow the pattern `worker/rest/api/<resource>.<METHOD>`
- `@dxs-ts/task-api` — `TaskApi`, `TaskBackendProvider`, `TaskBackendProviderProps` types wired in `AnyTaskRoute`
- `@dxs-ts/task-composer-v2` — `TaskCreate` dialog opened from `AnyTaskRoute`
- `@dxs-ts/task-feedback` — `FeedbackProvider`, `FeedbackAllTasks` used in feedback routes
- `@dxs-ts/cockpit-api` — `CockpitsBackendProvider`, `CockpitsBackendProviderProps` wired in `AnyCockpitRoute`
- `@dxs-ts/contract-api` — `ContractBackendProvider`, `ContractBackendProviderProps` wired in `AnyContractRoute`
- `@dxs-ts/ledger-api` — `LedgerBackendProvider`, `LedgerBackendProviderProps` wired in `AnyLedgerRoute`
- `@dxs-ts/gamut-api` — `DialobProvider`, `WithFormProvider`, `LocaleProvider` used inside `DialobReview`, `DialobReviewBasedOnForm`, and `InHouseFill`
- `@dxs-ts/gamut-form` — `GFormTip` rendered inside dialob review and in-house fill flows
- `@dxs-ts/gamut-theme` — `GThemeOptions` applied as the MUI theme inside dialob review and in-house fill
- `@dxs-ts/gamut-form-review` — `GFormReview` used in `DialobReview` (read-only review mode)
- `@dxs-ts/stencil-api` — `StencilApi`, `StencilComposerApi` service wired in the stencil asset route
- `@dxs-ts/stencil-routes` — `StencilSetup`, `StencilStickySave`, `parseStencilSearchParams` consumed in the stencil and migrate routes
- `@dxs-ts/wrench-api` — `HdesApi`, `WrenchComposerApi` service wired in the wrench asset route
- `@dxs-ts/wrench-routes` — `WrenchSetup`, `WrenchStickySave`, `parseWrenchSearchParams` consumed in the wrench asset route
- `@dxs-ts/fs-api` — `FsDirentProvider`, `FsDirentProviderProps` wired in the filesystem route
- `@dxs-ts/fs-composer` — `FsSetup.Main`, `FsSetup.Secondary` rendered inside `EveliApp` on the filesystem route
- `@dxs-ts/fs-nav` — `FsNavProvider` used as the `wrapper` prop in `EveliApp` on the filesystem route; `parseFsSearchParams`, `FsRouteSearchParams`
- `@dxs-ts/user-profile` — `UserProfileApi` types used in `UserProfile` form
- `@dxs-ts/xui-table` — `WithTableStyles`, `anyDateFilter`, `TableDateFilter` used in table-based page components
- `@dxs-ts/xui-datetime` — `DatePicker`, `DateTimeFormatter` injected as slot components into `TaskBackendProvider`, `ContractBackendProvider`, and `LedgerBackendProvider`

## Frontend Architecture

**Stack:** React 18, TanStack Router (file-based), TanStack Query, MUI v5, react-intl, luxon, recharts, `@monaco-editor/react`, yaml

**Sub-module structure:**

- `routes/` — TanStack Router file-based route tree. Route filenames map directly to URL segments. The tree covers:
  - `__root.tsx` — root route; sets `document.title`; uses `EveliError` and `EveliErrorNotFound` as error/notFound components
  - `public.$locale.auth.tsx` — unauthenticated login dialog; renders `EveliLogin`
  - `secured.$locale.tsx` — auth guard; redirects `ANON` users to the public auth route
  - `secured.$locale.index.tsx` — default redirect; sends `wrench-only` tenants to `/assets/wrench`, all others to `/worker/tasks`
  - `secured.$locale.worker.tsx` — the primary worker shell; renders `EveliApp` with `Secondary` and `Toolbar`, or `contentOnly` mode; **bypasses `EveliApp` entirely when the path contains `/filesystem`** to avoid nested shell layouts
  - `secured.$locale.worker.dashboard.index.tsx` — renders `EveliTaskStats`
  - `secured.$locale.worker.tasks.tsx` — wraps all task sub-routes in `AnyTaskRoute`
  - `secured.$locale.worker.batches.*` — batch list and detail routes; delegates to `EveliBatchView` from `@dxs-ts/eveli-primitives`
  - `secured.$locale.worker.queues.*` — queue, message, and delivery list routes
  - `secured.$locale.worker.monitoring.index.tsx` — renders `EveliProcExecution`
  - `secured.$locale.worker.feedback.*` — feedback list and detail routes; wraps in `FeedbackProvider`
  - `secured.$locale.worker.cockpits.*` — cockpit list and detail routes; wraps in `AnyCockpitRoute`
  - `secured.$locale.worker.contracts.*` — contract list and detail routes; wraps in `AnyContractRoute`
  - `secured.$locale.worker.ledgers.*` — ledger list and detail routes; wraps in `AnyLedgerRoute`
  - `secured.$locale.worker.in-house.*` — in-house form launch and fill flow
  - `secured.$locale.worker.filesystem.index.tsx` — full file-system editor; wires `FsDirentProvider` + `FsNavProvider` + `EveliApp`
  - `secured.$locale.worker.user-activity.index.tsx` — renders `EveliUserActivity`
  - `secured.$locale.worker.task-activity.index.tsx` — renders `EveliTaskActivity`
  - `secured.$locale.worker.profile.tsx` — renders `UserProfile`
  - `secured.$locale.assets.wrench.index.tsx` — builds `HdesApi.ServiceImpl`, wraps in `WrenchComposerApi.Provider`, renders `EveliApp`
  - `secured.$locale.assets.stencil.index.tsx` — builds `StencilApi.service`, wraps in `StencilComposerApi.Provider`, renders `EveliApp` with merged `EveliSetup.Toolbar` + `StencilSetup.Toolbar`
  - `secured.$locale.assets.tagomi.index.tsx` — tagomi asset route
  - `secured.$locale.assets.forms.*` — Dialob form management routes
  - `secured.$locale.assets.migrate.index.tsx` — upload-only migration dialog; POSTs a JSON deployment file
  - `secured.$locale.publications.index.tsx` — renders `EveliPublicationsTable`

- `eveli-app/` — `EveliApp` component and `ContainerProps`. Renders `IconbarProvider` > `EveliShell` > optional permanent `Drawer` (via `ToggleDrawer`) > optional `AppBar` (tabs) > `main`. The `wrapper` prop allows injecting a context provider around the entire shell content. `contentOnly` mode suppresses the drawer and app bar entirely.

- `eveli-setup/` — `Toolbar` and `Secondary` components. `Toolbar` renders the vertical icon bar with navigation buttons, feature flags (`EveliTenantFeatureEnabled`), permission guards (`EveliPermissions`), a locale switcher (`EveliLocales`), and a build-info dialog (fetches `worker/rest/api/version.GET`). `Secondary` is a placeholder for secondary sidebar content. `Impersonation` renders the current user's name/email/role in a popover.

- `eveli-task-stats/` — `EveliTaskStats` dashboard. `WithDashboardData` is a builder class that wraps a `TaskApi.TaskDasboard` and exposes `.filter()`, `.intl()`, `.fill()`, `.groupByDate()`, and `.map()` for transforming raw events into recharts-ready data. Exported as `withDs(init)` factory.

- `eveli-task-queue/` — Three sub-modules: `find-all-queues` (`FindAllQueues` — lists queues and consumers from `useQueue`), `find-all-messages` (`FindAllMessages`), `find-all-deliveries` (`FindAllDeliveries`).

- `eveli-publications/` — `PublicationsTable` (table of asset publications with status change dialog, new-publication dialog, and upload dialog). `NewPublicationDialog` and `UploadPublicationDialog` are local dialog components. Status values: `DEPLOYED`, `ERROR`, `READY`, or pending (color-coded via `CircleIcon`).

- `eveli-user-profile/` — `UserProfile` full-page form with editable first/last name and tenant config selection. Uses TanStack Query directly (`useQuery`) to fetch `currentUserProfile`. `UserAvatar`, `UserProfileDetailRow`, `UserProfileFirstName`, `UserProfileLastName`, `TenantConfigSelect`, and `UserProfileNotifications` (commented-out TODO) are child components.

- `eveli-proc-execution/` — `EveliProcExecution` table showing process executions from the last 6 months. Status values are translated from raw strings (`ANSWERED`, `CREATED`, `ANSWERING`, `IN_PROGRESS`, `WAITING`, `COMPLETED`, `REJECTED`).

- `eveli-task-activity/` — `EveliTaskActivity` table showing task-level health/audit data fetched from `worker/rest/api/health.GET`. Body values open in a full-screen Monaco editor displaying YAML.

- `eveli-user-activity/` — `EveliUserActivity` table showing user-level health/audit data from the same health endpoint. Workers are identified by `usedFor === 'WORKER'`.

- `eveli-in-house/` — `EveliInHouse` list, `InHouseTable`, `InHouseFillStart`, `InHouseFill`, and `InHouseFillEnd`. `InHouseFill` embeds a full Gamut form stack (`DialobProvider` > `LocaleProvider` > `WithFormProvider` > `GFormTip`) inside a standalone `QueryClientProvider` and `ThemeProvider` using `GThemeOptions`.

- `dialob-review/` — Two review components. `DialobReview` renders a `GFormReview` (read-only review from a stored session). `DialobReviewBasedOnForm` renders an interactive `GFormTip` in read-only mode (`disabled` prop) using `WithFormProvider`, for cases where the questionnaire ID is used as the form ID. Both wrap their own `QueryClientProvider` and `ThemeProvider`.

- `eveli-any-task-route/` — `AnyTaskRoute` context provider that wires all task REST endpoints into `TaskBackendProvider`. Also mounts `FeedbackProvider` and the `TaskCreate` dialog. Injects `XuiDatePicker` and `XuiDateTimeFormatter` as slot components, and `DialobReviewBasedOnForm` as the `DialobReview` slot (the direct `DialobReview` slot is commented out).

- `eveli-any-cockpit-route/` — `AnyCockpitRoute` wraps children in `CockpitsBackendProvider` with navigation and persistence built from `useFetch` calls.

- `eveli-any-contract-route/` — `AnyContractRoute` wraps children in `ContractBackendProvider`. `permissions` currently returns an empty array.

- `eveli-any-ledger-route/` — `AnyLedgerRoute` wraps children in `LedgerBackendProvider`. `permissions` currently returns an empty array.

- `router.ts` — Creates the TanStack Router instance from the generated `routeTree.gen`. Registers the router type globally via `declare module '@tanstack/react-router' { interface Register { router: typeof router } }`.

**Patterns:**

- **Backend-provider wrappers:** Each domain (tasks, cockpits, contracts, ledgers) has a dedicated `Any*Route` component that constructs a typed persistence object from `useFetch` calls and passes it to the corresponding `*BackendProvider`. This decouples REST wiring from composer UI logic.
- **Filesystem route bypass:** `secured.$locale.worker.tsx` checks `location.pathname.includes('/filesystem')` and returns `<Outlet />` directly, skipping `EveliApp`. This prevents nested shell layouts when the filesystem route renders its own `EveliApp` with a wider drawer (450 px vs the default).
- **Embedded Gamut stacks:** `InHouseFill` and both dialob-review components instantiate their own `QueryClient` and `ThemeProvider` to isolate Gamut form state from the worker shell's query context.
- **Feature + permission gating:** Navigation items in `Toolbar` use `EveliTenantFeatureEnabled` (tenant feature flags) composed with `EveliPermissions` (IAM role checks) from `@dxs-ts/eveli-primitives` and `@dxs-ts/eveli-api`.

## Error Handling

The root route (`__root.tsx`) assigns `EveliError` as the `errorComponent` and `EveliErrorNotFound` as the `notFoundComponent` for the entire tree; individual routes repeat `notFoundComponent: EveliErrorNotFound`. API errors from `useFetch` are not caught at the route component level — they surface through `EveliError` via TanStack Router's error boundary mechanism. `EveliTaskActivity` and `EveliUserActivity` destructure `error` from `useQuery` but do not render it; only `isPending` triggers a spinner. The migration upload route catches `SyntaxError` and JSON serialisation errors locally and displays them in an MUI `Alert`. `EveliProcExecution` contains a leftover `console.log(data)` call with no error path.

## Bootstrapping

This module is the top-level wiring layer — it does not itself require a parent provider. The host application must:

1. Mount `IntlProvider` (react-intl) above the router outlet to satisfy `useIntl` in all route components.
2. Supply a `QueryClientProvider` (TanStack Query) above the router; individual features (`DialobReview`, `InHouseFill`) create their own isolated `QueryClient` instances internally.
3. Configure `@dxs-ts/envir-fetch` (via its own provider or configuration mechanism) so that `useFetch` resolves REST keys to concrete fetch functions before any route component mounts.
4. Mount `@dxs-ts/eveli-api` providers (`IamProvider`, `LocaleProvider`, tenant config providers, `QueueProvider`) that `useIam`, `useLocale`, `useConfig`, `useQueue`, `useTenantConfig`, and `useFeedbackBackend` depend on.
5. Pass the exported `router` instance to TanStack Router's `RouterProvider`.

The module exports `router`, `routeTree`, `FileRouteTypes`, and `RootRouteChildren` for the host application to consume.

## TypeScript Extension Points

- **`declare module '@tanstack/react-router' { interface Register { router: typeof router } }`** in `router.ts` — global router type registration; gives all `useNavigate`, `useParams`, and `createFileRoute` calls full type inference throughout the application.
- No `GComponentsPropsList` or MUI theme augmentation is declared in this module. Theme overrides are delegated to `@dxs-ts/eveli-primitives` and `@dxs-ts/gamut-theme`.

## Common Tasks

### Add a new worker route

- Create a new file in `routes/` following the TanStack Router filename convention (e.g. `routes/secured.$locale.worker.<segment>.index.tsx`). The filename maps directly to the URL segment.
- Call `createFileRoute(...)` at the top of the file — TanStack Router's code generator will pick it up and regenerate `routeTree.gen.ts`.
- If the route belongs to an existing domain (tasks, cockpits, contracts, ledgers), nest it under the matching parent route file (e.g. `routes/secured.$locale.worker.tasks.tsx`) so it inherits the corresponding `Any*Route` backend-provider wrapper automatically.
- Add a navigation button for the new route in `eveli-setup/Toolbar.tsx`, wrapping it with `EveliTenantFeatureEnabled` and/or `EveliPermissions` as required.
- If the route renders its own `EveliApp` instance (as the filesystem route does), update the bypass check in `routes/secured.$locale.worker.tsx` to include the new path segment; otherwise the parent shell will double-wrap it.

### Add a permission check to an existing route

- Open the relevant `Any*Route` component (`eveli-any-task-route/AnyTaskRoute.tsx`, `eveli-any-cockpit-route/AnyCockpitRoute.tsx`, `eveli-any-contract-route/AnyContractRoute.tsx`, or `eveli-any-ledger-route/AnyLedgerRoute.tsx`).
- For contract and ledger routes, `useContractPermissions` and `useLedgerPermissions` currently `return []` — implement the real permission logic there before adding a guard.
- Wrap the relevant JSX in the route file with `EveliPermissions` from `@dxs-ts/eveli-primitives`, passing the required role(s) from `EveliPermissions` constants in `@dxs-ts/eveli-api`.
- For feature-flag-gated routes, compose `EveliTenantFeatureEnabled` (from `@dxs-ts/eveli-api`) around the permission guard, mirroring the pattern already used in `eveli-setup/Toolbar.tsx`.

### Wire a new REST endpoint into a domain backend provider

- Identify which `Any*Route` owns the domain: `eveli-any-task-route/AnyTaskRoute.tsx`, `eveli-any-cockpit-route/AnyCockpitRoute.tsx`, `eveli-any-contract-route/AnyContractRoute.tsx`, or `eveli-any-ledger-route/AnyLedgerRoute.tsx`.
- Add a new `useFetch` call using the REST key pattern `worker/rest/api/<resource>.<METHOD>` (consistent with existing calls in the file).
- Pass the resulting fetch function as a new prop on the corresponding `*BackendProvider` (`TaskBackendProvider`, `CockpitsBackendProvider`, `ContractBackendProvider`, or `LedgerBackendProvider`).
- If the endpoint requires date formatting, inject `XuiDatePicker` or `XuiDateTimeFormatter` as slot components — see how `AnyTaskRoute.tsx` already does this for the task domain.

## Known Limitations and Gotchas

- **Nested `EveliApp` prevention is path-string-based.** `secured.$locale.worker.tsx` uses `location.pathname.includes('/filesystem')` to decide whether to render its own `EveliApp`. A route whose URL segment happens to contain the substring `filesystem` for any other reason would incorrectly bypass the shell.
- **`console.log` in production code.** `EveliProcExecution.tsx` line 22 has `console.log(data)` left in place. `TenantConfigSelect.tsx` line 56 has `console.log("Selected", valueArray)`.
- **`UserProfileNotifications` is commented out.** `UserProfile.tsx` has a `//TODO` block wrapping the notifications section, which is non-functional.
- **`DialobReview` (session-based) slot is commented out in `AnyTaskRoute`.** The active slot uses `DialobReviewBasedOnForm`; the older `RealDialobReview` import is present but the JSX is commented out, leaving dead import code.
- **`AnyContractRoute` and `AnyLedgerRoute` return empty permission arrays.** `useContractPermissions` and `useLedgerPermissions` both `return []`, meaning no permission checks are active for these domains.
- **Per-route `QueryClient` isolation.** `InHouseFill`, `DialobReview`, and `DialobReviewBasedOnForm` each instantiate their own `QueryClient` at module scope (not inside a hook), meaning the same instance is reused across all component mounts. This prevents proper cache invalidation if the same component is mounted a second time in the same session.
- **`secured.$locale.assets.tsx` is a transparent pass-through.** The assets branch has no auth guard of its own beyond the parent `secured.$locale.tsx` redirect; the outer `useIam` check is the only protection.
- **`router.ts` imports from `./routeTree.gen`** which is a code-generated file. It will not exist until TanStack Router's file-based code generation has run. The module cannot be compiled from a clean checkout without this generation step.

## Open Questions

| Item | Current State | Risk | Action Needed |
|------|---------------|------|---------------|
| Filesystem bypass is path-string-based | `secured.$locale.worker.tsx` uses `location.pathname.includes('/filesystem')` | Any future route URL containing the substring `filesystem` for an unrelated reason will incorrectly skip `EveliApp` | Replace string check with a route-aware mechanism (e.g. a route context flag or TanStack Router's `matchRoute`) |
| `console.log` in production code | `EveliProcExecution.tsx` line 22 has `console.log(data)`; `TenantConfigSelect.tsx` line 56 has `console.log("Selected", valueArray)` | Leaks internal data in production builds | Remove both `console.log` calls |
| `UserProfileNotifications` is commented out | `UserProfile.tsx` has a `//TODO` block wrapping the notifications section | Feature is non-functional and silently absent | Implement or formally remove the component and its import |
| `DialobReview` (session-based) slot is commented out in `AnyTaskRoute` | Active slot uses `DialobReviewBasedOnForm`; `RealDialobReview` import is present but JSX is commented out | Dead import code increases bundle noise and causes confusion | Remove the commented-out JSX and the dead import, or re-enable the slot intentionally |
| `AnyContractRoute` and `AnyLedgerRoute` return empty permission arrays | `useContractPermissions` and `useLedgerPermissions` both `return []` | No permission checks are active for these two domains | Implement real permission logic for contract and ledger domains |
| Per-route `QueryClient` instances created at module scope | `InHouseFill`, `DialobReview`, and `DialobReviewBasedOnForm` each instantiate their own `QueryClient` outside a hook | The same instance is reused across all mounts, preventing proper cache invalidation on remount | Move `QueryClient` instantiation inside a `useMemo` or `useState` so each mount gets a fresh instance |
| `secured.$locale.assets.tsx` has no dedicated auth guard | Only protection is the parent `secured.$locale.tsx` redirect via `useIam` | If the parent guard is ever bypassed or weakened, the entire assets branch is unprotected | Add an explicit auth check or route guard at the `assets` layout level |
| `routeTree.gen.ts` is code-generated and absent on a clean checkout | `router.ts` imports from `./routeTree.gen` which does not exist until TanStack Router codegen runs | Module cannot be compiled from a clean checkout without the generation step | Document the codegen step in the onboarding/build guide; consider adding a pre-build script |

## Scope Boundaries

This module does not implement any backend. It does not own the Dialob session state, the Gamut customer portal, any domain API type definitions, or any composer UI components — all of those are sourced from their respective `@dxs-ts/*` packages. It does not define authentication logic (that is in `@dxs-ts/eveli-api` and `@dxs-ts/eveli-primitives`). It does not handle i18n message definitions (those are in each domain's own intl package). It does not manage theme or MUI overrides beyond applying `GThemeOptions` locally inside the embedded Gamut form stacks.
