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
