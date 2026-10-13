_last updated: 13.10.2026_

# Gamut Architecture Overview

## 1. Layer Diagram

```
┌──────────────────────────────────────────────────────────────────────┐
│  ROUTING / COMPOSITION LAYER                                         │
│  @dxs-ts/gamut-routes                                                │
│  TanStack Router route tree · __root.tsx · SecuredSetup/PublicSetup  │
│  GRouterSecuredServices · GRouterProduct · GRouterOffer · GRouterInbox│
└───────────────────┬──────────────────────────────────────────────────┘
                    │ composes
┌───────────────────▼──────────────────────────────────────────────────┐
│  LAYOUT / SHELL LAYER                                                │
│  @dxs-ts/gamut-shell      GShell · GShellProvider · useGShell        │
│  @dxs-ts/gamut-primitives GAppBar · GLayout · GFooter · GFlex        │
│  @dxs-ts/gamut-theme      GThemeProvider · useGTheme                 │
└───────────────────┬──────────────────────────────────────────────────┘
                    │ composes
┌───────────────────▼──────────────────────────────────────────────────┐
│  FEATURE COMPONENT LAYER                                             │
│  @dxs-ts/gamut-primitives  GArticle · GInbox · GOffers · GContracts  │
│                            GBookings · GAuth* · GLocales · GError*   │
│  @dxs-ts/gamut-form        GForm → GFormIterator → GFormBase →       │
│                            GFormBaseElement → GInput* / GFormGroup*  │
│  @dxs-ts/gamut-form-review GFormReview → GFormReviewQuestionnaire →  │
│                            GFormReviewGroup → GFormReview<Type>      │
│  @dxs-ts/gamut-md          GMarkdown                                 │
│  @dxs-ts/xui-datetime      DatePicker · YearPicker · DateTimeFormatter│
└───────────────────┬──────────────────────────────────────────────────┘
                    │ reads via hooks/contexts
┌───────────────────▼──────────────────────────────────────────────────┐
│  STATE / API LAYER                                                   │
│  @dxs-ts/gamut-api                                                   │
│  ConfigProvider · useConfig                                          │
│  IamBackendProvider · SiteBackendProvider · DialobProvider           │
│  OfferProvider · ContractProvider · CommsProvider · BookingProvider  │
│  SearchProvider · MapProvider                                        │
│  usePopulateContext (per domain) · ActionsQueue · FormImpl           │
└───────────────────┬──────────────────────────────────────────────────┘
                    │ depends on
┌───────────────────▼──────────────────────────────────────────────────┐
│  FOUNDATION LAYER (no @dxs-ts/* deps)                                │
│  @dxs-ts/gamut-cockpit-store   CockpitStore · window.GamutCockpit    │
│  @dxs-ts/gamut-intl            messages (bs/en/fi/my/sv)             │
│  @dxs-ts/envir-util            VersionInfoApi · VersionInfoBuilder   │
└──────────────────────────────────────────────────────────────────────┘
```

---

## 2. Data Flow

### 2a. Loading and Displaying Localised Content

1. **Boot**: The host app mounts `GThemeProvider` (reads `CockpitStore.get()` to select the secondary `ThemeOptions`), then `ConfigProvider` (assembles `ConfigApi.default_config` from fetch factory instances).

2. **Route entry**: `__root.tsx` (in `@dxs-ts/gamut-routes`) mounts `IamBackendProvider`. Based on `useIam().authType`, it wraps the outlet in either `SecuredSetup` or `PublicSetup`, each composing the relevant backend context providers from `@dxs-ts/gamut-api` (`SiteBackendProvider`, `OfferProvider`, etc.).

3. **Site data fetch**: `SiteBackendProvider` internally calls the `usePopulateContext` hook for `api-site`. The hook issues a TanStack Query request via the `createSiteFetch` factory (targeting `GamutSiteController`'s `/portal/site` endpoint). The raw response is mapped to `SiteApi.Topic[]` / `SiteApi.TopicView[]` and stored in the `SiteBackendContext`.

4. **Locale selection**: `LocaleContextType` (from `api-locale`) holds the active locale. The host app passes the resolved locale code to `react-intl`'s `IntlProvider`, supplying `messages[locale]` from `@dxs-ts/gamut-intl`.

5. **Route render**: A public route (e.g. `public.$locale.index`) renders `GRouterUnsecured`, which composes `GShell` (layout shell), `GAppBar` (navigation bar with `GLocales` locale switcher), and `GArticle`.

6. **Content render**: `GArticle` receives a `SiteApi.TopicView` (read via `useSite()` from `@dxs-ts/gamut-api`) and passes the markdown body string to `GMarkdown` (`@dxs-ts/gamut-md`). `GMarkdown` feeds the string to `react-markdown`, mapping each HTML element to an MUI `Typography` component using the locale-aware theme.

7. **Translation keys**: Any UI string (labels, button text, status messages) is resolved by `react-intl`'s `useIntl()` or `<FormattedMessage>`, drawing from the `messages` record supplied in step 4.

### 2b. Dialob Form Submission

1. **Form initiation**: A user selects an offer or product. `GRouterOffer` (in `@dxs-ts/gamut-routes`) activates and calls `useRouteToOffer`. The router renders `GForm` (from `@dxs-ts/gamut-form`) inside the `DialobProvider` context.

2. **Form session hydration**: `DialobProvider` (from `api-dialob` in `@dxs-ts/gamut-api`) fetches the form session via `FetchActionGET`, targeting `GamutUserActionsController`'s `/portal/secured/actions` endpoint. The raw response is parsed by `FormImpl.parsePage` and `FormImpl.parseInputRow` into `DialobApi.FormStore`, held in a `@tanstack/react-store` store.

3. **Form rendering**: `GForm` wraps the store in `FormProvider` and renders `GFormTip` and `GFormIterator`. `GFormIterator` maps `tip.items` (the current page's item IDs) to `GFormBase` nodes. Each `GFormBase` reads its `ActionItem` from the store via `useForm`, resolves theme props via `useThemeInfra`, and renders via `GFormBaseElement`.

4. **Input dispatch**: `GFormBaseElement` calls `useSlotVariant`, inspecting `ActionItem.type`, `ActionItem.view`, and `ActionItem.props` to resolve a `GFormBaseSlotVariant` key. That key is looked up in the `Slots` record to select the concrete input component (e.g. `GInputTextDialob`, `GInputListDialob`, `GInputAddressDialob`). Date/time inputs delegate to `DatePicker` / `YearPicker` from `@dxs-ts/xui-datetime`.

5. **User input → action**: Each `*Dialob` input wrapper calls an `ActionItem` action creator (e.g. `ANSWER`) on user interaction. The resulting `Action` is pushed into `ActionsQueue` (from `api-dialob`).

6. **Debounced sync**: `ActionsQueue` accumulates actions and flushes them after a `syncWait` debounce period via `FetchActionPOST` to `/portal/secured/actions`. The backend response returns updated `Action[]`; `ActionsQueue` applies them to the store, updating `FormStore` state and triggering re-renders via `@tanstack/react-store`.

7. **Validation and error display**: `GFormBaseElement` registers each item's DOM ref with `GFormErrorVisibilityProvider`. On page navigation, `GFormErrorVisibility` becomes visible; `useScrollTo` scrolls to the first element with a non-empty `ActionError[]`. `GInputError` renders the error messages beneath each input via `GInputBase`'s error slot.

8. **Submission**: On final page completion, a submit `Action` is dispatched through `ActionsQueue`. After the POST confirms completion, `GRouterOffer` navigates to `GRouterOfferSummary`, which renders `FormFillSummaryBox` (and optionally `FormFillRpsRating` for NPS collection).

9. **Review (post-submit)**: Within the inbox, `GInbox` (in `@dxs-ts/gamut-primitives`) renders `GInboxFormReview`, which mounts `GFormReview` (from `@dxs-ts/gamut-form-review`). `GFormReview` calls `useDialobReview` (from `api-dialob`) with a `formId`, fetches the completed session, and passes the data to `GFormReviewContextType`. `GFormReviewQuestionnaire` maps each item ID through `GFormReviewContextType.createItem`, dispatching to per-type read-only components (`GFormReviewText`, `GFormReviewChoice`, `GFormReviewBoolean`, etc.).

---

## 3. Module Dependency Direction

### Leaf Nodes (no `@dxs-ts/*` dependencies)

| Module | What it provides |
|---|---|
| `@dxs-ts/gamut-cockpit-store` | `CockpitStore`, `Cockpit`, `window.GamutCockpit` |
| `@dxs-ts/gamut-intl` | `messages` locale maps |
| `@dxs-ts/envir-util` | `VersionInfoApi`, `VersionInfoBuilder` |
| `@dxs-ts/xui-datetime` | `DatePicker`, `YearPicker`, `DateTimeFormatter`, `DateInputStateMachine` |

### Intermediate Consumers

| Module | Depends on |
|---|---|
| `@dxs-ts/gamut-api` | `gamut-cockpit-store`, `gamut-intl` |
| `@dxs-ts/gamut-theme` | `gamut-cockpit-store` |
| `@dxs-ts/gamut-shell` | `gamut-api` |
| `@dxs-ts/gamut-md` | `gamut-api` |
| `@dxs-ts/gamut-form-review` | `gamut-api`, `gamut-md` |
| `@dxs-ts/gamut-form` | `gamut-api`, `gamut-md`, `xui-datetime` |
| `@dxs-ts/gamut-primitives` | `gamut-api`, `gamut-md`, `gamut-shell`, `gamut-form-review` |

### Top-Level Consumer

| Module | Depends on |
|---|---|
| `@dxs-ts/gamut-routes` | `gamut-cockpit-store`, `gamut-api`, `gamut-shell`, `gamut-theme`, `gamut-md`, `gamut-primitives`, `gamut-form-review`, `gamut-form` |
| `@dxs-ts/gamut` (umbrella) | all of the above + `envir-util` |

### Dependency direction (acyclic)

```
envir-util ─────────────────────────────────────────────► gamut (umbrella)
gamut-cockpit-store ──► gamut-api ──► gamut-shell ──────► gamut-routes
                    └──► gamut-theme                  ┌──► gamut-routes
gamut-intl ──────────► gamut-api ──► gamut-md ─────────┤
                                  └► gamut-form-review ┤
xui-datetime ────────────────────────► gamut-form ─────┤
                               gamut-primitives ────────┘
```

---

## 4. Key Integration Points

### gamut-cockpit-store → gamut-api and gamut-theme

`CockpitStore.get()` is called at two points:
- In `api-config/config-types.ts` (`ConfigApi.mergeOptions`) and `api-offer/usePopulateContext.ts`, where `cockpitId` is injected into TanStack Query cache keys so queries re-fetch on cockpit change.
- In `GThemeProvider` (init), where the cockpit `aliasName` selects the initial entry from the `secondaryThemeOptions` map.

`window.GamutCockpit.enable()` / `.disable()` call `CockpitStore.save()`/`clear()` then `window.location.reload()`, causing both the theme and all queries to reinitialise from the new store value.

### gamut-api → feature components (context hooks)

`SecuredSetup` or `PublicSetup` in `__root.tsx` (gamut-routes) mounts each domain context provider (`IamBackendProvider`, `SiteBackendProvider`, `DialobProvider`, `OfferProvider`, `ContractProvider`, `CommsProvider`, `BookingProvider`). Feature components access domain data through hooks:

- `useIam()` — auth state, `authType`, `isFormLinkEnabled()`
- `useSite()` — `TopicView[]`, `getTopicGroups()`, `getLink()`
- `useOffers()` — `Offer[]` (mapped from `LegacyProcessApi.Process[]`)
- `useContracts()` — `Contract[]` with attachment upload/download
- `useComms()` — `Subject[]`, `unread`, `exchanges`
- `useBookings()` — `Booking[]`
- `useSearch()` / `useBackendSearch()` — `SearchStateImpl` with filtered results
- `useForm()` / `useFormTip()` — live Dialob `FormStore` and current page tip (gamut-form)
- `useDialobReview()` — completed session data (gamut-form-review)

No feature module below `gamut-routes` calls `window.fetch` directly; all network access flows through the fetch factory functions (`createSiteFetch`, `createDialobFetch`, etc.) assembled by `ConfigApi`.

### gamut-form: FormProvider → GFormBase → GFormBaseElement

`GForm` mounts `FormProvider` (from `api-dialob`), making `FormStore` available to the entire form tree. `GFormIterator` renders `GFormBase` for each item ID. `GFormBase` calls `useForm(id)` to read the item's `ActionItem` from the store. `GFormBaseElement` calls `useSlotVariant(actionItem)` and renders the resolved `*Dialob` input component. Each `*Dialob` wrapper closes the loop by writing user actions back to `ActionsQueue`.

### gamut-form-review: GFormReview → GFormReviewContextType → leaf components

`GFormReview` calls `useDialobReview(formId)` (from `gamut-api`), receives completed session data, and constructs a `GFormReviewContextType` instance. This context object is the only source of truth for the entire review tree: `getAnswer(id)`, `findValueSet(id)`, `getTranslated(item, lang)`, `substituteVariables(text, session)`, and `createItem(id, ...)` (which resolves the item's component from `ItemconfigType`). Components call `React.useContext(GFormReviewContext)` rather than receiving data as props.

### gamut-shell → gamut-primitives and gamut-routes (layout anchoring)

`GShellClassName` (a string constant exported by `gamut-shell`) appears in two places:
- `GAppBar` (gamut-primitives) applies it to the `Toolbar` so that `GShellRoot`'s scoped CSS selectors can apply toolbar-height layout calculations.
- `router.ts` (gamut-routes) passes it to `scrollToTopSelectors` in `createRouter`, making scroll restoration target the shell container on route navigation.

### gamut-theme: GThemeProvider and component default props

`GThemeProvider` wraps MUI's `ThemeProvider`. The bundled `ThemeOptions` in `g-theme-options` and `g-theme-options-alt-1` include a `components` map that supplies `defaultProps` for Gamut components (`GShell`, `GMarkdown`, `GUserOverview`, `GUserOverviewMenu`, `GArticleFeedback`) via module-local `INTERNAL_TYPE_PropsList` interfaces. Each component calls `useThemeProps({ props, name: MUI_NAME })` to merge these defaults at render time. `GComponentsPropsList` and `GComponentNameToClassKey` (gamut-routes `g-props`) extend MUI's `Components` interface so that consuming applications can configure all Gamut components in a single MUI `ThemeOptions` object.

### gamut-primitives: GInboxFormReview as the cross-layer bridge

`GInboxFormReview` (gamut-primitives) embeds `GFormReview` (gamut-form-review), linking the inbox feature (data from `useOffers`, `useContracts`, `CommsApi`) with the form review rendering layer. This is the only point where the primitives layer directly mounts a gamut-form-review component. The `formId` comes from the `OfferApi.Offer` associated with the selected inbox item.

### GRouterCockpits: runtime multi-tenant switching

`GRouterCockpits` (gamut-routes) listens for `Ctrl+Shift+D` and renders a `Dialog` containing `GCockpitDropdown`. Selecting a cockpit calls `CockpitStore.save()` and reloads the page, causing `GThemeProvider` to re-read the store and activate the matching secondary theme, and all TanStack Query keys scoped to `cockpitId` to invalidate and refetch.
