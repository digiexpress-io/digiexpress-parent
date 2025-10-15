# Audit System: Technical Architecture for Git Veterans

## Executive Summary

We've implemented a Git-inspired immutable audit system for a multi-tenant government digital services platform. Each tenant gets physically isolated tables. Every state mutation creates a commit with tree objects containing before/after diffs. Commits form mission-scoped DAGs with parent pointers. Access events are tracked separately with privacy-preserving hashing for citizens. Three-stage archival provides compliance with data protection requirements while maintaining operational flexibility.

## Architecture Philosophy

If you've built Git, you'll recognize this model immediately: we're treating state mutations as commits, entities as blob objects, and changes as tree diffs. The key differences: mission-scoped commit chains (not global), bi-temporal tracking (tree updates vs entity updates), and access audit as first-class citizens.

**Core principles:**
- **Immutability**: Commits are append-only, never modified
- **Complete history**: Full audit trail via commit chains
- **Atomicity**: One transaction = one commit = multiple trees
- **Traceability**: Every entity points to commits that created/modified it
- **Privacy by design**: Citizen identifiers hashed in access logs

## Multi-Tenancy: Security & Scalability

**Physical table isolation per tenant:**

```
Database: public schema
│
├── tenants (registry)
│   ├── id, rev, prefix, type, name, external_id
│   └── example: prefix="TASK_TENAN13_", type="grim"
│
├── Tenant A tables (prefix: TASK_TENAN13_)
│   ├── task_tenan13_grim_commit
│   ├── task_tenan13_grim_commit_tree
│   ├── task_tenan13_grim_mission
│   └── ... (all grim tables)
│
├── Tenant B tables (prefix: WRENCH_ASS11_)
│   ├── wrench_ass11_grim_commit
│   └── ... (different type, different schema)
│
└── Tenant C tables (prefix: EVELI_ASSE10_)
    └── ... (isolated from A and B)
```

**Security benefits:**
- Zero cross-tenant data leakage (no shared tables, no row-level filtering)
- Database-level isolation (strongest boundary)
- Independent access control per tenant
- Audit trails cannot be mixed across customers

**Scalability benefits:**
- Per-tenant optimization (indexes, partitioning, archival)
- Independent schema evolution per tenant type
- Horizontal scaling (distribute tenants across databases)
- Tenant-specific retention policies

## Conceptual Data Model

```
                         ┌──────────┐
                         │ tenants  │ (multi-tenant registry)
                         └────┬─────┘
                              │ [defines prefix]
                              ▼
                    ┌───────────────────┐
                    │  grim_commit      │ (immutable commit log)
                    │  - commit_id      │
                    │  - parent_id ──┐  │ (forms DAG per mission)
                    │  - mission_id   │  │
                    │  - commit_author│  │
                    │  - commit_log   │  │
                    └────┬────┬───────┘  │
                         │    │          │
                         │    └──────────┘ (self-reference)
              ┌──────────┴────┴───────────┐
              │                            │
              ▼                            ▼
    ┌──────────────────┐        ┌──────────────────┐
    │ grim_commit_tree │        │grim_commit_viewer│ (access audit)
    │ - commit_id      │        │ - commit_id      │
    │ - operation_type │        │ - object_id      │
    │ - body_before    │        │ - used_by        │
    │ - body_after     │        │ - used_for       │
    └──────────────────┘        └──────────────────┘
    (what changed)               (who accessed)
              │
              │ [references entities]
              ▼
    ┌──────────────────┐
    │  grim_mission    │ (root business entity)
    │  - id            │
    │  - commit_id     │◄─── last direct change
    │  - created_commit_id ◄─ birth certificate
    │  - updated_tree_commit_id ◄─ last tree change
    └────┬─────────────┘
         │
         ├─────────┬──────────┬───────────┬──────────┐
         │         │          │           │          │
         ▼         ▼          ▼           ▼          ▼
    objective   remark   assignment    label      link
         │                   │           │          │
         ▼                   ▼           ▼          ▼
       goal            (metadata tables with commit_id tracking)
```

**Key observations:**
- **Commits are mission-scoped**: Each mission has its own commit DAG
- **Dual commit tracking**: Entities track both direct changes (`commit_id`) and tree changes (`updated_tree_commit_id`)
- **Access audit is separate**: `commit_viewer` tracks reads, not writes
- **Everything is versioned**: Every entity references commits

*For detailed field-level diagrams, see: [Detailed Data Model Document](#)*

## Commit Model: Mission-Scoped DAGs

Unlike Git's global commit graph, our commits form **per-mission Directed Acyclic Graphs**:

```
Mission A commit chain:
NULL ← commit_1 ← commit_2 ← commit_3 ← commit_4
       (create)   (assign)   (update)   (complete)

Mission B commit chain:
NULL ← commit_5 ← commit_6 ← commit_7
       (create)   (assign)   (reject)

Mission C commit chain:
NULL ← commit_8 ← commit_9
       (create)   (archive)
```

**Commit structure:**
```sql
CREATE TABLE grim_commit (
    commit_id         varchar(40) PRIMARY KEY,
    parent_id         varchar(40) REFERENCES grim_commit(commit_id),
    mission_id        varchar(40),
    created_at        timestamptz NOT NULL,
    commit_log        text NOT NULL,        -- summary: "+3 objectives, -1 remark"
    commit_author     varchar(255) NOT NULL, -- who made the change
    commit_message    varchar(255) NOT NULL  -- what API operation attempted
);
```

**Navigation patterns:**
- **Forward**: Not stored (would require scanning)
- **Backward**: Follow `parent_id` chain to traverse history
- **Mission history**: Filter by `mission_id`, walk `parent_id` chain
- **Time-based**: Index on `created_at` for temporal queries

## Tree Objects: Before/After Diffs

Each commit references one or more **tree objects** that capture what changed:

```
Commit 42:
  ├── Tree 1: mission status change
  │   ├── operation_type: MERGE (update)
  │   ├── body_before: {"status": "IN_PROGRESS", "priority": "NORMAL"}
  │   └── body_after:  {"status": "COMPLETED", "priority": "NORMAL"}
  │
  ├── Tree 2: new remark added
  │   ├── operation_type: ADD (create)
  │   ├── body_before: null
  │   └── body_after:  {"id": "r123", "text": "Work completed", ...}
  │
  └── Tree 3: assignment removed
      ├── operation_type: REMOVE (delete)
      ├── body_before: {"assignee": "worker_456", ...}
      └── body_after:  null
```

**Operation types:**
- `ADD`: Entity creation (body_before is null)
- `REMOVE`: Entity deletion (body_after is null)
- `MERGE`: Entity update (both before and after present)

**JSONB advantages:**
- Schema-flexible: Can store any entity type
- Queryable: Can search within body_before/body_after using JSONB operators
- Compact: Only changed fields need to be stored (though we store complete state)
- Recoverable: Full entity state at any point in time

## Bi-Temporal Tracking

This is where we diverge from standard Git. Entities track **two different commit timestamps**:

```
Mission entity:
├── commit_id               ← Last time THIS mission entity changed
├── created_commit_id       ← When mission was born (never changes)
└── updated_tree_commit_id  ← Last time ANYTHING in mission tree changed

Objective entity:
├── commit_id               ← Last time THIS objective changed
└── created_commit_id       ← When objective was created

Goal entity:
├── commit_id               ← Last time THIS goal changed
└── created_commit_id       ← When goal was created
```

**Example scenario:**

```
Initial state:
Mission M1:
  commit_id = C1
  updated_tree_commit_id = C1
  └── Objective O1:
        commit_id = C1

User updates O1 title → Creates commit C2:
Mission M1:
  commit_id = C1              (mission itself unchanged)
  updated_tree_commit_id = C2 (tree changed!)
  └── Objective O1:
        commit_id = C2        (objective modified)

User updates M1 status → Creates commit C3:
Mission M1:
  commit_id = C3              (mission changed)
  updated_tree_commit_id = C3 (tree changed)
  └── Objective O1:
        commit_id = C2        (objective unchanged)
```

**Query optimization:**
- "Show missions modified recently": Check `commit_id`
- "Show missions with any activity": Check `updated_tree_commit_id`
- "Show missions where objectives changed": Join on objective `commit_id`

## Access Audit: Privacy-Preserving Read Tracking

Separate from mutation commits, we track **who accessed what**:

```sql
CREATE TABLE grim_commit_viewer (
    id          varchar(40) PRIMARY KEY,
    mission_id  varchar(40) NOT NULL,
    commit_id   varchar(40) REFERENCES grim_commit(commit_id),
    object_id   varchar(40) NOT NULL,    -- what was viewed
    object_type varchar(255) NOT NULL,   -- mission/objective/goal/remark
    used_by     varchar(255) NOT NULL,   -- WHO (hashed for citizens)
    used_for    varchar(255) NOT NULL,   -- CUSTOMER or WORKER
    updated_at  timestamptz NOT NULL,
    created_at  timestamptz NOT NULL
);
```

**Privacy design:**

```
Worker access:
├── used_for: "WORKER"
├── used_by: "worker_juhani_123"  (readable identifier)
└── Purpose: Accountability, citizen transparency

Citizen access:
├── used_for: "CUSTOMER"
├── used_by: "hash(citizen_ssn)"  (one-way hash, not reversible)
└── Purpose: Audit that citizen accessed, but preserve anonymity
```

**Why hash citizen identifiers?**
- Workers/admins can see "a citizen accessed this" but NOT which citizen
- Citizen can verify "yes, that was me" by re-hashing their SSN from login session
- Prevents correlation attacks across missions
- GDPR compliance: Minimal identifiable data in audit logs

**Access patterns:**
- Citizen query: "Who (which workers) viewed my case?" → Filter by mission, used_for=WORKER
- Worker supervisor: "Did anyone access mission X?" → Filter by mission_id
- Citizen self-verification: "Did I view this?" → Hash SSN, check against used_by

## Transaction Semantics

```
REST API Request
      │
      ▼
┌─────────────────────────────────────┐
│ Spring REST Controller              │
│ - Authentication/Authorization      │
│ - Input validation                  │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│ Java API / Business Logic           │
│ - Mission service                   │
│ - Objective service                 │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│ Commit Service (Transaction)        │
│                                     │
│ BEGIN TRANSACTION                   │
│ 1. Create commit record             │
│ 2. Create tree entries (per entity) │
│ 3. Update entity commit_id fields   │
│ 4. Update mission tree_commit_id    │
│ COMMIT TRANSACTION                  │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│ PostgreSQL Database                 │
│ - Atomic commit                     │
│ - Constraint validation             │
│ - Index updates                     │
└─────────────────────────────────────┘
```

**Atomicity guarantees:**
- One REST operation = One database transaction = One commit
- Either all changes succeed, or all fail (no partial commits)
- Commit and trees are written atomically
- Foreign key constraints ensure referential integrity

**Idempotency:**
- Not inherently idempotent (each request creates new commit)
- API layer must implement idempotency if required (request deduplication)

## Three-Stage Archival: Compliance & Recovery

```
┌─────────────────────────────────────────────────────────────┐
│ STAGE 1: SOFT DELETE (Reversible)                          │
│                                                              │
│ Action: Set archived_at timestamp + archived_status         │
│ Effect: Hidden from UI via WHERE archived_at IS NULL        │
│ Data: Fully intact, all commits preserved                   │
│ Recovery: Update flags, mission reappears                   │
│                                                              │
│ [Mission] archived_at = 2025-10-15, archived_status = "SOFT"│
└─────────────────────────────────────────────────────────────┘
                            │
                            │ (Time passes / Admin action)
                            ▼
┌─────────────────────────────────────────────────────────────┐
│ STAGE 2: HARD DELETE / COMPRESSION (Partially Reversible)  │
│                                                              │
│ Action:                                                      │
│ 1. Create ONE final restoration commit                      │
│    - commit_log: "ARCHIVED: Full mission data compressed"  │
│    - commit_tree with complete mission snapshot in JSONB    │
│ 2. Hard DELETE all mission data:                           │
│    - All child entities (objectives, goals, remarks, etc.)  │
│    - All previous commits and trees                         │
│    - All commit_viewer access logs                          │
│ 3. Only restoration commit remains                          │
│                                                              │
│ Data: Compressed into single JSONB blob in final commit     │
│ Recovery: Possible if restoration commit exists (expensive) │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ (Time passes / Legal requirement)
                            ▼
┌─────────────────────────────────────────────────────────────┐
│ STAGE 3: PERMANENT DELETION (Irreversible)                 │
│                                                              │
│ Action: DELETE restoration commit                           │
│ Effect: No data remains in database                         │
│ Recovery: Impossible                                        │
│                                                              │
│ [All traces of mission removed]                             │
└─────────────────────────────────────────────────────────────┘
```

**Compliance considerations:**
- Stage 1→2 transition: Configurable retention period per tenant
- Stage 2→3 transition: Legal hold support, compliance review
- Audit of deletions: Deletion events themselves may be logged at system level
- Right to erasure: Stage 3 satisfies data protection requirements

## Technical Specifications

**Database:**
- PostgreSQL 12+ (JSONB support required)
- Multi-tenant schema: Physical table separation via prefix
- Timezone: All timestamps in UTC (timestamptz)

**Indexes:**
- Commit lookup: `commit_id` (PK)
- History traversal: `parent_id`, `mission_id`
- Access audit: `object_id`, `mission_id`, `created_at`
- Business queries: `mission_ref`, `assignee`, `label_value`

**Data types:**
- IDs: varchar(40) - UUIDs or content-addressable hashes
- JSONB: body_before/body_after, mission_data extensions
- Text: Unlimited length for descriptions, commit logs
- Dates: timestamptz for all temporal data

**Constraints:**
- Referential integrity: All foreign keys enforced
- Unique constraints: Per-tenant unique mission refs, external IDs
- NULL handling: NULLS NOT DISTINCT for composite unique keys

**Performance considerations:**
- Write-heavy workload: Commits are append-only (no updates)
- Read optimization: Denormalized commit_id fields on entities (avoid joins for current state)
- History queries: May require walking long commit chains (consider materialized views for reporting)
- JSONB indexing: GIN indexes on body_before/body_after if querying within

## Comparison to Git

| Feature | Git | Our System |
|---------|-----|------------|
| Commit scope | Global DAG | Per-mission DAG |
| Object model | Blob/Tree/Commit | Entity/Tree/Commit |
| Branching | First-class (refs) | Not applicable |
| Merging | Three-way merge | Not applicable |
| Diffs | Computed on demand | Stored explicitly (before/after) |
| Access audit | Not tracked | First-class (commit_viewer) |
| Multi-tenancy | Not applicable | Physical isolation |
| Read tracking | No | Yes (privacy-preserving) |

**What we borrowed:**
- Immutable commit objects with parent pointers
- Content-addressable storage (commit IDs)
- Tree objects representing state changes
- Append-only write model

**What we adapted:**
- Mission-scoped instead of global (bounded contexts)
- Bi-temporal tracking (entity vs tree commits)
- Explicit before/after storage (not computed diffs)
- Privacy-first access audit
- Multi-tenant physical isolation

---

**For complete field-level data model with all tables, foreign keys, and constraints, see the companion document: "Detailed Data Model Reference"**
