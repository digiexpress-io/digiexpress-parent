_last updated: 13.10.2026_

# fs-api

## Purpose

Defines the shared type namespace (`Fs`), React context providers, and change-management layer for the filesystem explorer domain. Consuming modules use this module to read dirent trees, dispatch mutations, and track in-progress edits without owning any HTTP transport or persistence logic.

## Dependencies

**dependencies:** none

**peerDependencies:**
- `react`
- `@mui/material`
- `@mui/utils`
- `react-intl`
- `notistack`
- `@tanstack/react-store`

## Module Relationships

This module imports from no `@dxs-ts/*` siblings. It is the foundational leaf for the fs domain; all fs domain packages import from it.

## Frontend Architecture

**Stack:** React, TypeScript, `@tanstack/react-store`, `notistack`, `react-intl`

**Sub-module structure:**
- `fs-types.ts` — the `Fs` namespace: `DirentBase`, `PropsBase`, all per-body-type `Props` variants, `BodyType`, `SelectOptions`, `WrenchBody`, `WrenchAstBody<T>`, YAML AST types, debug result types, primitive ID aliases (`ArticleId`, `FlowId`, etc.)
- `fs-provider/` — `FsDirentProvider` (React context + `useFsDirent` hook), `FsDirentBodyProvider` (per-dirent body loader + `useFsDirentBody` hook), `FsWorld` (in-memory dirent index with lazy `SelectOptions`, parent lookup, reference lookup)
- `fsu-provider/` — `FsuProvider.tsx` (TanStack Store-backed change layer + hooks `useFsu`, `useFsuChange`, `useFsuIsChanged`, `useFsuValue`), `FsuWorld.ts` (immutable change map), `fsu-tanstack.ts` (standalone `fsuStore` + `fsuActions` action helpers), `fsu-types.ts` (`FsuChange`, `FsuCreateChange` interfaces)

**Patterns:**
- **Persistence-unit injection** — `FsDirentProvider` receives a `persistenceUnit` prop object containing all async I/O methods (`fetchDirents`, `fetchDirentBody`, `pushChange`, `pushCreate`, `pushDescription`, `pushName`, `pushCopy`, `pushLabels`, `deleteDirent`, `compilePrintoutPage`). The provider owns no fetch logic.
- **Immutable world replacement** — `FsWorld` and `FsuWorld` treat state as immutable. Every mutation method returns a new instance; React state is updated by replacing the whole world object.
- **Lazy `SelectOptions`** — `FsWorld.selectOptions` is computed on first access and cached on the instance. The cache is invalidated when `FsDirentProvider` replaces the `FsWorld` after a backend refresh.
- **treeId-gated body load** — `FsDirentBodyProvider` re-fetches the body only when `dirent.commitIndex.treeId` changes, preventing redundant fetches on unrelated state updates.
- **Reinit guard** — `useFsuChange` calls `FsuWorld.isReinit(id, treeId)` before registering a new change. If the stored change's `treeId` is stale and the change is not dirty, the change is cleared and re-initialized from current state.
- **Dual store API** — `fsu-provider` exposes both context-free action helpers (`fsuStore`, `fsuActions` in `fsu-tanstack.ts`) and a React-hook API (`useFsu`, `useFsuChange` in `FsuProvider.tsx`). Both operate on separate module-level `Store` instances; they are not synchronized.

## Error Handling

Each async method in `FsDirentProvider` catches backend call failures and passes them to the module-local `useNotify` hook, which formats an intl message (keys: `fs.snackbar.saveSuccess`, `fs.snackbar.saveFailed`, `fs.snackbar.deleteSuccess`, `fs.snackbar.deleteFailed`) and calls `notistack`'s `enqueueSnackbar` with `variant: 'error'`. Only `copyDirent` and `createDirent` re-throw — their callers need the returned `Fs.DirentBase` to proceed.

Domain-level asset errors use `Fs.AssetError` (`code`, `severity: ErrorSeverityType`, `message`) stored on `Fs.PropsBase.errors`. Five error codes are defined in `fs-error-descriptions.md` inside the module:

| Code | Severity | Applies To |
|------|----------|------------|
| `FS-1` | `CRITICAL` | all types — missing translation |
| `FS-2` | `WARNING` | language — no level-1 heading found |
| `FS-3` | `CRITICAL` | service — no Dialob form defined |
| `FS-4` | `CRITICAL` | service — no flow defined |
| `FS-5` | `WARNING` | all types — asset in disabled mode |

`useFsDirent()` and `useFsDirentBody()` throw `'FsDirentPropsContext is not created!'` outside their respective providers. `FsuWorld.getChange` throws `'Change not created!'` on a missing id. `FsuWorld.withNewChange` throws `'Change already created!'` on a duplicate id.

## Bootstrapping

A consuming app must mount providers in this order:

1. `notistack`'s `SnackbarProvider` and an `IntlProvider` — must be above `FsDirentProvider`, as `useNotify` depends on both.
2. **`FsDirentProvider`** — receives the `persistenceUnit` prop. Calls `fetchDirents()` on mount with no arguments.
3. **`FsDirentBodyProvider`** — nested inside `FsDirentProvider`; receives `direntId`. One instance per open editor. Renders an empty fragment until the body resolves and the treeId is current.

`useFsu`, `useFsuChange`, and related hooks require no dedicated provider — they read the module-level store in `FsuProvider.tsx` directly. They do require `FsDirentProvider` because `useFsu.push` calls `useFsDirent().updateDirent`.

`fsuStore` and `fsuActions` from `fsu-tanstack.ts` are fully standalone — no provider required.

## TypeScript Extension Points

`FsuChange` is an extension base interface. Consuming packages define concrete change types that extend it and pass them as `T` to `useFsuChange<T>`, `useFsu().withChange<T>`, and `FsuWorld.withChange<T>`. `FsuCreateChange` follows the same pattern for creation payloads.

`Fs.Props` is a discriminated union of all per-body-type props interfaces, narrowed by the `type` field. Adding a new asset type requires adding a member to `Fs.BodyType`, a new props interface, and a new entry to the `Fs.Props` union in `fs-types.ts`.

No `declare module` augmentation or interface merging patterns are present.

## Known Limitations and Gotchas

**`bodyStatment` typo in `Fs.WrenchAstBodyChange`** — the field is spelled `bodyStatment` (missing an `e`). The source comment reads `// typo matches backend field name`. Renaming it requires a coordinated backend change.

**Two unsynchronized stores** — `FsuProvider.tsx` holds `const store = new Store<FsuWorld>(new FsuWorld())` and `fsu-tanstack.ts` holds `fsuStore = new Store<FsuStoreState>({ world: new FsuWorld() })` with a different state shape. Both are module-level singletons. A consuming package must pick one and use it consistently; mixing them will result in diverged state.

**`FsDirentBodyProvider` suppresses children during load** — while `body` is undefined or the loaded `treeId` does not match the current treeId, the provider renders `<></>` with no loading indicator. Consumers see a blank slot with no feedback.

**Non-awaited `refresh()` calls** — `updateDirentDescription`, `updateDirentName`, `updateDirentLabels`, and `pushDelete` call `refresh()` without `await`. Success notification can fire before the updated dirent tree is reflected in state.

**`findReferencesToDirent` matches by name, not id** — `FsWorld.findReferencesToDirent` identifies references by comparing `dirent.name === targetDirent.name` (combined with `props.reference === true`). Dirents with identical names across different paths will appear as mutual references.

**`useChangeState` breaks rules of hooks** — `useFsu().useChangeState` calls `useSelector` inside a non-hook function body; `FsuProvider.tsx` suppresses the lint error with an `eslint-disable` comment. Calling `useChangeState` outside a React component or custom hook breaks the rules of hooks at runtime.

**`FsDirentBodyProvider` non-null assertions on `commitIndex`** — `dirent.commitIndex!.treeId` is asserted non-null in three places. A dirent without a `commitIndex` throws at runtime with no recoverable error path.

**`FsuWorld.getChange` logs to console before throwing** — `console.log("cant find change", ...)` on a cache miss appears in production builds.

## Scope Boundaries

This module does not implement HTTP clients, route definitions, UI components, editor panels, tree navigation, search, or intl message definitions. It provides types, in-memory data structures (`FsWorld`, `FsuWorld`), React context providers, and hooks only. All backend communication is delegated to the `persistenceUnit` supplied by the consuming package.
