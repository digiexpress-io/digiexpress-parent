# @dxs-ts/gamut-md

## Purpose

Renders markdown content as MUI-styled React elements. Wraps `react-markdown` with MUI `Typography` component mappings for headings, paragraphs, and list items, and applies theme-aware spacing to list structures. Provides a single component, `GMarkdown`, with an optional span root variant for inline rendering.

## Dependencies

**dependencies:**
- `@dxs-ts/gamut-api` — `*`

**peerDependencies:**
- `react` — `*`
- `react-markdown` — `*`
- `@mui/material` — `*`
- `@mui/utils` — `*`

## Module Relationships

### Depends on

- `@dxs-ts/gamut-api` — provides `GOverridableComponent`, used as the type for the `component` prop on `GMarkdownProps` to support MUI-style component overriding.

### Consumed by

- `@dxs-ts/gamut-form` — renders markdown content within form contexts; imports `GMarkdown` directly.
- `@dxs-ts/gamut-form-review` — renders markdown in read-only form review views; imports `GMarkdown` directly.
- `@dxs-ts/gamut-primitives` — `GArticle` receives a `SiteApi.TopicView` markdown body string (via `useSite()`) and passes it to `GMarkdown` for rendering.
- `@dxs-ts/gamut-routes` — listed as a direct dependency in the top-level consumer dependency table; also supplies `defaultProps` for `GMarkdown` via the MUI theme (`GThemeProvider` in `gamut-theme` configures `GMarkdown` component defaults through `GComponentsPropsList`).

## Frontend Architecture

**Stack:** React, MUI Material, MUI Utils, react-markdown

**Sub-module structure:**
- `g-md` — contains `GMarkdown.tsx` (component) and `useUtilityClasses.tsx` (styled roots, class generation, default styles)

**Patterns:**
- MUI theme integration via `useThemeProps` with slot name `GMarkdown`, enabling theme-level prop and style overrides.
- Two styled roots: `GMarkdownRootDiv` (`styled("div")`) and `GMarkdownRootSpan` (`styled("span")`), selected at render time based on the `span` prop.
- `useUtilityClasses` follows the standard MUI utility class pattern using `generateUtilityClass` and `composeClasses` from `@mui/utils`.
- `GMarkdownProps.overrides` accepts a `react-markdown` `Components` map, allowing per-call override of individual element renderers on top of the default MUI mappings.
- Default element mappings: `h1`–`h5` → `Typography` with matching variant; `h6` and `p` → `Typography variant="body1"`; `li` → `<li>` wrapping `Typography variant="body1"`; `a` → `linkRenderer` (opens in new tab with `rel="noreferrer"`).

## Error Handling

None identified. `GMarkdown` has no error boundary, no loading state, and no validation logic. When `children` is `undefined`, `ReactMarkdown` receives the literal fallback string `"no children to render"` — this is the only runtime guard. API failures and loading states are the responsibility of the consuming component.

## Bootstrapping

None — leaf node. `GMarkdown` reads theme props via `useThemeProps` with slot name `GMarkdown`, so any theme-level `defaultProps` or `styleOverrides` configured for `GMarkdown` in `GComponentsPropsList` (in `gamut-theme`) take effect automatically. No provider, context, or initialization call is required from the consuming app beyond having an MUI theme in scope.

## TypeScript Extension Points

None identified within this module. `GMarkdownClasses` (`root: string`) and `GMarkdownClassKey` are exported for theme-side use, and `gamut-theme` registers `GMarkdown` in its `INTERNAL_TYPE_Props`, `INTERNAL_TYPE_Overrides`, and `INTERNAL_TYPE_Variants` maps (in `components-g.tsx`) to enable typed `defaultProps` and `styleOverrides`. No `declare module` augmentation originates from `gamut-md` itself.

## Known Limitations and Gotchas

- **`remarkPlugins` typed as `any[]`**: `GMarkdownProps.remarkPlugins` is typed `any[] | undefined`. No type safety is enforced on plugin shape; incorrect plugins fail silently at runtime.
- **`span` root uses a different MUI slot name**: `GMarkdownRootSpan` registers under the slot name `GMarkdownSpan` (not `GMarkdown`), so `styleOverrides` targeting `GMarkdown` in the theme do not apply to the span variant.
- **`h6` mapped to `body1`**: `h6` renders as `Typography variant="body1"`, not `variant="h6"`. Callers expecting a distinct heading style for `h6` will not get one without an explicit `overrides` entry.
- **Firefox `li > p` margin fix**: Commit `cb565594b` (2026-08-31) added `'li > p': { margin: 0 }` to `_defaultStyles` to correct a Firefox-specific rendering issue where `react-markdown` wraps list item text in a `<p>` tag, adding unwanted vertical margin.

## Scope Boundaries

`gamut-md` renders markdown strings only. It does not fetch or load markdown content, manage routing, handle user input, provide a markdown editor, validate markdown syntax, or apply syntax highlighting to code blocks. All data fetching, state management, and content sourcing are outside its scope and belong to consuming modules.
