_last updated: 13.10.2026_

# fs-nav

## Purpose

Manages URL-serialized navigation state for the fs domain's multi-tab editor: open tabs, active tab, and tree-node expansion. Encodes all state as TanStack Router search params so the full editor layout survives page refresh and is shareable via URL.

## Dependencies

**dependencies:** none

**peerDependencies:**
- `react` — React runtime
- `@tanstack/react-router` — `useSearch`, `useNavigate`
- `@dxs-ts/fs-api` — `Fs` type namespace (`DirentBase`, `BodyType`), `useFsDirent` (dirent lookup by id and name)

## Module Relationships

- `@dxs-ts/fs-api` — `useFsDirent` supplies `getDirent(id)` and `getDirentName(id)`, used in `useFsRouteNav` to resolve tab descriptors to full dirent objects and compute tab display paths.

## Backend Architecture

No backend. This module is frontend-only.

## Frontend Architecture

**Stack:** React, TypeScript, `@tanstack/react-router`

**Sub-module structure:**
- `fs-nav-types.ts` — all types (`FsTab`, `FsEditTab`, `FsCreateTab`, `FsTabDescriptor`, `FsRouteSearchParams`), pure functions for serialisation (`toTabId`, `parseFsSearchParams`), and tab-state reducers (`mergeFsSearchParams`, `closeFsTab`, `closeAllFsTabs`, `closeTabsToTheRight`, `closeOtherFsTabs`)
- `FsNavProvider.tsx` — React context (`FsNavContext`) and provider (`FsNavProvider`) that hold tree-expansion state in local `useState`, sync it to URL via `navigate` on change (skipping the first render with an `isMounted` ref), and expose `useFsNavContext`
- `useFsRouteNav.ts` — hook that reads `openTabs` and `activeTab` from URL search params, resolves `FsTabDescriptor` entries to `FsTab` objects via `useFsDirent`, and returns all tab-management actions (`openAsset`, `openCreateTab`, `closeTab`, `closeAllTabs`, `closeTabsToTheRight`, `closeOtherTabs`, `setActiveTab`)
- `useFsNav.ts` — facade hook that merges `useFsNavContext` (expansion) and `useFsRouteNav` (tabs) into a single return object

**Patterns:**
- URL-as-state: all tab and expansion state is stored in TanStack Router search params; `FsNavProvider` syncs local `useState` back to the URL via `navigate({ replace: true })` after every change.
- Discriminated unions: `FsTab` (`FsEditTab | FsCreateTab`) and `FsTabDescriptor` use a `type` field as the discriminant; `toTabId` converts a descriptor to a stable string key (`edit:<id>` or `create:<direntType>:<instanceId>`).
- Hydration guard: `FsNavProvider` uses an `isMounted` ref to skip the URL write on the initial render, preventing a spurious navigation on mount.
- Pure reducers in types file: all search-param transformations (`mergeFsSearchParams`, `closeFsTab`, etc.) are pure functions in `fs-nav-types.ts`, testable without React or routing.

## Error Handling

`useFsNavContext` throws a synchronous `Error('FsNavContext is not created!')` when called outside `FsNavProvider`. All operations are synchronous URL mutations, so there are no loading states or API failure handlers. Invalid or missing tab descriptors parsed from the URL are silently dropped by `parseTabs` / `isValidTabDescriptor`.

## Bootstrapping

`FsNavProvider` must be mounted inside both a TanStack Router context (it calls `useSearch` and `useNavigate`) and a `FsDirentProvider` from `@dxs-ts/fs-api` (required by `useFsRouteNav` → `useFsDirent`). The route `useSearch` call is hardcoded to `from: '/secured/$locale/worker/filesystem/'`, so the provider must only be rendered under that route. Correct provider order:

1. TanStack Router `RouterProvider` (outermost)
2. `FsDirentProvider` (from `@dxs-ts/fs-api`)
3. `FsNavProvider` (this module)

Consuming components call `useFsNav()` to access both expansion and tab state.

## TypeScript Extension Points

No `declare module` augmentations, interface merging, or extension records exist in this module. The public API is fully self-contained via the exported types and hooks.

## Known Limitations and Gotchas

- **Hardcoded route path**: `useSearch({ from: '/secured/$locale/worker/filesystem/' })` and `navigate({ from: '/secured/$locale/worker/filesystem', to: '.' })` appear verbatim in both `FsNavProvider` and `useFsRouteNav`. The provider and hook will not work under any other route without source edits.
- **`instanceId` via `Date.now().toString(36)`**: `openCreateTab` generates create-tab instance IDs from the current timestamp in base-36. Two tabs opened in the same millisecond share the same `instanceId` and collapse into one tab.
- **Expansion state is not in the tab descriptor**: tree expansion (`expandedIds`) is managed in local `useState` inside `FsNavProvider` and written back to the URL via `useEffect`. On fast navigations the URL may briefly lag behind the in-memory state.
- **`useFsRouteNav` is not exported**: `useFsRouteNav.ts` exists in the module but is absent from `index.ts`. Consumers must use `useFsNav()` (the facade) to access route-nav functionality.
- **`parentFolder` on create tabs is always `undefined`**: `toFsTab` in `useFsRouteNav` constructs `FsCreateTab` with `parentFolder: undefined` regardless of what the descriptor carries; `openCreateTab` passes a `_parentFolder` argument (underscore-prefixed, indicating it is intentionally unused) to its internal `mergeFsSearchParams` call.

## Scope Boundaries

This module renders no UI components, fetches no data, and does not define the TanStack Router route itself. It manages only the serialised navigation state (open tabs, active tab, expanded tree nodes) and provides hooks to read and mutate that state. Rendering the editor shell, the tree explorer, and per-dirent editors is the responsibility of `@dxs-ts/fs-composer`.
