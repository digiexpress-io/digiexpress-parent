# @dxs-ts/xui-datetime

## Purpose

Provides date and year picker input components for MUI-based UIs. Includes a segmented day/month/year text input with state machine, an interactive calendar popover, a standalone year picker, a `DateTimeFormatter` display component, and formatting utilities. No backend.

## Dependencies

**dependencies:** none

**peerDependencies:**
- `react`
- `react-intl`
- `@mui/material`
- `@mui/utils`
- `@mui/icons-material`
- `luxon`

## Module Relationships

`@dxs-ts/xui-datetime` is a leaf node in the Gamut dependency graph: it imports from no other `@dxs-ts/*` module.

**Direct consumer: `@dxs-ts/gamut-form`**

`gamut-form` imports `DatePicker` and `YearPicker`. They are used inside `GFormBaseElement`: when `useSlotVariant` resolves an `ActionItem` with a date or year type, the resulting slot renders the corresponding `xui-datetime` component. The Dialob wrapper around each picker calls an `ANSWER` action creator on change, pushing the value to `ActionsQueue`.

**Indirect consumers (via `gamut-form`)**

- `@dxs-ts/gamut-primitives` — depends on `gamut-form` for `GForm` and related components; does not import `xui-datetime` directly.
- `@dxs-ts/gamut-routes` — composes `gamut-form` inside `GRouterOffer` and `GRouterProduct`; does not import `xui-datetime` directly.
- `@dxs-ts/gamut` (umbrella) — aggregates all modules including `gamut-form`.

**Position in the dependency graph**

```
xui-datetime ───► gamut-form ───► gamut-primitives ───► gamut-routes ───► gamut (umbrella)
```

No module below `gamut-form` in the graph (e.g. `gamut-api`, `gamut-intl`, `gamut-cockpit-store`) depends on `xui-datetime`.

## Frontend Architecture

**Stack:** React, TypeScript, MUI Material, luxon, react-intl

**Sub-module structure:**

- `calendar-input` — segmented date text input. `DateInputStateMachine` (immutable class) holds all field state (`day`, `month`, `year`, `DateInputState`, `DateField`) and exposes transition methods (`typeDayDigit`, `typeMonthDigit`, `typeYearDigit`, `focusField`, `blurField`, `setFromDate`, `clear`). `CalendarInputProvider` wraps the machine in a React context (`CalendarInputContextType`) and manages focus refs (`dayRef`, `monthRef`, `yearRef`). `CalendarInput` and `Input` are the rendered field components.
- `calendar-interactive` — popover calendar UI. `CalendarProvider` wraps a locale-specific `IntlProvider` (via `getMessages`) and renders `Calendar`. `Calendar` manages month/year navigation state and toggles between `DayGrid` and `YearPicker`. `DayGrid` renders a Monday-based 7-column grid with min/max date clamping. `CalendarHeader` provides month navigation controls. `CardWrapper` is the popover shell. `intl.ts` supplies locale message maps. `calendar_constants.ts` exports `SQUARE_WIDTH`.
- `date-picker` — public `DatePicker` component. Composes `CalendarInputProvider`, `DateFieldContainer`, `XuiDateFieldRoot` (styled component), and a MUI `Popover` containing `CalendarProvider`. `useUtilityClasses` generates BEM class names via `@mui/utils`. Accepts `DatePickerProps`: `value`, `onChange`, `disabled`, `fullWidth`, `size`, `sx`, `onValidity`.
- `year-picker` — standalone `YearPicker` component. Mirrors the `date-picker` pattern with `YearInputProvider`, `YearFieldContainer`, `XuiYearPickerRoot`, and a `Popover` containing `calendar-interactive/YearPicker` (the grid). Accepts `YearPickerProps`: `value: number | null`, `onChange`, `disabled`, `fullWidth`, `size`, `sx`, `onValidity`.
- `date-time-formatter` — `DateTimeFormatter` React component. Parses a `string | Date` via ISO or SQL format using luxon, converts to local time, and renders date and time strings as a MUI `Stack` or inline text fragment depending on the `variant` prop.
- `date-format` — `formatAnyDateShort` function and `FormatAnyDateTimeShort` component. Both format a `Date | string` to Finnish short date (`fi` locale) using luxon.

**Patterns:**

- Immutable state machine: `DateInputStateMachine` returns new instances on every transition; React stores the machine in `useState` and derives UI from getters.
- Context provider + hook pair: `CalendarInputProvider` / `useCalendarInput` expose the machine and focus actions to child components without prop drilling.
- Popover anchor via `ref`: both `DatePicker` and `YearPicker` attach a `ref` to the root styled component and pass it as `anchorEl` to MUI `Popover`.
- Utility classes pattern: `useUtilityClasses` in `date-picker` and `year-picker` uses `@mui/utils` to generate class strings applied to styled root components.
- Auto-advance focus: after entering two valid digits for `day` or `month`, the state machine sets `shouldAutoAdvance` and shifts focus to the next field via `useEffect` in `CalendarInputProvider`.

## Error Handling

Validation errors are handled entirely within `DateInputStateMachine`. When all three fields (`day`, `month`, `year`) are non-empty, `tryValidateComplete()` runs range checks (day 1–31, month 1–12, year 1926–2099) and constructs a `Date` to verify the combination is real (e.g. 31 February). On failure it sets `DateInputState` to `'error'` and populates `data.error` with a plain string (`'Invalid date range'` or `'Invalid date'`). The machine also holds an `'empty'`, `'typing'`, and `'validating'` state, but only `'error'` and `'valid'` are observable externally.

Validation state surfaces to the consumer through two paths:

1. `DateFieldContainer` reads `machine.isValid` and the field-filled flags directly from the context provided by `useCalendarInput()`. It derives `isError: boolean` locally and calls the `onValidity(isError: boolean)` callback (passed as `DatePickerProps.onValidity`) via `useEffect` whenever the error state changes. The same pattern is replicated in `YearFieldContainer` using `useYearInput()` and `YearPickerProps.onValidity`.
2. `XuiDateFieldInput` and `XuiYearPickerInput` styled components receive `ownerState={{ isError }}` and apply a red border via MUI's `shouldForwardProp` / styled-component theming.

No API call layer exists in this module. There are no loading states or async error surfaces. `DateTimeFormatter` and `formatAnyDateShort` return a fallback string (`"-"` and `"--"` respectively) when `value` is `undefined` or falsy; they do not throw.

`useCalendarInput` and `useYearInput` both throw a plain `Error` if called outside their respective providers — this is a programming error, not a runtime one.

## Bootstrapping

None — leaf node. `DatePicker`, `YearPicker`, `DateTimeFormatter`, and `formatAnyDateShort` / `FormatAnyDateTimeShort` can be imported and rendered directly. The only peer-dependency setup the consuming app must provide is a `react-intl` `IntlProvider` ancestor in the tree (required by `DateFieldContainer` and `YearFieldContainer` for ARIA label formatting). `CalendarProvider` (rendered inside the `DatePicker` popover) mounts its own nested `IntlProvider` scoped to the popover, using the locale string passed by `DatePicker` from `useIntl()`. No app-level MUI theme extension or custom provider is required.

## TypeScript Extension Points

None identified. The module contains no `declare module` augmentations, no interface merging, and no extension record patterns.

## Known Limitations and Gotchas

**Hardcoded `console.log` in `blurField`**: `DateInputStateMachine.blurField()` (line 131) contains `console.log('next value', newData.month)` left from development. This will emit to the browser console on every month-field blur.

**Date construction sets hours to 15**: Both `CalendarInputProvider` (line 99, `finalDate.setHours(15)`) and `DayGrid.handleDateClick` (line 59, `new Date(year, month, day, 15)`) hard-code the time component to 15:00. The resulting `Date` objects passed to `onChange` are not midnight-normalised and are not in UTC. Callers storing ISO strings should be aware the time will be `T15:00:00` in local time.

**Year range hardcoded in two places**: The calendar popover (`CalendarProvider`) clamps `minDate` / `maxDate` to approximately 1925–2100 using runtime `DateTime.now()` arithmetic (`NOW.minus({ years: 101 })` / `NOW.plus({ years: 75 })`). The `DateInputStateMachine.tryValidateComplete()` uses fixed integer bounds (`yearNum <= 1925 || yearNum >= 2100`). The two ranges are not identical and will diverge as time passes.

**`YearInputProvider.isError` is always `false`**: `YearInputProvider` initialises `isError` with `useState(false)` and the `handleInputChange` function sets it to `false` unconditionally on every change. No code path ever sets it to `true`. The `onValidity` callback on `YearPicker` will therefore never receive `true` through the text-input path (it can only receive `true` if the parent sets it externally).

**`FormatAnyDateTimeShort` accepts `value: any`**: `date-format/index.tsx` types the prop as `any` with no runtime guard beyond a falsy check.

**`DateTimeFormatter` only handles ISO and SQL formats**: The `parse()` function in `date-time-formatter/DateTimeFormatter.tsx` has a `// TODO other formats` comment and returns the (invalid) SQL-parsed `DateTime` as a fallback when both ISO and SQL parsing fail, producing `"Invalid DateTime"` in the rendered output.

**`DayGrid` uses inline `sx` with `display: grid`**: The grid layout in `DayGrid.tsx` (line 64) uses `sx={{ display: 'grid', ... }}` directly on a `Box`. This is inconsistent with the styled-component pattern used in `date-picker` and `year-picker`.

## Scope Boundaries

`@dxs-ts/xui-datetime` provides only client-side date and year input UI. It does not perform any API calls, manage form state, integrate with Dialob or any questionnaire engine, provide time-of-day input, handle timezone conversion, expose a date range picker, or manage validation at the form level. Integration with Dialob action creators and form state management is the responsibility of the consuming module (`@dxs-ts/gamut-form`).
