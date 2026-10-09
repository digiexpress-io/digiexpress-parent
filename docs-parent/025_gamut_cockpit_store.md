# gamut-cockpit-store

## Purpose

Persists and retrieves a `Cockpit` selection in `localStorage` under the key `gamut_cockpit`. Used to enable or disable a cockpit context (identified by `id`, `aliasName`, `aliasDesc`) across page reloads. Also exposes a `window.GamutCockpit` console API (`enable`, `disable`, `status`) for manual toggling in browser DevTools.

## Dependencies

No `dependencies` or `peerDependencies` declared. Foundation layer with no internal dependencies.

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

`gamut-cockpit-store` is the only foundation-layer module that affects both the theme layer and the API/query layer. Calling `CockpitStore.save()` or `CockpitStore.clear()` followed by `window.location.reload()` (as `window.GamutCockpit` does) causes `GThemeProvider` to reinitialise the secondary theme and all TanStack Query keys scoped to `cockpitId` to invalidate and refetch.

## Frontend Architecture

**Stack:** TypeScript, browser `localStorage` API

**Key exports (`CockpitStore.ts`):**

- `Cockpit` — interface with fields `id: string`, `aliasName: string`, `aliasDesc: string`
- `CockpitStore` — static class with four methods:
  - `get(): Cockpit | null` — parses `localStorage` entry; returns `null` on missing or malformed data
  - `save(cockpit: Cockpit | null | undefined): void` — writes to `localStorage`; removes the entry if `cockpit` is falsy
  - `clear(): void` — removes the `localStorage` entry
  - `isEnabled(): boolean` — returns `true` when a stored cockpit exists

**Patterns:**

- `window.GamutCockpit` global — registered at module load time (guarded by `typeof window !== 'undefined'`); calls `CockpitStore.save`/`clear` then `window.location.reload()` to apply the change

## Error Handling

`CockpitStore.get()` wraps `JSON.parse` in a `try/catch` block. A malformed `localStorage` entry under the key `gamut_cockpit` is silently swallowed and `null` is returned. No error is thrown, logged, or surfaced to the caller. This module contains no React components or hooks, so there are no loading states, validation error types, or React error boundaries.

## Bootstrapping

None — leaf node. The module registers `window.GamutCockpit` automatically at import time (guarded by `typeof window !== 'undefined'`). No provider, context, or configuration step is required. Consumers call `CockpitStore` methods directly.

## TypeScript Extension Points

None identified. The module declares no `declare module` augmentations, no interface merging, and no extension records. `window.GamutCockpit` is assigned via a cast to `any` (`(window as any).GamutCockpit`) rather than through a `declare global` interface extension.

## Known Limitations and Gotchas

No inline `NOTE`, `TODO`, `FIXME`, or `HACK` comments are present in the source. No `.md` files exist inside the module directory. The following constraints are notable from reading the code:

- **Silent parse failure**: `CockpitStore.get()` returns `null` for both a missing entry and a malformed entry. Callers cannot distinguish between "no cockpit set" and "corrupted storage."
- **No schema validation**: `CockpitStore.get()` casts the parsed JSON directly to `Cockpit` without checking that `id`, `aliasName`, or `aliasDesc` are present. A partial or wrongly-shaped object in `localStorage` will be returned as a `Cockpit` instance.
- **Reload on every console toggle**: `window.GamutCockpit.enable` and `window.GamutCockpit.disable` both call `window.location.reload()` unconditionally, which discards all in-memory application state.
- **`window as any` cast**: The `window.GamutCockpit` registration uses `(window as any)` instead of a `declare global` interface, meaning TypeScript provides no type-checking or autocompletion for the console API.
- **`package.json` version placeholder**: The `version` field is `"${project.version}"` — a Maven-style token that must be resolved by the build system before publishing. The raw file is not a valid `package.json` for direct `npm` consumption.

## Scope Boundaries

`gamut-cockpit-store` does not manage cockpit lifecycle beyond persistence. It does not fetch cockpit data from any API, validate that a stored cockpit ID exists in any backend catalog, react to storage events from other browser tabs, emit any events or observables, or provide any React components, hooks, or context providers. All of that responsibility lies in consuming modules such as `@dxs-ts/gamut-api`, `@dxs-ts/gamut-theme`, and `@dxs-ts/gamut-routes`.
