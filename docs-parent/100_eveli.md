_last updated: 13.10.2026_

# Eveli

## Purpose

Eveli is the worker-facing task management and process orchestration module. It provides the backend REST API for task lifecycle management, Dialob form integration, process execution, feedback, queue management, and asset publishing. On the frontend it provides the worker shell, navigation, route tree, and all worker-facing page components.

## Dependencies

### `@dxs-ts/eveli-api`

**dependencies:**
- `@dxs-ts/envir-fetch`
- `@dxs-ts/envir-util`
- `@dxs-ts/wrench-api`
- `@dxs-ts/stencil-api`
- `@dxs-ts/eveli-intl`
- `@dxs-ts/task-api`
- `@dxs-ts/contract-api`
- `@dxs-ts/cockpit-api`
- `@dxs-ts/ledger-api`
- `@dxs-ts/tagomi-api`
- `@dxs-ts/gamut-theme`
- `@dxs-ts/gamut-api`
- `@dxs-ts/user-profile`
- `@dxs-ts/task-feedback`
- `@dxs-ts/fs-api`
- `@dxs-ts/xui-table`

**peerDependencies:**
- `react`
- `@tanstack/react-query`
- `@mui/material`
- `@mui/utils`
- `notistack`
- `react-intl`

### `@dxs-ts/eveli-primitives`

**dependencies:**
- `@dxs-ts/xui-datetime`
- `@dxs-ts/xui-table`
- `@dxs-ts/eveli-api`
- `@dxs-ts/task-api`
- `@dxs-ts/user-profile`
- `@dxs-ts/envir-fetch`

**peerDependencies:**
- `ts-md5`
- `react`
- `react-intl`
- `@mui/material`
- `@mui/system`
- `@mui/icons-material`
- `@mui/utils`
- `@mui/x-tree-view`
- `@tanstack/react-router`
- `@tanstack/react-query`
- `@tanstack/react-table`
- `notistack`
- `luxon`
- `numbro`
- `file-saver`

### `@dxs-ts/eveli-routes`

**dependencies:**
- `@dxs-ts/gamut-api`
- `@dxs-ts/gamut-form`
- `@dxs-ts/gamut-shell`
- `@dxs-ts/gamut-theme`
- `@dxs-ts/gamut-form-review`
- `@dxs-ts/stencil-api`
- `@dxs-ts/stencil-routes`
- `@dxs-ts/wrench-api`
- `@dxs-ts/wrench-routes`
- `@dxs-ts/tagomi-api`
- `@dxs-ts/tagomi-routes`
- `@dxs-ts/envir-fetch`
- `@dxs-ts/task-api`
- `@dxs-ts/eveli-api`
- `@dxs-ts/eveli-primitives`
- `@dxs-ts/fs-api`
- `@dxs-ts/fs-composer`
- `@dxs-ts/user-profile`
- `@dxs-ts/task-feedback`
- `@dxs-ts/task-composer-v2`
- `@dxs-ts/contract-api`
- `@dxs-ts/contract-composer`
- `@dxs-ts/cockpit-api`
- `@dxs-ts/cockpit-composer`
- `@dxs-ts/ledger-api`
- `@dxs-ts/ledger-composer`
- `@dxs-ts/xui-table`
- `@dxs-ts/xui-datetime`
- `@dxs-ts/fs-nav`

**peerDependencies:**
- `react`
- `react-intl`
- `@mui/material`
- `@mui/icons-material`
- `@mui/utils`
- `@mui/system`
- `@tanstack/react-router`
- `@tanstack/react-table`
- `@tanstack/react-query`
- `@monaco-editor/react`
- `yaml`
- `luxon`
- `notistack`
- `recharts`

### `@dxs-ts/eveli-intl`

No runtime dependencies. Provides only translation message maps.

## Module Relationships

- `@dxs-ts/envir-fetch` — provides `useFetch`, `FetchProvider`, and `useFetchConfig`; eveli-api routes every backend call through this hook and re-exports `FetchProvider` from its barrel
- `@dxs-ts/envir-util` — utility helpers eveli-api uses for environment configuration
- `@dxs-ts/gamut-api` — eveli-api consumes `useLocale`; eveli-routes consumes Dialob rendering contexts, gamut-api fetch hooks, and IAM primitives embedded in the worker shell
- `@dxs-ts/gamut-form` — eveli-routes embeds the interactive Dialob form renderer for in-house form fills (`InHouseFill`)
- `@dxs-ts/gamut-form-review` — eveli-routes renders completed Dialob sessions in read-only mode via `DialobReviewBasedOnForm`
- `@dxs-ts/gamut-shell` — eveli-primitives builds `EveliShell` using the same shell layout primitives as the portal
- `@dxs-ts/gamut-theme` — eveli-api and eveli-routes rely on the shared MUI theme provider
- `@dxs-ts/task-api` — eveli-api and eveli-primitives consume task domain types and fetch hooks
- `@dxs-ts/task-feedback` — eveli-api and eveli-routes consume feedback data access
- `@dxs-ts/task-composer-v2` — eveli-routes embeds the task composer for task creation and editing
- `@dxs-ts/contract-api` and `@dxs-ts/contract-composer` — eveli-routes embeds contract management views
- `@dxs-ts/cockpit-api` and `@dxs-ts/cockpit-composer` — eveli-routes embeds cockpit management views
- `@dxs-ts/ledger-api` and `@dxs-ts/ledger-composer` — eveli-routes embeds ledger views
- `@dxs-ts/wrench-api` and `@dxs-ts/wrench-routes` — eveli-routes embeds the Wrench decision table editor
- `@dxs-ts/stencil-api` and `@dxs-ts/stencil-routes` — eveli-routes embeds the Stencil content management editor
- `@dxs-ts/tagomi-api` and `@dxs-ts/tagomi-routes` — eveli-routes embeds the Tagomi workflow editor
- `@dxs-ts/fs-api`, `@dxs-ts/fs-composer`, `@dxs-ts/fs-nav` — eveli-routes embeds the filesystem asset browser
- `@dxs-ts/xui-datetime` — eveli-primitives and eveli-routes use `DateTimeFormatter` for timestamp display
- `@dxs-ts/xui-table` — eveli-api and eveli-routes use table rendering utilities
- `@dxs-ts/user-profile` — eveli-api and eveli-primitives consume user profile data and context

## Backend Architecture

**Stack:** Java 21, Spring Boot, Vert.x Mutiny (reactive PostgreSQL via `io.vertx.mutiny.pgclient`), Spring Data JPA, Flyway, Dialob, Thena (event sourcing / GRIM), ThenaMq (message queue over PostgreSQL)

**Key controllers / services:**

Worker-facing (`/worker/rest/api/`):
- `TaskApiController` — full CRUD for tasks, comments, file attachments, form assignments, PDF export, reviews, transfers; route `/worker/rest/api/tasks`
- `ProcessApiController` — lists active process executions; route `/worker/rest/api/processes`
- `FeedbackApiController` — CRUD plus sentiment, similar-feedback, and template queries for worker feedback management; route `/worker/rest/api/feedback`
- `QueueApiController` — queue channel configs, messages, and deliveries; route `/worker/rest/api/queues`
- `CockpitApiController` — cockpit management and tenant association; route `/worker/rest/api/cockpits`
- `LedgerApiController` — ledger listing; route `/worker/rest/api/ledgers`
- `ContractApiController` — contract listing; route `/worker/rest/api/contracts`
- `BatchApiCotroller` — batch execution management; route `/worker/rest/api/batches`
- `MetisApiController` — search status and re-index triggers; route `/worker/rest/api/metis`
- `MetisSearchApiController` — full-text search operations; route `/worker/rest/api/metis/search`
- `WorkerIamController` — IAM liveness check; route `/worker/rest/api/iam`
- `HealthApiController` — system health status; route `/worker/rest/api/health`
- `UserProfileController` — worker user profiles; route `/worker/rest/api/userprofiles`
- `TenantApiController` — tenant configuration; route `/worker/rest/api/tenant-configs`
- `AttachmentApiController` — file attachment upload and download; route `/worker/rest/api`
- `PdfApiController` — PDF generation; route `/worker/rest/api`
- `SchedulerApiCotroller` — scheduler management; route `/worker/rest/api/schedulers`

Asset management (`/worker/rest/api/assets/`):
- `AssetsDialobController` — Dialob form asset CRUD plus proxy to external Dialob API; route `/worker/rest/api/assets/dialob`
- `AssetsPublicationController` — publication management; route `/worker/rest/api/assets/publications`
- `AssetsStencilController` — Stencil content asset management; route `/worker/rest/api/assets/stencil`
- `AssetsTagomiController` — Tagomi workflow asset management; route `/worker/rest/api/assets/tagomi`
- `AssetsFsController` — filesystem-backed dirent management; route `/worker/rest/api/assets/fs`
- `AssetsWrenchController` — Wrench decision table asset management; route `/worker/rest/api/assets/wrench`
- `AssetsDeploymentController` — deployment management; route `/worker/rest/api/assets/deployments`

Gamut (customer portal) facing (`/portal/`):
- `GamutSiteController` — public site content serving; route `/portal/site`
- `GamutSiteSearchController` — site content search; route `/portal/site/search`
- `GamutFeedbackController` — customer-facing feedback submission; route `/portal/feedback`
- `GamutUserActionsController` — customer user actions (form fills, task creation); route `/portal/secured/actions`
- `GamutIamController` — customer IAM; route `/portal/secured/iam`

**Patterns:**
- API/SPI separation: every domain area exposes a Java interface in `client.api` (e.g. `TaskClient`, `FeedbackClient`, `GamutClient`) with implementation classes in `client.spi`
- Spring `@Configuration` auto-config split: `EveliAutoConfig` (core), `EveliAutoConfigWorker` (worker REST beans), `EveliAutoConfigJpa`, `EveliAutoConfigBatches`, `EveliAutoConfigMetis`, `EveliAutoConfigMetisSearch` each activate their own slice
- Flyway versioned migrations in `db/postgresql/` manage the JPA-backed schema; Thena manages event-sourced data separately
- `DialobScheduler` runs a Spring `ThreadPoolTaskScheduler` to poll for Dialob form fill events and publish them to downstream services
- `ThenaMqClient` backed by a dedicated Vert.x reactive PgPool (separate from the JPA pool) handles message channel state

## Frontend Architecture

**Stack:** React 18, MUI v5, TanStack Router (file-based), TanStack Query, react-intl, notistack

**Sub-module structure (`@dxs-ts/eveli-api`):**
- `api-config` — `ConfigContextProvider` / `ConfigContext` / `useConfig`; fetches runtime config from the backend and sets `FetchProvider` context path after load
- `api-iam` — IAM-related types and fetch wrappers
- `api-iconbar` — `IconbarProvider` / context; manages sidebar icon bar state
- `api-locale` — locale context and `useLocale` hook
- `api-tenant-config` — tenant configuration types and fetch
- `api-variants` — variant/flag types
- `api-batch` — batch operation types and fetch
- `api-dialob-form` — Dialob form and fill types
- `api-proc-execution` — process execution types and fetch
- `api-publications` — publication asset types
- `api-queue` — `QueueProvider` / `QueueContext` / `useQueue`; wraps channel config, messages, and deliveries fetches
- `api-rps` — RPS (repetitive process steps) types
- `api-health` — health status types
- `api-feedback-backend` — worker feedback types and fetch
- `api-metis` — Metis search types and fetch
- `fetch/` — generated `fetchTree.gen.ts` containing the complete typed fetch tree for all backend endpoints; this file is the sole source of truth for URL strings

**Sub-module structure (`@dxs-ts/eveli-primitives`):**
- `eveli-shell` — `EveliShell` responsive app shell with breakpoint-aware toolbar height and `EveliShellContext` for drawer open/close state
- `eveli-shell-compose` / `eveli-shell-explorer` — composable drawer content sub-components
- `eveli-error` — `EveliError` full-page error boundary component; catches `useLocale` failures defensively
- `eveli-error-not-found` — `EveliErrorNotFound` 404 page
- `eveli-login` — login/logout UI component
- `eveli-logo` — `EveliLogo` with variant support
- `eveli-layout` / `eveli-flex` — layout utility components
- `eveli-spinner` — loading spinner
- `eveli-search` — search input primitive
- `eveli-activities` — activity feed components
- `eveli-alert` — alert display components
- `eveli-permissions` / `eveli-permissions-none` — permission guard components
- `eveli-theme` — theme-related helpers
- `eveli-locales` — locale selector components
- `eveli-datepicker` / `eveli-datetime-formatter` — date input and display
- `eveli-batches` / `eveli-batches-step` — batch management UI primitives
- `eveli-metis` — Metis search primitives
- `dialob-dashboard-smart` — smart component wrapping Dialob dashboard
- `eveli-props` — MUI theme augmentation; declares `EveliComponentsPropsList`, `EveliComponentNameToClassKey`, and all `declare module` patches

**Sub-module structure (`@dxs-ts/eveli-routes`):**
- `eveli-app` — `EveliApp` top-level layout wrapper; composes `EveliShell`, `IconbarProvider`, drawer, `AppBar`, and `main` content slot
- `eveli-task-queue` — worker task queue pages (queue list, messages, deliveries)
- `eveli-task-stats` — task statistics views
- `eveli-task-activity` / `eveli-user-activity` — audit trail and user activity views
- `eveli-proc-execution` — process execution management page
- `eveli-publications` — publication asset management (upload, new publication dialogs)
- `eveli-user-profile` — worker user profile page
- `eveli-in-house` — in-house (internal) form fill flow with start/fill/end stages and impersonation support
- `eveli-setup` — setup/configuration views
- `eveli-any-cockpit-route` / `eveli-any-contract-route` / `eveli-any-ledger-route` / `eveli-any-task-route` — generic parametric route shells
- `eveli-customer-ratings` — customer rating views
- `dialob-review` — read-only Dialob session review
- `routes/` — TanStack Router file-based route tree under `secured.$locale.worker.*` and `secured.$locale.assets.*` namespaces
- `router.ts` — creates and exports the `router` instance; registers it with `declare module '@tanstack/react-router'`

**Patterns:**
- File-based TanStack Router routes under `routes/secured.$locale.*`; `routeTree.gen.ts` is generated and must not be hand-edited
- `EveliApp` accepts slot props (`main`, `secondary`, `toolbar`, `tabs`, `wrapper`) to let consuming applications inject navigation and content without modifying shell internals
- `ConfigContextProvider` gates child rendering on `pending` being false — children do not mount until the backend config response arrives
- `QueueProvider` and other domain providers expose a single context hook (`useQueue`, `useConfig`) following the same pattern as Gamut API providers

## Error Handling

**Frontend:** `EveliError` (in `eveli-primitives`) is the `errorComponent` on the TanStack Router root route and renders a full-page error screen. It wraps `useLocale()` in a try/catch because the locale context may not be available during a crash. `EveliErrorNotFound` handles 404 routes as the `notFoundComponent` on the root route. Individual route components fetch data via `useEffect` + context hooks and handle absence by rendering intl-keyed empty-state strings rather than error UI.

**Backend:** Spring MVC exception handling is not centralised in a single `@ControllerAdvice`; controllers return `ResponseEntity` with explicit HTTP status codes. `PortalAccessValidatorImpl` enforces authorization and throws `UserActionNotAllowedException` (defined on `GamutClient`). `PgErrors` from the Thena library maps reactive PostgreSQL errors. `RepoAssert` provides assertion-style precondition checks used across spi classes.

## Bootstrapping

**Frontend:** A consuming application must:
1. Wrap the app in `FetchProvider` (from `@dxs-ts/envir-fetch`) to establish the fetch context.
2. Wrap in `IntlProvider` (react-intl) with messages from `@dxs-ts/eveli-intl`.
3. Wrap in a MUI `ThemeProvider` (optionally using `@dxs-ts/gamut-theme`'s `GThemeProvider`).
4. Wrap in `ConfigContextProvider` (from `@dxs-ts/eveli-api`) with `loginUrl` and `logoutUrl` props; this component fetches the backend config and blocks rendering until the response arrives.
5. Mount the TanStack `RouterProvider` with the `router` exported from `@dxs-ts/eveli-routes`.
6. Use `EveliApp` (from `eveli-routes`) as the route layout component, receiving `main`, `secondary`, and `toolbar` slot components.

**Backend:** `EveliAutoConfig` is the root `@Configuration`. It imports `EveliAutoConfigBatches`, `EveliAutoConfigBatchesDev`, `EveliAutoConfigContract`, `EveliAutoConfigDb`, and `DialobAutoConfig`. Worker REST beans are registered separately via `EveliAutoConfigWorker`. Additional opt-in slices include `EveliAutoConfigJpa`, `EveliAutoConfigMetis`, and `EveliAutoConfigMetisSearch`. The host application must supply a `ThenaMqClient` bean (backed by a dedicated reactive PgPool), an `OrgClient` bean, and a `WorkerAuthClient` bean. All configuration properties bind under the `eveli.*` prefix via `@EnableConfigurationProperties` on `EveliAutoConfig`.

## TypeScript Extension Points

- `EveliComponentsPropsList` (in `eveli-primitives/eveli-props/eveli-props.ts`) — interface listing all Eveli components that participate in MUI `defaultProps`; only `EveliLogo` is registered. Add a new entry here to make a component theming-configurable.
- `EveliComponentNameToClassKey` (same file) — interface mapping component names to their class key types for `styleOverrides` support in the MUI theme.
- `EveliComponents<Theme>` (same file) — interface that extends MUI `Components` via `declare module '@mui/material'`; the module augmentation that injects Eveli components into the MUI theme `components` config.
- `declare module '@mui/material/Button'` (same file) — adds two Button variants (`explorerInactive`, `explorerActive`) for the shell drawer nav buttons.
- `declare module 'react'` (same file) — adds CSS custom properties (`--tree-view-text-color`, `--tree-view-color`, `--tree-view-bg-color`, `--tree-view-hover-color`) to `React.CSSProperties` for use in the tree view sidebar.
- `declare module '@tanstack/react-router'` (in `eveli-routes/router.ts`) — registers the `router` instance type for full TanStack Router type inference across all route components.

## Known Limitations and Gotchas

- **`Config` fields marked for deletion**: `taskDeleteGroups`, `taskAdminGroups`, and `serviceUrl` in `ConfigContext.tsx` carry `// TODO:: will be deleted` comments. They are still read by components and must not be removed without auditing all consumers.
- **`serviceUrl` is set asynchronously**: `ConfigContextProvider` calls `setContextPath(config.serviceUrl)` in a `useEffect` after the config fetch resolves. Any fetch attempted before `ConfigContextProvider` has mounted and received a response will use the default context path (`/`), not the backend-configured one.
- **`QueueContext` initialised as `{} as any`**: `QueueContext` is created with `{} as any` as its default value. Calling `useQueue()` outside a `QueueProvider` returns an empty object with no runtime error — method calls on it will throw at call time, not at hook call time.
- **`routeTree.gen.ts` is generated**: the file at `ts_setup/modules/eveli-routes/routeTree.gen.ts` is auto-generated by TanStack Router CLI and must not be hand-edited. Changes to route files under `routes/` require re-running the generator.
- **`eveli-ide/intl.csv` is the only editable translation source**: the `en.ts`, `fi.ts`, `sv.ts` files under `eveli-intl` are generated from `intl.csv`. Editing the generated files directly will be overwritten.
- **`GamutFeedbackController` TODO**: a comment on `GamutFeedbackController` (`// TODO:: should be public forms and not feedback`) indicates the endpoint semantics may change.
- **`EveliError` defensive locale fallback**: `EveliError` wraps `useLocale()` in a try/catch because React hooks cannot conditionally be called — it calls the hook unconditionally but swallows the error, falling back to `'en'`. This is an intentional exception to the no-silent-catch rule.
- **Separate PgPool for MQ**: `AppConfig` creates a dedicated `io.vertx.mutiny.pgclient.PgPool` with `maxSize(1)` for `ThenaMqClient`, separate from the JPA pool. Running both pools against the same database under high concurrency can exhaust connection limits.

## Scope Boundaries

Eveli does not implement:
- The customer-facing portal (`@dxs-ts/gamut` and its sub-modules)
- The Dialob form engine, Wrench decision tables, Stencil content management, Tagomi workflow engine, Cockpit, Contract, or Ledger domain logic — it embeds those modules' route components and REST controllers but delegates all domain behaviour to their respective modules
- Authentication infrastructure — it consumes `WorkerAuthClient` and `GamutAuthClient` supplied by the host application
