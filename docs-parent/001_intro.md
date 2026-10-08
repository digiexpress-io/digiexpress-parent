# Intro

## Modules

### Gamut

Customer portal. Displays localised content (markdown). Manages form submissions (Dialob), tasks, messaging, and feedback. Connects to backend process workflows via REST controllers and Thena event sourcing.

- **`@dxs-ts/gamut`** (`modules/lib-gamut`) — umbrella package that re-exports all Gamut sub-modules: API contexts, form rendering (Dialob), form review, shell layout, primitives, theming, i18n message maps, and TanStack Router route tree for public and secured portal routes.
- **`@dxs-ts/gamut-cockpit-store`** (`modules/gamut-cockpit-store`) — static `localStorage` store for persisting the active `Cockpit` selection; exposes a `window.GamutCockpit` console API for manual enable/disable in DevTools.
- **`@dxs-ts/gamut-api`** (`modules/gamut-api`) — API layer for the Gamut portal: fetch factory functions, domain type namespaces, `usePopulateContext` hooks for TanStack Query data fetching, and in-memory state implementations for site content, IAM, Dialob forms, offers, contracts, comms, bookings, search, and map geocoding.
- **`@dxs-ts/gamut-md`** (`modules/gamut-md`) — renders markdown strings as MUI-styled React elements via a single `GMarkdown` component with theme-integrated heading, paragraph, list, and link mappings.
- **`@dxs-ts/gamut-primitives`** (`modules/gamut-primitives`) — concrete MUI-themed React components for the Gamut portal covering navigation (`GAppBar`, `GLayout`), authentication forms (`GAuth`, `GAuthFormStart`), content display (`GArticle`), inbox/messaging (`GInbox`), offers, contracts, bookings, user identity, and utility primitives (`GFlex`, `GConfirm`, `GLoader`).
- **`@dxs-ts/gamut-form-review`** (`modules/gamut-form-review`) — read-only review renderer for completed Dialob form sessions; fetches session and form data via `useDialobReview`, maps Dialob item types to per-type MUI components through a configurable `ItemconfigType` registry, and suppresses unanswered items at render time.
- **`@dxs-ts/gamut-routes`** (`modules/gamut-routes`) — TanStack Router route tree for the Gamut portal; defines the `router` instance, file-based `public` and `secured` route hierarchies, per-view page layout components (`GRouterProduct`, `GRouterSecuredServices`, `GRouterOffer`, `GRouterInbox`, etc.), and the MUI theme-integration interfaces (`GComponentsPropsList`, `GComponents`) that enumerate prop and class-key types for every Gamut component.
- **`@dxs-ts/gamut-form`** (`modules/gamut-form`) — interactive Dialob form renderer; dispatches each Dialob item type to a dedicated MUI input component (`GInputText`, `GInputDate`, `GInputAddress`, etc.) via a slot registry in `GFormBaseElement`, with per-page error visibility tracking and MUI theme override support.
- **`@dxs-ts/gamut-intl`** (`modules/gamut-intl`) — static translation message maps for five locales (en, fi, sv, bs, my), exported as a single `messages` object keyed by locale code for use with an `IntlProvider`.
- **`@dxs-ts/gamut-shell`** (`modules/gamut-shell`) — top-level page shell layout component (`GShell`) that manages responsive toolbar height, collapsible drawer state, and fullscreen state via a React context and a single MUI styled root.
- **`@dxs-ts/gamut-theme`** (`modules/gamut-theme`) — MUI theme provider (`GThemeProvider`) with runtime theme switching, two bundled `ThemeOptions` sets (`GThemeOptions`, `GThemeOptionsAlt1`), and MUI component overrides for both Gamut-specific and standard MUI components.
- **`@dxs-ts/xui-datetime`** (`modules/xui-datetime`) — date and year picker input components built on an immutable `DateInputStateMachine`, with a segmented day/month/year text input, interactive calendar popover, standalone year picker, and luxon-based date formatting utilities.

### TanStack

- **`090_tanstack.md`** — documents how `@tanstack/react-router`, `@tanstack/react-query`, and `@tanstack/react-store` are used across the Gamut modules: file-based route structure, the provider-context data flow pattern, query key conventions, the absence of loaders and `useMutation`, and key constraints a developer must know before adding a route or query.