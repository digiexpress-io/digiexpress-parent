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
