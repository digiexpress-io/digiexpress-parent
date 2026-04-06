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


CREATE TABLE doc
(
  id VARCHAR(100) PRIMARY KEY,
  commit_id VARCHAR(40) NOT NULL,
  created_with_commit_id VARCHAR(40) NOT NULL,
  external_id VARCHAR(100) UNIQUE,
  owner_id VARCHAR(100),
  doc_parent_id VARCHAR(100),
  doc_type VARCHAR(40) NOT NULL,
  doc_status VARCHAR(8) NOT NULL,
  doc_starts_at TIMESTAMP WITH TIME ZONE,
  doc_ends_at TIMESTAMP WITH TIME ZONE,
  doc_name TEXT UNIQUE,
  doc_description TEXT,
  doc_sub_status VARCHAR(100),
  doc_meta jsonb
);
CREATE INDEX doc_DOC_STARTS_AT_INDEX ON doc (doc_starts_at);
CREATE INDEX doc_DOC_ENDS_AT_INDEX ON doc (doc_ends_at);
CREATE INDEX doc_DOC_SUB_STATUS_INDEX ON doc (doc_sub_status);
CREATE INDEX doc_DOC_NAME_INDEX ON doc (doc_name);
CREATE INDEX doc_DOC_EXT_INDEX ON doc (external_id);
CREATE INDEX doc_DOC_PARENT_INDEX ON doc (doc_parent_id);
CREATE INDEX doc_DOC_TYPE_INDEX ON doc (doc_type);
CREATE INDEX doc_DOC_OWNER_INDEX ON doc (owner_id);
ALTER TABLE doc
  ADD CONSTRAINT doc_DOC_PARENT_FK
  FOREIGN KEY (doc_parent_id)
  REFERENCES doc (id);


CREATE TABLE doc_branch
(
  doc_id                   VARCHAR(100) NOT NULL,
  branch_id                VARCHAR(40) NOT NULL,
  commit_id                VARCHAR(40) NOT NULL,
  created_with_commit_id   VARCHAR(40) NOT NULL,
  branch_name              VARCHAR(255) NOT NULL,
  branch_status            VARCHAR(40) NOT NULL,
  value                    JSONB NOT NULL,
  value_starts_at          TIMESTAMP WITH TIME ZONE,
  value_ends_at            TIMESTAMP WITH TIME ZONE,
  value_name               TEXT,
  value_description        TEXT,
  value_status             VARCHAR(100),
  PRIMARY KEY (branch_id),
  UNIQUE (doc_id, branch_name)
);
CREATE INDEX doc_branch_VALUE_STARTS_AT_INDEX ON doc_branch (value_starts_at);
CREATE INDEX doc_branch_VALUE_ENDS_AT_INDEX ON doc_branch (value_ends_at);
CREATE INDEX doc_branch_VALUE_STATUS_INDEX ON doc_branch (value_status);
CREATE INDEX doc_branch_VALUE_NAME_INDEX ON doc_branch (value_name);
CREATE INDEX doc_branch_DOC_DOC_ID_INDEX ON doc_branch (doc_id);
CREATE INDEX doc_branch_DOC_BRANCH_NAME_INDEX ON doc_branch (branch_name);
CREATE INDEX doc_branch_DOC_COMMIT_ID_INDEX ON doc_branch (commit_id);

ALTER TABLE doc_branch
  ADD CONSTRAINT doc_branch_DOC_ID_FK
  FOREIGN KEY (doc_id)
  REFERENCES doc (id);


CREATE TABLE doc_commits
(
  id VARCHAR(40) PRIMARY KEY,
  branch_id VARCHAR(40),
  doc_id VARCHAR(100) NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL,
  author VARCHAR(255) NOT NULL,
  message TEXT NOT NULL,
  commit_log TEXT NOT NULL,
  parent VARCHAR(40)
);
CREATE INDEX doc_commits_DOC_COMMIT_DOC_ID_INDEX ON doc_commits (doc_id);
CREATE INDEX doc_commits_DOC_COMMIT_PARENT_INDEX ON doc_commits (parent);
CREATE INDEX doc_commits_DOC_COMMIT_BRANCH_ID_INDEX ON doc_commits (branch_id);
ALTER TABLE doc_commits
  ADD CONSTRAINT doc_commits_DOC_COMMIT_PARENT_FK
  FOREIGN KEY (parent)
  REFERENCES doc_commits (id);


ALTER TABLE doc_commits
  ADD CONSTRAINT doc_commits_DOC_COMMIT_FK
  FOREIGN KEY (doc_id)
  REFERENCES doc (id);


CREATE TABLE doc_log
(
  id VARCHAR(40) PRIMARY KEY,
  commit_id VARCHAR(40) NOT NULL,
  doc_id VARCHAR(100) NOT NULL,
  branch_id VARCHAR(40),
  operation_type VARCHAR(100) NOT NULL,
  body_type VARCHAR(100) NOT NULL,
  body_after jsonb,
  body_before jsonb,
  body_patch jsonb
);
CREATE INDEX doc_log_DOC_INDEX ON doc_log (doc_id);
CREATE INDEX doc_log_BRANCH_INDEX ON doc_log (branch_id);
CREATE INDEX doc_log_COMMIT_INDEX ON doc_log (commit_id);

ALTER TABLE doc_log
  ADD CONSTRAINT doc_log_DOC_LOG_COMMIT_FK
  FOREIGN KEY (commit_id)
  REFERENCES doc_commits (id);


CREATE TABLE doc_commands
(
  id VARCHAR(40) PRIMARY KEY,
  commit_id VARCHAR(40) NOT NULL,
  doc_id VARCHAR(100) NOT NULL,
  branch_id VARCHAR(40),
  commands JSONB[] NOT NULL
);
CREATE INDEX doc_commands_DOC_INDEX ON doc_commands (doc_id);

--- constraints fordoc_commands
ALTER TABLE doc_commands
  ADD CONSTRAINT doc_commands_DOC_FK
  FOREIGN KEY (doc_id)
  REFERENCES doc (id);

ALTER TABLE doc_commands
  ADD CONSTRAINT doc_commands_BRANCH_FK
  FOREIGN KEY (branch_id)
  REFERENCES doc_branch (branch_id);

