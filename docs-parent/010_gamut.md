# Gamut

## Purpose

Gamut is the customer-facing portal. It displays localised content to users via markdown files served per locale. It also manages form submissions (Dialob questionnaires), tasks, messaging, and feedback. Connects to backend process workflows via REST controllers and Thena event sourcing.

## Dependencies

**Direct:**
- `@dxs-ts/envir-util`
- `@dxs-ts/gamut`
- `@dxs-ts/gamut-cockpit-store`
- `@dxs-ts/gamut-api`
- `@dxs-ts/gamut-md`
- `@dxs-ts/gamut-primitives`
- `@dxs-ts/gamut-form-review`
- `@dxs-ts/gamut-form`
- `@dxs-ts/gamut-shell`
- `@dxs-ts/gamut-intl`
- `@dxs-ts/gamut-routes`
- `@dxs-ts/gamut-theme`
- `@dxs-ts/xui-datetime`

**Peer:**
- `react`
- `react-dom`
- `react-intl`
- `@tanstack/react-router`
- `@tanstack/react-query`
- `@tanstack/react-store`
- `@mui/material`
- `@mui/utils`

## Backend Architecture

**Stack:** Spring Boot + SmallRye Mutiny (reactive, non-blocking)

**Key controllers:**
- `GamutSiteController` — serves localized content (`/portal/site`), cockpit management
- `GamutUserActionsController` — manages tasks and form submissions (`/portal/secured/actions`)
- `GamutFeedbackController` — customer feedback
- `GamutIamController` — identity and authorization

**Patterns:**
- `GamutClientImpl` orchestrates operations via builder pattern
- Mutiny `Uni<T>` / `Multi<T>` throughout for non-blocking async
- Data access via Thena (event sourcing), with commit tracking
- `UserAttachmentBuilder` and `AttachmentDownloadQuery` handle file uploads/downloads

## Frontend Architecture

**Stack:** React 18, TypeScript, MUI, TanStack Router, TanStack React Query, React Intl, Leaflet (maps)

**Sub-module structure:**
- `gamut-api` — foundation layer: React hooks (`useSite`, `useFormTip`), data fetching, business logic, config management
- `gamut-form` — input components (`GInput*`, `GFormGroup`) built on Material-UI
- `gamut-routes` — full page layouts and navigation
- `gamut-primitives` — basic UI building blocks
- `gamut-theme` — theme and styling

**Patterns:**
- Form state via `FormProvider` context wrapping `GForm`; uses Dialob `ActionItem` types for questionnaire state
- Component hierarchy: `GForm → GFormIterator → GInputBoolean / GInputAddress / GInputAutocomplete / ...`
- Styling via MUI + `useUtilityClasses` (CSS-in-JS utility classes)
- Data fetching via TanStack React Query
- Multi-tenant support: `CockpitStore` persists selected tenant/cockpit in localStorage
