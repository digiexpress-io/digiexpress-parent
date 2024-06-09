CREATE TABLE IF NOT EXISTS tenants
(
  id VARCHAR(40) PRIMARY KEY,
  rev VARCHAR(40) NOT NULL,
  prefix VARCHAR(40) NOT NULL,
  type VARCHAR(40) NOT NULL,
  name VARCHAR(255) NOT NULL,
  external_id VARCHAR(255),
  UNIQUE(name), UNIQUE(rev), UNIQUE(prefix), UNIQUE(external_id)
);
CREATE INDEX IF NOT EXISTS tenants_NAME_INDEX ON tenants (name);
CREATE INDEX IF NOT EXISTS tenants_EXT_INDEX ON tenants (external_id);

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
  doc_meta jsonb
);
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
  PRIMARY KEY (branch_id),
  UNIQUE (doc_id, branch_name)
);
CREATE INDEX doc_branch_DOC_DOC_ID_INDEX ON doc_branch (doc_id);
CREATE INDEX doc_branch_DOC_BRANCH_NAME_INDEX ON doc_branch (branch_name);
CREATE INDEX doc_branch_DOC_COMMIT_ID_INDEX ON doc_branch (commit_id);

ALTER TABLE doc_branch
  ADD CONSTRAINT doc_branch_DOC_ID_FK
  FOREIGN KEY (doc_id)
  REFERENCES doc (id) ON DELETE CASCADE;


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
  REFERENCES doc (id) ON DELETE CASCADE;

ALTER TABLE doc_commands
  ADD CONSTRAINT doc_commands_BRANCH_FK
  FOREIGN KEY (branch_id)
  REFERENCES doc_branch (branch_id) ON DELETE CASCADE;


CREATE TABLE org_rights
(
  id VARCHAR(40) PRIMARY KEY,
  commit_id VARCHAR(40) NOT NULL,
  created_commit_id VARCHAR(40) NOT NULL,
  external_id VARCHAR(40) UNIQUE,
  right_sub_type VARCHAR(40) NOT NULL,
  right_name VARCHAR(255) UNIQUE NOT NULL,
  right_description VARCHAR(255) NOT NULL,
  right_status VARCHAR(40) NOT NULL,
  right_data_extension JSONB
);
CREATE INDEX org_rights_NAME_INDEX ON org_rights (right_name);
CREATE INDEX org_rights_COMMIT_INDEX ON org_rights (commit_id);
CREATE INDEX org_rights_EXTERNAL_INDEX ON org_rights (external_id);

CREATE TABLE org_parties
(
  id VARCHAR(40) PRIMARY KEY,
  commit_id VARCHAR(40) NOT NULL,
  created_commit_id VARCHAR(40) NOT NULL,
  external_id VARCHAR(40) UNIQUE,
  parent_id VARCHAR(40),
  party_name VARCHAR(255) UNIQUE NOT NULL,
  party_description VARCHAR(255) NOT NULL,
  party_status VARCHAR(40) NOT NULL,
  party_data_extension JSONB,
  party_sub_type VARCHAR(40) NOT NULL
);

ALTER TABLE org_parties
  ADD CONSTRAINT org_parties_PARENT_FK
  FOREIGN KEY (parent_id)
  REFERENCES org_parties (id);
CREATE INDEX org_parties_NAME_INDEX ON org_parties (party_name);
CREATE INDEX org_parties_COMMIT_INDEX ON org_parties (commit_id);
CREATE INDEX org_parties_EXTERNAL_INDEX ON org_parties (external_id);

CREATE TABLE org_party_rights
(
  id VARCHAR(40) PRIMARY KEY,
  commit_id VARCHAR(40) NOT NULL,
  party_id VARCHAR(40) NOT NULL,
  right_id VARCHAR(40) NOT NULL,
  UNIQUE (right_id, party_id)
);
CREATE INDEX org_party_rights_COMMIT_INDEX ON org_party_rights (commit_id);
CREATE INDEX org_party_rights_PARTY_INDEX ON org_party_rights (party_id);
CREATE INDEX org_party_rights_RIGHT_INDEX ON org_party_rights (right_id);

CREATE TABLE org_members
(
  id VARCHAR(40) PRIMARY KEY,
  commit_id VARCHAR(40) NOT NULL,
  created_commit_id VARCHAR(40) NOT NULL,
  external_id VARCHAR(40) UNIQUE,
  member_status VARCHAR(40) NOT NULL,
  member_data_extension JSONB,
  username VARCHAR(255) UNIQUE NOT NULL,
  email VARCHAR(255) NOT NULL
);
CREATE INDEX org_members_COMMIT_INDEX ON org_members (commit_id);
CREATE INDEX org_members_EXTERNAL_INDEX ON org_members (external_id);
CREATE INDEX org_members_MEMBER_NAME_INDEX ON org_members (username);

CREATE TABLE org_member_rights
(
  id VARCHAR(40) PRIMARY KEY,
  commit_id VARCHAR(40) NOT NULL,
  member_id VARCHAR(40) NOT NULL,
  right_id VARCHAR(40) NOT NULL,
  party_id VARCHAR(40),
  UNIQUE NULLS NOT DISTINCT(member_id, right_id, party_id)
);
CREATE INDEX org_member_rights_COMMIT_INDEX ON org_member_rights (commit_id);
CREATE INDEX org_member_rights_RIGHT_INDEX ON org_member_rights (right_id);
CREATE INDEX org_member_rights_MEMBER_INDEX ON org_member_rights (member_id);
CREATE INDEX org_member_rights_PARTY_INDEX ON org_member_rights (party_id);
CREATE INDEX org_member_rights_REF_INDEX ON org_member_rights (right_id, member_id);
CREATE INDEX org_member_rights_REF_2_INDEX ON org_member_rights (right_id, member_id, party_id);

CREATE TABLE org_memberships
(
  id VARCHAR(40) PRIMARY KEY,
  commit_id VARCHAR(40) NOT NULL,
  member_id VARCHAR(40) NOT NULL,
  party_id VARCHAR(40) NOT NULL,
  UNIQUE (member_id, party_id)
);
CREATE INDEX org_memberships_COMMIT_INDEX ON org_memberships (commit_id);
CREATE INDEX org_memberships_MEMBER_INDEX ON org_memberships (member_id);
CREATE INDEX org_memberships_PARTY_INDEX ON org_memberships (party_id);
CREATE INDEX org_memberships_REF_INDEX ON org_memberships (party_id, member_id);

CREATE TABLE org_commits
(
  commit_id VARCHAR(40) PRIMARY KEY,
  parent_id VARCHAR(40),
  created_at TIMESTAMP WITH TIME ZONE NOT NULL,
  commit_log TEXT NOT NULL,
  commit_author VARCHAR(255) NOT NULL,
  commit_message VARCHAR(255) NOT NULL
);

ALTER TABLE org_commits
  ADD CONSTRAINT org_commits_PARENT_FK
  FOREIGN KEY (parent_id)
  REFERENCES org_commits (commit_id);
CREATE INDEX org_commits_PARENT_INDEX ON org_commits (parent_id);

--- constraints fororg_rights

ALTER TABLE org_member_rights
  ADD CONSTRAINT org_member_rights_RIGHT_FK
  FOREIGN KEY (right_id)
  REFERENCES org_rights (id);



ALTER TABLE org_party_rights
  ADD CONSTRAINT org_party_rights_RIGHT_FK
  FOREIGN KEY (right_id)
  REFERENCES org_rights (id);



--- constraints fororg_members

ALTER TABLE org_memberships
  ADD CONSTRAINT org_memberships_MEMBER_FK
  FOREIGN KEY (member_id)
  REFERENCES org_members (id);



ALTER TABLE org_member_rights
  ADD CONSTRAINT org_member_rights_MEMBER_FK
  FOREIGN KEY (member_id)
  REFERENCES org_members (id);



--- constraints fororg_parties

ALTER TABLE org_party_rights
  ADD CONSTRAINT org_party_rights_PARTY_FK
  FOREIGN KEY (party_id)
  REFERENCES org_parties (id);



ALTER TABLE org_member_rights
  ADD CONSTRAINT org_member_rights_PARTY_FK
  FOREIGN KEY (party_id)
  REFERENCES org_parties (id);



ALTER TABLE org_memberships
  ADD CONSTRAINT org_memberships_PARTY_FK
  FOREIGN KEY (party_id)
  REFERENCES org_parties (id);


ALTER TABLE org_member_rights
  ADD CONSTRAINT org_member_rights_PARTY_MEMBER_FK
  FOREIGN KEY (party_id, member_id)
  REFERENCES org_memberships (party_id, member_id);


--- constraints fororg_commits

ALTER TABLE org_commit_trees
  ADD CONSTRAINT org_commit_trees_COMMIT_FK
  FOREIGN KEY (commit_id)
  REFERENCES org_commits (commit_id);



ALTER TABLE org_members
  ADD CONSTRAINT org_members_COMMIT_FK
  FOREIGN KEY (commit_id)
  REFERENCES org_commits (commit_id);



ALTER TABLE org_member_rights
  ADD CONSTRAINT org_member_rights_COMMIT_FK
  FOREIGN KEY (commit_id)
  REFERENCES org_commits (commit_id);



ALTER TABLE org_memberships
  ADD CONSTRAINT org_memberships_COMMIT_FK
  FOREIGN KEY (commit_id)
  REFERENCES org_commits (commit_id);



ALTER TABLE org_parties
  ADD CONSTRAINT org_parties_COMMIT_FK
  FOREIGN KEY (commit_id)
  REFERENCES org_commits (commit_id);



ALTER TABLE org_party_rights
  ADD CONSTRAINT org_party_rights_COMMIT_FK
  FOREIGN KEY (commit_id)
  REFERENCES org_commits (commit_id);



ALTER TABLE org_rights
  ADD CONSTRAINT org_rights_COMMIT_FK
  FOREIGN KEY (commit_id)
  REFERENCES org_commits (commit_id);



ALTER TABLE org_members
  ADD CONSTRAINT org_members_CREATED_COMMIT_ID_FK
  FOREIGN KEY (created_commit_id)
  REFERENCES org_commits (commit_id);



ALTER TABLE org_parties
  ADD CONSTRAINT org_parties_CREATED_COMMIT_ID_FK
  FOREIGN KEY (created_commit_id)
  REFERENCES org_commits (commit_id);



ALTER TABLE org_rights
  ADD CONSTRAINT org_rights_CREATED_COMMIT_ID_FK
  FOREIGN KEY (created_commit_id)
  REFERENCES org_commits (commit_id);


