CREATE DOMAIN node_required_text AS TEXT NOT NULL;
CREATE DOMAIN node_required_uuid AS UUID NOT NULL;
CREATE TYPE node AS (
  node_id node_required_uuid,
  object_id node_required_text,
  -- technical id of the object (user api generated)
  node_path TEXT,
  node_name node_required_text,
  blob_id UUID,
  props_id UUID
);
CREATE DOMAIN node_strict AS node CHECK (
  -- num_nonnulls returns 1 if exactly one of the fields is NOT NULL
  num_nonnulls((VALUE).blob_id, (VALUE).props_id) = 1
);
COMMENT ON TYPE node IS 'File or directory entry within a version tree. Represents a single item in the filesystem hierarchy with optional references to content and metadata. Referential integrity for blob_id and props_id is enforced via triggers since PostgreSQL cannot validate foreign keys within composite types.';
CREATE TABLE blob (
  blob_id UUID PRIMARY KEY,
  blob_type TEXT NOT NULL,
  blob_class TEXT,
  blob_value JSONB NOT NULL
);
CREATE INDEX blob_type_idx ON blob(blob_type);
COMMENT ON TABLE blob IS 'Content-addressable storage for file data. Each blob represents immutable file content identified by its hash.';
COMMENT ON COLUMN blob.blob_id IS 'Content hash (SHA-1) serving as unique identifier for this blob';
COMMENT ON COLUMN blob.blob_type IS 'Content type classification (e.g., MIME types, application types) for efficient querying without parsing JSONB content';
COMMENT ON COLUMN blob.blob_class IS 'Classifier for mapping specific application-level class or entity';
COMMENT ON COLUMN blob.blob_value IS 'File content stored as JSONB for structured data support';
CREATE TABLE props (
  props_id UUID PRIMARY KEY,
  props_labels JSONB,
  props_comments JSONB,
  props_permissions JSONB,
  props_flags JSONB
);
COMMENT ON TABLE props IS 'Versioned metadata for files and directories. Content-addressable properties that can be attached to filesystem nodes.';
COMMENT ON COLUMN props.props_id IS 'Content hash of the properties, enabling deduplication of identical metadata sets';
COMMENT ON COLUMN props.props_labels IS 'User-defined labels and tags in JSONB format';
COMMENT ON COLUMN props.props_comments IS 'Comments and annotations in JSONB format';
COMMENT ON COLUMN props.props_permissions IS 'Access control and permission settings in JSONB format';
COMMENT ON COLUMN props.props_flags IS 'Boolean flags like hidden, disabled, etc. in JSONB format';
CREATE TABLE tree (tree_id UUID PRIMARY KEY, tree_nodes node [ ] NOT NULL);
COMMENT ON TABLE tree IS 'Directory structure snapshots. Each tree represents the complete filesystem hierarchy state at a specific point in time. Referential integrity for node array elements is enforced via triggers.';
COMMENT ON COLUMN tree.tree_id IS 'Content hash of the tree structure, enabling deduplication of identical directory states';
COMMENT ON COLUMN tree.tree_nodes IS 'Array of node entries representing files and subdirectories in this tree';
CREATE TABLE commit (
  commit_id UUID PRIMARY KEY,
  commit_created_at TIMESTAMPTZ NOT NULL,
  commit_author TEXT NOT NULL,
  commit_message TEXT NOT NULL,
  tree_id UUID NOT NULL REFERENCES tree(tree_id),
  parent_id UUID REFERENCES commit(commit_id),
  merge_id UUID REFERENCES commit(commit_id)
);
CREATE INDEX commit_tree_idx ON commit(tree_id);
CREATE INDEX commit_parent_idx ON commit(parent_id);
CREATE INDEX commit_merge_idx ON commit(merge_id);
CREATE INDEX commit_created_at_idx ON commit(commit_created_at);
COMMENT ON TABLE commit IS 'Version control commits representing immutable snapshots of the filesystem state with metadata about the change.';
COMMENT ON COLUMN commit.commit_id IS 'Unique commit identifier (hash)';
COMMENT ON COLUMN commit.commit_created_at IS 'Timestamp when this commit was created, stored in UTC';
COMMENT ON COLUMN commit.commit_author IS 'Author of this commit';
COMMENT ON COLUMN commit.commit_message IS 'Commit message describing the changes';
COMMENT ON COLUMN commit.tree_id IS 'Reference to the root tree representing the complete filesystem state';
COMMENT ON COLUMN commit.parent_id IS 'Reference to the previous commit in the linear history (NULL for initial commit)';
COMMENT ON COLUMN commit.merge_id IS 'Reference to the second parent commit for merge commits (NULL for regular commits)';
CREATE DOMAIN tree_index_required_text AS TEXT NOT NULL;
CREATE DOMAIN tree_index_required_uuid AS UUID NOT NULL;
CREATE TYPE tree_index_type AS (
  object_id tree_index_required_text,
  created_by tree_index_required_uuid,
  updated_by tree_index_required_uuid
);
CREATE TABLE tree_index (
  tree_id UUID PRIMARY KEY REFERENCES tree(tree_id),
  tree_ancestry tree_index_type [ ] NOT NULL
);
COMMENT ON TYPE tree_index_type IS 'Dedicated type for fast created/updated access';
COMMENT ON TABLE tree_index IS 'Sideloaded state for nodes, tracking the birth and last mutation of a logical object independent of its content hash.';
COMMENT ON COLUMN tree_index.tree_id IS 'Reference to the root tree representing the complete filesystem state';
COMMENT ON COLUMN tree_index.tree_ancestry IS 'Array of node entries representing files and subdirectories in this tree';
CREATE TABLE ref (
  ref_id UUID PRIMARY KEY,
  ref_name TEXT UNIQUE NOT NULL,
  ref_description TEXT,
  ref_props JSONB,
  ref_permissions JSONB,
  ref_flags JSONB,
  ref_author TEXT,
  ref_created_at TIMESTAMPTZ NOT NULL,
  ref_created_from UUID REFERENCES ref(ref_id) ON DELETE
  SET
    NULL,
    commit_id UUID NOT NULL REFERENCES commit(commit_id)
);
CREATE INDEX ref_commit_idx ON ref(commit_id);
CREATE INDEX ref_name_idx ON ref(ref_name);
CREATE INDEX ref_desc_idx ON ref(ref_description);
COMMENT ON TABLE ref IS 'Named references to commits, typically representing branches or bookmarks that can move to point to different commits over time.';
COMMENT ON COLUMN ref.ref_name IS 'Reference name (e.g., "main", "develop", "feature/xyz")';
COMMENT ON COLUMN ref.commit_id IS 'Current commit that this reference points to';
COMMENT ON COLUMN ref.ref_description IS 'Optional detailed description of this branch';
COMMENT ON COLUMN ref.ref_created_from IS 'Id of the ref from what this branch was created';
COMMENT ON COLUMN ref.ref_created_at IS 'Timestamp when this branch was created, stored in UTC';
COMMENT ON COLUMN ref.ref_author IS 'Author who created this branch';
COMMENT ON COLUMN ref.ref_props IS 'User annotations in JSONB format';
COMMENT ON COLUMN ref.ref_permissions IS 'Access control and permission settings in JSONB format';
COMMENT ON COLUMN ref.ref_flags IS 'Boolean flags like hidden, disabled, etc. in JSONB format';
CREATE TABLE tag (
  tag_id UUID PRIMARY KEY,
  tag_name TEXT UNIQUE NOT NULL,
  tag_description TEXT,
  tag_starts_at TIMESTAMPTZ,
  tag_ends_at TIMESTAMPTZ,
  tag_lifecycle TEXT,
  tag_health TEXT,
  tag_created_at TIMESTAMPTZ NOT NULL,
  tag_author TEXT NOT NULL,
  tag_extension JSONB,
  tag_errors JSONB,
  tag_report JSONB,
  external_id TEXT,
  ref_id UUID,
  --soft link
  commit_id UUID NOT NULL REFERENCES commit(commit_id)
);
CREATE INDEX tag_name_idx ON tag(tag_name);
CREATE INDEX tag_commit_idx ON tag(commit_id);
CREATE INDEX tag_created_at_idx ON tag(tag_created_at);
CREATE INDEX tag_external_id_idx ON tag(external_id);
CREATE INDEX tag_starts_at_idx ON tag(tag_starts_at);
CREATE INDEX tag_ref_idx ON tag(ref_id);
COMMENT ON TABLE tag IS 'Immutable named markers for specific commits, typically used for releases or important milestones.';
COMMENT ON COLUMN tag.tag_lifecycle IS 'The operational phase of the tag (e.g., in-force, waiting, lapsed).';
COMMENT ON COLUMN tag.tag_health IS 'The current health status or severity level (e.g., OK, WARNING, ERROR).';
COMMENT ON COLUMN tag.tag_id IS 'Unique tag identifier (hash)';
COMMENT ON COLUMN tag.tag_name IS 'Human-readable tag name (e.g., "v1.0.0", "release-2023")';
COMMENT ON COLUMN tag.tag_description IS 'Optional detailed description of this tag';
COMMENT ON COLUMN tag.commit_id IS 'The specific commit this tag points to (immutable)';
COMMENT ON COLUMN tag.tag_created_at IS 'Timestamp when this tag was created, stored in UTC';
COMMENT ON COLUMN tag.tag_author IS 'Author who created this tag';
COMMENT ON COLUMN tag.tag_extension IS 'Additional tag metadata in JSONB format for future extensibility';
COMMENT ON COLUMN tag.tag_errors IS 'Error information and validation issues stored in JSONB format for diagnostic purposes';
COMMENT ON COLUMN tag.external_id IS 'External system identifier for integration and tracking purposes';
COMMENT ON COLUMN tag.tag_starts_at IS 'Scheduled activation timestamp when this tag becomes effective or goes live';
COMMENT ON COLUMN tag.tag_report IS 'Operational reports and status information stored in JSONB format';
COMMENT ON COLUMN tag.ref_id IS 'Branch pointer when created from specific branch, otherwise just commit id is used';
CREATE
OR REPLACE FUNCTION tree_validate_tree() RETURNS TRIGGER AS $$
 DECLARE
 missing_count INTEGER;
 BEGIN
 -- Validate id uniqueness within the tree
 SELECT count(*) INTO missing_count
 FROM (
 SELECT nodes.node_id, count(*)
 FROM unnest(NEW.tree_nodes) nodes
 GROUP BY nodes.node_id
 HAVING count(*) > 1
 ) duplicates;
 IF missing_count > 0 THEN
 RAISE EXCEPTION 'Node(code 002) validation failed: % duplicate id values in tree', missing_count;
 END IF;
 -- Validate that object_id, node_path and node_name are not null
 SELECT count(*) INTO missing_count
 FROM unnest(NEW.tree_nodes) nodes
 WHERE nodes.object_id IS NULL OR nodes.node_name IS NULL;
 IF missing_count > 0 THEN
 RAISE EXCEPTION 'Node(code 003) validation failed: % nodes have null object_id or node_name', missing_count;
 END IF;
 -- Validate object_id uniqueness within the tree
 SELECT count(*) INTO missing_count
 FROM (
 SELECT nodes.object_id, count(*)
 FROM unnest(NEW.tree_nodes) nodes
 GROUP BY nodes.object_id
 HAVING count(*) > 1
 ) duplicates;
 IF missing_count > 0 THEN
 RAISE EXCEPTION 'Node(code 004) validation failed: % duplicate object_id values in tree', missing_count;
 END IF;
 -- Validate path + name uniqueness within the tree
 SELECT count(*) INTO missing_count
 FROM (
 SELECT nodes.node_path, nodes.node_name, count(*)
 FROM unnest(NEW.tree_nodes) nodes
 GROUP BY nodes.node_path, nodes.node_name
 HAVING count(*) > 1
 ) duplicates;
 IF missing_count > 0 THEN
 RAISE EXCEPTION 'Node(code 006) validation failed: % duplicate path+name combinations in tree', missing_count;
 END IF;
 -- Validate blob_id references
 SELECT count(*) INTO missing_count
 FROM unnest(NEW.tree_nodes) nodes
 LEFT JOIN blob b ON b.blob_id = nodes.blob_id
 WHERE nodes.blob_id IS NOT NULL AND b.blob_id IS NULL;
 IF missing_count > 0 THEN
 RAISE EXCEPTION 'Node(code 007) validation failed: % blob_id references do not exist', missing_count;
 END IF;
 -- Validate props_id references
 SELECT count(*) INTO missing_count
 FROM unnest(NEW.tree_nodes) nodes
 LEFT JOIN props p ON p.props_id = nodes.props_id
 WHERE nodes.props_id IS NOT NULL AND p.props_id IS NULL;
 IF missing_count > 0 THEN
 RAISE EXCEPTION 'Node(code 008) validation failed: % props_id references do not exist', missing_count;
 END IF;
 RETURN NEW;
 END;
 $$ LANGUAGE plpgsql;
CREATE TRIGGER tree_validation_trigger BEFORE
INSERT
  OR
UPDATE
  ON tree FOR EACH ROW EXECUTE FUNCTION tree_validate_tree();