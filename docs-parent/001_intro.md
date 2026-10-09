# Intro

## Modules

### Gamut

Customer portal. Displays localised content (markdown). Manages form submissions (Dialob), tasks, messaging, and feedback. Connects to backend process workflows via REST controllers and Thena event sourcing.

- **`@dxs-ts/gamut`** (`modules/lib-gamut`) — umbrella package that re-exports all Gamut sub-modules: API contexts, form rendering (Dialob), form review, shell layout, primitives, theming, i18n message maps, and TanStack Router route tree for public and secured portal routes.
- **`@dxs-ts/gamut-cockpit-store`** (`modules/gamut-cockpit-store`) — static `localStorage` store for persisting the active `Cockpit` selection; exposes a `window.GamutCockpit` console API for manual enable/disable in DevTools.
- **`@dxs-ts/gamut-api`** (`modules/gamut-api`) — API layer for the Gamut portal: fetch factory functions, domain type namespaces, `usePopulateContext` hooks for TanStack Query data fetching, and in-memory state implementations for site content, IAM, Dialob forms, offers, contracts, comms, bookings, search, and map geocoding.
- **`@dxs-ts/gamut-md`** (`modules/gamut-md`) — renders markdown strings as MUI-styled React elements via a single `GMarkdown` component with theme-integrated heading, paragraph, list, and link mappings.
- **`@dxs-ts/gamut-primitives`** (`modules/gamut-primitives`) — concrete MUI-themed React components for the Gamut portal covering navigation (`GAppBar`, `GLayout`), authentication forms (`GAuth`, `GAuthFormStart`), content display (`GArticle`), inbox/messaging (`GInbox`), offers, contracts, bookings, user identity, and utility primitives (`GFlex`, `GConfirm`, `GLoader`). See `095_gamut_primitives_how_to_add_component.md` for step-by-step instructions on adding a new primitive component.
- **`@dxs-ts/gamut-form-review`** (`modules/gamut-form-review`) — read-only review renderer for completed Dialob form sessions; fetches session and form data via `useDialobReview`, maps Dialob item types to per-type MUI components through a configurable `ItemconfigType` registry, and suppresses unanswered items at render time.
- **`@dxs-ts/gamut-routes`** (`modules/gamut-routes`) — TanStack Router route tree for the Gamut portal; defines the `router` instance, file-based `public` and `secured` route hierarchies, per-view page layout components (`GRouterProduct`, `GRouterSecuredServices`, `GRouterOffer`, `GRouterInbox`, etc.), and the MUI theme-integration interfaces (`GComponentsPropsList`, `GComponents`) that enumerate prop and class-key types for every Gamut component.
- **`@dxs-ts/gamut-form`** (`modules/gamut-form`) — interactive Dialob form renderer; dispatches each Dialob item type to a dedicated MUI input component (`GInputText`, `GInputDate`, `GInputAddress`, etc.) via a slot registry in `GFormBaseElement`, with per-page error visibility tracking and MUI theme override support.
- **`@dxs-ts/gamut-intl`** (`modules/gamut-intl`) — static translation message maps for five locales (en, fi, sv, bs, my), exported as a single `messages` object keyed by locale code for use with an `IntlProvider`.
- **`@dxs-ts/gamut-shell`** (`modules/gamut-shell`) — top-level page shell layout component (`GShell`) that manages responsive toolbar height, collapsible drawer state, and fullscreen state via a React context and a single MUI styled root.
- **`@dxs-ts/gamut-theme`** (`modules/gamut-theme`) — MUI theme provider (`GThemeProvider`) with runtime theme switching, two bundled `ThemeOptions` sets (`GThemeOptions`, `GThemeOptionsAlt1`), and MUI component overrides for both Gamut-specific and standard MUI components.
- **`@dxs-ts/xui-datetime`** (`modules/xui-datetime`) — date and year picker input components built on an immutable `DateInputStateMachine`, with a segmented day/month/year text input, interactive calendar popover, standalone year picker, and luxon-based date formatting utilities.

### Eveli

Worker-facing task management and process orchestration module. Provides the backend REST API (Spring Boot, Vert.x Mutiny, Thena) for tasks, Dialob form integration, process execution, feedback, queues, and asset publishing, plus the React worker shell (`EveliShell`, `EveliApp`), TanStack Router route tree, and all worker-facing page components.

- **`fs` domain** (`modules/fs-api`, `modules/fs-composer`, `modules/fs-nav`) — worker-facing File Explorer for managing typed content assets (articles, flows, decision tables, Dialob forms, printouts, locales, links, and folders) via a multi-tab editor shell, hierarchical tree explorer, search with type/label filtering, and an immutable unsaved-change store backed by TanStack Store. See `105_fs.md`.
  - **`@dxs-ts/fs-api`** (`modules/fs-api`) — foundational type namespace (`Fs`), React context providers (`FsDirentProvider`, `FsDirentBodyProvider`), and TanStack Store-backed unsaved-change layer (`useFsu`, `useFsuChange`, `FsuWorld`) shared by all fs domain packages. See `110_fs_api.md`.
  - **`@dxs-ts/fs-composer`** (`modules/fs-composer`) — React component library implementing the full fs editor shell: multi-tab layout (`FsSetup.Main`/`FsSetup.Secondary`), hierarchical dirent tree explorer with search and label filtering, per-dirent-type create/update/properties/help views dispatched via a `DirentWidget` factory, and a switchable right-panel system (properties, preview, history, errors, debug, stats, changes, article-order, article-locale-overview, references). See `115_fs_composer.md`.
  - **`@dxs-ts/fs-nav`** (`modules/fs-nav`) — URL-serialised navigation state for the fs editor: encodes open tabs, active tab, and tree-node expansion as TanStack Router search params; exposes `useFsNav` (facade hook) and `FsNavProvider` for consuming applications. See `120_fs_nav.md`.

### TanStack

- **`090_tanstack.md`** — documents how `@tanstack/react-router`, `@tanstack/react-query`, and `@tanstack/react-store` are used across the Gamut modules: file-based route structure, the provider-context data flow pattern, query key conventions, the absence of loaders and `useMutation`, and key constraints a developer must know before adding a route or query.