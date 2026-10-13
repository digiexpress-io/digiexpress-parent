_last updated: 13.10.2026_

# @dxs-ts/gamut-api

## Purpose

API layer for the Gamut customer portal: type definitions, fetch factory functions, React context population hooks, and in-memory state implementations for all portal domains. Consumers receive typed context interfaces and pre-wired fetch functions; they do not call `window.fetch` directly.

## Dependencies

**dependencies:**
- `@dxs-ts/gamut-intl`
- `@dxs-ts/gamut-cockpit-store`

**peerDependencies:**
- `react`
- `react-intl`
- `@mui/material`
- `@tanstack/react-query`
- `@tanstack/react-router`
- `luxon`
- `leaflet-geosearch`
- `ts-md5`

## Module Relationships

### Depends on

- `@dxs-ts/gamut-cockpit-store` — calls `CockpitStore.get()` in `api-config/config-types.ts` (`ConfigApi.mergeOptions`) and `api-offer/usePopulateContext.ts` to read the active `cockpitId`. That value is injected into TanStack Query cache keys so all domain queries re-fetch when the active cockpit changes.
- `@dxs-ts/gamut-intl` — supplies the `messages` locale maps (bs/en/fi/my/sv) consumed when context providers supply locale strings to `react-intl`.

### Consumed by

- `@dxs-ts/gamut-shell` — mounts context providers from `gamut-api` (e.g. `IamBackendProvider`, `SiteBackendProvider`) and reads domain data via `useIam()`, `useSite()`.
- `@dxs-ts/gamut-md` — reads site content via `useSite()` to obtain `SiteApi.TopicView` data passed to `GMarkdown`.
- `@dxs-ts/gamut-form` — mounts `DialobProvider` (from `api-dialob`); reads `FormStore` via `useForm()` and `useFormTip()`; pushes user input as `Action` objects into `ActionsQueue`; uses `FormImpl` for form session parsing.
- `@dxs-ts/gamut-form-review` — calls `useDialobReview()` (from `api-dialob`) to fetch completed form session data; constructs `GFormReviewContextType` from the result and makes it available to `GFormReviewQuestionnaire` and leaf review components.
- `@dxs-ts/gamut-primitives` — reads `Offer[]`, `Contract[]`, `Subject[]`, and `Booking[]` via `useOffers()`, `useContracts()`, `useComms()`, and `useBookings()`; reads site navigation via `useSite()`; uses `useIam()` for auth state; uses `useSearch()` / `useBackendSearch()` for site search; mounts `GFormReview` (gamut-form-review) inside `GInboxFormReview`, passing `formId` sourced from `OfferApi.Offer`.
- `@dxs-ts/gamut-routes` — mounts all domain context providers (`ConfigProvider`, `IamBackendProvider`, `SiteBackendProvider`, `OfferProvider`, `ContractProvider`, `CommsProvider`, `BookingProvider`, `SearchProvider`, `MapProvider`) in `__root.tsx` via `SecuredSetup` / `PublicSetup`; reads auth state via `useIam()` to select the setup branch.

## Frontend Architecture

**Stack:** React, TanStack Query, MUI, Luxon

**Sub-module structure:**

- `api-config` — assembles the top-level `ConfigApi` namespace: holds `default_config` (all fetch factory instances + scalar options) and `mergeOptions()` which deep-merges caller-supplied overrides into defaults. Exports `ConfigProvider` and `ConfigApi.ConfigContextType`.
- `api-site` — types for Stencil-sourced site data (`Site`, `Topic`, `TopicLink`, `TopicView`, `TopicGroup`, `Feedback`, `FeedbackRating`). Exports `useSite()` hook, `SiteBackendProvider`, and `SITE_SEARCH_TOPIC`. `useSite()` sorts and filters `TopicView[]` and provides `getTopicGroups()` and `getLink()`.
- `api-iam` — types for user identity: `User`, `UserRoles`, `UserProducts`, `Token`, `UserLiveness`, `AuthType`, `FormLinkAuthType`. Context type `IamBackendContextType` exposes `getFormLinkAuthType()` and `isFormLinkEnabled()` against `SiteApi.TopicLink`. Fetch factories (`createIamFetch`) target `/portal/secured/iam`.
- `api-dialob` — Dialob form session types: `Action` (discriminated union of 13 action types), `ActionItem`, `ActionValueSet`, `ActionError`, `FormState`, `Form`, `FormStore`, `FormContextType`. Class `FormImpl` implements `DialobApi.Form` using `parsePage` and `parseInputRow`. Class `ActionsQueue` batches `Action[]` and syncs to backend via `FetchActionPOST` / `FetchActionGET` with configurable `syncWait` debounce.
- `api-offer` — types `Offer`, `OfferRequest`, `OfferRps`, and context type `OfferContextType`. `usePopulateContext` queries `/portal/secured/` via TanStack Query, maps raw `LegacyProcessApi.Process[]` to `OfferApi.Offer[]` using `mapToOffer` / `mapToOfferData`.
- `api-bookings` — types `Booking` (references `ContractApi.ContractId`) and context type `BookingContextType`. Fetch types target `/portal/secured/` booking endpoints.
- `api-contract` — types `Contract` (aggregates `ProductApi.Product`, `OfferApi.Offer`, `BookingApi.Booking`) and context type `ContractContextType` with attachment upload/download. `createContractFetch` implements a two-step pre-signed upload flow for contract attachments.
- `api-comms` — types `Subject`, `Message`, `ReplyTo`, and context type `CommsContextType`. Tracks `unread` and `exchanges` stats. Fetch targets `/portal/secured/actions` (GET subjects, POST reply, PUT viewed).
- `api-locale` — types `LocaleContextType` with `locale` and `setLocale`. Scalar key/value localization maps typed as `Localizations`.
- `api-search` — `SearchApi` namespace with `SearchState`, `SearchContextType`, `BackendSearchState`. `SearchStateImpl` holds filtered topic/link/form results and is created via `SearchApi.getInstance()`. Exports `SearchProvider`, `useSearch`, `useBackendSearch`, and `useSemanticResults`.
- `api-map` — types `GeoLocation`, `RawAddress`, `RawResult`, `SearchResult`, `PointTuple`, `BoundsTuple` for geocoding via `leaflet-geosearch`. Exports `MapProvider`.
- `api-product` — types `ProductPage`, `Product`, `Phone`, `AdditionalInfo`, `ProductHeading`. Fetch type `GetProductFetchGET`.
- `api-legacy-processes` — raw backend type `LegacyProcessApi.Process` (process + task status, Dialob form reference, messages, attachments). Used as the intermediate data shape before mapping to `OfferApi.Offer`.
- `api-override` — utility types `OverrideProps<T>` and `GOverridableComponent<T>` for MUI slot override pattern.
- `api-variants` — `useVariantOverride(props, styles)` resolves MUI `ownerState.variant` against a style map, supporting both keyed and array-form variant overrides.

**Patterns:**

- **Fetch factory pattern** — each sub-module defines fetch type aliases (e.g. `FetchUserGET`, `CreateOfferFetchPOST`) and a corresponding `createXxxFetch(url?)` factory in `api-config/fetch_*.ts`. Callers override individual methods via `ConfigApi.mergeOptions()`.
- **`usePopulateContext` hooks** — each domain sub-module (`api-offer`, `api-bookings`, `api-contract`, `api-comms`) exposes a `usePopulateContext` hook that wires TanStack Query, maps raw backend data, and returns a typed context object. Context providers in the consuming app call this hook and pass the result to a React context.
- **Namespace + declare namespace split** — every domain uses `export namespace XxxApi { }` for runtime values (mapper references, factory instances) and `export declare namespace XxxApi { }` for types. This keeps types and values in the same namespace identifier without runtime cost.
- **`ActionsQueue` debounced sync** — Dialob form actions accumulate in `ActionsQueue` and flush on a `syncWait` timer, coalescing rapid user input into fewer round-trips.
- **Cockpit-scoped queries** — TanStack Query keys include `cockpitId` from `CockpitStore.get()`, ensuring queries re-fetch when the active cockpit changes.

## Error Handling

Errors surface through several distinct mechanisms depending on the domain.

**HTTP 401 responses** are handled by `assertAuthenticatedResponse` (`api-iam/IamBackendProvider.tsx`), which throws `UnauthorizedRequestError`. Consuming hooks call `useAssertAuthentication(error)` (also in `IamBackendProvider.tsx`) to intercept this error and route it to `onAuthorizationFail` from `IamBackendContextType`. This pattern is used in `api-offer/usePopulateContext.ts`, `api-contract/usePopulateContext.ts`, and `api-dialob/DialobContext.tsx`.

**Session expiry** is handled by `IamLiveness` (`api-iam/IamLiveness.tsx`), which polls `fetchUserLivenessGET` on a configurable `timeout` interval (default 60000 ms). When the server returns `expiresIn <= 1000`, it calls `onExpire`. The default `handleExpire` in `ConfigApi.default_config` calls `alert('sessionExpired')` — consuming apps must override this.

**Dialob sync errors** are reported via `ActionsQueueErrorEvent` (`api-dialob/impl-actions-queue.ts`). `ActionsQueue` fires `error` event handlers with type `'SYNC'` on first failure and `'SYNC-REPEATED'` after three consecutive failures. The error class is `ActionsQueueRequestError` (for HTTP-level failures) or `ActionsQueueError` (for queue-level failures). `FormStore.pendingError` (exposed by `useFormStore`) is set to `true` when the initial `pull()` fails; `FormProvider` renders the `formUnavailable` component in that case.

**Site fetch failures** fall back silently: `SiteBackendProvider` logs a `console.error` and substitutes `maintainace_en` (from `api-site/fallback-content.ts`) when the site fetch returns a non-OK response.

**Forced login** is handled by `IamForcedLogin` (`api-iam/IamForcedLogin.tsx`), a non-component function that opens a popup window and polls until the user authenticates or a 60-second timeout rejects the promise.

No validation error types are defined inside this module. Dialob field-level validation errors arrive from the backend as `ActionError` objects (fields: `id`, `code`, `description`) and are stored in `FormState.errors`.

## Bootstrapping

A consuming app must mount providers in the following order (outer to inner):

1. **`LocaleProvider`** (`api-locale/LocaleProvider.tsx`) — must be outermost because `SiteBackendProvider` and `SearchProvider` call `useLocale()` and `useIntl()`. Accepts `options` (`LocaleApi.Localizations`) for per-locale message overrides, `enabledLocales`, and an optional `defaultLocale` factory. Without `defaultLocale`, locale is inferred from `window.location.pathname` (expects `/secured/<locale>/` or `/public/<locale>/` path segments) with a fallback to `navigator.language`.

2. **`ConfigProvider`** (`api-config/ConfigProvider.tsx`) — wraps all domain providers; supplies `ConfigApi.ConfigContextType` via `ConfigApi.mergeOptions(options)`. Pass a `ConfigApi.Options` object overriding only the fetch factories or scalar values that differ from `ConfigApi.default_config`. All `create*Fetch` factories default to relative URLs; override individual methods to point at non-default endpoints.

3. **`IamBackendProvider`** (`api-iam/IamBackendProvider.tsx`) — requires `fetchUserGET`, `fetchUserRolesGET`, `fetchUserProductsGET`, `fetchUserLivenessGET`, `onAuthorizationFail`, and `onExpire`. Blocks rendering with `<>I'm loading...</>` until the first IAM response is received.

4. **`SiteBackendProvider`** (`api-site/SiteBackendProvider.tsx`) — requires `fetchCockpitsGet`, `fetchSiteGet`, `fetchFeedbackGet`, and `fetchFeedbackRatingPut`. Must be inside `LocaleProvider` (reads locale) and inside a TanStack `QueryClientProvider`.

5. **`SearchProvider`** (`api-search/SearchContext.tsx`) — must be inside `SiteBackendProvider` (reads `views` and `pending` from `useSite()`).

6. **Domain providers** (`OfferContext`, `ContractContext`, `CommsContext`, `BookingContext`) — each wraps its `usePopulateContext` hook and exposes a typed context. Must be inside `IamBackendProvider` and `SiteBackendProvider`. These are typically assembled by `gamut-routes` in `SecuredSetup`.

7. **`MapProvider`** (`api-map/MapProvider.tsx`) — requires `options.countrycodes` and `options.locale`; independent of other domain providers.

8. **`DialobProvider`** (`api-dialob/DialobContext.tsx`) — required only within form-rendering subtrees. Accepts `fetchActionPost`, `fetchActionGet`, `fetchReviewGet`, `fetchAttachmentPost`, and optional `syncWait` (ms; defaults to 250 in `useFormStore`).

All providers require `@tanstack/react-query`'s `QueryClientProvider` above them in the tree.

## TypeScript Extension Points

No `declare module` augmentations or interface merging are present in this module.

The module uses the `export namespace` / `export declare namespace` split pattern (e.g. `ConfigApi`, `SiteApi`, `IamApi`, `DialobApi`, `OfferApi`, `ContractApi`, `CommsApi`, `BookingApi`, `SearchApi`, `MapApi`, `LocaleApi`, `ProductApi`, `LegacyProcessApi`) to co-locate runtime values and type declarations under a single identifier. These namespaces are not designed to be extended by consumers via declaration merging.

`OverrideProps<T>` and `GOverridableComponent<T>` in `api-override/index.ts` provide the MUI slot-override extension pattern for consuming modules, but these are structural utilities, not extension points in the TypeScript augmentation sense.

## Known Limitations and Gotchas

**`handleExpire` default calls `alert()`** — `ConfigApi.default_config.handleExpire` in `api-config/config-types.ts` is `() => alert('sessionExpired')`. Consuming apps that do not override this will display a browser alert dialog on session timeout.

**`mergeOptions` uses `@ts-ignore` for dynamic key assignment** — `ConfigApi.mergeOptions` in `api-config/config-types.ts` uses three `@ts-ignore` comments to suppress TypeScript errors when setting keys on the merged config object. The merge is a shallow loop over `Object.entries`; nested fetch objects receive method-level merges but there is no deep-merge beyond two levels.

**Address formatting in `_formatRaw` is hardcoded to a European postal format** — `api-map/_formatRaw.ts` combines Nominatim address fields into `[street, postcode, city]` order with a `// TODO: make it customizable` comment. The format is not configurable without forking the function.

**`IamBackendProvider` renders a plain string during first load** — When `isFirstLoad` is `true`, the provider renders `<>I'm loading...</>` (a bare string with no styling or translation) instead of a proper loading state component. Consuming apps cannot inject a custom loading element.

**`IamForcedLogin` uses a module-level variable for the popup window** — `api-iam/IamForcedLogin.tsx` stores the login popup in `var __login_window`, a module-level singleton. Multiple simultaneous calls will share the same window reference.

**`useDialobReview` returns untyped `any` for `form` and `session`** — The return type in `api-dialob/DialobContext.tsx` is `{ form: any, session: any }`. Consumers receive no TypeScript safety on the review payload shape.

**`FormProvider` falls back to a bare string on missing offer** — When `FormProvider` cannot resolve an offer by `executionId`, it renders `<>Failed to load form...</>` (a bare string) and logs a `console.error`. `formUnavailable` is only rendered when `store.pendingError` is `true`; this fallback path does not use it.

**Cockpit-scoped query keys do not include cockpit ID for contracts** — `api-contract/usePopulateContext.ts` uses `queryKey: [queryKey]` without appending `cockpitId`, unlike `api-offer/usePopulateContext.ts`. Contract queries do not re-fetch automatically when the active cockpit changes.

**`SiteBackendContextType` is not exported from the package root** — `SiteBackendContextType` is defined and exported from `api-site/SiteBackendProvider.tsx` but not re-exported from `api-site/index.ts`. Consumers importing from the package root cannot reference this type without a deep import.

## Scope Boundaries

This module provides API types, fetch factory configuration, React context providers, and in-memory state implementations for the Gamut portal domains (IAM, site, offers, contracts, communications, bookings, search, map, locale, Dialob forms). It does not render UI components beyond minimal loading/error fallback strings, does not own routing logic, delegates session expiry and authorization failure handling to callbacks supplied by the consuming app, and does not perform data persistence. Form rendering, markdown rendering, route tree definition, and shell layout are the responsibility of consuming modules (`gamut-form`, `gamut-md`, `gamut-routes`, `gamut-shell`).

## TanStack Integration

### Packages used

| Package | Peer version | Role in this module |
|---|---|---|
| `@tanstack/react-query` | `5.83.0` | Server state fetching inside all domain `usePopulateContext` hooks |
| `@tanstack/react-router` | `1.130.2` | Declared as a peer dependency; routing is owned by `gamut-routes`, not this module |

`@tanstack/react-store`, `@tanstack/react-table`, and `@tanstack/react-form` are not used in any Gamut module.

### `useQuery` inside providers

Every domain sub-module that fetches backend data follows the same pattern: a `usePopulateContext` hook calls `useQuery`, and a context provider calls that hook and exposes the result via a React context. Components never call `useQuery` directly.

End-to-end trace for `OfferProvider`:

1. `OfferProvider` (`api-offer/OfferContext.tsx`) calls `usePopulateContext` (`api-offer/usePopulateContext.ts`).
2. `usePopulateContext` calls `useQuery({ staleTime, queryKey: [queryKey, cockpitId], queryFn: () => getAllOffers(cockpitId) })`. `cockpitId` comes from `CockpitStore.get()` (localStorage).
3. Raw `LegacyProcessApi.Process[]` is mapped to `OfferApi.Offer[]` via `mapToOfferData`.
4. `usePopulateContext` returns a `PopulateOfferContext` object (`offers`, `isPending`, `createOffer`, `cancelOffer`, `refresh`, etc.).
5. `OfferProvider` sorts by date, constructs `OfferApi.OfferContextType`, and publishes it via `OfferContext.Provider`.
6. Consumers call `useOffers()` to read from `OfferContext`.

The same pattern applies in `api-bookings`, `api-contract`, and `api-comms` (each with its own `usePopulateContext` file), and in `api-site/SiteBackendProvider.tsx` and `api-iam/IamBackendProvider.tsx`.

The one exception is `useDialobReview` (`api-dialob/DialobContext.tsx`), which calls `useQuery` directly inside the hook for on-demand review fetching used by `gamut-form-review`.

`useSuspenseQuery` is not used anywhere. Pending state is handled via `isPending` guards in provider logic, not Suspense boundaries. The `queryOptions` helper is not used; query config objects are written inline at each `useQuery` call site.

### `QueryClient` configuration

`QueryClient` is instantiated with no global options (no global `staleTime`, `retry`, or `gcTime`). Per-query `staleTime` is sourced from `ConfigProvider` — `ConfigApi.default_config` sets it to `60_000` ms. Each domain provider receives `staleTime` as a prop and passes it into its `useQuery` call. The cockpits query in `SiteBackendProvider` hardcodes `staleTime: Infinity`.

### Query key structure

Keys are arrays constructed inline at each call site. There is no shared key factory.

| Query | Key |
|---|---|
| Site content | `['sites', selectedLocale, cockpit?.id]` |
| Feedback | `['feedback', selectedLocale]` |
| Cockpits | `['cockpits']` |
| IAM user | `['iam/user']` |
| Dialob review | `['reviews/' + id]` |
| Offers / comms | `[queryKey, cockpitId]` where `queryKey` defaults to `'legacy-processes'` |
| Contracts | `[queryKey]` — cockpit ID is **not** included (see Known Limitations) |
| Bookings | `['bookings']` — hardcoded in `BookingProvider` |

`cockpitId` is read from `CockpitStore.get()` and included in offer and comms keys so those queries re-fetch when the active cockpit changes. Contract queries do not include `cockpitId` and do not re-fetch on cockpit change.

### Write operations

`useMutation` is not used. All write operations (create offer, cancel offer, reply to subject, append contract attachment, cancel booking, vote on feedback) are plain `async` functions exposed on the context object. After each write, the function calls `refetch()` on the `useQuery` result for the relevant domain to update cached data. `queryClient.invalidateQueries` is not used.

For Dialob form interactions, `ActionsQueue` (`api-dialob/impl-actions-queue.ts`) handles writes as a debounced batch-and-POST cycle with no TanStack Query involvement.

### `UnauthorizedRequestError` / `handleExpire` and query failure

When a `queryFn` receives an HTTP 401 response, `assertAuthenticatedResponse` (`api-iam/IamBackendProvider.tsx`) throws `UnauthorizedRequestError`. The `useQuery` error is intercepted by `useAssertAuthentication(error)` (also in `IamBackendProvider.tsx`), which calls `onAuthorizationFail` from `IamBackendContextType`. This callback is supplied by the consuming app; `gamut-routes` wires it to navigate to the login route.

This flow is used in `api-offer/usePopulateContext.ts`, `api-contract/usePopulateContext.ts`, and `api-dialob/DialobContext.tsx`.

Session expiry (distinct from 401 errors) is handled by `IamLiveness` (`api-iam/IamLiveness.tsx`), which polls `fetchUserLivenessGET` outside of TanStack Query on a configurable `timeout` interval (default 60 000 ms). When `expiresIn <= 1000`, it calls `onExpire`. The default `handleExpire` in `ConfigApi.default_config` calls `alert('sessionExpired')`; consuming apps must override this via `ConfigApi.mergeOptions`.
