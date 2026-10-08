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

- `@dxs-ts/gamut-routes` — mounts `GThemeProvider` at the application root, wrapping the entire route tree so every component below has access to the active MUI theme. Also imports `GComponentsPropsList` and `GComponentNameToClassKey` (from `g-props`) to extend MUI's `Components` interface, allowing Gamut components to be configured inside a single `ThemeOptions` object.
- Any component that calls `useThemeProps({ props, name: MUI_NAME })` — the bundled `ThemeOptions` in `g-theme-options` and `g-theme-options-alt-1` supply `defaultProps` for `GShell`, `GMarkdown`, `GUserOverview`, `GUserOverviewMenu`, and `GArticleFeedback` via the `components` map. Each of those components merges these defaults at render time through MUI's `useThemeProps`.
- Components using `useGTheme()` — accesses `theme`, `themeOptionsPrimary`, `secondaryThemeOptions`, and `setThemeOptions` from `GThemeProviderContext`; `setThemeOptions(aliasName?)` switches the active secondary theme at runtime or reverts to primary when called with `undefined`.

## Frontend Architecture

**Stack:** React, MUI (`@mui/material`, `@mui/system`)

**Sub-module structure:**
- `g-theme-provider` — `GThemeProvider` component, `GThemeProviderContext`, `GThemeProviderContextType` interface, and `useGTheme` hook
- `g-theme-options` — `GThemeOptions` (`ThemeOptions`): default palette (teal primary `#1C7287`), Outfit typography scale with responsive breakpoints, MUI component overrides (`components-mui.tsx`), and Gamut component default props (`components-g.tsx`)
- `g-theme-options-alt-1` — `GThemeOptionsAlt1` (`ThemeOptions`): alternate palette (navy primary `#1E3A5F`), otherwise the same structure as `g-theme-options`

**Patterns:**

- **Dual context + MUI ThemeProvider**: `GThemeProvider` wraps MUI's `ThemeProvider` and exposes its own `GThemeProviderContext` alongside it. Consumers use `useGTheme()` to access `theme`, `themeOptionsPrimary`, `secondaryThemeOptions`, and `setThemeOptions`.
- **Runtime theme switching**: `setThemeOptions(aliasName?: string)` resolves a named entry from the `secondaryThemeOptions` record and replaces the active theme. Passing `undefined` reverts to the primary.
- **Component override typing via local interfaces**: `components-g.tsx` defines module-local interfaces (`INTERNAL_TYPE_s`, `INTERNAL_TYPE_PropsList`, etc.) to type default props and style overrides for Gamut-specific components (`GShell`, `GMarkdown`, `GUserOverview`, `GUserOverviewMenu`, `GArticleFeedback`) in the same shape MUI uses for its own components.
- **Typography breakpoints via `@mui/system` `createTheme`**: `typography.ts` constructs a standalone breakpoints helper from `@mui/system` to produce responsive font-size overrides (xs: 0, sm: 480, md: 768, lg: 1024, xl: 1200).
- **MUI component overrides covered in `components-mui.tsx`**: `MuiCard`, `MuiLink`, `MuiBreadcrumbs`, `MuiContainer`, `MuiPaper`, `MuiSelect`, `MuiTextField`, `MuiAlert`, `MuiButton` (contained / outlined / text variants), `MuiCssBaseline`.
