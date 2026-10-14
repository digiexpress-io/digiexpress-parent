# DigiExpress Architecture Summary

_Last updated: 14.10.2026_

This file is an index for agents. Read the cross-reference first to find where to look, then follow the Doc column for the relevant deep-dive before touching any code. If the Doc column is blank, invoke the `exploration` agent first.

---

## System overview

DigiExpress is a multi-portal platform with two user-facing frontends — **Gamut** (customer portal) and **Eveli** (worker/admin portal) — backed by a Java microservice stack. Content, forms, workflows, and decision tables are managed through the **fs-composer** editor shell. All persistence goes through **Thena**, an event-sourced multi-tenant database abstraction. **Limaone** compiles authoring assets into executable processes. **Metis** provides AI/semantic search. Frontend packages are TypeScript/React; backend packages are Java/Spring Boot.

```
Customer Browser
    └── Gamut (gamut-routes, gamut-primitives, gamut-form)
Worker Browser
    └── Eveli (eveli-routes, eveli-primitives, fs-composer editor)
            └── fs-composer ──► fs-api ──► eveli-client REST
                                               └── limaone-compiler (asset authoring)
                                               └── thena-* (event sourcing + storage)
                                               └── metis-client (AI/search)
```

---

## Cross-reference: find things fast

| Task | Where to look |
|------|--------------|
| Add a field to an existing dirent type | `116_fs_composer_add_field.md`, then the relevant `fs-dirent-*` module doc |
| Add a new dirent type editor | `114_fs_composer_modules.md`, `fs-factory`, and a reference dirent editor |
| Understand article / link / workflow associations | `119_fs_backend_article_relationships.md` |
| Change how a customer portal page looks | `gamut-primitives`, `gamut-routes`, `040_gamut_primitives.md`, `065_gamut_routes.md` |
| Change a Dialob form render component | `gamut-form`, `050_gamut_form.md` |
| Add a worker portal page | `eveli-routes`, `eveli-primitives`, `100_eveli_modules.md` |
| Understand the save/change-tracking mechanism | `fs-api` → `110_fs_api.md` |
| Understand tab and navigation state | `fs-nav` → `120_fs_nav.md` |
| Trace a REST call from UI to database | `eveli-client` → `limaone-compiler` → `thena-*-client` |
| Find coding standards | `999_ui_coding_standards.md` |

---

## Frontend — fs-composer sub-modules

All sub-modules live under `ts_setup/modules/fs-composer/`. They are leaf components rendered by the `DirentWidget` dispatch in `fs-factory`.

### Shell and navigation

| Module | Purpose | Doc |
|--------|---------|-----|
| `fs-setup` | Top-level entry points providing `Main` and `Secondary` layout components for the editor shell. | |
| `fs-main` | Core editor rendering the left content area, collapsible right panel, and vertical toolbar; dispatches views to the active dirent type. | |
| `fs-factory` | Widget factory mapping `Fs.BodyType` values to `DirentWidget` descriptors (icons, colors, create/update/properties/help views). | |
| `fs-explorer` | Hierarchical dirent tree with search toggle, new-file popover, collapse-all, and context menu. | |
| `fs-tabs` | Horizontal tab bar driven by `useFsNav` with right-click context menu and tab operations. | |
| `fs-tab-menu` | Context menu for tab-level operations (close, close others, etc.). | |
| `fs-search` | Search bar with asset-type and label filter chips; provides `FsSearchProvider` and `useFsSearch`. | |
| `fs-panel` | Generic titled panel wrapper shared by all right-side panel sub-modules. | |
| `fs-theme` | Color palette (`FsColors`), icon registry (`FsIcons`), and sized icon wrapper (`FsIcon`). | |
| `fs-utilities` | Shared form-field components: `FsDirentSelectSingle`, `FsDirentSelectMulti`, `FsDirentSelectGrouped`, `FsDirentTextField`, `FsDirentFormField`. | |

### Dirent infrastructure

| Module | Purpose | Doc |
|--------|---------|-----|
| `fs-dirent` | Recursive tree node rendering dirent icons, names with search highlight, unsaved-change indicators, and config options. | |
| `fs-diff-indicator` | Visual marker shown on dirents with unsaved changes. | |
| `fs-dirent-loader` | Loading spinner shown while a dirent body is loading. | |
| `fs-dirent-menu` | Right-click context menu for dirent tree actions (rename, copy, delete, reorder). | |
| `fs-dirent-menu-new` | Menu for creating new dirents of a chosen type. | |
| `fs-dirent-rename` | Rename dialog and form for renaming a dirent in place. | |
| `fs-dirent-copy` | Copy dialog and form for duplicating a dirent with a new name. | |
| `fs-dirent-button-save` | Save action button with `isDirty` guard. | |
| `fs-dirent-button-cancel` | Cancel/revert action button for discarding unsaved changes. | |
| `fs-dirent-button-delete` | Delete action button with confirmation dialog. | |
| `fs-dirent-description` | Description field editor used inside dirent property forms. | |
| `fs-dirent-labels` | Label multi-select field used inside dirent property forms. | |
| `fs-dirent-comments` | Comments panel for viewing and adding comments on a dirent. | |
| `fs-dirent-references` | References panel showing which other dirents reference the active dirent. | |
| `fs-dirent-textfield-autocomplete` | Autocomplete text field backed by dirent name data. | |
| `fs-dirent-locale-select` | Locale selection dropdown for multi-language dirent editing. | |

### Dirent type editors

| Module | Purpose | Doc |
|--------|---------|-----|
| `fs-dirent-article` | Create, update, properties, and help views for the `ARTICLE` dirent type. | `117_fs_dirent_article.md` |
| `fs-dirent-article-link` | Create, update, properties, and help views for the `ARTICLE_LINK` dirent type. | `118_fs_dirent_article_link.md` |
| `fs-dirent-article-page` | Create, update, properties, and help views for the `ARTICLE_PAGE` dirent type with content editing. | |
| `fs-dirent-article-template` | Create, update, properties, and help views for the `ARTICLE_TEMPLATE` dirent type for reusable article content. | |
| `fs-dirent-article-workflow` | Create, update, properties, and help views for the `ARTICLE_WORKFLOW` dirent type, binding Dialob forms, process flows, and portal articles. | `117_fs_dirent_article_workflow.md` |
| `fs-dirent-folder` | Create, update, properties, and help views for the `FOLDER` dirent type. | |
| `fs-dirent-dialob` | Create, update, properties, and help views for `DIALOB_FORM` dirents. | |
| `fs-dirent-flow` | Create, update, properties, and help views for `FLOW` dirents with autocomplete and asset selection. | |
| `fs-dirent-flow-task` | Create, update, properties, and help views for `FLOW_TASK` dirents. | |
| `fs-dirent-decision-table` | Create, update, properties, and help views for `DECISION_TABLE` dirents with a table builder sub-view. | |
| `fs-dirent-printout` | Create, update, properties, and help views for `PRINTOUT` dirents. | |
| `fs-dirent-printout-page` | Create, update, properties, and help views for `PRINTOUT_PAGE` dirents with autocomplete and preview. | |
| `fs-dirent-printout-resource` | Create, update, properties, and help views for `PRINTOUT_RESOURCE` dirents. | |
| `fs-dirent-locale` | Create, update, properties, and help views for `LOCALE` dirents managing portal site language support. | |

### Right-panel views

| Module | Purpose | Doc |
|--------|---------|-----|
| `fs-panel-article-locale-overview` | Locale coverage overview for articles. | |
| `fs-panel-article-order` | Article ordering and hierarchy management. | |
| `fs-panel-changes` | Lists all unsaved changes with save and discard actions. | |
| `fs-panel-debug` | Debug output, CSV export, and diagnostic information. | |
| `fs-panel-dirent-stats` | Asset counts by type, dangling assets, and disabled assets. | |
| `fs-panel-errors` | Error display panel (stub). | |
| `fs-panel-help` | Renders help text for the active dirent type. | |
| `fs-panel-history` | Change history for the active dirent. | |
| `fs-panel-preview` | Preview of the active dirent content. | |
| `fs-panel-preview-wrench` | Preview configuration tooling with flow graph visualisation. | |
| `fs-panel-properties` | Property editor for the active dirent. | |
| `fs-panel-references` | Shows which other dirents reference the active dirent. | |

---

## Frontend — shared packages

| Module | Purpose | Doc |
|--------|---------|-----|
| `fs-api` | Shared `Fs` type namespace, React context providers (`FsDirentProvider`, `FsDirentBodyProvider`), and change-management hooks for the filesystem editor. | `110_fs_api.md` |
| `fs-nav` | URL-serialised navigation state (open tabs, active tab, tree expansion) via TanStack Router search params. | `120_fs_nav.md` |
| `fs-composer` | Barrel package re-exporting the full file-system editor shell. | `114_fs_composer_modules.md` |

---

## Frontend — Gamut (customer portal)

| Module | Purpose | Doc |
|--------|---------|-----|
| `gamut` | Umbrella package re-exporting all Gamut sub-modules. | `020_gamut.md` |
| `gamut-api` | React contexts, fetch factories, and domain types for all backend APIs (IAM, Stencil, Dialob, offers, contracts, bookings, search). | `030_gamut_api.md` |
| `gamut-cockpit-store` | Persists cockpit (multi-tenant context) selection in localStorage across page reloads. | `025_gamut_cockpit_store.md` |
| `gamut-form` | Renders interactive Dialob form sessions as MUI-styled React components with input validation and state management. | `050_gamut_form.md` |
| `gamut-form-review` | Renders read-only review of completed Dialob form sessions with per-answer-type display components. | `045_gamut_form_review.md` |
| `gamut-intl` | Static translation message maps for portal locales (bs, en, fi, my, sv). | `060_gamut_intl.md` |
| `gamut-md` | Renders markdown content as MUI-styled React elements with theme-aware typography mappings. | `035_gamut_md.md` |
| `gamut-primitives` | Concrete MUI-themed component implementations for portal pages (auth, inbox, offers, contracts, bookings, articles, navigation). | `040_gamut_primitives.md` |
| `gamut-routes` | TanStack Router route tree for the customer portal with page-level route components. | `065_gamut_routes.md` |
| `gamut-shell` | Top-level page layout managing responsive toolbar, drawer state, and theme-aware shell styling. | `055_gamut_shell.md` |
| `gamut-theme` | MUI ThemeOptions with bundled theme sets and runtime theme switching via React context. | `070_gamut_theme.md` |

---

## Frontend — Eveli (worker/admin portal)

| Module | Purpose | Doc |
|--------|---------|-----|
| `eveli-api` | API layer aggregating all worker domain namespaces (Wrench, Stencil, Task, Contract, Cockpit, Ledger, Tagomi) with fetch factories and React contexts. | `100_eveli_modules.md` |
| `eveli-primitives` | Shared MUI-themed components for worker-facing views. | `100_eveli_modules.md` |
| `eveli-intl` | Static translation message maps for the worker portal UI. | `101_eveli_intl.md` |
| `eveli-routes` | TanStack Router route tree for the worker portal integrating all domain sub-modules into full-page layouts. | `100_eveli_routes.md` |

---

## Frontend — domain APIs and utility

| Module | Purpose | Doc |
|--------|---------|-----|
| `cockpit-api` | API layer and type definitions for cockpit (multi-tenant context) management. | |
| `contract-api` | API layer for contract domain data fetching and state management. | |
| `ledger-api` | API layer for ledger domain data fetching and state management. | |
| `stencil-api` | API layer for Stencil CMS content fetching and site structure. | |
| `tagomi-api` | API layer for tagomi (workflow engine) domain data fetching. | |
| `task-api` | API layer for task data types and context management. | |
| `wrench-api` | API layer for wrench (decision engine) domain data fetching. | |
| `cockpit-composer` | React components for cockpit editing interface. | |
| `contract-composer` | React components for contract editing interface. | |
| `ledger-composer` | React components for ledger display interface. | |
| `stencil-routes` | Route components for Stencil CMS management interface. | |
| `tagomi-routes` | Route components for tagomi domain management interface. | |
| `wrench-routes` | Route components for wrench decision-engine editing. | |
| `task-composer-v2` | React components for task/work-item composition and management. | |
| `task-feedback` | Task extension module for user feedback collection in task workflows. | |
| `user-profile` | User profile and user-configured settings persistence. | |
| `envir-fetch` | Generic HTTP fetch library with React hooks for data loading. | |
| `envir-util` | Renders ASCII art version and build info to the browser console at startup. | `015_envir_util.md` |
| `xui-datetime` | Date and year picker input components with calendar popover and formatting utilities. | `075_xui_datetime.md` |
| `xui-table` | Reusable table component with sorting, pagination, and MUI integration. | |
| `lib-eveli` | Umbrella package re-exporting all Eveli sub-modules. | |
| `lib-gamut` | Umbrella package re-exporting all Gamut sub-modules. | |
| `demo-app-gamut` | Standalone demo application for the Gamut customer portal. | |
| `demo-app-eveli` | Standalone demo application for the Eveli worker/admin portal. | |

---

## Backend — Eveli (`eveli-parent`)

| Module | Purpose | Doc |
|--------|---------|-----|
| `eveli-client` | Core Eveli library: Spring Boot auto-configuration, REST controllers, task/feedback/queue management, and Dialob integration. | `100_eveli_modules.md` |
| `eveli-permissions` | Permission and authorisation configuration for worker-facing roles and task operations. | |
| `eveli-user-profile` | Worker user profile management and persistence. | |
| `eveli-integration-suomifi-rest` | Suomi.fi integration adapter for Finnish digital identity authentication. | |
| `eveli-integration-google` | Google Cloud integration for external authentication and services. | |
| `eveli-integration-queue` | Message queue integration over PostgreSQL/ThenaMq for async task notifications. | |
| `eveli-migration` | Data migration utilities for importing legacy system data via Thena DB clients. | |
| `eveli-app` | Spring Boot executable application combining all Eveli modules with embedded Tomcat. | |
| `eveli-app-gcloud` | Google Cloud-specific application variant with Cloud Storage and managed service bindings. | |
| `eveli-app-wrench-only` | Minimal Eveli app configured for Wrench decision table editing only. | |
| `eveli-local-docker` | Docker Compose stack for local development (PostgreSQL, Ollama, message queue). | |

---

## Backend — Limaone (`limaone-parent`)

Limaone is the content modeling and authoring framework. It manages the lifecycle of all business content entities — articles, links, workflows, Dialob forms, flows, decision tables, and printouts — across two paths: a write side via the `Authoring` interface (`NewModel`, `ModifyModel`, `DeleteModel` builders for each entity type) and a read/compile side that transforms stored models into optimised runtime programs. All persistence is delegated to Thena. `eveli-client` consumes limaone directly, initialising the `l1Runtime` bean that compiles the world model on startup.

| Module | Purpose | Doc |
|--------|---------|-----|
| `limaone-compiler` | Compiles Dialob forms, workflow flows, and decision tables into executable processes; manages article/link/workflow authoring commands and asset caching. | `119_fs_backend_article_relationships.md` |

**Internal packages (`io.resys.limaone.*`):**

| Package | Purpose |
|---------|---------|
| `model` | Data model interfaces for all core domain concepts: articles, flows, decision tables, Dialob forms, deployments, and related entities. |
| `authoring` | Interfaces and implementations for creating, modifying, copying, and deleting content entities (articles, links, workflows, flows, printouts, etc.). |
| `persistence` | Concrete authoring operation implementations extending `AuthoringTemplate`; handles creation, modification, and deletion for each model entity type. |
| `program` | Compiler and program interfaces for converting stored models into executable programs for each domain (flows, Dialob, decision tables, workflows). |
| `ast` | Abstract syntax tree interfaces for domain models used during compilation. |
| `fs` | File system abstraction (`WorldFs`) representing hierarchical content structure and directory entries. |
| `spi` | Service provider interfaces for AST handling, bundling, compilation, PDF/HTTP output, and runtime execution (11 sub-packages). |
| `yaml` | YAML serialisation/deserialisation utilities (`YamlMapper`, `MutableYaml`). |

---

## Backend — Thena (`thena-parent`)

Thena is the event-sourced, multi-tenant database abstraction layer. All domain persistence goes through a Thena client.

| Module | Purpose | Doc |
|--------|---------|-----|
| `thena-sql-client` | Reactive SQL client with tenant-aware table management using Vert.x Mutiny. | |
| `thena-sql-client-annot-proc` | Annotation processor generating type-safe SQL repositories from `@TenantSql` annotations. | |
| `thena-db-client` | Event-sourced database abstraction with GRIM (Git-like) revision management. | |
| `thena-grim-client` | GRIM (Git-like Revision In Memory) client for immutable revision trees and branching. | |
| `thena-mq-client` | Message queue client backed by PostgreSQL event tables. | |
| `thena-batch-client` | Batch job persistence and status tracking via event sourcing. | |
| `thena-doc-client` | Document/content management via event-sourced Thena DB. | |
| `thena-git-client` | Git integration client for version control and deployment workflows. | |
| `thena-product-client` | Product/offer domain persistence via Thena event sourcing. | |
| `thena-contract-client` | Contract/agreement management via Thena persistence. | |
| `thena-ledger-client` | Ledger/accounting domain persistence. | |
| `thena-fs-client` | File system asset persistence for published content and hierarchical dirents. | |
| `thena-test-client` | Testcontainers utilities for Thena integration testing. | |
| `thena-sql-client-sample` | Sample implementation demonstrating `thena-sql-client` usage patterns. | |

---

## Backend — Metis (`metis-parent`)

| Module | Purpose | Doc |
|--------|---------|-----|
| `metis-client` | AI platform library: vector embeddings, semantic search indexing, and chat service integration with Ollama/Spring AI. | |

---

## Backend — Tagomi (`tagomi-parent`)

| Module | Purpose | Doc |
|--------|---------|-----|
| `tagomi-client` | Workflow execution and orchestration client library with world-building, state management, PDF compilation, and CRUD operations for services, templates, resources, locales, and tags. | |

**Internal packages (`io.digiexpress.tagomi.*`):**

| Package | Purpose |
|---------|---------|
| `api` | Public API interfaces: `TagomiClient` (world builder), `TagomiStore` (state queries and upsert), `TagomiComposer` (CRUD commands), `TagomiImageStorage`, `TagomiStoreConfig`. |
| `api.entities` | Immutable entity model definitions: `TagomiContainer`, `TagomiWorld`, `TagomiProgram`, `TagomiAst`; domain objects for Service, Template, Resource, Locale, and Tag. |
| `api.commands` | Command interfaces for domain operations: `TagomiCreateCommands`, `TagomiUpdateCommands`, `TagomiDeleteCommands` with nested immutable command DTOs. |
| `spi` | Concrete implementations of public API interfaces: `TagomiClientImpl`, `TagomiComposerImpl`, `TagomiStoreImpl`, `TagomiWorldImpl`. |
| `spi.builders` | Builder implementations: `QueryBuilderImpl` (state queries), `UpsertBuilderImpl` (create/update/delete operations). |
| `spi.commands` | Command implementation classes for domain object mutations. |
| `spi.json` | Jackson-based JSON serialisation helpers: `FromJsonObject`, `ToJsonObject`. |
| `spi.support` | Exception hierarchy: `StoreException`, `ConstraintException`, `RepoException`. |
| `spi.visitors` | Visitor pattern implementations, e.g. `ServiceDeleteVisitor` for cascading service deletion with template and resource cleanup. |
| `rust` | PDF compilation integration: `TagomiPdfCommand` for remote PDF service communication via REST. |
| `rust.entities` | PDF DTOs: `PdfRequest`, `PdfDocument`, `AnyResponse` for REST payload handling. |

---

## Backend — Legacy Process (`lp-parent`)

| Module | Purpose | Doc |
|--------|---------|-----|
| `lp-client` | Legacy process domain client for process definition and execution APIs. | |
| `lp-products` | Product/offer domain types and repository clients. | |
| `lp-batches` | Batch job execution and scheduling for workflow jobs. | |
