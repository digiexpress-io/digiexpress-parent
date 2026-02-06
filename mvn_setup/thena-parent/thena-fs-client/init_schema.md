# Git-like File System Schema

## Overview
PostgreSQL schema for a versioned file system with Git-like semantics. Features content-addressable storage, hierarchical directory structures, and versioned metadata with referential integrity enforced through triggers.

## Custom Types

```sql
-- Custom type
CREATE TYPE fs_node AS (
  node_path TEXT,
  node_name TEXT,
  blob_id TEXT,
  props_id TEXT
);

COMMENT ON TYPE fs_node IS 'File or directory entry within a version tree. Represents a single item in the filesystem hierarchy with optional references to content and metadata. Referential integrity for blob_id and props_id is enforced via triggers since PostgreSQL cannot validate foreign keys within composite types.';
```

## Tables

### Content Storage

```sql
-- Blob storage
CREATE TABLE fs_blob (
  id TEXT PRIMARY KEY,
  blob_type TEXT NOT NULL,
  blob_value JSONB NOT NULL
);

COMMENT ON TABLE fs_blob IS 'Content-addressable storage for file data. Each blob represents immutable file content identified by its hash.';
COMMENT ON COLUMN fs_blob.id IS 'Content hash (SHA-1) serving as unique identifier for this blob';
COMMENT ON COLUMN fs_blob.blob_type IS 'Content type classification (e.g., MIME types, application types) for efficient querying without parsing JSONB content';
COMMENT ON COLUMN fs_blob.blob_value IS 'File content stored as JSONB for structured data support';
```

### Directory Structure

```sql
-- Tree structure
CREATE TABLE fs_tree (
  id TEXT PRIMARY KEY,
  tree_nodes fs_node[] NOT NULL
);

COMMENT ON TABLE fs_tree IS 'Directory structure snapshots. Each tree represents the complete filesystem hierarchy state at a specific point in time. Referential integrity for fs_node array elements is enforced via triggers.';
COMMENT ON COLUMN fs_tree.id IS 'Content hash of the tree structure, enabling deduplication of identical directory states';
COMMENT ON COLUMN fs_tree.tree_nodes IS 'Array of fs_node entries representing files and subdirectories in this tree';
```

### Metadata

```sql
-- File/directory metadata
CREATE TABLE fs_props (
  id TEXT PRIMARY KEY,
  props_labels JSONB,
  props_comments JSONB,
  props_permissions JSONB,
  props_flags JSONB
);

COMMENT ON TABLE fs_props IS 'Versioned metadata for files and directories. Content-addressable properties that can be attached to filesystem nodes.';
COMMENT ON COLUMN fs_props.id IS 'Content hash of the properties, enabling deduplication of identical metadata sets';
COMMENT ON COLUMN fs_props.props_labels IS 'User-defined labels and tags in JSONB format';
COMMENT ON COLUMN fs_props.props_comments IS 'Comments and annotations in JSONB format';
COMMENT ON COLUMN fs_props.props_permissions IS 'Access control and permission settings in JSONB format';
COMMENT ON COLUMN fs_props.props_flags IS 'Boolean flags like hidden, disabled, etc. in JSONB format';
```

### Version Control

```sql
-- Version commits
CREATE TABLE fs_commit (
  id TEXT PRIMARY KEY,
  commit_created_at TIMESTAMPTZ NOT NULL,
  commit_author TEXT NOT NULL,
  commit_message TEXT NOT NULL,
  tree_id TEXT NOT NULL REFERENCES fs_tree(id),
  parent_id TEXT REFERENCES fs_commit(id),
  merge_id TEXT REFERENCES fs_commit(id)
);

COMMENT ON TABLE fs_commit IS 'Version control commits representing immutable snapshots of the filesystem state with metadata about the change.';
COMMENT ON COLUMN fs_commit.id IS 'Unique commit identifier (hash)';
COMMENT ON COLUMN fs_commit.commit_created_at IS 'Timestamp when this commit was created, stored in UTC';
COMMENT ON COLUMN fs_commit.commit_author IS 'Author of this commit';
COMMENT ON COLUMN fs_commit.commit_message IS 'Commit message describing the changes';
COMMENT ON COLUMN fs_commit.tree_id IS 'Reference to the root tree representing the complete filesystem state';
COMMENT ON COLUMN fs_commit.parent_id IS 'Reference to the previous commit in the linear history (NULL for initial commit)';
COMMENT ON COLUMN fs_commit.merge_id IS 'Reference to the second parent commit for merge commits (NULL for regular commits)';
```

### References

```sql
-- Branch and reference pointers with metadata
CREATE TABLE fs_ref (
  ref_name TEXT PRIMARY KEY,
  commit_id TEXT NOT NULL REFERENCES fs_commit(id),
  branch_description TEXT,
  branch_props JSONB,
  branch_permissions JSONB,
  branch_flags JSONB,
  branch_author TEXT
);

COMMENT ON TABLE fs_ref IS 'Named references to commits with branch metadata, typically representing branches or bookmarks that can move to point to different commits over time.';
COMMENT ON COLUMN fs_ref.ref_name IS 'Reference name (e.g., "main", "develop", "feature/xyz")';
COMMENT ON COLUMN fs_ref.commit_id IS 'Current commit that this reference points to';
COMMENT ON COLUMN fs_ref.branch_description IS 'Optional human-readable description explaining the purpose and scope of this branch';
COMMENT ON COLUMN fs_ref.branch_props IS 'Optional extension properties for UI configuration, workflow settings, and application-specific metadata';
COMMENT ON COLUMN fs_ref.branch_permissions IS 'Optional access control rules defining who can push, merge, delete, or perform operations on this branch';
COMMENT ON COLUMN fs_ref.branch_flags IS 'Optional behavioral flags controlling branch protection rules, automation settings, and special handling';
COMMENT ON COLUMN fs_ref.branch_author IS 'Optional identifier of who created this branch for accountability and ownership tracking';
```

### Tags

```sql
-- Immutable tags
CREATE TABLE fs_tag (
  id TEXT PRIMARY KEY,
  tag_name TEXT NOT NULL,
  tag_description TEXT,
  commit_id TEXT NOT NULL REFERENCES fs_commit(id),
  tag_created_at TIMESTAMPTZ NOT NULL,
  tag_author TEXT NOT NULL,
  tag_extension JSONB,
  tag_errors JSONB NOT NULL,
  external_id TEXT,
  external_tenant_id TEXT,
  tag_starts_at TIMESTAMPTZ,
  tag_report JSONB
);

COMMENT ON TABLE fs_tag IS 'Immutable named markers for specific commits, typically used for releases or important milestones.';
COMMENT ON COLUMN fs_tag.id IS 'Unique tag identifier (hash)';
COMMENT ON COLUMN fs_tag.tag_name IS 'Human-readable tag name (e.g., "v1.0.0", "release-2023")';
COMMENT ON COLUMN fs_tag.tag_description IS 'Optional detailed description of this tag';
COMMENT ON COLUMN fs_tag.commit_id IS 'The specific commit this tag points to (immutable)';
COMMENT ON COLUMN fs_tag.tag_created_at IS 'Timestamp when this tag was created, stored in UTC';
COMMENT ON COLUMN fs_tag.tag_author IS 'Author who created this tag';
COMMENT ON COLUMN fs_tag.tag_extension IS 'Additional tag metadata in JSONB format for future extensibility';
COMMENT ON COLUMN fs_tag.tag_errors IS 'Error information and validation issues stored in JSONB format for diagnostic purposes';
COMMENT ON COLUMN fs_tag.external_id IS 'External system identifier for integration and tracking purposes';
COMMENT ON COLUMN fs_tag.external_tenant_id IS 'External tenant identifier for multi-tenant system integration';
COMMENT ON COLUMN fs_tag.tag_starts_at IS 'Scheduled activation timestamp when this tag becomes effective or goes live';
COMMENT ON COLUMN fs_tag.tag_report IS 'Operational reports and status information stored in JSONB format';
```

## Referential Integrity

Since PostgreSQL cannot enforce foreign key constraints on elements within composite types or arrays, referential integrity for the `fs_node` array elements must be enforced through triggers.

A validation trigger on the `fs_tree` table should check that:
- All `blob_id` references exist in `fs_blob.id`
- All `props_id` references exist in `fs_props.id`

Example trigger implementation:

```sql
CREATE OR REPLACE FUNCTION validate_fs_tree_references() 
RETURNS TRIGGER AS $$
DECLARE
    missing_count INTEGER;
BEGIN
    -- Validate blob_id references
    SELECT count(*) INTO missing_count
    FROM unnest(NEW.tree_nodes) nodes
    LEFT JOIN fs_blob b ON b.id = nodes.blob_id
    WHERE nodes.blob_id IS NOT NULL AND b.id IS NULL;

    IF missing_count > 0 THEN
        RAISE EXCEPTION 'Validation failed: % blob_id references do not exist', missing_count;
    END IF;

    -- Validate props_id references
    SELECT count(*) INTO missing_count
    FROM unnest(NEW.tree_nodes) nodes
    LEFT JOIN fs_props p ON p.id = nodes.props_id
    WHERE nodes.props_id IS NOT NULL AND p.id IS NULL;

    IF missing_count > 0 THEN
        RAISE EXCEPTION 'Validation failed: % props_id references do not exist', missing_count;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER fs_tree_validation_trigger
    BEFORE INSERT OR UPDATE ON fs_tree
    FOR EACH ROW EXECUTE FUNCTION validate_fs_tree_references();
```

## Design Decisions

1. **Content-addressable storage**: All major objects (blobs, trees, props, commits, tags) use content hashes as primary keys for deduplication
2. **Embedded tree structure**: Using `fs_node[]` arrays instead of normalized junction tables for better read performance
3. **JSONB for flexibility**: Metadata fields use JSONB for schema evolution without migrations
4. **Git-like semantics**: Immutable commits and trees, with mutable references (branches/refs)
5. **Trigger-based validation**: Maintaining referential integrity where PostgreSQL's native FK constraints cannot reach
6. **Timezone awareness**: All timestamps use `TIMESTAMPTZ` for proper timezone handling
7. **Consistent naming**: `fs_` prefix prevents conflicts with PostgreSQL and application keywords