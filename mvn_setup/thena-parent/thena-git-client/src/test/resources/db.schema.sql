CREATE TABLE IF NOT EXISTS tenants (
  id VARCHAR(40) PRIMARY KEY,
  rev VARCHAR(40) NOT NULL,
  prefix VARCHAR(40) NOT NULL,
  type VARCHAR(40) NOT NULL,
  name VARCHAR(255) NOT NULL,
  external_id VARCHAR(255),
  label TEXT,
  comment TEXT,
  UNIQUE(name), UNIQUE(rev), UNIQUE(prefix), UNIQUE(external_id)
);

CREATE INDEX IF NOT EXISTS tenants_NAME_INDEX ON tenants (name);
CREATE INDEX IF NOT EXISTS tenants_EXT_INDEX ON tenants (external_id);

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'tenant_alias_config') THEN

    CREATE DOMAIN tenant_alias_text AS TEXT NOT NULL;
    CREATE DOMAIN tenant_alias_jsonb AS JSONB NOT NULL;

    CREATE TYPE tenant_alias_config AS (
      config_type tenant_alias_text,
      config_body tenant_alias_jsonb
    );
  END IF;
END
$$;

CREATE TABLE IF NOT EXISTS tenant_alias (
  id UUID PRIMARY KEY,
  ref_tenant_id TEXT NOT NULL REFERENCES tenants(id),
  alias_tenant_id TEXT NOT NULL REFERENCES tenants(id),

  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL,

  created_by TEXT NOT NULL,
  updated_by TEXT NOT NULL,

  alias_name TEXT NOT NULL UNIQUE,
  alias_desc TEXT,
  alias_config tenant_alias_config[]
);

CREATE INDEX IF NOT EXISTS tenant_alias_REF_INDEX ON tenant_alias (ref_tenant_id);
CREATE INDEX IF NOT EXISTS tenant_alias_ALIAS_INDEX ON tenant_alias (alias_tenant_id);
CREATE INDEX IF NOT EXISTS tenant_alias_NAME_INDEX ON tenant_alias (alias_name);


CREATE TABLE IF NOT EXISTS tenant_member (
  id UUID PRIMARY KEY,
  external_id TEXT NOT NULL,
  alias_id UUID REFERENCES tenant_alias(id),
  alias_status BOOLEAN NOT NULL,

  UNIQUE (external_id, alias_id)
);

CREATE INDEX IF NOT EXISTS tenant_member_EXT_INDEX ON tenant_member (external_id);
CREATE INDEX IF NOT EXISTS tenant_member_ALIAS_INDEX ON tenant_member (alias_id);
CREATE INDEX IF NOT EXISTS tenant_member_GRP_INDEX ON tenant_member (external_id, alias_id);


CREATE TABLE git_blobs
(
  id VARCHAR(40) PRIMARY KEY,
  value jsonb NOT NULL
);

CREATE TABLE git_commits
(
  id VARCHAR(40) PRIMARY KEY,
  datetime VARCHAR(29) NOT NULL,
  author VARCHAR(40) NOT NULL,
  message VARCHAR(255) NOT NULL,
  tree VARCHAR(40) NOT NULL,
  parent VARCHAR(40),
  merge VARCHAR(40)
);
CREATE INDEX git_commits_TREE_INDEX ON git_commits (tree);
CREATE INDEX git_commits_PARENT_INDEX ON git_commits (tree);

CREATE TABLE git_treeItems(  id SERIAL PRIMARY KEY,  name VARCHAR(255) NOT NULL,  blob VARCHAR(40) NOT NULL,  tree VARCHAR(40) NOT NULL);CREATE INDEX git_treeItems_TREE_INDEX ON git_treeItems (tree);
CREATE INDEX git_treeItems_PARENT_INDEX ON git_treeItems (tree);

CREATE TABLE git_trees
(
  id VARCHAR(40) PRIMARY KEY
);

CREATE TABLE git_refs
(
  name VARCHAR(100) PRIMARY KEY,
  commit VARCHAR(40) NOT NULL
);

CREATE TABLE git_tags
(
  id VARCHAR(40) PRIMARY KEY,
  commit VARCHAR(40) NOT NULL,
  datetime VARCHAR(29) NOT NULL,
  author VARCHAR(40) NOT NULL,
  message VARCHAR(100) NOT NULL
);

ALTER TABLE git_commits
  ADD CONSTRAINT git_commits_COMMIT_PARENT_FK
  FOREIGN KEY (parent)
  REFERENCES git_commits (id);
ALTER TABLE git_commits
  ADD CONSTRAINT git_commits_COMMIT_TREE_FK
  FOREIGN KEY (tree)
  REFERENCES git_trees (id);

ALTER TABLE git_refs
  ADD CONSTRAINT git_refs_REF_COMMIT_FK
  FOREIGN KEY (commit)
  REFERENCES git_commits (id);

ALTER TABLE git_tags
  ADD CONSTRAINT git_tags_TAG_COMMIT_FK
  FOREIGN KEY (commit)
  REFERENCES git_commits (id);

ALTER TABLE git_treeItems
  ADD CONSTRAINT git_treeItems_TREE_ITEM_BLOB_FK
  FOREIGN KEY (blob)
  REFERENCES git_blobs (id);
ALTER TABLE git_treeItems
  ADD CONSTRAINT git_treeItems_TREE_ITEM_PARENT_FK
  FOREIGN KEY (tree)
  REFERENCES git_trees (id);
ALTER TABLE git_treeItems
  ADD CONSTRAINT git_treeItems_TREE_NAME_BLOB_UNIQUE
  UNIQUE (tree, name, blob);
