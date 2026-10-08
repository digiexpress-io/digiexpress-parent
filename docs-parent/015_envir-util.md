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

## Error Handling

Errors surface through two mechanisms:

1. **Validation errors thrown at `render()` time** — `VersionInfoBuilder.render()` throws `Error('Missing required fields: logo, projectName, version, and releaseDate must be set')` if any of those four fields are unset. This throw is not caught internally; the caller is responsible for wrapping `render()` in a try/catch if silent failure is required at startup.

2. **Command-level failures logged to `console.error`** — Each command (`Command_RenderLogo`, `Command_RenderSeparator`, `Command_RenderProjectInfo`, `Command_RenderComponentList`) returns `{ success: boolean; error?: string }`. If `success` is `false`, `VersionInfoBuilder.render()` calls `console.error(...)` and returns early. No exception is thrown for command failures.

3. **Invalid logo key** — `setLogo(logoKey)` throws `Error("Logo '<key>' not found")` immediately if the key is absent from the `LOGOS` record.

There are no loading states, async operations, React error boundaries, or validation error types — all error paths are synchronous.

## Bootstrapping

None — leaf node. `VersionInfoApi.builder()` is a plain factory function. No React providers, context, stores, or module-level initialization are required. Call `VersionInfoApi.builder()...render()` directly at application startup, typically once in the top-level entry file of the consuming application.

## TypeScript Extension Points

None identified. The module uses no `declare module` augmentation, no interface merging, and no extension records. `VersionInfoApi.ThemeColors` and `VersionInfoApi.ProjectInfo` are declared inside a namespace and are not designed for external augmentation. The `LOGOS` and `THEMES` records are module-local exports typed as `Record<string, string>` and `Record<'purple' | 'red', VersionInfoApi.ThemeColors>` respectively — new entries cannot be added without modifying the source files.

## Known Limitations and Gotchas

- **Theme is a closed union** — `setTheme()` accepts only `'purple' | 'red'`. There is no mechanism to pass a custom `ThemeColors` object; custom themes require modifying `themes.ts` directly.

- **Logo registry is closed** — `LOGOS` is a module-level constant with one entry (`logo_1_great_ones_wisdom`). Calling `setLogo()` with any other key throws immediately. There is no registration API.

- **Style count / line count mismatch is silent** — `VersionInfoBuilder.render()` assembles `allLines` and `allStyles` arrays separately. Empty lines push to `allLines` but no corresponding style is pushed to `allStyles`. The browser console does not error on a style count mismatch, so mismatches are invisible but may cause styling shifts on empty lines.

- **`render()` returns `void`, not a result object** — Unlike the four command classes which return `{ success, error? }`, `render()` returns `void` and uses `console.error` for failure reporting. Callers cannot programmatically detect whether rendering succeeded without wrapping in a try/catch.

- **`package.json` version is a Maven placeholder** — `"version": "${project.version}"` is unresolved at the TypeScript source level; the actual version is substituted during the Maven build. Importing the raw `package.json` at runtime will yield the literal string `${project.version}`.

## Scope Boundaries

`envir-util` handles only one-time diagnostic output to the browser console at application startup. It does not manage application state, expose React components or hooks, participate in any data flow, handle routing, perform HTTP requests, manage internationalization, or provide any runtime utility functions beyond the ASCII art rendering pipeline. It has no opinion on when `render()` is called — that is the consuming application's responsibility.
