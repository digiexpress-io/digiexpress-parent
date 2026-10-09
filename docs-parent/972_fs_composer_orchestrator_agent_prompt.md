# Agent Prompt: fs-composer Orchestrator

## Setup

Read `digiexpress-parent/docs-parent/900_agent_rules.md` before starting work. All rules in that file apply to this task.

## Required Input

Before starting work, the invoker must provide the following. Do not proceed without all of them.

- **Change type** — `ui_update` or `field_change` (passed directly to the creation agent).
- **Description** — the full task description to pass to the creation agent.
- If `field_change`, also: **Field name**, **Value type**, **Purpose**, **Operation**.

## Task / Goal

Execute the following pipeline in order. Do not skip a step. Do not proceed to the next step if the current step is `BLOCKED`.

### Step 1 — Creation

Read `digiexpress-parent/docs-parent/987_fs_composer_component_agent_prompt.md` in full and follow its instructions using the provided required input. Note the files it creates or modifies — these are the **modified files** for the next steps.

### Step 2 — Compliance check

Read `digiexpress-parent/docs-parent/982_fs_composer_compliance_agent_prompt.md` in full and follow its instructions, passing:
- **Task specification** — the description provided to this orchestrator.
- **Files modified** — the files from Step 1.

If the compliance report status is `PASS`, skip Step 3 and proceed to the final report.

### Step 3 — Cleanup

Read `digiexpress-parent/docs-parent/977_fs_composer_cleanup_agent_prompt.md` in full and follow its instructions, passing:
- **Compliance report** — the full output from Step 2.
- **Files to fix** — the files from Step 1.

After cleanup completes, re-run Step 2 against the same files. If the new compliance report is `PASS`, proceed to the final report. If it is still `FAIL`, repeat Step 3 once more. If issues persist after two cleanup passes, stop and report `BLOCKED`.

## Blockers

If any step returns `BLOCKED`, stop immediately. Do not attempt the next step.

## Report

**Status:** `COMPLETE` or `BLOCKED`

If `BLOCKED`:
- Which step blocked
- The blocking agent's full output

If `COMPLETE`, report all three sections:

### 1. Creation (Step 1)

List every file the creation agent touched:

| File | Action |
|------|--------|
| `<path>` | Created / Modified |

### 2. Compliance (Step 2)

State the initial compliance result: `PASS` or `FAIL`.

If `FAIL`, include the full compliance table and violations list exactly as the compliance agent reported them.

If `PASS`, write: "No violations found."

### 3. Cleanup (Step 3)

If compliance passed immediately (no cleanup needed), write: "No cleanup required."

If cleanup ran, list every fix applied:

| File | Line | Fix applied |
|------|------|-------------|

State the final compliance status after cleanup: `PASS` or `FAIL`. State how many cleanup passes were required.
