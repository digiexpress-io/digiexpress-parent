# Dialob Fill Component Properties

Properties configured in Dialob Composer are stored in `element.props` and read by the `*Dialob.tsx` wrapper components. All values arrive as strings. Boolean-like values must be compared with `=== 'true'`, not `=== true`.

---

# Group

*(GFormGroup)*

## How to use them

| Key | Value | What it produces |
|-----|-------|-----------------|
| `border` | `true` / `false` | Wraps the group in a bordered container (Paper-like). Default: no border |
| `collapsible` | `true` / `false` | Renders the group as an accordion — collapsed by default |
| `columns` | `2`, `3` | Lays children out in N columns. Default: single column |

---

# Multi-row group

*(GInputGroup)*

## How to use them

| Key | Value | What it produces |
|-----|-------|-----------------|
| `border` | `true` / `false` | Wraps the group in a bordered container |

---

# Multi-row group item

*(GInputGroupRow)*

## How to use them

| Key | Value | What it produces |
|-----|-------|-----------------|
| `columns` | `2`, `3` | Lays children out in N columns. Default: single column |

---

# Multi-choice

*(GInputMultilist)*

## How to use them

| Key | Value | What it produces |
|-----|-------|-----------------|
| `border` | `true` / `false` | Wraps in a bordered container |
| `variant` | `multilist` | Checkbox list for multi-selection *(default)* |
| `variant` | `autocomplete` | Autocomplete-style multi-select input |

---

# Survey

*(GInputSurvey)*

## How to use them

| Key | Value | What it produces |
|-----|-------|-----------------|
| `border` | `true` / `false` | Wraps in a bordered container |

---

# Note

*(GFormNote)*

## How to use them

| Key | Value | What it produces |
|-----|-------|-----------------|
| `style` | `error` | Red — critical information |
| `style` | `warning` | Orange — caution |
| `style` | `info` | Blue — informational *(typical default)* |
| `style` | `success` | Green — confirmation |

---

# Validation message

*(GFormNoteValidation)*

## How to use them

| Key | Value | What it produces |
|-----|-------|-----------------|
| `style` | `error` | Red |
| `style` | `warning` | Orange |
| `style` | `info` | Blue |
| `style` | `success` | Green |

---

# Text box

*(GInputTextArea)*

## How to use them

| Key | Value | What it produces |
|-----|-------|-----------------|
| `rows` | `4`, `10`, etc. | Sets visible height. Clamped 1–20. Default: component default |
| `charLimit` | `200`, `500`, etc. | Shows live `current / max` counter below the input; blocks input at limit; counter turns red when limit is reached |
| `charCount` | `true` | Shows current character count below the input (no maximum). Hidden when `charLimit` is also set |

---

# Text

*(GInputText)*

## How to use them

| Key | Value | What it produces |
|-----|-------|-----------------|
| `currency` | `EUR`, `€`, `USD`, `$`, etc. | Routes to currency input; shows symbol as end adornment; Finnish locale formatting (space = thousands, comma = decimal); 2 decimal places enforced on blur |
| `unitLabel` | `km`, `kg`, `€/h`, `%`, etc. | Routes to numeric-only input; shows unit symbol as end adornment; no number formatting |
| `controlType` | `fileUpload` | Routes to file upload component |

**Note:** If `currency` or `unitLabel` key is present but the value is empty, the input renders as a plain text box with no adornment.

---

# Choice

*(GInputList)*

## How to use them

| Key | Value | What it produces |
|-----|-------|-----------------|
| `variant` | `autocomplete` | Dropdown with autocomplete text input *(default)* |
| `variant` | `radio` | Flat list of choices with radio buttons |

---

# Boolean

*(GInputBoolean)*

## How to use them

| Key | Value | What it produces |
|-----|-------|-----------------|
| `variant` | `checkbox` | Two buttons — Yes and No — each individually togglable *(default)* |
| `variant` | `singleCheckbox` | Single full-width button with checkbox icon and inline label; toggles true/false |

**singleCheckbox behaviour:**
- Clicking an unchecked button stores `true`; clicking a checked button stores `false`
- `undefined` initialises as no selection (no answer stored until first click)
- The label configured in Dialob Composer appears inline to the right of the checkbox icon
- The button is right-aligned in the form layout (occupies the input column, not the label column)
- Disabled and read-only states are visually consistent with the standard `checkbox` variant

---

## Notes

- All props are optional. Missing props fall back to component defaults.
- Boolean props (`border`, `collapsible`, `charCount`) must be set as the string `'true'` in Dialob Composer — not as a native boolean.
- `variant`, `controlType`, `currency`, and `unitLabel` affect which component is rendered entirely — not just styling.
- `style`, `rows`, and `columns` are purely presentational.
