# ts_setup — Claude Operating Context

## Reference files

- **Coding standards**: `digiexpress-parent/docs-parent/999_ui_coding_standards.md`
- **Agent rules**: `digiexpress-parent/ts_setup/.claude/agents/agent-rules.md`

## Module documentation

`digiexpress-parent/docs-parent/` contains project-specific documentation for individual modules. Before starting work on any module, check there first — a doc file for that module may already exist and will be the fastest way to understand its structure, conventions, and patterns.

If no doc exists for the module, invoke the `exploration` agent to produce one before starting work.

## Invoking agents

When invoking any building agent, the brief must include:
- The user's exact request verbatim
- All required input fields for that agent — no paraphrasing, no omissions

If any required field is unclear, ask the user before invoking. Do not fill in missing fields with assumptions.

## Agent pipeline

UI work follows this chain:

```
ui-builder → ui-compliance → ui-cleanup (only if FAIL)
```

`ui-compliance` is invoked automatically by UI-building agents when they finish. If it returns `FAIL`, it invokes `ui-cleanup` to fix the issues.

## Agents

| Agent | When to invoke |
|-------|---------------|
| `fs-composer-component` | Create or modify a component in `modules/fs-composer/` |
| `gamut-form-component` | Create or modify a form component in `modules/gamut-form/` |
| `gamut-primitives-component` | Create or modify a primitive component in `modules/gamut-primitives/` |
| `gamut-route-component` | Create or modify a route-level component in `modules/gamut-routes/` |
| `ui-compliance` | Audit existing code for standards violations (Mode 2) |
| `ui-cleanup` | Fix violations from a compliance report — invoked by `ui-compliance`, not directly |
| `exploration` | Explore source code and produce a module documentation file |
| `doc-nitpicker` | Review and tighten existing documentation files |
| `agent-creation` | Create a new agent prompt file |
| `gaps-manager` | Add or update entries in the gaps work queue in `docs-parent/gaps.md` |
