# thena-fs-client — SQL Reference

## Schema

Full DDL: [`src/test/resources/db.schema.sql`](src/test/resources/db.schema.sql)

All table names are tenant-scoped. Every query uses a `{prefix}` placeholder that is substituted at runtime with the tenant-specific table prefix (e.g. `tenant1_`).

---

## Table Overview

| Table | Purpose |
|-------|---------|
| `{prefix}blob` | Content-addressable file storage (immutable, keyed by SHA-1) |
| `{prefix}props` | Versioned metadata (labels, comments, permissions, flags) |
| `{prefix}tree` | Directory snapshots — array of `node` composite entries |
| `{prefix}tree_index` | Sideloaded ancestry: tracks created_by / updated_by per object |
| `{prefix}commit` | Immutable version snapshots with author, message, timestamp |
| `{prefix}ref` | Named branch pointers (mutable HEAD reference per branch) |
| `{prefix}tag` | Immutable named markers (releases, milestones) |

---

## Core Pattern

`ref` → `commit` → `tree` → `unnest(tree_nodes)` → `blob` / `props`

- `ref` always points to HEAD (the latest commit on a branch).
- `tree.tree_nodes` is a PostgreSQL array of the `node` composite type. Use `LATERAL unnest()` to expand it into rows.
- Each `node` holds an `object_id` (the stable logical identity) and either a `blob_id` (file content) or `props_id` (metadata), never both.

---

## Queries

### Get blob content for an object at HEAD

Returns the blob content for a given `object_id` on the HEAD commit of a named branch.

```sql
SELECT
  file_blob.blob_id,
  file_blob.blob_type,
  file_blob.blob_class,
  file_blob.blob_value
FROM {prefix}ref AS branch
JOIN {prefix}commit AS head_commit
  ON head_commit.commit_id = branch.commit_id
JOIN {prefix}tree AS directory_tree
  ON directory_tree.tree_id = head_commit.tree_id
CROSS JOIN LATERAL unnest(directory_tree.tree_nodes) AS file_node
JOIN {prefix}blob AS file_blob
  ON file_blob.blob_id = file_node.blob_id
WHERE branch.ref_name = :ref_name
  AND file_node.object_id = :object_id
```

**Parameters**

| Parameter | Type | Description |
|-----------|------|-------------|
| `:ref_name` | `TEXT` | Branch name, e.g. `'main'` |
| `:object_id` | `TEXT` | Stable logical object identifier |

---

## Naming Conventions

- **Table aliases**: use descriptive names (`branch`, `head_commit`, `directory_tree`, `file_node`, `file_blob`). Never use single-letter or acronym aliases.
- **Parameters**: use named parameters (`:param_name`), not positional `$1` placeholders.
- **Prefix substitution**: always write `{prefix}table_name` — substitution is handled by `ThenaSqlDataSource` at runtime.
