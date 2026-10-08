# gamut-form-review

## Purpose

Renders a read-only review of a completed Dialob form session. Fetches questionnaire session data via `@dxs-ts/gamut-api`, builds a typed context from the response, and delegates rendering to a set of per-answer-type components. Unanswered items are suppressed. The item-to-component mapping is configurable via `ItemconfigType` and defaults to `DEFAULT_ITEM_CONFIG`.

## Dependencies

**dependencies:**
- `@dxs-ts/gamut-api` — `*`
- `@dxs-ts/gamut-md` — `*`

**peerDependencies:**
- `react` — `*`
- `react-intl` — `*`
- `react-markdown` — `*`
- `@mui/material` — `*`
- `@mui/icons-material` — `*`
- `@mui/system` — `*`
- `@mui/utils` — `*`

## Module Relationships

### Depends on

- `@dxs-ts/gamut-api` — provides `useDialobReview` hook (fetches completed session + form data by `formId`); `GOverridableComponent` type used in `GroupItemProps`.
- `@dxs-ts/gamut-md` — provides `GMarkdown`; used by `GFormReviewGroup` to render optional item description text via `react-markdown`.

### Consumed by

- `@dxs-ts/gamut-primitives` — `GInboxFormReview` mounts `GFormReview` and passes a `formId` sourced from the `OfferApi.Offer` associated with the selected inbox item. This is the only point in the stack where a primitives component directly embeds a `gamut-form-review` component.
- `@dxs-ts/gamut-routes` — lists `gamut-form-review` as a direct dependency; `GRouterOffer` navigates to `GRouterOfferSummary` after form submission, which hosts the review surface indirectly through `gamut-primitives`.
- `@dxs-ts/gamut` (umbrella) — re-exports the module as part of the full package bundle.

## Frontend Architecture

**Stack:** React, MUI (Material UI v6 Grid2, Typography, Box, CircularProgress, styled, useThemeProps, generateUtilityClass, composeClasses), react-markdown.

**Sub-module structure:**
- `g-form-review` — top-level entry: `GFormReview` component, `useUtilityClasses`, and `GFormReviewRoot` styled wrapper.
- `g-form-review-components` — all item-type components, context class, context object, item config, and shared types.

**Key components:**

- `GFormReview` (`g-form-review/GFormReview.tsx`) — root component. Accepts `formId` and optional `itemConfig`. Calls `useDialobReview`, shows `CircularProgress` while pending, locates the root `questionnaire` item in `form.data`, instantiates `GFormReviewContextType`, and renders `GFormReviewQuestionnaire` inside a `GFormReviewContext.Provider`.
- `GFormReviewContextType` (`g-form-review-components/GFormReviewContext.tsx`) — class that wraps session and form data. Methods: `getAnswer`, `getTranslated`, `substituteVariables`, `createItem`, `findValueSet`, `getItem`, `getLanguage`. `createItem` resolves an item id to an `ItemConfigEntry`, checks `answerRequired`/`childrenRequired`, and returns the matched React component or `null`.
- `GFormReviewQuestionnaire` (`g-form-review-components/GFormReviewQuestionnaire.tsx`) — maps the root item's `items` array through `dC.createItem(id, null, true)` and renders results inside a styled `Grid2`.
- `GFormReviewGroup` (`g-form-review-components/GFormReviewGroup.tsx`) — renders a labeled group with a nested `GroupContext` that tracks depth (1–4) to select heading variant (`h3`–`h6`). Uses `react-markdown` for optional description. Skips groups with no renderable children.
- `GFormReviewChoice` (`g-form-review-components/GFormReviewChoice.tsx`) — resolves a `list`-type answer against the form's value set via `dC.findValueSet`, then renders the translated label through `GFormReviewItem`.
- `GFormReviewItem` (`g-form-review-components/GFormReviewItem.tsx`) — shared label+value layout primitive used by leaf answer components.
- `TimestampFormatter` (`g-form-review-components/TimestampFormatter.tsx`) — utility component, used by date/time components.

**Default item config (`DEFAULT_ITEM_CONFIG` in `itemConfig.ts`):**

Maps Dialob item types to components:

| Dialob type | Component | Answer required |
|---|---|---|
| main group item (flag) | `GFormReviewPage` | no |
| `group` + `view=page` | `GFormReviewPage` | no |
| `group` | `GFormReviewGroup` | no |
| `surveygroup` | `GFormReviewSurveyGroup` | no |
| `rowgroup` | `GFormReviewRowGroup` | no |
| `boolean` | `GFormReviewBoolean` | yes |
| `text` | `GFormReviewText` | yes |
| `list` | `GFormReviewChoice` | yes |
| `multichoice` | `GFormReviewMultiChoice` | yes |
| `note` | `GFormReviewNote` | no |
| `date` | `GFormReviewDate` | yes |
| `time` | `GFormReviewTime` | yes |
| `number` / `decimal` | `GFormReviewDecimal` | yes |

**Patterns:**
- MUI theme overrides: every component uses `useThemeProps` + `generateUtilityClass` + `composeClasses` to expose MUI `styleOverrides`-compatible class slots.
- Context-driven rendering: `GFormReviewContextType` is the single source of truth for answer lookup, translation, value-set resolution, and item instantiation. Components read it via `React.useContext(GFormReviewContext)`.
- Configurable item registry: `ItemconfigType` and `ItemConfigEntry` allow callers to pass a custom `itemConfig` to `GFormReview`, replacing or extending the default matcher/component pairs.
- Null suppression: `createItem` returns `null` for unanswered items (when `answerRequired` is true) and for groups with no renderable children (when `childrenRequired` is true), so the rendered output contains only answered content.
