# @dxs-ts/gamut-shell

## Purpose

Top-level page shell layout component (`GShell`) for the Gamut portal. Manages responsive toolbar height, collapsible drawer state, and fullscreen state. Applies MUI theme overrides for appbar, drawer, container, and footer regions via a single styled root.

## Dependencies

**dependencies:**
- `@dxs-ts/gamut-api` — `*`

**peerDependencies:**
- `react` — `*`
- `@mui/material` — `*`
- `@mui/utils` — `*`

## Module Relationships

### Depends on

- `@dxs-ts/gamut-api` — exports `GOverridableComponent`, used as the type for the `component` prop on `GShellProps` to allow consumers to swap the root element.

### Consumed by

- `@dxs-ts/gamut-primitives` — imports `GShellClassName` and applies it to the `Toolbar` inside `GAppBar` so that `GShellRoot`'s scoped CSS selectors can compute toolbar-height layout for the appbar region.
- `@dxs-ts/gamut-routes` — imports `GShell` as the layout wrapper in route components (e.g. `GRouterUnsecured`). Also imports `GShellClassName` and passes it to `scrollToTopSelectors` in `createRouter` so TanStack Router's scroll restoration targets the shell container on route navigation.

## Frontend Architecture

**Stack:** React, MUI Material, MUI Utils

**Sub-module structure:**
- `g-shell` — single sub-module; exports `GShell`, `GShellProps`, `GShellContext`, `GShellProvider`, `useGShell`, `GShellClassName`, and `GShellRoot`.

**Key components and hooks:**

- `GShell` (`g-shell/GShell.tsx`) — top-level shell component. Wraps children in `GShellProvider` and `CssBaseline`, resolves theme props via `useThemeProps` under the MUI name `GShell`, and delegates layout rendering to `GShellInternal`.
- `GShellInternal` (`g-shell/GShell.tsx`) — inner layout renderer. Reads `drawerOpen` from context, resolves `toolbarHeight` via `useToolbarHeight`, and renders `GShellRoot` (or a consumer-supplied `component`) with an `ownerState` carrying `toolbarHeight`, `footerHeight`, `drawerWidth`, and `drawerOpen`.
- `useToolbarHeight` (`g-shell/GShell.tsx`) — resolves a single numeric toolbar height from `GShellToolbarHeightOptions` by querying MUI breakpoint media queries (`xs` through `xl`).
- `GShellProvider` / `useGShell` (`g-shell/GShellContext.tsx`) — React context provider holding `open` (drawer) and `fullscreen` boolean states with `toggleDrawer` and `toggleFullscreen` mutators. `useGShell` returns the context value.
- `GShellRoot` (`g-shell/useUtilityClasses.tsx`) — MUI `styled('div')` root element. Applies flex column layout and scoped CSS for `.GAppBar-root`, `.MuiToolbar-root.GShellBase`, `.MuiContainer-root`, `.GFooter-root`, and `.GShellBase .MuiDrawer-paper`. Toolbar height, drawer width, drawer open state, and footer height are injected via `ownerState`.

**Props:**

- `GShellProps` — `children`, `drawerOpen?`, `toolbarHeight?: Partial<GShellToolbarHeightOptions>`, `footerHeight?`, `drawerWidth?`, `component?`.
- `GShellToolbarHeightOptions` — per-breakpoint numeric heights: `xs`, `sm`, `md`, `lg`, `xl`. Defaults: `xs`/`sm` → 150px, `md`/`lg`/`xl` → 90px.
- `footerHeight` default: 300px. `drawerWidth` default: 300px.

**Patterns:**
- MUI `useThemeProps` integration: `GShell` registers under MUI name `GShell`, allowing theme-level prop overrides via `components.GShell.defaultProps`.
- `overridesResolver` on `GShellRoot` enables theme-level style overrides for the `root` slot.
- `ownerState` pattern: layout-affecting values are passed as a typed `ownerState` object into the styled component rather than as direct props, keeping MUI conventions consistent with the rest of the Gamut component set.

## Error Handling

No error boundaries, validation logic, API calls, or loading states. `GShellContext` is initialised with `{} as any` as the default context value (`GShellContext.tsx`, line 10), so `useGShell` called outside a `GShellProvider` returns an empty object without throwing — `open` and `fullscreen` are `undefined` and `toggleDrawer`/`toggleFullscreen` are missing, causing runtime errors silently rather than surfacing a clear failure.

## Bootstrapping

`GShell` renders `GShellProvider` and `CssBaseline` internally. A consuming app wraps its layout tree in `<GShell>` and passes no additional providers. Configuration is limited to `GShellProps` and optional MUI theme-level overrides via `components.GShell.defaultProps`.

Required order:
1. An MUI `ThemeProvider` must be present above `GShell` in the tree — `GShell` calls `useThemeProps`, `useTheme`, and `useMediaQuery`, all of which depend on MUI's theme context.
2. Place `<GShell>` below `ThemeProvider` and above all layout children. No other provider setup is required from the consumer.

## TypeScript Extension Points

- `GShellClasses` / `GShellClassKey` (`GShellProps.tsx`) — defines the `root` slot. Consumers can augment MUI's `Components` interface to add `GShell` to `components.GShell.defaultProps` and `components.GShell.styleOverrides` using the standard MUI theme augmentation pattern, keyed on `GShellClassKey` for style overrides.
- `GOverridableComponent` (from `@dxs-ts/gamut-api`) — the `component` prop on `GShellProps` accepts any type satisfying `GOverridableComponent<GShellProps>`, allowing consumers to substitute the root element.

No `declare module` blocks are present in the module source.

## Known Limitations and Gotchas

- **`useGShell` outside provider gives silent empty object**: `GShellContext` is created with `{} as any` as its default value. Calling `useGShell` outside a `GShellProvider` returns `{}` typed as `GShellContextType` — no error is thrown, but all properties are `undefined` at runtime.
- **`drawerOpen` prop is initial state only**: `GShellProps.drawerOpen` seeds `GShellProvider`'s `useState` initial value. After mount, drawer state is owned by `GShellProvider` and `drawerOpen` prop changes are ignored — there is no `useEffect` to synchronise external state changes.
- **Drawer hidden below `md` via CSS only**: The drawer is hidden on small viewports via `display: none` in `GShellRoot`'s scoped CSS for `.GShellBase .MuiDrawer-paper` at breakpoints below `md`. The drawer remains mounted in the DOM.
- **`drawerOpen: false` leaves a `calc(100% - undefinedpx)` width**: In `useUtilityClasses.tsx` lines 84 and 93, when `drawerOpen` is `false`, `drawerWidth` is passed as `undefined` to template literals (`${drawerOpen ? drawerWidth : undefined}px`), producing the string `"undefinedpx"` in the generated CSS for `.MuiContainer-root` width and `.GFooter-root` margin.
- **No `useEffect` sync for `useToolbarHeight` when no breakpoint matches**: `useToolbarHeight` falls through all breakpoint checks and returns `options['xs'] ?? 0` as default because `currentBreakpoint` is initialised to `'xs'`. If none of the five media queries fires, the fallback is `'xs'` height, not `0`.

## Scope Boundaries

`@dxs-ts/gamut-shell` provides only the outermost page-level layout shell. It does not implement the appbar, toolbar, drawer, footer, navigation, routing, authentication, or any content area. It does not fetch data, manage user session state, or define any MUI theme. `@dxs-ts/gamut-primitives` and `@dxs-ts/gamut-routes` are responsible for rendering the actual appbar, drawer, and footer components inside the shell's child slots.
