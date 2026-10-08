# @dxs-ts/gamut-form

## Purpose

Renders interactive Dialob form sessions as a tree of MUI-styled React components. Provides the top-level form shell (`GForm`), a recursive item iterator (`GFormIterator`), a slot dispatcher (`GFormBaseElement`) that maps each Dialob item type to a concrete input component, and all individual input components (text, integer, decimal, currency, date, time, boolean, list, multilist, survey, address, file upload, textarea, note, group, row).

## Dependencies

### dependencies

| Package | Version |
|---|---|
| `@dxs-ts/gamut-api` | `*` |
| `@dxs-ts/gamut-md` | `*` |
| `@dxs-ts/xui-datetime` | `*` |

### peerDependencies

| Package | Version |
|---|---|
| `react` | `*` |
| `react-intl` | `*` |
| `react-leaflet` | `*` |
| `@mui/material` | `*` |
| `@mui/icons-material` | `*` |
| `@mui/system` | `*` |
| `@mui/utils` | `*` |
| `@mui/types` | `*` |
| `luxon` | `*` |
| `numbro` | `*` |
| `leaflet` | `*` |
| `@tanstack/react-store` | `*` |

## Module Relationships

### Depends on

- `@dxs-ts/gamut-api` — `FormProvider`, `useForm`, `useFormTip`, `MapProvider`, `ActionsQueue`; `DialobApi` type namespace (`ActionItem`, `ActionError`, `Form`, `FormStore`, `ControlLabelPosition`); `GOverridableComponent`, `useVariantOverride`
- `@dxs-ts/gamut-md` — `GMarkdown`, used in `GFormNote` and `GFormNoteDialob` to render Dialob note label text as markdown
- `@dxs-ts/xui-datetime` — `DatePicker`, `YearPicker` used by `GInputDate` / `GInputDateDialob` and `GInputTime` / `GInputTimeDialob`; `DateInputStateMachine` for date field state

### Consumed by

- `@dxs-ts/gamut-routes` — mounts `GForm` as the entry point for Dialob form sessions; `GRouterOffer` renders `GForm` inside the `DialobProvider` context; `GComponentsPropsList` and `GComponentNameToClassKey` (in `g-props`) extend MUI's `Components` interface to include Gamut form components for theme configuration
- `@dxs-ts/gamut` (umbrella) — re-exports the full `gamut-form` public surface as part of the monorepo's top-level package

## Frontend Architecture

**Stack:** React, MUI (`@mui/material`, `@mui/icons-material`, `@mui/system`, `@mui/utils`), `react-intl`, `react-leaflet` / `leaflet`, `luxon`, `numbro`, `@tanstack/react-store`

**Sub-module structure:**

- `g-form` — `GForm` (entry point, wraps `FormProvider`, renders `GFormTip` / `GFormIterator`), `GFormIterator` (recursively maps `tip.items` to `GFormBase` nodes)
- `g-form-base` — `GFormBase` resolves a Dialob item by id from the form store and renders it via `GFormBaseElement`; uses `useThemeInfra` for MUI theme-prop resolution and `useVariantOverride` for CSS overrides
- `g-form-base-element` — `GFormBaseElement` dispatches to the correct slot component via `useSlotVariant`; registers each element with `useGFormErrorVisibility` for scroll-to-error tracking
- `g-form-error-visibility` — `GFormErrorVisibilityProvider` / `useGFormErrorVisibility` manages per-page error visibility state and scroll-to-first-error via `useScrollTo`
- `g-form-page` — `GFormPage` / `GFormPageDialob` render a Dialob page container with `GFormErrorVisibilityProvider`
- `g-form-group` — `GFormGroup`, `GFormGroupCollapsible`, `GFormGroupDialob` render grouped items, optionally collapsible
- `g-form-stepper` — `GFormStepper` renders a page-step navigation indicator
- `g-form-note` — `GFormNote` / `GFormNoteDialob` render Dialob note items as MUI `Alert`-like elements with optional `GMarkdown` body and popover description
- `g-form-note-validation` — `GFormNoteValidation` / `GFormNoteValidationDialob` render validation-scoped note items
- `g-input-base` — `GInputBase` is the shared input shell: `Grid`-based layout with `label`, `adornment`, `input`, `error`, and optional `secondary` slots; conditionally hides errors based on `useGFormErrorVisibility`
- `g-input-label` — `GInputLabel` renders field labels; `GInputCurlyBracket` provides a decorative bracket element
- `g-input-error` — `GInputError` renders `DialobApi.ActionError[]` beneath an input
- `g-input-adornment` — `GInputAdornment` renders a help/description popover trigger
- `g-input-text` — `GInputText` / `GInputTextDialob` for `type=text,view=text` items; supports read-only and editable variants via `ReadOnlyTextInput` / `TextInput` (MUI `TextField`)
- `g-input-textarea` — `GInputTextArea` / `GInputTextAreaDialob` for `type=text,view=text-textBox` items
- `g-input-currency` — `GInputCurrency` / `GInputCurrencyDialob` for `type=text` items with `props.currency` set; uses `numbro` for formatting
- `g-input-int` — `GInputInt` / `GInputIntDialob` for `type=number` items
- `g-input-decimal` — `GInputDecimal` / `GInputDecimalDialob` for `type=decimal` items
- `g-input-date` — `GInputDate` / `GInputDateDialob` for `type=date` items; includes `DateAndCalendar` and `YearAndCalendar` sub-components
- `g-input-time` — `GInputTime` / `GInputTimeDialob` for `type=time` items; includes `TimeAndCalendar`
- `g-input-boolean` — `GInputBoolean` / `GInputBooleanDialob` for `type=boolean` items; renders as `SingleCheckbox` or `YesAndNoCheckbox`; includes `ReadOnlyYesAndNoCheckbox`
- `g-input-list` — `GInputList` / `GInputListDialob` for `type=list` items; renders as `GInputDropdown` or `GInputRadio`
- `g-input-multilist` — `GInputMultilist` / `GInputMultilistDialob` for `type=multichoice` items; renders as `CheckboxList` or `MultilistAutocomplete`
- `g-input-autocomplete` — `GInputAutocomplete`, `GInputAutocompleteMulti`, `GInputAutocompleteUni`, `GInputAutocompleteProvider`, `GInputAutocompleteOption` provide a shared autocomplete primitive used by list/multilist inputs
- `g-input-survey` — `GInputSurvey` / `GInputSurveyDialob` for `type=surveygroup` items; renders `GInputSurveyOption` columns
- `g-input-survey-question` — `GInputSurveyQuestion` / `GInputSurveyQuestionDialob` for individual `type=survey` row items
- `g-input-group` — `GInputGroup` / `GInputGroupDialob` for `type=rowgroup` items
- `g-input-group-row` — `GInputGroupRow` / `GInputGroupRowDialob` for `type=row` items within a rowgroup
- `g-input-address` — `GInputAddress` / `GInputAddressDialob` for `type=text,view=text-address` items; embeds `InputMap` (Leaflet map), `InputAddress` (text field with geocoding), `InputProvider` (local state for lat/lng), wrapped in `MapProvider` from `gamut-api`
- `g-input-upload` — `GInputUpload` / `GInputUploadDialob` for `type=text,view=text-fileUpload` items

**Patterns:**

- **Slot dispatch via `useSlotVariant`**: `GFormBaseElement` inspects `ActionItem.type`, `ActionItem.view`, and `ActionItem.props` to resolve a `GFormBaseSlotVariant` string, then looks up the concrete component in the `Slots` record.
- **Dialob variant components**: Each input sub-module exports both a standalone component (e.g. `GInputText`) and a `*Dialob` wrapper (e.g. `GInputTextDialob`) that reads from the form store via `useForm` and maps store state to the standalone component's props.
- **MUI theme override integration**: All components call `useThemeProps` with a `MUI_NAME` constant, enabling per-component style and prop overrides via MUI theme `components` configuration. Styled roots use `useVariantOverride` (from `gamut-api`) for variant-keyed CSS resolution.
- **`GInputBase` slot layout**: Input components compose their UI by passing typed `slots` and `slotProps` to `GInputBase`, which renders label, adornment, input, error, and secondary slots in a responsive MUI `Grid`.
- **Error visibility**: `GFormErrorVisibilityProvider` (mounted per page) tracks whether validation errors should be shown. `GFormBaseElement` registers each item's DOM ref; `useScrollTo` scrolls to the first invalid field when errors become visible.
- **`component` prop override**: Top-level components accept an optional `component` prop of type `GOverridableComponent`, allowing full component substitution at render time.

## Error Handling

Validation errors surface as `DialobApi.ActionError[]` values on each item, read from `DialobApi.FormStore` via `store.form.toErrors(id)`. They are rendered by `GInputError`, which displays one MUI `FormHelperText` per error entry. `GInputError` suppresses rendering until `useGFormErrorVisibility` reports that errors should be visible for the given field.

Visibility is controlled per page by `GFormErrorVisibilityProvider` (in `g-form-error-visibility/GFormErrorVisibility.tsx`). The context type is `GFormErrorVisibilityContextType`, with `isErrorsVisible` and `setErrorsVisible` as its primary surface. Errors become visible either when `setErrorsVisible` is called explicitly (e.g. on a next-page navigation attempt) or when a field has already been edited (`store.form.isItemEdited(controlId)` returns `true`).

Scroll-to-first-error is handled by `RefState` (in `g-form-error-visibility/useScrollTo.ts`), which maintains a `@tanstack/react-store` `Store` of per-page DOM refs. On `setErrorsVisible`, the store triggers `scrollTo()` after a 250 ms `setTimeout`, resolving to the item identified by `DialobApi.ControlPage.firstErrorControlId`.

Unknown item types do not throw. `GFormBaseElement` falls through to `UnknownSlot` (in `g-form-base-element/UnknownSlot.tsx`), which logs the element via `console.error` and renders a plain `<div>` displaying the item id.

There is no loading-state error boundary in this module. The loading state in `GForm` is a `CircularProgress` spinner shown while `useFormTip()` returns a falsy tip value; no error UI is rendered if the tip never resolves.

## Bootstrapping

`GForm` mounts `FormProvider` from `@dxs-ts/gamut-api` internally. A consuming application does not need to wrap `GForm` in `FormProvider` manually — `GForm` does this itself using the `executionId`, `variant`, `onAfterComplete`, `onCancel`, `debugFormId`, and `formUnavailable` props it receives.

The address input (`GInputAddressDialob`) depends on `MapProvider` from `@dxs-ts/gamut-api` being present in the React tree above `GForm`. If `MapProvider` is absent, `useMap()` inside `InputAddress` will fail at runtime. `MapProvider` must be configured with a geocoding backend before `GForm` is mounted if address fields are used.

`GFormErrorVisibilityProvider` is mounted automatically per page by `GFormPageDialob`; no external setup is required.

MUI theme overrides for gamut-form components are registered by the consuming application's MUI theme under the `components` key, using each component's `MUI_NAME` constant (e.g. `GFormBaseElement`, `GInputError`, `GInputLabel`, etc.). This is optional; components function without theme overrides.

## TypeScript Extension Points

None identified. The module contains no `declare module` augmentations and no interface merging. MUI theme integration is done via `useThemeProps` and `useDefaultProps` at runtime using string-keyed `MUI_NAME` constants, not via TypeScript module augmentation within this package. Theme augmentation (registering gamut-form components in MUI's `Components` interface) is performed by `@dxs-ts/gamut-routes` via `GComponentsPropsList` and `GComponentNameToClassKey` in its `g-props` sub-module, outside this package.

## Known Limitations and Gotchas

**`currency` prop empty string falls back to plain text.** `useSlotVariant` routes to `text-currency` only when `element.props?.currency` is truthy. If the `currency` key is present in Dialob Composer but its value is empty, the item renders as a plain `GInputTextDialob` with no currency formatting. This is documented in `DIALOB_FILL_PROPS.md` but contradicts the expectation that setting the key at all enables currency mode.

**Boolean props in Dialob Composer must be strings, not booleans.** Props such as `border`, `collapsible`, and `columns` are read from `element.props` as strings. Passing a native boolean `true` from the Dialob form definition will not match the expected string `'true'` and will fall back to the default. Documented in `DIALOB_FILL_PROPS.md`.

**`variant`, `controlType`, and `currency` props reroute the rendered component entirely.** Setting any of these on a Dialob item changes which component class is instantiated by `GFormBaseElement`, not just its appearance. Misconfiguring these produces an `UnknownSlot` render (visible `unknown = <id>` text in the UI plus a `console.error`).

**`rows` on `GInputTextArea` is clamped to 1–20.** `GInputTextAreaDialob` applies `safeParseRows` which silently clamps any out-of-range value; values outside the range produce no warning.

**Scroll-to-error uses a fixed 250 ms `setTimeout`.** `RefState.consumed` in `useScrollTo.ts` schedules the scroll with `setTimeout(() => this.scrollTo(), 250)`. If the DOM has not painted by then (e.g. on slow devices or under heavy re-render), the scroll target coordinates may be stale.

**`list-radio` slot variant is commented out.** In `useSlotVariant.tsx`, the `list-radio` branch is wrapped in a block comment. A `type=list, view=radio` item will fall through to the generic `variant = element.type + '-' + element.view` path, producing `'list-radio'`, which has no entry in the `Slots` record and will render as `UnknownSlot`.

**Address geocoding depends on `MapProvider`'s external service.** `InputAddress` calls `map.findAll(newValue)` via debounce. Network failures or misconfigured `MapProvider` produce unhandled promise rejections; there is no error state or fallback UI in `InputAddress`.

**`GInputUpload` stores only a text value.** `GInputUploadDialob` calls `store.setAnswer(element.id, event.target.value)` — the file content itself is not transmitted through the form store. Actual file upload handling must be wired externally.

## Scope Boundaries

This module is responsible only for rendering Dialob form sessions as a React component tree. It does not manage session lifecycle (creation, submission, or cancellation of Dialob sessions), network communication with the Dialob API, authentication, routing, form definition authoring, or theme registration in MUI's `Components` interface. It does not implement a file upload transport — `GInputUpload` captures a text value only. Geocoding service configuration and the `MapProvider` connection are the responsibility of the consuming application. Multi-language message loading is delegated to `react-intl`; this module provides no `IntlProvider`.

## TanStack Integration

**Packages in use:** `@tanstack/react-store` only. `@tanstack/react-query` and `@tanstack/react-router` are not used by any component in this module.

### Dialob form writes bypass TanStack Query entirely

All user interactions that mutate form state — `setAnswer`, `next`, `previous`, `complete`, `addRow`, etc. — are dispatched through `ActionsQueue` (`api-dialob/impl-actions-queue.ts` in `@dxs-ts/gamut-api`). `ActionsQueue` is a plain class: it batches and debounces actions, POSTs them via `fetchActionPost`, and applies server-returned updates to local `FormImpl` state. `useMutation` from `@tanstack/react-query` is not called anywhere in this module. There is no query invalidation or `refetch()` call triggered from `gamut-form` components.

### `@tanstack/react-store` in `useScrollTo.ts`

`@tanstack/react-store` is used in one file: `g-form-error-visibility/useScrollTo.ts`. The `RefState` class instantiates `new Store(...)` directly as a plain class field to maintain a map of per-page DOM refs keyed by Dialob control id. The store is not shared across components — it is scoped to a single `useScrollTo` call inside `GFormErrorVisibilityProvider`. The reactive hooks `useStore` and `useSelector` from `@tanstack/react-store` are not used; `RefState` reads and writes the store via its `.state` property directly. On `setErrorsVisible`, `RefState.consumed` schedules `this.scrollTo()` after a 250 ms `setTimeout`, resolving the scroll target from `DialobApi.ControlPage.firstErrorControlId`.

### Components do not call `useQuery` directly

No component in `gamut-form` calls `useQuery`. Form session data — `tip`, `items`, `errors`, `pages` — is read from `DialobApi.FormStore` via `useForm` and `useFormTip`, which are context hooks provided by `FormProvider` in `@dxs-ts/gamut-api`. `FormProvider` is mounted by `GForm` itself. The query that backs `DialobContext` is owned by `gamut-api`; `gamut-form` components consume it only through these typed context hooks.

### TanStack packages not used in this module

- `@tanstack/react-query` — no `useQuery`, `useSuspenseQuery`, `useMutation`, `QueryClient`, or `QueryClientProvider` calls
- `@tanstack/react-router` — no `useNavigate`, `createFileRoute`, `Link`, or search param hooks; routing is the responsibility of `@dxs-ts/gamut-routes`
- `@tanstack/react-table` and `@tanstack/react-form` — not used anywhere in Gamut