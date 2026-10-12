# fs-dirent-article-link

## Purpose

Implements the create, update, properties, and help views for the `ARTICLE_LINK` dirent type within `@dxs-ts/fs-composer`. An article link is a navigable URL entry associated with one or more articles; it can be internal, external, or a telephone number, and carries per-locale display labels.

## Dependencies

This sub-module lives inside `@dxs-ts/fs-composer` and inherits its package-level dependencies. It does not have its own `package.json`.

**Inherited `dependencies` (from `@dxs-ts/fs-composer`):**
- `@dxs-ts/eveli-primitives`
- `@dxs-ts/eveli-api`
- `@dxs-ts/envir-fetch`
- `@dxs-ts/fs-api`
- `@dxs-ts/fs-nav`
- `@dxs-ts/xui-datetime`

**Inherited `peerDependencies`:**
- `react`, `react-intl`, `@mui/material`, `@mui/icons-material`, `@mui/utils`, `@monaco-editor/react`, `@uiw/react-md-editor`, `@xyflow/react`, `file-saver`, `yaml`, `react-markdown`, `elkjs`, `luxon`, `notistack`, `monaco-editor`

## Module Relationships

- `@dxs-ts/fs-api` — supplies `Fs` type namespace (`Fs.DirentBase`, `Fs.LinkProps`, `Fs.LinkType`, `Fs.ConfigOption`, `Fs.SelectOption`, `Fs.BodyType`), context hooks `useFsDirent` (dirent lookup, select-options) and `useFsuChange` / `useFsu` (unsaved-change store), and the `FsuChange` / `FsuCreateChange` interfaces that `_ChangeState` and `_CreateState` implement.
- `@dxs-ts/fs-nav` — supplies `useFsNav` for reading `activeTabPath` (displayed as the editor title in `FsDirentArticleLinkUpdate`) and `openAsset` (called after a successful create to navigate to the new dirent).

## Frontend Architecture

**Stack:** React, MUI, react-intl, `@mui/utils/composeClasses`

**Sub-module structure:**
- `FsDirentArticleLinkCreate` — form component for creating a new article-link dirent; uses `useCreateOwnerState` for local React state; calls `createDirent` on save and navigates to the new asset via `openAsset`.
- `FsDirentArticleLinkUpdate` — form component for editing an existing article-link dirent identified by `direntId`; uses `useUpdateOwnerState` which writes into the TanStack Store-backed unsaved-change layer via `useFsuChange`.
- `FsPropertiesLink` — read-only properties panel that renders URL value, content type, enabled config options, locales, and linked articles for a given `direntId`.
- `FsHelpArticleLink` — renders the `helpArticleLink` markdown string via `FsHelpMarkdown`; the `direntId` prop is accepted but unused.
- `useCreateOwnerState` — manages create form state with an immutable `_CreateState` class (implements `FsuCreateChange`); tracks a `localeEntries` array that auto-appends a blank row when the last entry is completed.
- `useUpdateOwnerState` — manages update form state with an immutable `_ChangeState` class (implements `FsuChange`); initialises from the existing `Fs.LinkProps` on the dirent and persists changes through `useFsuChange`.
- `useUtilityClasses` / `FsDirentArticleLinkRoot` — MUI utility-class generation and a single styled `div` root for both create and update forms; component name is `FsDirentArticleLink`.

**Patterns:**
- Immutable state machine: both `_CreateState` and `_ChangeState` are plain classes whose `with*` methods return new instances. `isDirty` is computed by JSON-stringifying origin vs. current.
- Locale entry list with auto-grow: `localeEntries` is a local `React.useState` array separate from the `FsuChange` state. Entries are synced into the change state via `syncLabelsToState`, which filters out incomplete rows before writing. A blank trailing row is appended automatically when the last entry is fully filled and unused locales remain.
- Label resolution: locale codes stored in `Fs.LinkProps.intlValues` are dirent IDs (UUIDs); the UI layer resolves them to human-readable language labels via `selectOptions.languages` at render time, converting back to IDs on write.

## Error Handling

No dedicated error UI exists. The save button in `FsDirentArticleLinkCreate` is disabled when `!isDirty || isLocaleEntryIncomplete`; `isLocaleEntryIncomplete` is true when any locale entry has a code but an empty label. `FsDirentArticleLinkUpdate` has no save button — saves are triggered upstream by the `fs-composer` shell. Async errors from `createDirent` in `onSave` are not caught locally; they propagate to the caller. `FsPropertiesLink` returns `undefined` (renders nothing) when the dirent is absent or not of type `ARTICLE_LINK`.

## Bootstrapping

This sub-module is consumed exclusively through `ArticleLinkWidget` in `fs-factory/ArticleLinkWidget.tsx`, which registers the four view components (`CreateView`, `UpdateView`, `PropertiesView`, `HelpView`) into the `DirentWidget` factory. The factory is invoked by `@dxs-ts/fs-composer`'s `DirentWidget` dispatch system. No additional provider setup is required beyond what `@dxs-ts/fs-composer` already mandates (i.e., `FsDirentProvider`, `FsDirentBodyProvider`, and `FsNavProvider` in the host application, plus an `IntlProvider` for react-intl).

## TypeScript Extension Points

No `declare module` augmentation, interface merging, or extension records are defined in this sub-module. `FsDirentArticleLinkClasses` and `FsDirentArticleLinkClassKey` are exported from `useUtilityClasses.ts` for MUI theme-override use, but no global augmentation wires them into a theme interface.

## Known Limitations and Gotchas

- **`createLinkFsuChange` is a public cross-module export.** `fs-dirent-article/useUpdateOwnerState.ts` imports `createLinkFsuChange` and `LinkArticleChange` directly from `../fs-dirent-article-link/useUpdateOwnerState` (bypassing the barrel). This function constructs a `_ChangeState` from a dirent and an articles array so that the article editor can drive link state for co-editing. The coupling is intentional but creates a hidden dependency between `fs-dirent-article` and `fs-dirent-article-link` internals.
- **`localeEntries` is local React state, not persisted in `FsuChange`.** If the component unmounts and remounts (e.g., tab switch in the editor shell), the `localeEntries` array is re-initialised from `linkProps.intlValues`, which means any partially entered locale row that was not yet committed is lost.
- **`FsHelpArticleLink` ignores its `direntId` prop.** The prop is declared (`direntId: string`) and accepted by the component signature, but the help content is static markdown — the prop has no effect.
- **`devMode` config option is mapped asymmetrically.** On write, `DEV_MODE` in `configOptions` is serialised as `devMode: true` in `changes` (or `undefined` if absent). The read path reconstructs `configOptions` from `linkProps.configOptions`, not from `linkProps.devMode`. These two representations must remain in sync with the backend.
- **`treeId` is accessed with a non-null assertion.** `dirent?.commitIndex?.treeId!` in `useUpdateOwnerState` will be `undefined` at runtime if the dirent has no commit index, silently producing an invalid change state.

## Scope Boundaries

This sub-module does not handle article content editing, article ordering, locale overview, history, or references — those views are managed by other panels in `@dxs-ts/fs-composer`. It does not persist changes to the backend directly; persistence is delegated to `@dxs-ts/fs-api`'s `createDirent` (create path) and the `FsuChange` store flush mechanism (update path). It does not render a standalone page or route; it is always embedded inside the `fs-composer` editor shell.
