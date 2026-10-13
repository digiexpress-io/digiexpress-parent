_last updated: 13.10.2026_

# TanStack Usage — Gamut

## Packages

| Package | Version (peer) | Role |
|---|---|---|
| `@tanstack/react-router` | `1.130.2` | File-based routing, navigation, search param typing |
| `@tanstack/react-query` | `5.83.0` | Server state fetching in provider hooks |
| `@tanstack/react-store` | `0.11.0` | In-memory reactive store for scroll-to-error ref tracking in `gamut-form` |

Source for versions: `lib-gamut/version.ts` (externals declaration).

`@tanstack/react-table` and `@tanstack/react-form` are not used in any Gamut module.

---

## TanStack Router

**Route definition pattern:** File-based. All route files live under `gamut-routes/routes/` and use `createFileRoute`. The generated tree is in `gamut-routes/routeTree.gen.ts`, assembled via `._addFileChildren()` calls. The root route uses `createRootRoute`. No `createRoute` (code-based) pattern is used anywhere in Gamut.

**Register augmentation:** Done in two places — `gamut-routes/router.ts` (the library export) and `demo-app-gamut/index.tsx` (the app entry point). Both declare:

```ts
declare module '@tanstack/react-router' {
  interface Register {
    router: typeof router
  }
}
```

`routeTree.gen.ts` additionally augments `FileRoutesByPath` inside the same module declaration.

**Search params:** Manual typing via `validateSearch`. One route uses it: `secured.$locale.views.$viewId.index.tsx`.

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

The param is consumed inside the component via `Route.useSearch()`. `zodSearchValidator` is not used.

**Route loaders / beforeLoad:** Not used. No route file defines a `loader` or `beforeLoad` option. Data fetching is handled entirely by TanStack Query inside provider components, not in route lifecycle hooks.

**Error / not-found components:** Defined on the root route only (`routes/__root.tsx`):

```ts
export const Route = createRootRoute({
  component: RouteComponent,
  notFoundComponent: GErrorNotFound,
  errorComponent: GError,
})
```

`GErrorNotFound` and `GError` are from `@dxs-ts/gamut-primitives`. No child route defines its own `errorComponent` or `notFoundComponent`.

**Navigation:** `useNavigate` is imported directly from `@tanstack/react-router` at the call site — no wrapper abstraction. It appears in:

- `routes/__root.tsx` — redirects to login on auth failure
- `routes/secured.$locale.forms.$formId.index.tsx` and `secured.$locale.forms.$formId.review.tsx` — cancel navigation
- `g-router-offer/GRouterOffer.tsx` — navigates to summary after form completion, or to home on cancel
- `g-router-secured-services/Nav.tsx` — breadcrumb navigation between secured views

`Navigate` (component redirect) is used in `routes/index.tsx` to redirect from `/` to either the public or secured landing based on `authType`.

`Link` from `@tanstack/react-router` is not used. Navigation breadcrumbs in `Nav.tsx` use MUI `Link` with `onClick` calling `useNavigate`.

**Router context:** No `context` is passed to `createRouter`. `QueryClient` is not in the router context.

---

## TanStack Query

**QueryClient configuration:** Defaults are used — no explicit `staleTime`, `retry`, or `gcTime` at the `QueryClient` level. In `demo-app-gamut/DemoApp.tsx`, the client is instantiated with no options:

```ts
const queryClient = new QueryClient()
```

Per-query `staleTime` and `refetchInterval` are passed in from `ConfigProvider` (default `staleTime` is `60_000` ms, defined in `ConfigApi.default_config`). The bookings provider hardcodes `queryKey: 'bookings'`; all other providers receive `queryKey: processesQueryKey` (default value `'legacy-processes'`).

**Query pattern:** `useQuery` only. `useSuspenseQuery` is not used anywhere. The `queryOptions` helper is not used — query config objects are written inline at each `useQuery` call. Pending state is handled via `isPending` guards in provider logic, not via Suspense boundaries.

**Query key conventions:** Arrays with one or two elements. Examples:

| Query | Key |
|---|---|
| Site content | `['sites', selectedLocale, cockpit?.id]` |
| Feedback | `['feedback', selectedLocale]` |
| Cockpits | `['cockpits']` |
| IAM user | `['iam/user']` |
| Dialob review | `['reviews/' + id]` |
| Offers / contracts / comms / bookings | `[queryKey]` or `[queryKey, cockpitId]` where `queryKey` defaults to `'legacy-processes'` or `'bookings'` |

There is no shared key factory. Keys are constructed inline at each `useQuery` call site.

**Mutations:** `useMutation` is not used anywhere in Gamut. All write operations (create offer, cancel offer, reply to subject, append contract attachment, cancel booking, vote on feedback) are plain `async` functions that call the fetch function and then call `refetch()` on the relevant query to update cached data.

**Provider integration — end-to-end trace (`OfferProvider`):**

1. `__root.tsx` renders `OfferProvider` with fetch functions from `useConfig()` and options `{ staleTime, queryKey: processesQueryKey }`.
2. `OfferProvider` (`api-offer/OfferContext.tsx`) delegates all data loading to `usePopulateContext` (`api-offer/usePopulateContext.ts`).
3. `usePopulateContext` calls `useQuery({ staleTime, queryKey: [queryKey, cockpitId], queryFn: () => getAllOffers(cockpitId)... })`. The `cockpitId` comes from `CockpitStore.get()` (localStorage).
4. The resolved data (`LegacyProcessApi.Process[]`) is mapped to `OfferApi.Offer[]` via `mapToOfferData`.
5. `usePopulateContext` returns a `PopulateOfferContext` object with `offers`, `isPending`, `createOffer`, `cancelOffer`, `refresh`, etc.
6. `OfferProvider` sorts offers by date, builds a `OfferApi.OfferContextType`, and exposes it via `OfferContext.Provider`.
7. Components call `useOffers()` to read from `OfferContext` — they never call `useQuery` directly.

The pattern is identical across `BookingProvider`, `ContractProvider`, and `CommsProvider`, each with their own `usePopulateContext` file.

---

## Component Data Flow

Components do not call `useQuery` directly. They consume data via typed context hooks:

- `useSite()` — reads from `SiteBackendContext`
- `useOffers()` — reads from `OfferContext`
- `useIam()` — reads from `IamBackendContext`
- `useDialob()` — reads from `DialobContext`
- `useConfig()` — reads from `ConfigContext`

The one exception is `useDialobReview` in `api-dialob/DialobContext.tsx`, which calls `useQuery` directly inside the hook for on-demand review data fetching (used by `gamut-form-review`).

**Loading / error handling:** `isPending` guards in provider components. `OfferProvider` returns `<></>` while `data.isPending` is true — children do not render until data is ready. `IamBackendProvider` renders `<>I'm loading...</>` during first load. There are no Suspense boundaries and no `ErrorBoundary` wrappers inside provider trees.

**Mutation / action dispatch:**

- For backend write operations (create offer, cancel booking, etc.): providers expose plain async functions (e.g. `createOffer`, `cancelOffer`) that POST to the API and then call `refetch()` on the relevant query.
- For Dialob form interactions: `useFormStore` (`api-dialob/useFormStore.tsx`) manages an `ActionsQueue` instance (`api-dialob/impl-actions-queue.ts`). `ActionsQueue` is a plain class — no TanStack involvement. It batches and debounces `ANSWER`, `NEXT`, `PREVIOUS`, `ADD_ROW`, etc. actions, POSTs them via `fetchActionPost`, and applies server-returned action updates to local `FormImpl` state. Components call `queueAction` via the store's typed methods (`setAnswer`, `next`, `complete`, etc.) — not `useMutation`.

**Navigation in components:** `useNavigate` is imported directly from `@tanstack/react-router` wherever it is used. There is no project-level wrapper hook or abstraction. MUI `Link` (not TanStack `Link`) is used for clickable breadcrumb elements, with the `onClick` handler calling the `nav` function returned by `useNavigate`.

---

## Router + Query Integration

The two are not directly integrated. `QueryClient` is not passed through router context and is not accessible inside route loaders (no loaders are defined). `QueryClientProvider` wraps the entire app in `DemoApp.tsx` and the router is instantiated separately in `index.tsx`. There is no query prefetching in route lifecycle hooks. All data fetching begins when provider components mount as part of the route component tree.

---

## Key Constraints and Gotchas

- **No loaders or beforeLoad.** Route-level data fetching does not exist. All data dependencies are handled by provider mounts inside route components. Adding a loader that accesses `QueryClient` requires passing it through router context, which is not configured.
- **`staleTime` is per-query, not global.** `QueryClient` has no global `staleTime`. Each provider passes its own value from `ConfigProvider`. The default is `60_000` ms. The cockpits query hardcodes `staleTime: Infinity`.
- **No `useMutation`.** All write+invalidate cycles use `refetch()` called on the original `useQuery` reference. Adding a mutation requires manually calling `refetch()` or `queryClient.invalidateQueries` — the existing pattern does not use `invalidateQueries`.
- **`useSuspenseQuery` is not used.** Adding Suspense boundaries around new routes or components will not interact with existing query calls, which all use `useQuery` with `isPending` guards.
- **`validateSearch` is used in only one route.** Only `secured.$locale.views.$viewId.index.tsx` validates search params. Adding search params to any other route requires adding `validateSearch` explicitly — the router does not infer types from usage of `Route.useSearch()` without it.
- **`@tanstack/react-store`'s `Store` is used directly as a class instance**, not via `useStore` / `useSelector`. The `RefState` class in `gamut-form/g-form-error-visibility/useScrollTo.ts` instantiates `new Store(...)` internally. This store is not shared across components — it is scoped to a single `useScrollTo` call.
- **`Register` is declared in two places.** Both `gamut-routes/router.ts` and `demo-app-gamut/index.tsx` declare `Register.router`. Only the app-level declaration takes effect at runtime. The library-level declaration enables type inference when consuming `gamut-routes` as a package.
- **File-based route tree is generated.** `routeTree.gen.ts` is auto-generated and must not be edited. The generator is configured separately (likely via `tsr.config.json` or vite plugin). Route file naming follows TanStack Router's dot-separator convention for nested routes.
