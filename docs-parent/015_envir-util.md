# envir-util

## Purpose

Renders ASCII art version and build info to the browser console. Used to display project metadata, internal/external dependencies, and commit history at application startup. Purely diagnostic — no runtime data flow involvement.

## Dependencies

No dependencies or peerDependencies.

## Module Relationships

`@dxs-ts/envir-util` has no `@dxs-ts/*` dependencies. It is a leaf node in the dependency graph.

**Direct consumers**

| Module | What it uses |
|---|---|
| `@dxs-ts/gamut` (umbrella) | `VersionInfoApi`, `VersionInfoBuilder` — called once at application startup to render build metadata to the browser console |

**Position in the dependency graph**

`envir-util` sits in the foundation layer alongside `@dxs-ts/gamut-cockpit-store` and `@dxs-ts/gamut-intl`. None of the intermediate modules (`gamut-api`, `gamut-theme`, `gamut-shell`, `gamut-md`, `gamut-form`, `gamut-form-review`, `gamut-primitives`, `gamut-routes`) import from it. Only the top-level umbrella package `@dxs-ts/gamut` depends on it, which is consistent with its role: diagnostic console output, not runtime data flow.

## Key Exports

- `VersionInfoApi` — namespace exposing a `builder()` factory
- `VersionInfoBuilder` — fluent builder with chainable methods: `setLogo()`, `setTheme()`, `setProjectInfo()`, `addInternalComponents()`, `addExternalComponents()`, `setCommits()`, `render()`
- `ProjectInfo` — interface: `projectName`, `version`, `releaseDate`
- `ThemeColors` — interface: `logo`, `separator`, `projectName`, `info`, `componentTitle`, `componentItem`

## Patterns

- **Builder pattern** — method chaining, validated on `render()`
- **Command objects** — `Command_RenderLogo`, `Command_RenderProjectInfo`, `Command_RenderComponentList`, `Command_RenderSeparator` each execute independently and return `{ success, renderedLines?, error? }`
- **Theme system** — two hardcoded themes (`purple`, `red`) stored in a `THEMES` record
- **Logo registry** — ASCII art strings stored in a `LOGOS` record
- **Console output** — wrapped in `console.groupCollapsed("version info")` with `%c` formatting
