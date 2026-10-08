# @dxs-ts/gamut-shell

## Purpose

Provides the top-level page shell layout component (`GShell`) for the Gamut portal. Manages responsive toolbar height, collapsible drawer state, and fullscreen state. Applies MUI theme overrides for appbar, drawer, container, and footer regions via a single styled root.

## Dependencies

**dependencies:**
- `@dxs-ts/gamut-api` — `*`

**peerDependencies:**
- `react` — `*`
- `@mui/material` — `*`
- `@mui/utils` — `*`

## Module Relationships

### Depends on

- `@dxs-ts/gamut-api` — provides `GOverridableComponent`, used as the type for the `component` prop on `GShellProps` to allow consumers to swap the root element.

### Consumed by

- `@dxs-ts/gamut-primitives` — imports `GShellClassName` and applies it to the `Toolbar` inside `GAppBar` so that `GShellRoot`'s scoped CSS selectors can compute toolbar-height layout for the appbar region.
- `@dxs-ts/gamut-routes` — imports `GShell` and composes it as the layout wrapper in route components (e.g. `GRouterUnsecured`). Also imports `GShellClassName` and passes it to `scrollToTopSelectors` in `createRouter` so TanStack Router's scroll restoration targets the shell container on route navigation.

## Frontend Architecture

**Stack:** React, MUI Material, MUI Utils

**Sub-module structure:**
- `g-shell` — single sub-module; exports `GShell`, `GShellProps`, `GShellContext`, `GShellProvider`, `useGShell`, `GShellClassName`, and `GShellRoot`.

**Key components and hooks:**

- `GShell` (`g-shell/GShell.tsx`) — top-level shell component. Wraps children in `GShellProvider` and `CssBaseline`, resolves theme props via `useThemeProps` under the MUI name `GShell`, and delegates layout rendering to `GShellInternal`.
- `GShellInternal` (`g-shell/GShell.tsx`) — inner layout renderer. Reads `drawerOpen` from context, resolves the current `toolbarHeight` via `useToolbarHeight`, and renders `GShellRoot` (or a consumer-supplied `component`) with an `ownerState` carrying `toolbarHeight`, `footerHeight`, `drawerWidth`, and `drawerOpen`.
- `useToolbarHeight` (`g-shell/GShell.tsx`) — resolves a single numeric toolbar height from `GShellToolbarHeightOptions` by querying MUI breakpoint media queries (`xs` through `xl`).
- `GShellProvider` / `useGShell` (`g-shell/GShellContext.tsx`) — React context provider that holds `open` (drawer) and `fullscreen` boolean states with `toggleDrawer` and `toggleFullscreen` mutators. `useGShell` returns the context value.
- `GShellRoot` (`g-shell/useUtilityClasses.tsx`) — MUI `styled('div')` root element. Applies flex column layout and scoped CSS for `.GAppBar-root`, `.MuiToolbar-root.GShellBase`, `.MuiContainer-root`, `.GFooter-root`, and `.GShellBase .MuiDrawer-paper`. Toolbar height, drawer width, drawer open state, and footer height are injected via `ownerState`.

**Props:**

- `GShellProps` — `children`, `drawerOpen?`, `toolbarHeight?: Partial<GShellToolbarHeightOptions>`, `footerHeight?`, `drawerWidth?`, `component?`.
- `GShellToolbarHeightOptions` — per-breakpoint numeric heights: `xs`, `sm`, `md`, `lg`, `xl`. Defaults: `xs`/`sm` → 150px, `md`/`lg`/`xl` → 90px.
- `footerHeight` default: 300px. `drawerWidth` default: 300px.

**Patterns:**
- MUI `useThemeProps` integration: `GShell` registers under MUI name `GShell`, allowing theme-level prop overrides via `components.GShell.defaultProps`.
- `overridesResolver` on `GShellRoot` enables theme-level style overrides for the `root` slot.
- `ownerState` pattern: layout-affecting values are passed as a typed `ownerState` object into the styled component rather than as direct props, keeping MUI conventions consistent with the rest of the Gamut component set.
