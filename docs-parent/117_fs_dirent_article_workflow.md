_last updated: 13.10.2026_

# fs-dirent-article-workflow

## Purpose

Sub-module of `@dxs-ts/fs-composer` that implements the four standard dirent views — create, update, properties, and help — for the `ARTICLE_WORKFLOW` asset type. An article-workflow binds a Dialob form (with a specific tagged version), a process flow, a set of portal articles, locale labels, a validity date range, and a set of configuration flags (`DISABLED_MODE`, `DEV_MODE`, `ANONYMOUS_MODE`, `ASSIGNABLE_MODE`, `IN_HOUSE_MODE`, `RPS_MODE`). Contains no backend code.

## Dependencies

This sub-module is not a standalone package. It ships inside `@dxs-ts/fs-composer` and inherits all of its parent's dependencies and peerDependencies. For the full list see `114_fs_composer_modules.md`.

Direct internal imports used by this sub-module:

- `@dxs-ts/fs-api` — `Fs` type namespace, `useFsDirent`, `useFsuChange`, `FsuChange`, `FsuCreateChange`
- `@dxs-ts/fs-nav` — `useFsNav`
- `@dxs-ts/xui-datetime` — `DatePicker`
- `../fs-factory` — `createWidget` (to resolve `ARTICLE_WORKFLOW` config options)
- `../fs-utilities` — `FsDirentFormField`, `FsDirentSelectMulti`, `FsDirentSelectSingle`, `FsDirentTextField`
- `../fs-dirent-button-save` — `FsDirentButtonSave`
- `../fs-panel-properties` — `usePanelProperties` (class names for the properties panel)
- `../fs-panel-help` — `FsHelpMarkdown`
- `../fs-theme` — `FsColors`

## Module Relationships

- `@dxs-ts/fs-api` — supplies `Fs.WorkflowProps` (the typed props shape read from `dirent.props`), `useFsDirent` (provides `selectOptions`, `getDirent`, `getDirentName`, `createDirent`), `useFsuChange` (TanStack Store-backed change tracking for the update view), `FsuChange` (interface implemented by `_ChangeState`), and `FsuCreateChange` (interface implemented by `_CreateState`).
- `@dxs-ts/fs-nav` — supplies `useFsNav`; the create view calls `openAsset` after `createDirent` succeeds to navigate to the newly created dirent; the update view reads `activeTabPath` for the edit-view title.
- `@dxs-ts/xui-datetime` — supplies `DatePicker`; used in both create and update views for validity-start and validity-end date fields.

## Frontend Architecture

**Stack:** React, MUI Material, react-intl, luxon (`FsPropertiesWorkflow` uses `DateTime.fromISO` for Finnish locale date formatting)

**Sub-module structure:**

- `FsDirentArticleWorkflowCreate` — create form; collects name, Dialob form, Dialob tag, flow, articles, config options, per-locale labels, and validity dates; save is disabled until `isDirty && flowName && formName && formTag` are all truthy; calls `createDirent(state)` then navigates to the new asset.
- `FsDirentArticleWorkflowUpdate` — edit form; loads existing `Fs.WorkflowProps` from the dirent; all changes are tracked by `useFsuChange` (TanStack Store); no inline save button — saving is handled by the global `fs-panel-changes` save mechanism.
- `FsPropertiesWorkflow` — read-only right-panel properties view; displays config options, service name, locales, validity dates, Dialob form/tag, flow, and linked articles; guards on `dirent.type === 'ARTICLE_WORKFLOW'` before rendering.
- `FsHelpArticleWorkflow` — renders `helpArticleWorkflow` via `FsHelpMarkdown`; the content string is a stub (`# ARTICLE WORKFLOW TODO`).
- `useCreateOwnerState` — local React state (`_CreateState` class); tracks `value`, `formName`, `formTag`, `flowName`, `articles`, `intlValues`, `configOptions`, `validityStart`, `validityEnd`; implements `FsuCreateChange` for `createDirent`.
- `useUpdateOwnerState` — TanStack Store-backed state (`_ChangeState` class via `useFsuChange`); initialised from `Fs.WorkflowProps`; implements `FsuChange` for the global unsaved-change layer.
- `useUtilityClasses` — generates MUI utility class names for `FsDirentArticleWorkflowRoot` slots: `root`, `titleRow`, `title`, `formContainer`, `localeLabel`, `localeRow`, `sectionTitle`.
- `FsDirentArticleWorkflowRoot` — MUI `styled('div')` root element shared by both create and update views.

**Patterns:**

- Immutable state class with `with*` methods: both `_CreateState` and `_ChangeState` are immutable value-object classes. Every setter creates a new instance via spread, keeping the `_origin` reference stable for dirty-check comparison via `JSON.stringify`.
- Dialob tag cascade: changing `formName` in the create view automatically resets `formTag` to `''` (via `withFormName`). The tag select options are derived by calling `selectOptions.collectDialobTags(formName)` on every render, so the available tags always reflect the currently selected form.
- Config options filtering: `withConfigOptions` intersects the incoming string array with the `ARTICLE_WORKFLOW` widget's `meta.configOptions` list before storing, preventing unknown flag values from entering state.
- `getCurrentProps` serialisation: both state classes implement `getCurrentProps()`, which maps the typed state to the flat `changes` record expected by `createDirent` / `useFsuChange`. Boolean flags (`disabled`, `devMode`, `anon`, `assignable`, `inHouse`, `rps`) are serialised as `true | undefined` (falsy values are dropped).

## Error Handling

No error boundary, loading spinner, or error state type is present in this sub-module. `useFsDirent().getDirent` returns `undefined` if the dirent is not yet loaded; `FsPropertiesWorkflow` guards with an early return, but `useUpdateOwnerState` accesses `getDirent(props.direntId)!` with a non-null assertion and will throw at runtime if the dirent is missing. `FsDirentLoader` (from `fs-dirent-loader`) is rendered upstream by `FsMain` while the body is loading, so the update view is not mounted until data is available under normal flow.

## Bootstrapping

This sub-module is a leaf: it is mounted by the `DirentWidget` dispatch in `fs-factory` and `fs-main`, which are already inside `FsSetup.Main`. No additional providers are required beyond what `@dxs-ts/fs-composer` already requires (see `114_fs_composer_modules.md` Bootstrapping section). The `FsPropertiesWorkflow` component is registered in the properties panel and is instantiated by `fs-panel-properties`; no consumer wiring is needed.

## TypeScript Extension Points

No `declare module` augmentation, interface merging, or extension records are present. `FsDirentArticleWorkflowClasses` and `FsDirentArticleWorkflowClassKey` are exported and could be referenced by a consuming application for MUI theme overrides targeting `FsDirentArticleWorkflowRoot`, but no augmentation pattern for them exists in this sub-module or in `fs-composer`.

## Known Limitations and Gotchas

- **`helpArticleWorkflow` is a stub.** `help-article-workflow.ts` exports the string `# ARTICLE WORKFLOW TODO`. `FsHelpArticleWorkflow` renders this via `FsHelpMarkdown`, so the help panel for article-workflow dirents shows only the heading placeholder.
- **`onChangeName` in `useUpdateOwnerState` delegates to `withFlowName`.** The function named `onChangeName` calls `prev.withFlowName(value)` rather than `prev.withValue(value)`. The update form's name field is therefore wired to `flowName` state instead of `value`. This means editing the "name" field in the update view changes the flow name, not the dirent name; `value` (the dirent name) cannot be changed from the update view.
- **`formId` is redundant in create serialisation.** `_CreateState.getCurrentProps` writes both `formName` and `formId` with the same value (`current.formName`). This appears to be a legacy field duplication.
- **Update view has no inline save button.** Unlike the create view (which has `FsDirentButtonSave`), the update view relies entirely on the global `fs-panel-changes` save panel. Unsaved changes are tracked via `useFsuChange` and saved from that panel only.
- **`FsPropertiesWorkflow` uses Finnish locale hardcoded for date formatting.** `formatFiDate` always calls `.setLocale('fi')` regardless of the active application locale.

## Scope Boundaries

This sub-module does not own the `Fs.WorkflowProps` type definition (that is in `@dxs-ts/fs-api`), the tab routing (that is in `@dxs-ts/fs-nav`), the global save mechanism (that is in `fs-panel-changes`), or the widget registration that maps `ARTICLE_WORKFLOW` to these views (that is in `fs-factory/WidgetFactory.tsx`). It renders no backend REST endpoints or persistence logic.
