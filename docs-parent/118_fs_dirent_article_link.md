_last updated: 13.10.2026_

# fs-dirent-article-link

## Purpose

Implements the create, update, properties, and help views for the `ARTICLE_LINK` dirent type within `@dxs-ts/fs-composer`. An article link is a navigable URL entry associated with one or more articles; it can be internal, external, or a telephone number, and carries per-locale display labels.

## Quick Navigation

| Concept | File | Key Exports |
|---------|------|-------------|
| Create form component | `fs-dirent-article-link/FsDirentArticleLinkCreate.tsx` | `FsDirentArticleLinkCreate` |
| Update form component | `fs-dirent-article-link/FsDirentArticleLinkUpdate.tsx` | `FsDirentArticleLinkUpdate` |
| Properties panel | `fs-dirent-article-link/FsPropertiesLink.tsx` | `FsPropertiesLink` |
| Help panel | `fs-dirent-article-link/FsHelpArticleLink.tsx` | `FsHelpArticleLink` |
| Shared props interface | `fs-dirent-article-link/FsDirentArticleLinkProps.ts` | `FsDirentArticleLinkProps` |
| Create form state hook | `fs-dirent-article-link/useCreateOwnerState.ts` | `useCreateOwnerState`, `CreateOwnerState`, `LocaleEntry` |
| Update form state hook | `fs-dirent-article-link/useUpdateOwnerState.ts` | `useUpdateOwnerState`, `UpdateOwnerState`, `createLinkFsuChange`, `LinkArticleChange`, `LocaleEntry` |
| MUI styling utilities | `fs-dirent-article-link/useUtilityClasses.ts` | `useUtilityClasses`, `FsDirentArticleLinkRoot`, `FsDirentArticleLinkClasses`, `FsDirentArticleLinkClassKey` |
| Help content string | `fs-dirent-article-link/help-article-link.ts` | (default export: markdown string) |
| Barrel | `fs-dirent-article-link/index.ts` | re-exports all public symbols above |

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

## Common Tasks

### Add a new locale label to a link (create path)

- `fs-dirent-article-link/useCreateOwnerState.ts` — no code changes needed; locale rows auto-grow via `onChangeLocaleEntry` / `onChangeLabelEntry`. To change how locale codes are resolved to dirent IDs, update `syncLabelsToState`.
- `fs-dirent-article-link/FsDirentArticleLinkCreate.tsx` — if the locale row UI needs a new field, add it here and wire it through `CreateOwnerState`.
- Locale options come from `selectOptions.languages` (supplied by `useFsDirent` from `@dxs-ts/fs-api`); no changes to this module are needed to support additional locales already present in that list.

### Add a new locale label to a link (update path)

- `fs-dirent-article-link/useUpdateOwnerState.ts` — same auto-grow logic as the create path; changes flow through `onChangeLocaleEntry` / `onChangeLabelEntry` → `syncLabelsToState` → `_ChangeState.withAllLabels`. To change ID-to-label resolution on read, update the `localeEntries` initialiser (the `React.useState` lazy init).
- `fs-dirent-article-link/FsDirentArticleLinkUpdate.tsx` — update the UI rendering if a new locale-row field is required.

### Change the link type options

- `fs-dirent-article-link/useCreateOwnerState.ts` — `onChangeContentType` calls `_CreateState.withContentType`; the allowed values are constrained by `Fs.LinkType` in `@dxs-ts/fs-api`. To add or remove a type, update `Fs.LinkType` in `@dxs-ts/fs-api` and then update any switch/map in `FsDirentArticleLinkCreate.tsx` that renders the type selector.
- `fs-dirent-article-link/useUpdateOwnerState.ts` — `onChangeContentType` mirrors the create path; update `_ChangeState.withContentType` if validation logic is added.
- `fs-dirent-article-link/FsDirentArticleLinkCreate.tsx` and `FsDirentArticleLinkUpdate.tsx` — update the rendered link-type select options to match any new `Fs.LinkType` values.

## Known Limitations and Gotchas

- **`createLinkFsuChange` is a public cross-module export.** `fs-dirent-article/useUpdateOwnerState.ts` imports `createLinkFsuChange` and `LinkArticleChange` directly from `../fs-dirent-article-link/useUpdateOwnerState` (bypassing the barrel). This function constructs a `_ChangeState` from a dirent and an articles array so that the article editor can drive link state for co-editing. The coupling is intentional but creates a hidden dependency between `fs-dirent-article` and `fs-dirent-article-link` internals.
- **`localeEntries` is local React state, not persisted in `FsuChange`.** If the component unmounts and remounts (e.g., tab switch in the editor shell), the `localeEntries` array is re-initialised from `linkProps.intlValues`, which means any partially entered locale row that was not yet committed is lost.
- **`FsHelpArticleLink` ignores its `direntId` prop.** The prop is declared (`direntId: string`) and accepted by the component signature, but the help content is static markdown — the prop has no effect.
- **`devMode` config option is mapped asymmetrically.** On write, `DEV_MODE` in `configOptions` is serialised as `devMode: true` in `changes` (or `undefined` if absent). The read path reconstructs `configOptions` from `linkProps.configOptions`, not from `linkProps.devMode`. These two representations must remain in sync with the backend.
- **`treeId` is accessed with a non-null assertion.** `dirent?.commitIndex?.treeId!` in `useUpdateOwnerState` will be `undefined` at runtime if the dirent has no commit index, silently producing an invalid change state.

## Open Questions

| Item | Current State | Risk | Action Needed |
|------|--------------|------|---------------|
| `createLinkFsuChange` is a public cross-module export | `fs-dirent-article/useUpdateOwnerState.ts` imports `createLinkFsuChange` and `LinkArticleChange` directly from `../fs-dirent-article-link/useUpdateOwnerState`, bypassing the barrel | Hidden inter-module coupling; a rename or refactor of `_ChangeState` internals silently breaks `fs-dirent-article` | Decide whether to formalise the coupling (add to barrel, document as stable API) or decouple via a shared interface in `@dxs-ts/fs-api` |
| `localeEntries` is local React state, not persisted in `FsuChange` | Re-mounting the component (e.g., tab switch) re-initialises `localeEntries` from `linkProps.intlValues`, discarding any partially entered row | In-progress locale edits are silently lost on tab switch | Persist the in-progress locale row in `FsuChange`, or warn the user before navigation |
| `FsHelpArticleLink` ignores its `direntId` prop | The prop is declared and accepted but has no effect; help content is static markdown | API contract is misleading; callers may expect dynamic help | Remove the prop from the signature, or implement dynamic help keyed by `direntId` |
| `devMode` config option is mapped asymmetrically | On write, `DEV_MODE` is serialised as `devMode: true` in `changes`; on read, `configOptions` is reconstructed from `linkProps.configOptions` | If the backend diverges from this dual representation, `devMode` state will silently desync | Align serialisation with the backend contract and add a test that round-trips the `devMode` flag |
| `treeId` accessed with a non-null assertion | `dirent?.commitIndex?.treeId!` in `useUpdateOwnerState.ts` produces `undefined` at runtime when the dirent has no commit index | Invalid change state is silently constructed; downstream save calls may fail or corrupt data | Guard the assertion with an explicit runtime check and surface a clear error if `treeId` is missing |

## Scope Boundaries

This sub-module does not handle article content editing, article ordering, locale overview, history, or references — those views are managed by other panels in `@dxs-ts/fs-composer`. It does not persist changes to the backend directly; persistence is delegated to `@dxs-ts/fs-api`'s `createDirent` (create path) and the `FsuChange` store flush mechanism (update path). It does not render a standalone page or route; it is always embedded inside the `fs-composer` editor shell.
