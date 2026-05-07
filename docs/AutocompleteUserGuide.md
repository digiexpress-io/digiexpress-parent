# Flow YAML autocomplete – user guide

Autocomplete in the **Wrench Flow editor** helps you build a flow YAML without
having to remember every keyword, indentation level, or asset name. This guide
explains **what suggestions you can get**, **where they show up**, and **how to
use them** while editing.

---

## How to trigger autocomplete

While editing a flow:

| Action | What happens |
|---|---|
| Press **`Ctrl` + `Space`** | Opens the suggestion list manually at the cursor. |
| Press **`Esc`** | Closes the suggestion list without inserting anything. |

A few practical rules apply to all suggestions in the flow editor:

1. **The flow must currently parse.** Suggestions are computed against the
   server-side AST. If the YAML has a syntax error (e.g. a task without `id`),
   you may temporarily get **no suggestions at all** until you fix the error.
2. **Most "new node" suggestions only appear on a blank line.** If the
   suggestion you expect is missing, move the cursor to an empty line at the
   right indentation level and try again.
3. **Suggestions are context aware.** The same shortcut produces different
   results under `inputs:` than it does under `tasks:` etc.

---

## Quick reference

| You are editing… | You can get suggestions for… |
|---|---|
| The top of the flow (no `id` yet) | `id` |
| After `id`, no `description` yet | `description` |
| After `id` / `description`, no `inputs` yet | `inputs block` (template) |
| After `inputs`, no `tasks` yet | `tasks block` (template) |
| Inside an input | `type: …`, `required: …`, `debugValue` |
| Empty line under `inputs:` / between / after inputs | `new input` (template) |
| A task that has no `id` | `id` |
| A task that has `id` but no `then` | `then: end`, `then: <other-task>`, `new switch task` |
| Empty line under `tasks:` / between / after tasks  | `new service task`, `new decision task`, `new switch task` |
| `ref:` line under `service:` | All matching site **services** |
| `ref:` line under `decisionTable:` | All matching site **decision tables** |
| `collection:` line under `service:` / `decisionTable:` (or empty line in one of those bodies missing `collection`) | `collection: true`, `collection: false` |
| `inputs:` block under a service / decision task | `add missing mapping: <name> <type>` |
| A single mapping line under that `inputs:` | `flow input: …`, `task output: …` |
| `then:` line inside a `switch:` case (or empty line in a case missing `then`) | `then: end`, `then: <other-task>` |

---

## Building a flow from scratch

The hints are designed so you can write a flow top-to-bottom, accepting
suggestions in order. A typical session looks like this:

### 1. Start with `id`

Empty file. Trigger autocomplete on the first line:

```yaml
id            <-- suggestion: "id"
```

Accept the suggestion, type the flow id, then press **Enter**.

### 2. Add a `description` (optional)

On a new line **after** `id`, trigger autocomplete:

```yaml
id: my-flow
description   <-- suggestion: "description"
```

The `description` suggestion only shows up after `id` exists and before any
`inputs` / `tasks` blocks.

### 3. Add the `inputs` block

On a new line **after** `id` (and optional `description`), trigger autocomplete.
You will get a multi-line **template** that includes a sample input:

```yaml
inputs:
  myInputParam:
    required: true
    type: STRING
    debugValue: "test-string"
```

Suggestion label: **`inputs block`**. Replace `myInputParam`, the type, and the
debug value with what your flow actually needs.

### 4. Add the `tasks` block

On a new line **after** the `inputs` block, the suggestion **`tasks block`**
becomes available. Accept it to get the `tasks:` header. From there you can
start adding individual tasks (see below).

---

## Editing inputs

Once you have an `inputs:` block, the editor offers help for each individual
input. Suggestions are based on the position of the cursor inside the input
body.

### Property suggestions (`type`, `required`, `debugValue`)

Place the cursor on **an empty line inside an input** (one whose name is
something like `myInputParam:`). Trigger autocomplete and you will see, for any
property the input is **missing**:

| Suggestion | Inserted as |
|---|---|
| `type: STRING`, `type: INTEGER`, `type: BOOLEAN`, `type: DATE`, `type: DATE_TIME`, `type: TIME`, `type: LONG`, `type: DECIMAL`, `type: ARRAY` | `type: <selected>` |
| `required: true`, `required: false` | `required: <selected>` |
| `debugValue` | `debugValue : ""` (you fill in the value) |

You can also **change an existing value** by triggering autocomplete on the
property line itself, e.g. on a `type:` line you can pick a different type, on
a `required:` line you can switch between `true` / `false`.

> **Tip.** These property suggestions do **not** appear on the input-name line
> (`myInputParam:`). Move down one line to a blank line inside the input.

### Adding another input (`new input`)

When you want another input, place the cursor on **an empty
line** inside the `inputs:` block – either:

- between two existing inputs,
- right after the last input (after both `required` and `type` are set), or
- at the very end of the `inputs:` section.

Trigger autocomplete and accept the **`new input`** suggestion. It inserts a
ready-to-edit input template:

```yaml
myInputParam:
  required: true
  type: STRING
  debugValue: "test-string"
```

> **Tip.** `new input` is intentionally **not** offered:
> - on the `inputs:` line itself,
> - on an existing input-name line (so it cannot overwrite it), or
> - on an empty line **between** an input's own `required` and `type`
>   properties (so you don't insert an input inside another input).

---

## Editing tasks

Tasks are the core of a flow. The editor distinguishes between three
positions:

1. **An empty line directly under `tasks:`** – you are starting a new task.
2. **An empty line at the end of an existing task** – you are extending that
   task with a body or starting another task.
3. **A line inside an existing task** – you may be filling in `id`, `then`, or
   one of the asset blocks (`service`, `decisionTable`, `switch`, …).

### Adding a new task (`service` / `decision` / `switch`)

On an **empty line inside `tasks:`** (right under `tasks:`, between two tasks,
or after the last task once it has a body), trigger autocomplete. You will see:

| Suggestion | What it builds |
|---|---|
| `new service task` | A task whose body is a `service:` block (`ref`, `collection`, `inputs`). Opens a **dialog** to pick or create the service to reference. |
| `new decision task` | A task whose body is a `decisionTable:` block. Opens the same kind of dialog for decision tables. |
| `new switch task` | A task whose body is a `switch:` block with two starter `case` entries. |

The suggestion adapts to where the cursor is:

- **Brand-new task slot** (empty line under `tasks:`, or after a task that
  already has a complete body) → the snippet inserts the **whole** task
  (`- name`, `id`, `then`, body).
- **Inside a task that already has `id` + `then` but no body yet** → the
  snippet inserts **only the body** for the existing task, so it is just
  added below the `then:` line.
- **Inside a task that has only `id`** → only **`new switch task`** is
  offered (a switch task does not need a `then` at the top level), and only
  the `switch` body is inserted.

> **Tip.** "New task" suggestions are intentionally **not** offered on:
> - the `tasks:` line itself,
> - a task name line (`- myTask:`),
> - lines inside the task's own `inputs:` mapping block.

After accepting `new service task` or `new decision task`, a small **dialog**
opens so you can either pick an existing asset or create a new one – the
chosen asset is then wired into the inserted snippet.

### Choosing `then`

When a task has an `id` but no `then`, trigger autocomplete on an empty line
inside that task. You will see:

- **`then: end`** – terminate the flow after this task,
- **`then: <other-task-name>`** – jump to one of the other tasks in the flow.
- **`new switch task`** – convert the task to a switch task.

The current task is excluded from the list (you cannot route a task back to
itself in a single step from this hint).

You can also **change an existing `then` value** by triggering autocomplete on
the `then:` line; the currently selected target is marked with
`" - currently selected"` in the suggestion list.

#### `then` inside a switch case

The same suggestions are also available **inside a `switch:` case**. A switch
case looks like this:

```yaml
switch:
  - when: someCondition
    then: someTask    <-- cursor on this line, or on an empty line below `when:`
```

Trigger autocomplete on the `then:` line itself to switch the case to a
different target task (or `end`), or on an empty line inside a case that has
`when:` set but `then:` not yet, to insert the property. The list always
excludes the **surrounding task's own id**, so a switch case can never route
back to the very task that contains it.


### Wiring up a service / decision task

Once a task body is in place (a `service:` or `decisionTable:` block with a
`ref:` and an `inputs:` block under it), autocomplete helps you fill in the
properties that wire the task to its asset:

#### 1. Pick the referenced asset (`ref:`)

On the **`ref:` line** under either `service:` or `decisionTable:`, trigger
autocomplete:

- Under `service:` → you get one suggestion per **service** in the site.
- Under `decisionTable:` → you get one suggestion per **decision table**.

Each suggestion is labelled `ref: <asset name>`. The currently referenced
asset (if any) is marked with `" - currently selected"` so you can see at a
glance which one is wired in.

#### 2. Toggle `collection:`

`service:` and `decisionTable:` both have a `collection:` flag that controls
whether the asset is invoked once or for every element of an input array. You
can set or change it from autocomplete:

- **On the existing `collection:` line** — trigger autocomplete to swap
  between `collection: true` and `collection: false` without retyping.
- **On an empty line inside a `service:` or `decisionTable:` whose
  `collection:` is missing** — the same two suggestions appear and insert the
  property at the right indentation.

These suggestions only appear inside `service:` / `decisionTable:` bodies.

#### 3. Map asset inputs

Each service / decision table accepts named inputs. The editor knows what
those are (from the asset's own headers) and helps you fill in the mapping.

#### Add a missing mapping line

Place the cursor on an empty line **under the `inputs:` line of the task asset**, e.g.:

```yaml
service:
  ref: myService
  collection: false
  inputs:
            <-- cursor on this line
```

Trigger autocomplete. For every input the asset declares but the task does
**not yet** map, you get a suggestion labelled
`add missing mapping: <name> <type>`. Accepting it inserts a new mapping line
with that input's name; you then provide the value (e.g. a flow input or a
prior task's output).

#### Choose a value for one mapping line

Place the cursor on **a single mapping line** inside that `inputs:` block,
e.g.:

```yaml
service:
  inputs:
    customerName:    <-- cursor on this line
```

Trigger autocomplete. You will get two kinds of suggestions:

| Suggestion | What it inserts |
|---|---|
| `flow input: <name> <type>` | The mapping points at one of the flow's own inputs, e.g. `customerName: customerName`. |
| `task output: <taskId>.<field> <type>` | The mapping points at an output of an **earlier** task in the flow, e.g. `customerName: lookupCustomer.fullName`. |

Only **prior** tasks (tasks that come earlier in the flow, before the cursor
position) are offered as sources – this matches the order in which tasks
actually run.

---

## Things to check if a suggestion does not appear

| Symptom | What to try |
|---|---|
| **No suggestions at all in any flow.** | Check the validation panel: if the flow has a syntax error the AST cannot be built and most hints will go silent until the error is fixed. |
| **`new input` / `new <kind> task` doesn't appear where I expect.** | Make sure the cursor is on an **empty line** at the right indentation. These hints never overwrite an existing line. |
| **`then:` suggestions don't include another task.** | The other task probably has no `id` yet. Add `id:` to it and try again. |
| **`ref:` shows no assets.** | The site finished loading after the editor opened, or there are no matching services / decision tables yet. Reopen the suggestion list (`Ctrl` + `Space`) – the editor reads the latest site each time. |
| **Suggestions appear duplicated.** | Switch to another tab and back, or refresh the page. The editor disposes its Monaco completion provider when the flow is closed; if a previous registration ever leaks, this clears it. |
