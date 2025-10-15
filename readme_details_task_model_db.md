# Detailed Data Model Reference

Complete field-level documentation of all tables in the audit system.

## Legend

```
┌─────────────────┐
│ table_name      │
│─────────────────│
│ PK primary_key  │  ← Primary key
│ FK foreign_key  │──→ References another table
│    regular_field│
│ UK unique_field │  ← Unique constraint
│ IX indexed_field│  ← Has index
└─────────────────┘
```

**Data types:**
- `varchar(N)` - Variable character string
- `text` - Unlimited text
- `timestamptz` - Timestamp with timezone (UTC)
- `date` - Date only
- `jsonb` - Binary JSON (queryable, indexable)
- `_jsonb` - Array of JSONB

---

## Multi-Tenant Registry

```
┌──────────────────────┐
│ tenants              │
│──────────────────────│
│ PK id                │ varchar(40)
│ UK rev               │ varchar(40)
│ UK prefix            │ varchar(40) - Table prefix (e.g., "TASK_TENAN13_")
│ UK type              │ varchar(40) - System type: grim/git/doc/batch
│ UK name              │ varchar(255)
│ UK,IX external_id    │ varchar(255)
└──────────────────────┘

Purpose: Registry of all tenants in the system
Security: Each tenant gets isolated table set with unique prefix
```

---

## Core Audit Tables

### Commit Log (Immutable Event Store)

```
┌─────────────────────────┐
│ grim_commit             │
│─────────────────────────│
│ PK commit_id            │ varchar(40)
│ FK,IX parent_id         │ varchar(40) ──┐ Self-reference
│ IX mission_id           │ varchar(40)   │ (forms commit chain)
│ created_at              │ timestamptz   │
│ commit_log              │ text          │
│ IX commit_author        │ varchar(255)  │
│ commit_message          │ varchar(255)  │
└──────────────┬──────────┘               │
               │                          │
               └──────────────────────────┘

Purpose: Immutable log of all state changes
Indexes:
- commit_id (PK): O(1) commit lookup
- parent_id: History traversal
- mission_id: All commits for a mission
- commit_author: Who made changes

Fields:
- commit_log: Summary "+3 objectives, -1 remark"
- commit_message: Auto-generated API operation description
- parent_id: Previous commit in mission chain (NULL for first)
```

### Tree Objects (What Changed)

```
┌─────────────────────────┐
│ grim_commit_tree        │
│─────────────────────────│
│ PK id                   │ varchar(40)
│ FK,IX commit_id         │ varchar(40) ───→ grim_commit
│ operation_type          │ varchar(40) - ADD/REMOVE/MERGE
│ body_after              │ jsonb - Entity state after change
│ body_before             │ jsonb - Entity state before change
└─────────────────────────┘

Purpose: Captures before/after snapshots of changed entities
One commit can have multiple tree entries

Operation types:
- ADD: body_before = null, body_after = new entity
- REMOVE: body_before = entity, body_after = null
- MERGE: body_before = old state, body_after = new state

JSONB structure: Complete entity snapshot
{
  "id": "entity_id",
  "field1": "value1",
  "field2": "value2",
  ...
}
```

### Access Audit (Read Tracking)

```
┌─────────────────────────┐
│ grim_commit_viewer      │
│─────────────────────────│
│ PK id                   │ varchar(40)
│ IX mission_id           │ varchar(40)
│ FK,IX commit_id         │ varchar(40) ───→ grim_commit
│ IX object_id            │ varchar(40) - What was viewed
│ object_type             │ varchar(255) - mission/objective/goal/remark
│ used_by                 │ varchar(255) - Who viewed (hashed for citizens)
│ used_for                │ varchar(255) - CUSTOMER or WORKER
│ updated_at              │ timestamptz
│ IX created_at           │ timestamptz
└─────────────────────────┘

Purpose: Track who accessed what data (GDPR transparency)

Privacy model:
- used_for = WORKER: used_by = readable worker ID
- used_for = CUSTOMER: used_by = one-way hash(SSN)

Indexes:
- mission_id: All access for a mission
- object_id: All access to specific entity
- created_at: Temporal queries
```

---

## Business Entity: Mission (Root)

```
┌─────────────────────────────────┐
│ grim_mission                    │
│─────────────────────────────────│
│ PK id                           │ varchar(40)
│ FK,IX commit_id                 │ varchar(40) ───→ grim_commit (last change to THIS entity)
│ FK,IX created_commit_id         │ varchar(40) ───→ grim_commit (birth certificate)
│ FK,IX updated_tree_commit_id    │ varchar(40) ───→ grim_commit (last change to ANY child)
│ IX questionnaire_id             │ varchar(40)
│ FK,IX parent_mission_id         │ varchar(40) ──┐ Self-reference (sub-missions)
│ UK external_id                  │ varchar(40)   │
│ reporter_id                     │ varchar(255)  │
│ IX mission_ref                  │ varchar(40)   │
│ mission_status                  │ varchar(100)  │
│ mission_priority                │ varchar(100)  │
│ mission_start_date              │ date          │
│ mission_due_date                │ date          │
│ mission_title                   │ text          │
│ mission_description             │ text          │
│ mission_completed_at            │ timestamptz   │
│ archived_at                     │ timestamptz   │
│ archived_status                 │ varchar(40)   │
└──────────────────────┬──────────┘               │
                       │                          │
                       └──────────────────────────┘

Purpose: Root entity representing citizen service request or work case

Bi-temporal tracking:
- commit_id: Last time mission entity itself changed
- updated_tree_commit_id: Last time anything in mission tree changed
- created_commit_id: Never changes after creation

Archival fields:
- archived_at: Stage 1 soft delete timestamp
- archived_status: SOFT/HARD/PERMANENT
```

---

## Business Entity: Objective (Sub-task)

```
┌─────────────────────────────────┐
│ grim_objective                  │
│─────────────────────────────────│
│ PK id                           │ varchar(40)
│ FK,IX commit_id                 │ varchar(40) ───→ grim_commit
│ IX created_commit_id            │ varchar(40) ───→ grim_commit
│ FK,IX mission_id                │ varchar(40) ───→ grim_mission
│ IX objective_status             │ varchar(100)
│ objective_start_date            │ date
│ objective_due_date              │ date
│ objective_title                 │ text
│ objective_description           │ text
│ objective_type                  │ varchar(100)
│ objective_external_id           │ varchar(255)
│ objective_process_id            │ varchar(255)
│ objective_questionnaire_id      │ varchar(255)
│ objective_locale                │ varchar(100)
└─────────────────────────────────┘

Purpose: Sub-tasks within a mission
Relationship: mission (1) ──→ (N) objectives
```

---

## Business Entity: Goal (Work item)

```
┌─────────────────────────────────┐
│ grim_objective_goal             │
│─────────────────────────────────│
│ PK id                           │ varchar(40)
│ FK,IX commit_id                 │ varchar(40) ───→ grim_commit
│ IX created_commit_id            │ varchar(40) ───→ grim_commit
│ FK,IX objective_id              │ varchar(40) ───→ grim_objective
│ goal_status                     │ varchar(100)
│ goal_start_date                 │ date
│ goal_due_date                   │ date
│ goal_title                      │ text
│ goal_description                │ text
└─────────────────────────────────┘

Purpose: Specific goals/tasks within objectives
Relationship: objective (1) ──→ (N) goals
```

---

## Business Entity: Remark (Comments/Notes)

```
┌─────────────────────────────────┐
│ grim_remark                     │
│─────────────────────────────────│
│ PK id                           │ varchar(40)
│ FK,IX commit_id                 │ varchar(40) ───→ grim_commit
│ FK parent_id                    │ varchar(40) ──┐ Self-reference (threaded)
│ IX created_commit_id            │ varchar(40)   │ ───→ grim_commit
│ FK,IX mission_id                │ varchar(40)   │ ───→ grim_mission
│ FK,IX objective_id              │ varchar(40)   │ ───→ grim_objective
│ FK,IX goal_id                   │ varchar(40)   │ ───→ grim_objective_goal
│ FK remark_id                    │ varchar(40)   │ ───→ grim_remark (extra ref)
│ reporter_id                     │ varchar(255)  │
│ remark_status                   │ varchar(100)  │
│ remark_type                     │ varchar(100)  │
│ remark_source                   │ varchar(100)  │
│ remark_text                     │ text          │
└──────────────────────┬──────────┘               │
                       │                          │
                       └──────────────────────────┘

Purpose: Comments/notes that can be attached at any level
Can be threaded: parent_id creates comment chains
Can reference: mission, objective, goal, or another remark
```

---

## Metadata: Assignment

```
┌─────────────────────────────────────────────────────────┐
│ grim_assignment                                         │
│─────────────────────────────────────────────────────────│
│ PK id                                                   │ varchar(40)
│ FK,IX commit_id                                         │ varchar(40) ───→ grim_commit
│ FK,IX mission_id                                        │ varchar(40) ───→ grim_mission
│ FK,IX objective_id                                      │ varchar(40) ───→ grim_objective
│ FK,IX goal_id                                           │ varchar(40) ───→ grim_objective_goal
│ FK,IX remark_id                                         │ varchar(40) ───→ grim_remark
│ assignee                                                │ varchar(255)
│ assignment_type                                         │ varchar(100)
│ assignee_contact                                        │ text
│ UK (mission_id, objective_id, goal_id, remark_id,      │
│     assignee, assignment_type) NULLS NOT DISTINCT       │
└─────────────────────────────────────────────────────────┘

Purpose: Track who is assigned to what
Can assign at any level: mission, objective, goal, or remark
Unique constraint ensures no duplicate assignments
```

---

## Metadata: Label (Tags/Categories)

```
┌─────────────────────────────────────────────────────────┐
│ grim_mission_label                                      │
│─────────────────────────────────────────────────────────│
│ PK id                                                   │ varchar(40)
│ FK,IX commit_id                                         │ varchar(40) ───→ grim_commit
│ label_type                                              │ varchar(100)
│ IX label_value                                          │ varchar(255)
│ label_body                                              │ jsonb
│ FK,IX mission_id                                        │ varchar(40) ───→ grim_mission
│ FK,IX objective_id                                      │ varchar(40) ───→ grim_objective
│ FK,IX goal_id                                           │ varchar(40) ───→ grim_objective_goal
│ FK,IX remark_id                                         │ varchar(40) ───→ grim_remark
│ UK (mission_id, objective_id, goal_id, remark_id,      │
│     label_type, label_value) NULLS NOT DISTINCT         │
└─────────────────────────────────────────────────────────┘

Purpose: Tags/categories for classification
Can label at any level: mission, objective, goal, or remark
label_body: Optional JSONB for extended metadata
Examples: status labels, priority tags, department codes
