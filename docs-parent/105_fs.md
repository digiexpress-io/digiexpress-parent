_last updated: 13.10.2026_

# fs Domain — File Explorer

## Purpose

The `fs` domain is the worker-facing File Explorer for eveli: it fetches, displays, and mutates a hierarchical tree of typed content assets (articles, flows, decision tables, Dialob forms, printouts, locales, links, folders, and more) via a persistence-unit interface, and exposes a multi-tab editing shell with per-asset form views, a secondary explorer panel, real-time unsaved-change tracking, and search with type/label filtering.

## Packages

- **`@dxs-ts/fs-api`** — Domain types (`Fs` namespace), two React context trees (`FsDirentProvider`/`FsDirentBodyProvider` and `useFsu`), the in-memory world models (`FsWorld`, `FsuWorld`), and the `FsuChange`/`FsuCreateChange` interfaces that form the contract between UI and backend.
- **`@dxs-ts/fs-nav`** — TanStack Router integration: `FsNavProvider` syncs tree-expansion state into URL search params; `useFsRouteNav` manages the open-tab list, active tab, and tab manipulation (open, close, close-right, close-others) through URL search.
- **`@dxs-ts/fs-composer`** — All React components and styling: the top-level `FsSetup.Main`/`FsSetup.Secondary` entry points, `FsExplorer` tree, `FsTabs` bar, `FsMain` content+panel layout, per-asset widget registry (`WidgetFactory`), `FsSearchProvider`, form-field utilities, and the `FsColors`/`FsIcons`/`FsIcon` theme primitives.

## Dependencies

### `@dxs-ts/fs-api`

**peerDependencies:**
- `react`
- `@mui/material`
- `@mui/utils`
- `react-intl`
- `notistack`
- `@tanstack/react-store`

### `@dxs-ts/fs-nav`

**peerDependencies:**
- `react`
- `@tanstack/react-router`
- `@dxs-ts/fs-api`

### `@dxs-ts/fs-composer`

**dependencies:**
- `@dxs-ts/eveli-primitives`
- `@dxs-ts/eveli-api`
- `@dxs-ts/envir-fetch`
- `@dxs-ts/fs-api`
- `@dxs-ts/fs-nav`
- `@dxs-ts/xui-datetime`

**peerDependencies:**
- `react`
- `react-intl`
- `@mui/material`
- `@mui/icons-material`
- `@mui/utils`
- `file-saver`
- `yaml`
- `react-markdown`
- `@monaco-editor/react`
- `monaco-editor`
- `@uiw/react-md-editor`
- `@xyflow/react`
- `elkjs`
- `luxon`
- `notistack`

## Module Relationships

- **`@dxs-ts/fs-api`** — provides `Fs` namespace types, `useFsDirent` (dirent CRUD and body fetch), `useFsu` / `useFsuChange` / `useFsuIsChanged` (unsaved-change store), `FsDirentProvider`, `FsDirentBodyProvider`, `FsWorld`, and `FsuWorld`.
- **`@dxs-ts/fs-nav`** — consumed by `fs-composer` components and per-asset owner-state hooks to read `openTabs`, `activeDirent`, `activeTabPath`, and to call `openAsset`, `openCreateTab`, `closeTab`, and tree-expansion methods.
- **`@dxs-ts/eveli-primitives`** — supplies shared primitive components used inside composer widgets.
- **`@dxs-ts/eveli-api`** — provides eveli-level context or API hooks referenced by composer views.
- **`@dxs-ts/envir-fetch`** — `useFetch` hook factory that maps named REST endpoints to typed fetch functions; the consuming route wires these directly into `FsDirentProviderProps['persistenceUnit']`.
- **`@dxs-ts/xui-datetime`** — date/time input components used by dirent edit views that carry datetime fields.

## Architecture

**Stack:** React 18, TypeScript, MUI v5, TanStack Router, TanStack Store, `notistack`, `react-intl`.

**Package structure and responsibilities:**

- **`fs-api`** — Pure state and context. `FsWorld` holds the hierarchical dirent tree in two representations: a `_hi_dirents` nested array for rendering and a `_flat_dirents` id-keyed record for O(1) lookup. It also lazily computes `selectOptions` (all articles, flows, dialobs, languages, printouts, links, labels) as a memoized property. `FsuWorld` is an immutable value object holding the in-flight change map; all mutations return a new `FsuWorld` instance. `FsuProvider.tsx` holds a module-level `@tanstack/react-store` `Store<FsuWorld>` (not a React state), so change subscriptions are selector-scoped, not component-tree-scoped.
- **`fs-nav`** — Stateless routing helpers. `fs-nav-types.ts` contains all tab descriptor/search-param logic as pure functions (`mergeFsSearchParams`, `closeFsTab`, `closeAllFsTabs`, `closeTabsToTheRight`, `closeOtherFsTabs`, `parseFsSearchParams`, `toTabId`). `FsNavProvider` owns tree-expansion state in local React state, persisting it into URL search on every change. `useFsRouteNav` reads tab state from URL search params and exposes tab actions as functions that call TanStack Router `navigate`.
- **`fs-composer`** — Component assembly. Organized into sub-directories per concern: `fs-factory` (widget registry), `fs-theme` (colors + icons), `fs-setup` (top-level entry), `fs-explorer` (tree panel), `fs-main` (editor + right panel), `fs-tabs` (tab bar), `fs-search` (search + filter), `fs-utilities` (reusable form fields), `fs-panel-*` (secondary view panels), `fs-dirent-*` (per-asset create/update/properties/help forms).

**Patterns:**

- **Persistence-unit injection:** `FsDirentProvider` accepts a `persistenceUnit` prop containing all backend operations as async functions. The domain never imports a fetch client directly; the consuming route (`eveli-routes`) supplies the concrete functions from `useFetch`.
- **Widget registry:** `WidgetFactory` maps each `Fs.BodyType` to a `DirentWidget` object containing icons, colors, class names, and four view components (`CreateView`, `UpdateView`, `PropertiesView`, `HelpView`). `FsMain` uses the active widget's `supportedViews` array to enable/disable secondary panel toolbar buttons.
- **Immutable change state:** Each editable asset's in-progress edits are held as an immutable `_ChangeState` class implementing `FsuChange`. Mutations return new instances; `isDirty` compares serialized origin vs. current via `JSON.stringify`. Changes register into `FsuWorld` via `useFsuChange`, which reinitializes automatically when `treeId` changes and the change is not dirty.
- **Owner-state hooks:** Every composable component pair (tree row, tab, explorer, main panel) uses a dedicated `useOwnerState` hook to separate data derivation from render logic, and `useUtilityClasses` for MUI `generateUtilityClasses`-style class name generation with styled-component roots.

## Error Handling

API call failures (fetch, push, delete) are caught inside `FsDirentProvider._initCtx` helper functions. On success, `notistack`'s `enqueueSnackbar` displays a success variant message; on failure it displays an error variant message with the error's `.message` as cause. The `useNotify` hook (private to `FsDirentProvider.tsx`) formats both messages through `react-intl` using keys `fs.snackbar.saveSuccess`, `fs.snackbar.saveFailed`, and `fs.snackbar.deleteSuccess`/`deleteFailed`. Asset-level validation errors are modeled as `Fs.AssetError[]` on `Fs.PropsBase.errors` and exposed via `FsWorld.isChildError` to propagate error indicators up the tree. Five named error codes are documented in `fs-api/fs-error-descriptions.md` (`FS-1` through `FS-5`), covering missing translations, missing headings, missing Dialob form references, missing flow references, and disabled-mode assets. No dedicated error boundary or error UI component exists at the domain level; errors surface exclusively through snackbar notifications.

## Bootstrapping

A consuming application (in practice, `eveli-routes`) must:

1. Instantiate typed fetch functions via `useFetch` for each backend endpoint (`fs.GET`, `fs/dirents.PUT`, `fs/dirents.POST`, `fs/dirents/$id.DELETE`, etc.).
2. Assemble these into a `FsDirentProviderProps['persistenceUnit']` object.
3. Render `<FsDirentProvider persistenceUnit={...}>` as the outermost provider.
4. Inside, render `<EveliApp main={FsSetup.Main} secondary={FsSetup.Secondary} wrapper={FsNavProvider} ... />`.
5. `FsNavProvider` must be inside the TanStack Router tree for the `/secured/$locale/worker/filesystem/` route, because it calls `useSearch` and `useNavigate` hardcoded to that route path.
6. `FsSearchProvider` is rendered by `FsSetup.Secondary` internally — the host does not need to mount it explicitly.
7. The TanStack Router route must declare `validateSearch: parseFsSearchParams` to type-validate URL search params on entry.

`FsDirentBodyProvider` is a leaf-level provider rendered per-dirent inside asset editor views; the host does not wire it up — individual asset components mount it internally.

## TypeScript Extension Points

No `declare module` augmentations, interface merging, or extension records exist in the `fs-*` packages. The `Fs` namespace is a `declare namespace` inside `fs-types.ts` and is extended only by adding to that file directly. The `FsuChange` and `FsuCreateChange` interfaces in `fsu-types.ts` serve as structural extension points: each per-asset owner-state file implements a local `_ChangeState` class that satisfies `FsuChange`, allowing `useFsuChange` and `FsuWorld` to operate generically over any asset type without modification.

## Known Limitations and Gotchas

- **`FsNavProvider` hardcodes the route path.** Both `FsNavProvider.tsx` and `useFsRouteNav.ts` call `useSearch({ from: '/secured/$locale/worker/filesystem/' })` and `navigate({ from: '/secured/$locale/worker/filesystem', ... })`. Mounting `FsNavProvider` outside that specific TanStack Router route will throw a router error at runtime.
- **Folder create is disabled.** In `FsExplorer.tsx`, the "New Folder" icon button carries `disabled` unconditionally. Folder creation via the UI is not functional.
- **Dangling-asset detection is incomplete.** In `fs-panel-dirent-stats/useOwnerState.ts`, the `items` field for `Flow Tasks`, `Decision Tables`, and `Printouts` dangling groups is typed as `string[] | 'TODO'` and returns the literal string `'TODO'`. The stats panel renders "TODO" in place of actual dangling counts for those three asset types.
- **Several help content files are placeholder stubs.** `help-flow.ts`, `help-flow-task.ts`, `help-article-workflow.ts`, `help-dialob.ts`, `help-decision-table.ts`, `help-printout.ts`, `help-printout-page.ts`, and `help-printout-resource.ts` each contain only a heading with "TODO" as the body.
- **`FolderWidget` and `DecisionTableWidget` render TODO stubs.** `fs-factory/FolderWidget.tsx` and `DecisionTableWidget.tsx` return `<>TODO</>` for their default export. These widgets register in `allWidgets` and appear in the "new file" popover, but their rendered output is a text node only.
- **References panel is commented out.** In `FsMain.tsx`, the `references` secondary-view panel button is wrapped in a JSX comment block with an inline `TODO` note.
- **`getDirentName` returns the parent folder name for `ARTICLE` type.** `FsWorld.getDirentName` resolves an `ARTICLE` dirent's display name to its parent folder's `name`, not its own `name`. This is intentional (articles are named after their containing folder) but non-obvious.
- **`bodyStatment` is a typo preserved from the backend.** `Fs.WrenchAstBodyChange.bodyStatment` (missing 'e') matches the backend JSON field name. Do not correct the spelling without a coordinated backend change.
- **Module-level store.** `FsuProvider.tsx` creates a `Store<FsuWorld>` at module scope (`const store = new Store<FsuWorld>(new FsuWorld())`). This means the change store is shared across all mounted `useFsu` consumers globally, not scoped to a React subtree. Mounting multiple independent `FsDirentProvider` trees would share the same change store.

## Scope Boundaries

The `fs` domain does not implement the backend REST API, authentication, route-level code splitting, i18n message files, the eveli app shell layout (`EveliApp`, `EveliShell`), or the Dialob form rendering engine. It does not manage tasks, queues, messaging, or feedback — those belong to other eveli domain packages. The domain does not own printout compilation logic; it delegates `compilePrintoutPage` to the persistence unit injected by the host.

## Fit Within Eveli

The `fs` domain is mounted at the `/secured/$locale/worker/filesystem/` route by `eveli-routes`. It is one of several worker-facing domain routes (alongside task management, process, feedback, etc.) assembled by `eveli-routes` into the full worker application. `eveli-api` is a dependency of `fs-composer`, meaning the fs domain can read eveli-level context (e.g., user session, tenant). The `envir-fetch` package supplies the named REST endpoint definitions that wire the domain to the `worker/rest/api/assets/` backend paths. `lib-eveli` re-exports all three `fs-*` packages as part of the eveli umbrella library, making them available to any host that depends on `@dxs-ts/eveli`. The `demo-app-eveli` application also depends on all three packages directly, suggesting the fs domain is demonstrated and tested in isolation outside the full production stack.
