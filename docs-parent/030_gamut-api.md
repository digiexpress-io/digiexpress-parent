# @dxs-ts/gamut-api

## Purpose

Provides the API layer for the Gamut customer portal: type definitions, fetch factory functions, React context population hooks, and in-memory state implementations for all portal domains. Consumers receive typed context interfaces and pre-wired fetch functions; they do not call `window.fetch` directly.

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
- `@dxs-ts/gamut-intl` — provides the `messages` locale maps (bs/en/fi/my/sv) consumed when this module's context providers supply locale strings to `react-intl`.

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

- `api-config` — assembles the top-level `ConfigApi` namespace: holds `default_config` (all fetch factory instances + scalar options) and `mergeOptions()` which deep-merges caller-supplied overrides into defaults. Exports `ConfigProvider` (not read in full) and `ConfigApi.ConfigContextType`.
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
- **`ActionsQueue` debounced sync** — Dialob form actions are accumulated in `ActionsQueue` and flushed on a `syncWait` timer, coalescing rapid user input into fewer round-trips.
- **Cockpit-scoped queries** — TanStack Query keys include `cockpitId` from `CockpitStore.get()`, ensuring queries re-fetch when the active cockpit changes.
