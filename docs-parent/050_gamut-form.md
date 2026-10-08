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
