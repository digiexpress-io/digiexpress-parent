# @dxs-ts/gamut-theme

## Purpose

Provides MUI `ThemeOptions` objects and a React context provider that applies them via MUI's `ThemeProvider`. Two bundled theme option sets are included (`GThemeOptions`, `GThemeOptionsAlt1`). The provider supports runtime theme switching between a primary and any number of named secondary themes, with the initial secondary selection resolved from `CockpitStore`.

## Dependencies

**dependencies:**
- `@dxs-ts/gamut-cockpit-store` — `*`

**peerDependencies:**
- `react` — `*`
- `@mui/material` — `*`
- `@mui/system` — `*`

## Module Relationships

### Depends on

- `@dxs-ts/gamut-cockpit-store` — `GThemeProvider` calls `CockpitStore.get()` at initialisation to read the active cockpit `aliasName` and select the matching entry from the `secondaryThemeOptions` map. When `GRouterCockpits` triggers a cockpit switch via `CockpitStore.save()` and `window.location.reload()`, `GThemeProvider` re-reads the store on remount and activates the new secondary theme.

### Consumed by

- `@dxs-ts/gamut-routes` — mounts `GThemeProvider` at the application root, wrapping the entire route tree so every component below has access to the active MUI theme. Imports `GComponentsPropsList` and `GComponentNameToClassKey` (from `g-props`) to extend MUI's `Components` interface, allowing Gamut components to be configured inside a single `ThemeOptions` object.
- Components calling `useThemeProps({ props, name: MUI_NAME })` — the bundled `ThemeOptions` in `g-theme-options` and `g-theme-options-alt-1` supply `defaultProps` for `GShell`, `GMarkdown`, `GUserOverview`, `GUserOverviewMenu`, and `GArticleFeedback` via the `components` map. Each component merges these defaults at render time through MUI's `useThemeProps`.
- Components using `useGTheme()` — accesses `theme`, `themeOptionsPrimary`, `secondaryThemeOptions`, and `setThemeOptions` from `GThemeProviderContext`. `setThemeOptions(aliasName?)` switches the active secondary theme at runtime or reverts to primary when called with `undefined`.

## Frontend Architecture

**Stack:** React, MUI (`@mui/material`, `@mui/system`)

**Sub-module structure:**
- `g-theme-provider` — `GThemeProvider` component, `GThemeProviderContext`, `GThemeProviderContextType` interface, and `useGTheme` hook
- `g-theme-options` — `GThemeOptions` (`ThemeOptions`): default palette (teal primary `#1C7287`), Outfit typography scale with responsive breakpoints, MUI component overrides (`components-mui.tsx`), and Gamut component default props (`components-g.tsx`)
- `g-theme-options-alt-1` — `GThemeOptionsAlt1` (`ThemeOptions`): alternate palette (navy primary `#1E3A5F`), otherwise the same structure as `g-theme-options`

**Patterns:**

- **Dual context + MUI ThemeProvider**: `GThemeProvider` wraps MUI's `ThemeProvider` and exposes `GThemeProviderContext` alongside it. Consumers call `useGTheme()` to access `theme`, `themeOptionsPrimary`, `secondaryThemeOptions`, and `setThemeOptions`.
- **Runtime theme switching**: `setThemeOptions(aliasName?: string)` resolves a named entry from the `secondaryThemeOptions` record and replaces the active theme. Passing `undefined` reverts to the primary.
- **Component override typing via local interfaces**: `components-g.tsx` defines module-local interfaces (`INTERNAL_TYPE_s`, `INTERNAL_TYPE_PropsList`, etc.) to type default props and style overrides for `GShell`, `GMarkdown`, `GUserOverview`, `GUserOverviewMenu`, and `GArticleFeedback` in the same shape MUI uses for its own components.
- **Typography breakpoints via `@mui/system` `createTheme`**: `typography.ts` constructs a standalone breakpoints helper from `@mui/system` to produce responsive font-size overrides (xs: 0, sm: 480, md: 768, lg: 1024, xl: 1200).
- **MUI component overrides in `components-mui.tsx`**: `MuiCard`, `MuiLink`, `MuiBreadcrumbs`, `MuiContainer`, `MuiPaper`, `MuiSelect`, `MuiTextField`, `MuiAlert`, `MuiButton` (contained / outlined / text variants), `MuiCssBaseline`.

## Error Handling

`useGTheme()` throws synchronously with `"GThemeProviderContext is not created!"` if called outside a mounted `GThemeProvider`. No error boundary or graceful fallback exists. If `CockpitStore.get()` returns an unexpected value at init, `_getSecondaryInitOptions` silently falls back to `undefined` (primary theme). No API calls are made at runtime; no async error paths exist in this module.

## Bootstrapping

Mount `GThemeProvider` above all components that consume the MUI theme or call `useGTheme()`. Required props: `themeOptions` (a `ThemeOptions` object, e.g. `GThemeOptions` or `GThemeOptionsAlt1`) and `children`. Pass `secondaryThemeOptions` as a `Record<string, ThemeOptions>` to enable runtime switching.

`GThemeProvider` reads `CockpitStore.get()` synchronously at mount to resolve the initial secondary theme, so `@dxs-ts/gamut-cockpit-store` must be initialised before `GThemeProvider` mounts. In practice, `@dxs-ts/gamut-routes` handles this wiring and is the expected entry point for consuming applications.

## TypeScript Extension Points

This module exports no `declare module` augmentations or interface merging. The Gamut-component override types (`INTERNAL_TYPE_s`, `INTERNAL_TYPE`, `INTERNAL_TYPE_NameToClassKey`, `INTERNAL_TYPE_PropsList`, `INTERNAL_TYPE_Props`, `INTERNAL_TYPE_Variants`, `INTERNAL_TYPE_Overrides`) are module-local in `components-g.tsx` within each sub-module. Consuming modules that need to extend MUI's `Components` interface for Gamut components (e.g. adding `GComponentsPropsList`) do so in their own package — see `@dxs-ts/gamut-routes` and `g-props`.

## Known Limitations and Gotchas

- **`MuiCssBaseline` `a` tag `fontWeight` is inoperative** — `components-mui.tsx` line 207 in both `g-theme-options` and `g-theme-options-alt-1` sets `fontWeight: 700` on the `a` selector inside `MuiCssBaseline.styleOverrides` with an inline comment `// broken, doesnt work`. The override is present in both theme variants but has no visual effect.
- **`GMarkdown` default children contains a misspelling** — the default prop value `"# Portal under maintainence"` (sic) is hardcoded in `components-g.tsx` in both `g-theme-options` and `g-theme-options-alt-1`. Consumers that render `GMarkdown` without providing `children` will display this string verbatim including the typo.
- **`@ts-ignore` on `GMarkdown` `styleOverrides.root`** — both `components-g.tsx` files suppress a TypeScript error on the `root` style override for `GMarkdown`. The type mismatch is unresolved.
- **`setThemeOptions` callback does not update when `secondaryThemeOptions` prop changes** — the `useCallback` dependency array in `GThemeProvider` is empty (`[]`), so if the parent re-renders with a different `secondaryThemeOptions` map, `setThemeOptions` still closes over the original props reference.
- **Theme switch is not persisted** — `setThemeOptions` updates React state only; it does not write to `CockpitStore`. After a `window.location.reload()`, `GThemeProvider` re-reads `CockpitStore` and reverts to whatever cockpit alias was last saved there, not the in-memory selection.
- **`GLinks` override exists only in `GThemeOptionsAlt1`** — `g-theme-options-alt-1/components-g.tsx` includes a `GLinks.styleOverrides.title` entry absent from `g-theme-options/components-g.tsx`. Switching from alt-1 to primary theme at runtime removes this override.
- **Breakpoints in `typography.ts` use `@mui/system` `createTheme`**, not `@mui/material` `createTheme`. The resulting breakpoint helper generates responsive font-size overrides only; it is separate from the breakpoints object in the final `ThemeOptions` and is not exported.

## Scope Boundaries

Provides static `ThemeOptions` objects and a context provider for applying and switching MUI themes. Does not manage:

- authentication, routing, data fetching, internationalisation, or component rendering beyond the provider wrapper
- Gamut component implementations — only their default props and style overrides as `ThemeOptions.components` entries
- theme selection persistence; persistence across reloads depends on `CockpitStore` being written by the caller before reload
