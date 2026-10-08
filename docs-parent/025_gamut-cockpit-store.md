# gamut-cockpit-store

## Purpose

Persists and retrieves a `Cockpit` selection in `localStorage` under the key `gamut_cockpit`. Used to enable or disable a cockpit context (identified by `id`, `aliasName`, `aliasDesc`) across page reloads. Also exposes a `window.GamutCockpit` console API (`enable`, `disable`, `status`) for manual toggling in browser DevTools.

## Dependencies

No `dependencies` or `peerDependencies` declared. The `package.json` notes this as the foundation layer with no internal dependencies.

## Module Relationships

`@dxs-ts/gamut-cockpit-store` has no `@dxs-ts/*` dependencies. It sits at the foundation layer of the dependency graph.

### Direct consumers

| Module | What it uses |
|---|---|
| `@dxs-ts/gamut-api` | `CockpitStore.get()` called in `api-config/config-types.ts` (`ConfigApi.mergeOptions`) and `api-offer/usePopulateContext.ts` to inject `cockpitId` into TanStack Query cache keys |
| `@dxs-ts/gamut-theme` | `CockpitStore.get()` called in `GThemeProvider` init to select the matching entry from the `secondaryThemeOptions` map via `aliasName` |
| `@dxs-ts/gamut-routes` | `GRouterCockpits` calls `CockpitStore.save()` directly when the user selects a cockpit from `GCockpitDropdown` (triggered by `Ctrl+Shift+D`) |

### Indirect consumers (through gamut-api and gamut-theme)

Every module that mounts `GThemeProvider` or any `@dxs-ts/gamut-api` provider transitively depends on `gamut-cockpit-store`. This includes `@dxs-ts/gamut-shell`, `@dxs-ts/gamut-primitives`, `@dxs-ts/gamut-form`, `@dxs-ts/gamut-form-review`, and the `@dxs-ts/gamut` umbrella package.

### Position in the dependency graph

```
gamut-cockpit-store ──► gamut-api ──► gamut-shell ──► gamut-routes
                    └──► gamut-theme ───────────────► gamut-routes
```

`gamut-cockpit-store` is the only foundation-layer module that affects both the theme layer and the API/query layer simultaneously. Calling `CockpitStore.save()` or `CockpitStore.clear()` followed by `window.location.reload()` (as `window.GamutCockpit` does) causes `GThemeProvider` to reinitialise the secondary theme and all TanStack Query keys scoped to `cockpitId` to invalidate and refetch.

## Frontend Architecture

**Stack:** TypeScript, browser `localStorage` API

**Key exports (`CockpitStore.ts`):**

- `Cockpit` — interface with fields `id: string`, `aliasName: string`, `aliasDesc: string`
- `CockpitStore` — static class with four methods:
  - `get(): Cockpit | null` — reads and JSON-parses `localStorage` entry; returns `null` on missing or malformed data
  - `save(cockpit: Cockpit | null | undefined): void` — serializes and writes to `localStorage`; removes the entry if `cockpit` is falsy
  - `clear(): void` — removes the `localStorage` entry
  - `isEnabled(): boolean` — returns `true` when a stored cockpit exists

**Patterns:**

- `window.GamutCockpit` global — registered at module load time (guarded by `typeof window !== 'undefined'`); calls `CockpitStore.save`/`clear` then `window.location.reload()` to apply the change
