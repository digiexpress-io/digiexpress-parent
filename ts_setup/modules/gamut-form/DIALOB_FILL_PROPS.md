# Dialob Fill Component Properties

## Group _(GFormGroup)_

### Properties

| Key | Value | What it produces | Default |
|-----|-------|-----------------|---------|
| `border` | `true` / `false` | Wraps the group in a bordered container (Paper-like) | |
| `collapsible` | `true` / `false` | Renders the group as an accordion — collapsed by default | |
| `columns` | `2`, `3` | Lays children out in N columns | |

---

## Multi-row group _(GInputGroup)_

### Properties

| Key | Value | What it produces | Default |
|-----|-------|-----------------|---------|
| `border` | `true` / `false` | Wraps the group in a bordered container | |

---

## Multi-row group item _(GInputGroupRow)_

### Properties

| Key | Value | What it produces | Default |
|-----|-------|-----------------|---------|
| `columns` | `2`, `3` | Lays children out in N columns | |

---

## Multi-choice _(GInputMultilist)_

### Properties

| Key | Value | What it produces | Default |
|-----|-------|-----------------|---------|
| `border` | `true` / `false` | Wraps in a bordered container | |
| `variant` | `multilist` | Autocomplete-style multi-select input | X |
| `variant` | `radio` | Checkbox list for multi-selection | |

---

## Survey _(GInputSurvey)_

### Properties

| Key | Value | What it produces | Default |
|-----|-------|-----------------|---------|
| `border` | `true` / `false` | Wraps in a bordered container | |

---

## Note _(GFormNote)_

### Properties

| Key | Value | What it produces | Default |
|-----|-------|-----------------|---------|
| `style` | `error` | Red — critical information | |
| `style` | `warning` | Orange — caution | |
| `style` | `info` | Blue — informational | |
| `style` | `success` | Green — confirmation | |

---

## Validation message _(GFormNoteValidation)_

### Properties

| Key | Value | What it produces | Default |
|-----|-------|-----------------|---------|
| `style` | `error` | Red | X |
| `style` | `warning` | Orange | |
| `style` | `info` | Blue | |
| `style` | `success` | Green | |

---

## Text box _(GInputTextArea)_

### Properties

| Key | Value | What it produces | Default |
|-----|-------|-----------------|---------|
| `rows` | `4`, `10`, etc. | Sets visible height. Clamped 1–20 | 10 |
| `charLimit` | `200`, `500`, etc. | Shows live counter below the input; blocks input at limit; counter turns red when limit is reached | |

---

## Text _(GInputText)_

By default renders a plain text input. The following properties transform it into a different type of text field entirely.

### Properties

| Key | Value | What it produces | Default |
|-----|-------|-----------------|---------|
| `currency` | `EUR`, `€`, `USD`, `$`, etc. | Routes to currency input; shows adornment at start of field; Finnish locale formatting (space = thousands, comma = decimal); 2 decimal places enforced on blur | |
| `controlType` | `fileUpload` | Routes to file upload component | |

**Note:** If `currency` key is present but the value is empty, the input renders as a plain text box with no adornment and no currency formatting.

---

## Choice _(GInputList)_

### Properties

| Key | Value | What it produces | Default |
|-----|-------|-----------------|---------|
| `variant` | `autocomplete` | Dropdown with autocomplete text input | X |
| `variant` | `radio` | Flat list of choices with radio buttons | |

---

## Boolean _(GInputBoolean)_

### Properties

| Key | Value | What it produces | Default |
|-----|-------|-----------------|---------|
| `variant` | `checkbox` | Two buttons — Yes and No — each individually togglable | X |
| `variant` | `singleCheckbox` | Single full-width button with checkbox icon and inline label; toggles true/false | |

**singleCheckbox behaviour:**
- Clicking an unchecked button stores `true`; clicking a checked button stores `false`
- `undefined` initialises as no selection (no answer stored until first click)
- The label configured in Dialob Composer appears inline to the right of the checkbox icon
- The button is right-aligned in the form layout (occupies the input column, not the label column)
- Disabled and read-only states are visually consistent with the standard `checkbox` variant

---

### Notes

- All props are optional. Missing props fall back to component defaults.
- Boolean props (`border`, `collapsible`) must be set as the string `'true'` in Dialob Composer — not as a native boolean.
- `variant`, `controlType`, and `currency` affect which component is rendered entirely — not just styling.
- `style`, `rows`, and `columns` are purely presentational.
