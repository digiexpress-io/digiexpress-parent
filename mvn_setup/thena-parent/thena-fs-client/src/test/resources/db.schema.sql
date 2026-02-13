CREATE TYPE node AS (
  node_path TEXT,
  node_name TEXT,
  blob_id TEXT,
  props_id TEXT
);
COMMENT ON TYPE node IS 'File or directory entry within a version tree. Represents a single item in the filesystem hierarchy with optional references to content and metadata. Referential integrity for blob_id and props_id is enforced via triggers since PostgreSQL cannot validate foreign keys within composite types.';
CREATE TABLE blob (
  id TEXT PRIMARY KEY,
  blob_type TEXT NOT NULL,
  blob_value JSONB NOT NULL
);
CREATE INDEX blob_type_idx ON blob(blob_type);
COMMENT ON TABLE blob IS 'Content-addressable storage for file data. Each blob represents immutable file content identified by its hash.';
COMMENT ON COLUMN blob.id IS 'Content hash (SHA-1) serving as unique identifier for this blob';
COMMENT ON COLUMN blob.blob_type IS 'Content type classification (e.g., MIME types, application types) for efficient querying without parsing JSONB content';
COMMENT ON COLUMN blob.blob_value IS 'File content stored as JSONB for structured data support';
CREATE TABLE props (
  id TEXT PRIMARY KEY,
  props_labels JSONB,
  props_comments JSONB,
  props_permissions JSONB,
  props_flags JSONB
);
COMMENT ON TABLE props IS 'Versioned metadata for files and directories. Content-addressable properties that can be attached to filesystem nodes.';
COMMENT ON COLUMN props.id IS 'Content hash of the properties, enabling deduplication of identical metadata sets';
COMMENT ON COLUMN props.props_labels IS 'User-defined labels and tags in JSONB format';
COMMENT ON COLUMN props.props_comments IS 'Comments and annotations in JSONB format';
COMMENT ON COLUMN props.props_permissions IS 'Access control and permission settings in JSONB format';
COMMENT ON COLUMN props.props_flags IS 'Boolean flags like hidden, disabled, etc. in JSONB format';
CREATE TABLE tree (id TEXT PRIMARY KEY, tree_nodes node [ ] NOT NULL);
COMMENT ON TABLE tree IS 'Directory structure snapshots. Each tree represents the complete filesystem hierarchy state at a specific point in time. Referential integrity for node array elements is enforced via triggers.';
COMMENT ON COLUMN tree.id IS 'Content hash of the tree structure, enabling deduplication of identical directory states';
COMMENT ON COLUMN tree.tree_nodes IS 'Array of node entries representing files and subdirectories in this tree';
CREATE TABLE commit (
  id TEXT PRIMARY KEY,
  commit_created_at TIMESTAMPTZ NOT NULL,
  commit_author TEXT NOT NULL,
  commit_message TEXT NOT NULL,
  tree_id TEXT NOT NULL REFERENCES tree(id),
  parent_id TEXT REFERENCES commit(id),
  merge_id TEXT REFERENCES commit(id)
);
CREATE INDEX commit_tree_idx ON commit(tree_id);
CREATE INDEX commit_parent_idx ON commit(parent_id);
CREATE INDEX commit_merge_idx ON commit(merge_id);
CREATE INDEX commit_created_at_idx ON commit(commit_created_at);
COMMENT ON TABLE commit IS 'Version control commits representing immutable snapshots of the filesystem state with metadata about the change.';
COMMENT ON COLUMN commit.id IS 'Unique commit identifier (hash)';
COMMENT ON COLUMN commit.commit_created_at IS 'Timestamp when this commit was created, stored in UTC';
COMMENT ON COLUMN commit.commit_author IS 'Author of this commit';
COMMENT ON COLUMN commit.commit_message IS 'Commit message describing the changes';
COMMENT ON COLUMN commit.tree_id IS 'Reference to the root tree representing the complete filesystem state';
COMMENT ON COLUMN commit.parent_id IS 'Reference to the previous commit in the linear history (NULL for initial commit)';
COMMENT ON COLUMN commit.merge_id IS 'Reference to the second parent commit for merge commits (NULL for regular commits)';
CREATE TABLE ref (
  ref_name TEXT PRIMARY KEY,
  commit_id TEXT NOT NULL REFERENCES commit(id)
);
CREATE INDEX ref_commit_idx ON ref(commit_id);
COMMENT ON TABLE ref IS 'Named references to commits, typically representing branches or bookmarks that can move to point to different commits over time.';
COMMENT ON COLUMN ref.ref_name IS 'Reference name (e.g., "main", "develop", "feature/xyz")';
COMMENT ON COLUMN ref.commit_id IS 'Current commit that this reference points to';
CREATE TABLE tag (
  id TEXT PRIMARY KEY,
  tag_name TEXT NOT NULL,
  tag_description TEXT,
  commit_id TEXT NOT NULL REFERENCES commit(id),
  tag_created_at TIMESTAMPTZ NOT NULL,
  tag_author TEXT NOT NULL,
  tag_extension JSONB,
  tag_errors JSONB NOT NULL,
  external_id TEXT,
  external_tenant_id TEXT,
  tag_starts_at TIMESTAMPTZ,
  tag_report JSONB
);
CREATE INDEX tag_name_idx ON tag(tag_name);
CREATE INDEX tag_commit_idx ON tag(commit_id);
CREATE INDEX tag_created_at_idx ON tag(tag_created_at);
CREATE INDEX tag_external_id_idx ON tag(external_id);
CREATE INDEX tag_starts_at_idx ON tag(tag_starts_at);
COMMENT ON TABLE tag IS 'Immutable named markers for specific commits, typically used for releases or important milestones.';
COMMENT ON COLUMN tag.id IS 'Unique tag identifier (hash)';
COMMENT ON COLUMN tag.tag_name IS 'Human-readable tag name (e.g., "v1.0.0", "release-2023")';
COMMENT ON COLUMN tag.tag_description IS 'Optional detailed description of this tag';
COMMENT ON COLUMN tag.commit_id IS 'The specific commit this tag points to (immutable)';
COMMENT ON COLUMN tag.tag_created_at IS 'Timestamp when this tag was created, stored in UTC';
COMMENT ON COLUMN tag.tag_author IS 'Author who created this tag';
COMMENT ON COLUMN tag.tag_extension IS 'Additional tag metadata in JSONB format for future extensibility';
COMMENT ON COLUMN tag.tag_errors IS 'Error information and validation issues stored in JSONB format for diagnostic purposes';
COMMENT ON COLUMN tag.external_id IS 'External system identifier for integration and tracking purposes';
COMMENT ON COLUMN tag.external_tenant_id IS 'External tenant identifier for multi-tenant system integration';
COMMENT ON COLUMN tag.tag_starts_at IS 'Scheduled activation timestamp when this tag becomes effective or goes live';
COMMENT ON COLUMN tag.tag_report IS 'Operational reports and status information stored in JSONB format';
CREATE
OR REPLACE FUNCTION tree_validate_tree() RETURNS TRIGGER AS $$
 DECLARE
 missing_count INTEGER;
 BEGIN
 -- Validate blob_id references
 SELECT count(*) INTO missing_count
 FROM unnest(NEW.tree_nodes) nodes
 LEFT JOIN blob b ON b.id = nodes.blob_id
 WHERE nodes.blob_id IS NOT NULL AND b.id IS NULL;
 IF missing_count > 0 THEN
 RAISE EXCEPTION 'Validation failed: % blob_id references do not exist', missing_count;
 END IF;
 -- Validate props_id references
 SELECT count(*) INTO missing_count
 FROM unnest(NEW.tree_nodes) nodes
 LEFT JOIN props p ON p.id = nodes.props_id
 WHERE nodes.props_id IS NOT NULL AND p.id IS NULL;
 IF missing_count > 0 THEN
 RAISE EXCEPTION 'Validation failed: % props_id references do not exist', missing_count;
 END IF;
 RETURN NEW;
 END;
 $$ LANGUAGE plpgsql;
CREATE TRIGGER tree_validation_trigger BEFORE
INSERT
  OR
UPDATE
  ON tree FOR EACH ROW EXECUTE FUNCTION tree_validate_tree();