_last updated: 13.10.2026_

# fs-composer

## Purpose

React component library that implements the full file-system editor shell for the `fs` domain. Provides a multi-tab editor layout, a hierarchical dirent explorer with search and filtering, per-dirent-type create/update/properties/help views, a switchable right-side panel system (properties, debug, errors, preview, history, help, stats, article-order, article-locale-overview, changes), and shared utility form fields. Contains no backend code.

## Dependencies

**dependencies:**

- `@dxs-ts/eveli-primitives` — MUI primitive components (`CancelButton`, `ConfirmDialog`, `Burger.*`)
- `@dxs-ts/eveli-api` — Dialob form integration (`DialobFormsProvider`, `useDialobForms`)
- `@dxs-ts/envir-fetch` — HTTP fetch hook (`useFetch`) for data loading within editor views
- `@dxs-ts/fs-api` — core `Fs` type namespace, all context hooks (`useFsDirent`, `useFsDirentBody`, `useFsu`, `useFsuChange`, `useFsuIsChanged`, `useFsuValue`), `FsDirentBodyProvider`, change tracking types
- `@dxs-ts/fs-nav` — tab navigation (`useFsNav`, `FsTab`) for opening/closing/switching editor tabs
- `@dxs-ts/xui-datetime` — `DatePicker` input component

**peerDependencies:**

- `file-saver`
- `yaml`
- `react-markdown`
- `react`
- `react-intl`
- `@monaco-editor/react`
- `@mui/material`
- `@mui/icons-material`
- `@mui/utils`
- `@uiw/react-md-editor`
- `@xyflow/react`
- `elkjs`
- `luxon`
- `notistack`
- `monaco-editor`

## Module Relationships

- `@dxs-ts/fs-api` — supplies the `Fs` type namespace (dirent types, body types, props shapes, change records); sub-modules consume `useFsDirent`, `useFsDirentBody`, `useFsu`, `useFsuChange`, `useFsuIsChanged`, and `useFsuValue` directly to read and mutate state.
- `@dxs-ts/fs-nav` — supplies `useFsNav` (active dirent, open tabs, `openCreateTab`, `isExpanded`) and the `FsTab` type; drives the tab bar and explorer open-asset behavior.
- `@dxs-ts/eveli-api` — supplies `DialobFormsProvider` and `useDialobForms`; `fs-dirent-dialob` consumes these to load and display Dialob form definitions.
- `@dxs-ts/eveli-primitives` — supplies `CancelButton`, `ConfirmDialog`, and the `Burger.*` namespace for action primitives used in dirent edit views.
- `@dxs-ts/envir-fetch` — supplies `useFetch` for loading external data (e.g. referenced assets) inside editor views.
- `@dxs-ts/xui-datetime` — supplies `DatePicker`; used in date-bearing dirent edit forms.

## Frontend Architecture

**Stack:** React, MUI Material, react-intl, Monaco Editor, `@uiw/react-md-editor`, `@xyflow/react` + elkjs (flow graph layout), notistack, file-saver, yaml, react-markdown, luxon

**Sub-module structure:**

- `fs-setup` — top-level layout entry points: `FsSetup.Main` (full-height tab+editor shell) and `FsSetup.Secondary` (explorer sidebar wrapped in `FsSearchProvider`)
- `fs-main` — `FsMain` component: left editor area + collapsible right panel + vertical toolbar; delegates content to `ContentPanel` (panel router) and dirent widget views
- `fs-factory` — `WidgetFactory`: `createWidget(dirent)` and `createIconWidget(dirent)` map `Fs.BodyType` values to `DirentWidget` descriptor objects; `allWidgets` array used for "new file" menus
- `fs-explorer` — `FsExplorer`: hierarchical dirent tree list with search toggle, new-file popover, collapse-all, and context menu; recurses via `FsDirent`
- `fs-tabs` — `FsTabs`: horizontal tab bar driven by `useFsNav`; supports right-click context menu via `FsTabMenu`
- `fs-tab-menu` — context menu for tab operations (close, close others, etc.)
- `fs-panel` — `FsPanel`: generic titled panel wrapper used by all right-side panel sub-modules
- `fs-search` — `FsSearch` + `FsSearchProvider` + `useFsSearch`: search bar with asset-type and label filter chips; `FsSearchProvider` is the context; `useFsSearch` is the consumer hook
- `fs-theme` — `FsColors` (color palette constant), `FsIcons` (icon registry), `FsIcon` (sized icon wrapper)
- `fs-dirent` — `FsDirent`: recursive tree node component; renders icon (via `createIconWidget`), name with search highlight, unsaved-change indicator (`FsDiffIndicator`), config-option icons
- `fs-diff-indicator` — `FsDiffIndicator`: visual marker shown on a dirent when it has unsaved changes
- `fs-dirent-loader` — `FsDirentLoader`: `CircularProgress` spinner shown while dirent body is loading
- `fs-dirent-menu` — `FsDirentMenu`: right-click context menu for dirent tree actions (rename, copy, delete, etc.)
- `fs-dirent-menu-new` — menu for creating new dirents of a chosen type
- `fs-dirent-rename` — rename dialog/form for a dirent
- `fs-dirent-copy` — copy dialog/form for a dirent
- `fs-dirent-description` — description field editor used inside dirent property forms
- `fs-dirent-labels` — label multi-select field used inside dirent property forms
- `fs-dirent-comments` — comments panel for a dirent
- `fs-dirent-references` — references display (what references this dirent)
- `fs-dirent-textfield-autocomplete` — autocomplete text field backed by dirent name data
- `fs-dirent-article` — article create/update/properties/help views
- `fs-dirent-article-link` — article-link create/update/properties/help views
- `fs-dirent-article-page` — article-page create/update/properties/help views
- `fs-dirent-article-template` — article-template create/update/properties/help views
- `fs-dirent-article-workflow` — article-workflow create/update/properties/help views
- `fs-dirent-decision-table` — decision-table create/update/properties/help views; includes an `editors/` sub-directory with builder components and a `table/` sub-directory
- `fs-dirent-dialob` — Dialob-form create/update/properties/help views; uses `DialobFormsProvider`/`useDialobForms` from `@dxs-ts/eveli-api`
- `fs-dirent-flow` — flow create/update/properties/help views; includes `autocomplete/` and `SelectOrCreateAsset/` sub-directories
- `fs-dirent-flow-task` — flow-task create/update/properties/help views
- `fs-dirent-folder` — folder create/update/properties/help views
- `fs-dirent-locale` — locale create/update/properties/help views
- `fs-dirent-printout` — printout create/update/properties/help views
- `fs-dirent-printout-page` — printout-page create/update/properties/help views; includes `autocomplete/` sub-directory
- `fs-dirent-printout-resource` — printout-resource create/update/properties/help views
- `fs-panel-article-locale-overview` — right-panel view: locale coverage overview for articles
- `fs-panel-article-order` — right-panel view: article ordering UI
- `fs-panel-changes` — right-panel view: lists all unsaved changes with save action
- `fs-panel-debug` — right-panel view: debug output drawer and outputs sub-directories
- `fs-panel-dirent-stats` — right-panel view: asset counts by type, dangling assets, and disabled assets
- `fs-panel-errors` — right-panel view: placeholder (renders `<>errors</>` — not yet implemented)
- `fs-panel-help` — right-panel view: help text for the active dirent type
- `fs-panel-history` — right-panel view: change history for the active dirent
- `fs-panel-preview` — right-panel view: rendered preview of the active dirent
- `fs-panel-preview-wrench` — right-panel view: preview configuration tooling; includes `graph/` and `vis/` sub-directories for flow graph visualization via `@xyflow/react` + elkjs
- `fs-panel-properties` — right-panel view: property editor for the active dirent
- `fs-panel-references` — right-panel view: shows what other dirents reference the active dirent
- `fs-utilities` — shared form-field components: `FsDirentSelectMulti`, `FsDirentSelectSingle`, `FsDirentSelectGrouped`, `FsDirentTextField`, `FsDirentFormField`

**Patterns:**

- `DirentWidget` descriptor: each dirent type registers a `DirentWidget` object with `icons`, `colors`, `classNames`, `views` (CreateView, UpdateView, PropertiesView, HelpView), and `meta` (type, extension, configOptions, supportedViews). `createWidget` and `createIconWidget` in `fs-factory/WidgetFactory.tsx` dispatch on `Fs.BodyType` to return the correct descriptor. Unknown types fall back to `_UN_IMPLEMENTED` (silent no-op stubs, `DeviceUnknown` icon).
- `useOwnerState` + `useUtilityClasses` per component: each sub-module encapsulates its derived state in a `useOwnerState` hook and its MUI styled-component class names in a `useUtilityClasses` hook.
- `FsSearchProvider` / `useFsSearch` context: search state (term, filters) lives in `FsSearchProvider`; `FsExplorer` reads it via `useFsSearch`.
- Auto-preview: `useOwnerState` in `fs-main` sets `selectedView` to `'preview'` and opens the right panel automatically when the active dirent type is `ARTICLE_PAGE`, `ARTICLE_TEMPLATE`, `FLOW`, or `PRINTOUT_PAGE`.
- Panel router: `ContentPanel` in `fs-main` dispatches `Fs.SecondaryView` string values to the appropriate panel component via a `switch` statement. Panels not yet implemented for a given view render a "not yet implemented" fallback inside `FsPanel`.

## Error Handling

No dedicated error boundary or error-state type exists in this module. Loading states surface via `FsDirentLoader` (`CircularProgress`). Unsaved-change conflicts surface visually through `FsDiffIndicator` on the dirent tree node. `FsPanelErrors` renders only `<>errors</>` — the implementation is a placeholder. `useFsSearch` throws a plain `Error` if consumed outside `FsSearchProvider`. API fetch errors are not caught or displayed within this module; that responsibility belongs to `@dxs-ts/fs-api` and its context providers.

## Bootstrapping

A consuming application must provide the following in the React tree above `FsSetup.Main` and `FsSetup.Secondary`:

1. `react-intl` `IntlProvider` with `fs.*` message keys loaded.
2. All providers from `@dxs-ts/fs-api` (`FsDirentProvider`, `FsDirentBodyProvider`) — these supply `useFsDirent`, `useFsDirentBody`, `useFsu`, and related hooks.
3. `useFsNav` provider from `@dxs-ts/fs-nav` — drives tab state and the active dirent.
4. `DialobFormsProvider` from `@dxs-ts/eveli-api` if `fs-dirent-dialob` views are used.
5. `notistack` `SnackbarProvider` if notistack notifications are triggered by any dirent action.
6. MUI `ThemeProvider` with a compatible theme.

`FsSetup.Secondary` wraps `FsExplorer` in `FsSearchProvider` internally — the consumer does not need to provide it separately.

`FsSetup.Main` renders the tab bar (`FsTabs`) and `FsMain` (left editor + right panel + toolbar). `FsSetup.Secondary` renders the explorer sidebar.

## TypeScript Extension Points

No `declare module` augmentation, interface merging, or extension records (`GComponentsPropsList`-style) are present in this module. The `DirentWidget` interface in `fs-factory/WidgetFactory.tsx` is the closest analog — adding a new dirent type requires creating a new widget object satisfying `DirentWidget` and registering it in the `createWidget` switch and the `allWidgets` array, but no TypeScript module augmentation is involved.

## Known Limitations and Gotchas

- **`FsPanelErrors` is a stub.** `fs-panel-errors/FsPanelErrors.tsx` renders `<>errors</>` with no content. The panel button is present in `FsMain` and routed in `ContentPanel`, but no error data is displayed.
- **`FolderWidget` and `DecisionTableWidget` CreateView/UpdateView are stubs.** `fs-factory/FolderWidget.tsx` and `fs-factory/DecisionTableWidget.tsx` render `<>TODO</>` for their create and update views. They appear in the "new file" popover and the widget factory but are not usable.
- **Folder create is disabled in the explorer.** The "new folder" `IconButton` in `FsExplorer` has `disabled` set unconditionally; folder creation from the toolbar is not functional.
- **References panel is commented out.** `FsMain.tsx` has the `'references'` `PanelButton` commented out with a `TODO` comment. The route case in `ContentPanel` still exists and renders `FsPanelReferences`, but the button to activate it is not rendered.
- **Several help text files are unfilled stubs.** `help-flow.ts`, `help-flow-task.ts`, `help-dialob.ts`, `help-printout.ts`, `help-printout-page.ts`, `help-printout-resource.ts`, `help-article-workflow.ts`, and `help-decision-table.ts` all export a string containing only `# <TYPE> TODO`.
- **Dangling-asset detection for Flow Tasks, Decision Tables, and Printouts is incomplete.** In `fs-panel-dirent-stats/useOwnerState.ts`, the `DanglingGroup.items` field is typed `string[] | 'TODO'` and those three groups are hard-coded to `'TODO'`. They are excluded from the dangling count.
- **`createIconWidget` recurses into folder children for type inference.** When a dirent is a `FOLDER`, `createIconWidget` walks the child tree to find the first non-folder descendant and borrows its type for the icon. A deeply nested or heterogeneous folder displays the icon of whichever non-folder child is encountered first in tree order.
- **Unknown `Fs.BodyType` values fall back silently.** The `default` branch of `createWidget` returns `_UN_IMPLEMENTED` (no-op stubs, `DeviceUnknown` icon) without logging or throwing. A new dirent type added to `@dxs-ts/fs-api` without a corresponding widget registration appears in the tree with an unknown icon and empty views.

## Scope Boundaries

`@dxs-ts/fs-api` owns the `Fs` type namespace, the unsaved-change store, and the data fetching layer. `@dxs-ts/fs-nav` owns tab routing and the active-dirent concept. `@dxs-ts/eveli-api` owns Dialob form rendering and submission. This module defines no backend REST endpoints, Java services, or persistence logic.
