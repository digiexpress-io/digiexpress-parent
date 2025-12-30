CREATE TABLE IF NOT EXISTS cockpit_config (
  id UUID PRIMARY KEY,
  commit_id UUID NOT NULL,
  created_commit_id UUID NOT NULL,
  updated_tree_commit_id UUID NOT NULL,
  external_id VARCHAR(255),
  cockpit_config_name VARCHAR(255) NOT NULL,
  cockpit_config_desc TEXT
);
CREATE INDEX IF NOT EXISTS cockpit_config_COMMIT_INDEX ON cockpit_config (commit_id);
CREATE INDEX IF NOT EXISTS cockpit_config_CREATED_COMMIT_INDEX ON cockpit_config (created_commit_id);
CREATE INDEX IF NOT EXISTS cockpit_config_UPDATED_TREE_COMMIT_INDEX ON cockpit_config (updated_tree_commit_id);
CREATE INDEX IF NOT EXISTS cockpit_config_NAME_INDEX ON cockpit_config (cockpit_config_name);
CREATE TABLE IF NOT EXISTS cockpit_config_tenant (
  id UUID PRIMARY KEY,
  cockpit_config_id UUID NOT NULL,
  commit_id UUID NOT NULL,
  created_commit_id UUID NOT NULL,
  external_id VARCHAR(255) NOT NULL,
  external_branch VARCHAR(255) NOT NULL,
  cockpit_config_tenant_desc TEXT,
  cockpit_config_tenant_extension JSONB
);
CREATE INDEX IF NOT EXISTS cockpit_config_tenant_CONFIG_INDEX ON cockpit_config_tenant (cockpit_config_id);
CREATE INDEX IF NOT EXISTS cockpit_config_tenant_COMMIT_INDEX ON cockpit_config_tenant (commit_id);
CREATE INDEX IF NOT EXISTS cockpit_config_tenant_CREATED_COMMIT_INDEX ON cockpit_config_tenant (created_commit_id);
CREATE INDEX IF NOT EXISTS cockpit_config_tenant_EXTERNAL_ID_INDEX ON cockpit_config_tenant (external_id);
CREATE INDEX IF NOT EXISTS cockpit_config_tenant_EXTERNAL_BRANCH_INDEX ON cockpit_config_tenant (external_branch);
CREATE TABLE IF NOT EXISTS cockpit_config_props (
  id UUID PRIMARY KEY,
  cockpit_config_id UUID NOT NULL,
  commit_id UUID NOT NULL,
  created_commit_id UUID NOT NULL,
  cockpit_config_props_type VARCHAR(255) NOT NULL,
  cockpit_config_props_extension JSONB
);
CREATE INDEX IF NOT EXISTS cockpit_config_props_CONFIG_INDEX ON cockpit_config_props (cockpit_config_id);
CREATE INDEX IF NOT EXISTS cockpit_config_props_COMMIT_INDEX ON cockpit_config_props (commit_id);
CREATE INDEX IF NOT EXISTS cockpit_config_props_CREATED_COMMIT_INDEX ON cockpit_config_props (created_commit_id);
CREATE INDEX IF NOT EXISTS cockpit_config_props_TYPE_INDEX ON cockpit_config_props (cockpit_config_props_type);
CREATE TABLE IF NOT EXISTS cockpit_commit (
  id UUID PRIMARY KEY,
  parent_id UUID,
  config_id UUID,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL,
  commit_author VARCHAR(255) NOT NULL,
  commit_message VARCHAR(255) NOT NULL
);
CREATE INDEX IF NOT EXISTS cockpit_commit_PARENT_INDEX ON cockpit_commit (parent_id);
CREATE INDEX IF NOT EXISTS cockpit_commit_AUTH_INDEX ON cockpit_commit (commit_author);
CREATE TABLE IF NOT EXISTS cockpit_commit_tree (
  id UUID PRIMARY KEY,
  commit_id UUID NOT NULL,
  operation_type VARCHAR(40),
  body_after JSONB,
  body_before JSONB
);
CREATE INDEX IF NOT EXISTS cockpit_commit_tree_COMMIT_INDEX ON cockpit_commit_tree (commit_id);
ALTER TABLE
  cockpit_config
ADD
  CONSTRAINT fk_cockpit_commit FOREIGN KEY (commit_id) REFERENCES cockpit_commit(id);
ALTER TABLE
  cockpit_config
ADD
  CONSTRAINT fk_cockpit_config_created_commit FOREIGN KEY (created_commit_id) REFERENCES cockpit_commit(id);
ALTER TABLE
  cockpit_config
ADD
  CONSTRAINT fk_cockpit_config_updated_tree_commit FOREIGN KEY (updated_tree_commit_id) REFERENCES cockpit_commit(id);
ALTER TABLE
  cockpit_config_tenant
ADD
  CONSTRAINT fk_cockpit_config_tenant_config FOREIGN KEY (cockpit_config_id) REFERENCES cockpit_config(id);
ALTER TABLE
  cockpit_config_tenant
ADD
  CONSTRAINT fk_cockpit_config_tenant_commit FOREIGN KEY (commit_id) REFERENCES cockpit_commit(id);
ALTER TABLE
  cockpit_config_tenant
ADD
  CONSTRAINT fk_cockpit_config_tenant_created_commit FOREIGN KEY (created_commit_id) REFERENCES cockpit_commit(id);
ALTER TABLE
  cockpit_config_props
ADD
  CONSTRAINT fk_cockpit_config_props_config FOREIGN KEY (cockpit_config_id) REFERENCES cockpit_config(id);
ALTER TABLE
  cockpit_config_props
ADD
  CONSTRAINT fk_cockpit_config_props_commit FOREIGN KEY (commit_id) REFERENCES cockpit_commit(id);
ALTER TABLE
  cockpit_config_props
ADD
  CONSTRAINT fk_cockpit_config_props_created_commit FOREIGN KEY (created_commit_id) REFERENCES cockpit_commit(id);
ALTER TABLE
  cockpit_commit
ADD
  CONSTRAINT fk_cockpit_commit_parent FOREIGN KEY (parent_id) REFERENCES cockpit_commit(id);
ALTER TABLE
  cockpit_commit_tree
ADD
  CONSTRAINT fk_cockpit_commit_tree_commit FOREIGN KEY (commit_id) REFERENCES cockpit_commit(id);