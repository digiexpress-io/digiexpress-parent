_last updated: 13.10.2026_

# fs-dirent-article

## Purpose

Implements the create, update, properties, and help views for the `ARTICLE` dirent type within the `@dxs-ts/fs-composer` editor shell. An article is a portal site-hierarchy node that can contain article pages, links, and workflows, and can be nested inside folders or other articles.

## Dependencies

This sub-module does not have its own `package.json`; it is part of `@dxs-ts/fs-composer` and inherits its dependencies. Relevant packages consumed within this sub-module:

**From `@dxs-ts/fs-composer` dependencies:**
- `@dxs-ts/fs-api` — `Fs` type namespace, `useFsDirent`, `useFsu`, `useFsuChange`, `FsuChange`, `FsuCreateChange`
- `@dxs-ts/fs-nav` — `useFsNav`
- `@mui/material` — `Typography`, `Button`, `Stack`, `Box`
- `@mui/utils` — `composeClasses`
- `react-intl` — `useIntl`

## Module Relationships

- `@dxs-ts/fs-api` — supplies `Fs.ArticleProps`, `Fs.LinkProps`, `Fs.WorkflowProps`, `Fs.ConfigOption`, `Fs.BodyType`, `Fs.DirentBase`; context hooks `useFsDirent` (for `getDirent`, `getDirentName`, `getParentDirent`, `selectOptions`, `createDirent`) and `useFsu`/`useFsuChange` for reading and committing unsaved changes.
- `@dxs-ts/fs-nav` — supplies `useFsNav` to read `openTabs`, `activeTabIndex`, `activeTabPath`, and to call `openAsset` after a successful create.
- `../fs-factory` — `createWidget({ type: 'ARTICLE' })` retrieves the article `DirentWidget` descriptor; its `meta.configOptions` array drives the config-option multi-select in both create and update forms.
- `../fs-utilities` — shared form field components: `FsDirentSelectMulti`, `FsDirentSelectSingle`, `FsDirentSelectGrouped`, `FsDirentTextField`, `FsDirentFormField`.
- `../fs-dirent-button-save` — `FsDirentButtonSave` action button used in the create form.
- `../fs-panel-help` — `FsHelpMarkdown` wrapper that renders the help markdown string.
- `../fs-panel-properties` — `usePanelProperties` hook supplying MUI utility class names for the properties panel layout.
- `../fs-theme` — `FsColors`, `FsIcon`, `FsIcons` for icon and color tokens used in the links list.
- `../fs-dirent-article-link/useUpdateOwnerState` — imports `createLinkFsuChange` and `LinkArticleChange` to propagate article–link association changes into the `FsuWorld` store when the user edits the links multi-select.

## Frontend Architecture

**Stack:** React, MUI Material, react-intl

**Sub-module structure:**

- `FsDirentArticleCreate` — create form: name, optional parent article, order number (numeric string), config options (multi-select). Guarded by `isDirty`; save calls `createDirent` and navigates to the new asset via `openAsset`.
- `FsDirentArticleUpdate` — update form: same fields as create, plus a links assignment field rendered as a grouped-select dialog (`FsDirentSelectGrouped`) backed by `ArticleLinkChangeState`. Displays existing associated links as a read-only chip list; a button opens the selection dialog.
- `FsPropertiesArticle` — right-panel properties view: shows enabled config options, associated links (derived from `selectOptions.direntProps` filtered by `ARTICLE_LINK` type), associated workflows (`ARTICLE_WORKFLOW`), and descendant articles collected via the local `collectDescendants` recursive function.
- `FsHelpArticle` — help view: renders the `helpArticle` markdown string through `FsHelpMarkdown`.
- `useCreateOwnerState` — local React state for the create form; wraps `_CreateState` (immutable value class implementing `FsuCreateChange`); `onSave` calls `createDirent` and then `openAsset`.
- `useUpdateOwnerState` — unsaved-change state for the update form; wraps `_ChangeState` (immutable value class implementing `FsuChange`) through `useFsuChange`; propagates link association deltas to sibling link dirents via `applyLinkArticleChange` inside a `useEffect` that runs whenever `linkState` changes.
- `ArticleLinkChangeState` — immutable value class tracking three snapshots of the linked-article set (origin, previous, current) to compute `addedFromPrevious` and `removedFromPrevious` diffs used by the `useEffect` in `useUpdateOwnerState`.
- `useUtilityClasses` / `FsDirentArticleRoot` — MUI `composeClasses` utility and the styled root `div` for the `FsDirentArticle` MUI component name. Exports `FsDirentArticleClasses` and `FsDirentArticleClassKey`.

**Patterns:**

- Immutable state machine per form: `_CreateState` and `_ChangeState` are plain classes with `with*` methods that return new instances. Dirty detection uses `JSON.stringify` comparison of origin vs. current.
- `useFsuChange` integration: the update form delegates persistence to `useFsuChange(direntId, init)`, which registers the change with the `FsuWorld` store. The store owns the commit lifecycle; the form only calls `update(prev => prev.with*(...))`.
- Cross-dirent side effects: when the user changes the link selection, `useUpdateOwnerState` writes `LinkArticleChange` records directly into the `FsuWorld` store for the affected link dirents. This keeps link–article associations bidirectionally consistent without a dedicated API call.
- `ArticleLinkChangeState` three-snapshot design: origin = server state, previous = last render's pending links, current = latest pending links. The diff between previous and current drives the `useEffect`, preventing stale re-application of already-applied changes.

## Error Handling

No error boundary, error display component, or loading spinner exists within this sub-module. Dirty-state is the only guard: `FsDirentButtonSave` is `disabled` when `isDirty` is `false`. If `createDirent` or context hooks throw, the error propagates uncaught to the nearest React error boundary above `FsSetup.Main`. Type narrowing guards (`dirent.type !== 'ARTICLE'`) cause `FsPropertiesArticle` to return `undefined` silently for mismatched dirent types.

## Bootstrapping

This sub-module is a leaf node rendered by the `DirentWidget` dispatch in `fs-factory`. A consuming application must have all `@dxs-ts/fs-composer` bootstrapping in place (see `115_fs_composer.md`):

1. `react-intl` `IntlProvider` with `fs.dirent.article.*` and `fs.dirent.configOption.*` message keys.
2. `FsDirentProvider` and `FsDirentBodyProvider` from `@dxs-ts/fs-api`.
3. `FsNavProvider` from `@dxs-ts/fs-nav`.
4. MUI `ThemeProvider`.

No additional providers are required by this sub-module beyond those already required by `@dxs-ts/fs-composer`.

## TypeScript Extension Points

No `declare module` augmentation, interface merging, or extension records are present in this sub-module. `FsDirentArticleClasses` and `FsDirentArticleClassKey` are exported for potential MUI theme override use, but no `GComponentsPropsList`-style registration pattern is used here.

## Known Limitations and Gotchas

- **`isDirty` uses `JSON.stringify` equality.** `_CreateState` and `_ChangeState` compare origin and current via `JSON.stringify`. Field ordering is assumed stable; this breaks if object keys are inserted in different orders across `with*` calls. In practice the spread operator preserves insertion order, but this is a non-obvious invariant.
- **Link association side effects fire in `useEffect`.** `useUpdateOwnerState` modifies sibling link dirent records inside a `useEffect` that depends on `state.linkState`. The effect fires after every render where `linkState` changes referentially. If the parent re-renders the component with the same `direntId` before the effect runs, the previous pending deltas may be double-applied. The `addedFromPrevious`/`removedFromPrevious` diff design mitigates this, but the window exists.
- **`initialParentId` in the update form is derived from the grandparent folder.** `useUpdateOwnerState` resolves the initial parent article by walking two levels up the tree (`getParentDirent` twice) and finding the first `ARTICLE`-type sibling of the container folder. This heuristic fails if the article has no grandparent or if the grandparent folder contains no article siblings — `initialParentId` becomes `undefined` and the parent-article field renders empty.
- **`FsDirentSelectGrouped` dialog title is hard-coded.** The string `"Article links"` in `FsDirentArticleUpdate` is not passed through `intl.formatMessage`. It is the only non-localised user-facing string in this sub-module.
- **Config option `AUTH_ONLY_MODE` is mapped in `getCurrentProps` but not present in `help-article.ts`.** The help text documents only `DEV_MODE`. `AUTH_ONLY_MODE` is available in the form but undocumented to the end-user.

## Scope Boundaries

This sub-module renders editor views only. It does not implement article-page body editing (that is `fs-dirent-article-page`), link editing (that is `fs-dirent-article-link`), workflow editing (that is `fs-dirent-article-workflow`), the article-ordering right panel (that is `fs-panel-article-order`), or the locale-coverage overview panel (that is `fs-panel-article-locale-overview`). It contains no backend code, REST calls, or persistence logic.
