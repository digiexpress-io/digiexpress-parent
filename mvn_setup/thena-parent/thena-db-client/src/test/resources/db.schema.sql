CREATE TABLE IF NOT EXISTS tenants
(
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



CREATE TABLE fs_commits
(
  commit_id VARCHAR(40) PRIMARY KEY,
  parent_id VARCHAR(40),
  dirent_id VARCHAR(40),
  created_at TIMESTAMP WITH TIME ZONE NOT NULL,
  commit_log TEXT NOT NULL,
  commit_author VARCHAR(255) NOT NULL,
  commit_message VARCHAR(255) NOT NULL
);
CREATE INDEX fs_commits_PARENT_INDEX ON fs_commits (parent_id);
CREATE INDEX fs_commits_DIRENT_INDEX ON fs_commits (dirent_id);
CREATE INDEX fs_commits_AUTH_INDEX ON fs_commits (commit_author);
ALTER TABLE fs_commits
  ADD CONSTRAINT fs_commits_PARENT_FK
  FOREIGN KEY (parent_id)
  REFERENCES fs_commits (commit_id);

CREATE TABLE fs_commit_trees
(
  id VARCHAR(40) PRIMARY KEY,
  commit_id VARCHAR(40) NOT NULL,
  operation_type VARCHAR(40),
  body_after JSONB,
  body_before JSONB
);
CREATE INDEX fs_commit_trees_COMMIT_INDEX ON fs_commit_trees (commit_id);

CREATE TABLE fs_dirents
(
  id VARCHAR(40) PRIMARY KEY,
  commit_id VARCHAR(40) NOT NULL,
  created_commit_id VARCHAR(40) NOT NULL,
  updated_tree_commit_id VARCHAR(40) NOT NULL,
  external_id VARCHAR(40) UNIQUE,
  dirent_parent_id VARCHAR(40),
  dirent_ref VARCHAR(40) NOT NULL,
  dirent_type VARCHAR(100) NOT NULL,
  dirent_name TEXT NOT NULL,
  dirent_description TEXT NOT NULL,
  dirent_user_type VARCHAR(100),
  archived_at TIMESTAMP WITH TIME ZONE,
  archived_status VARCHAR(40)
);
CREATE SEQUENCE fs_dirent_ref MINVALUE 1 MAXVALUE 999999 CYCLE;
CREATE INDEX fs_dirents_REF_INDEX ON fs_dirents (dirent_ref);
CREATE INDEX fs_dirents_EXT_ID_INDEX ON fs_dirents (external_id);
CREATE INDEX fs_dirents_NAME_INDEX ON fs_dirents (dirent_name);
CREATE INDEX fs_dirents_PARENT_INDEX ON fs_dirents (dirent_parent_id);
CREATE INDEX fs_dirents_COMMIT_INDEX ON fs_dirents (commit_id);
CREATE INDEX fs_dirents_CREATED_INDEX ON fs_dirents (created_commit_id);
CREATE INDEX fs_dirents_TREE_UPDATED_INDEX ON fs_dirents (updated_tree_commit_id);
ALTER TABLE fs_dirents
  ADD CONSTRAINT fs_dirents_PARENT_FK
  FOREIGN KEY (dirent_parent_id)
  REFERENCES fs_dirents (id);

CREATE TABLE fs_dirent_data
(
  id VARCHAR(40) PRIMARY KEY,
  commit_id VARCHAR(40) NOT NULL,
  created_commit_id VARCHAR(40) NOT NULL,
  data_extension JSONB,
  dirent_id VARCHAR(40) NOT NULL,
  UNIQUE NULLS NOT DISTINCT(dirent_id)
);
CREATE INDEX fs_dirent_data_CREATED_INDEX ON fs_dirent_data (created_commit_id);
CREATE INDEX fs_dirent_data_DIRENT_INDEX ON fs_dirent_data (dirent_id);

CREATE TABLE fs_dirent_labels
(
  id VARCHAR(40) PRIMARY KEY,
  commit_id VARCHAR(40) NOT NULL,
  label_type VARCHAR(100) NOT NULL,
  label_value VARCHAR(255) NOT NULL,
  label_body JSONB,
  dirent_id VARCHAR(40) NOT NULL,
  UNIQUE NULLS NOT DISTINCT(dirent_id, label_type, label_value)
);
CREATE INDEX fs_dirent_labels_DIRENT_INDEX ON fs_dirent_labels (dirent_id);
CREATE INDEX fs_dirent_labels_LABEL_INDEX ON fs_dirent_labels (label_value);

CREATE TABLE fs_dirent_links
(
  id VARCHAR(40) PRIMARY KEY,
  commit_id VARCHAR(40) NOT NULL,
  created_commit_id VARCHAR(40) NOT NULL,
  dirent_id VARCHAR(40) NOT NULL,
  link_type VARCHAR(100) NOT NULL,
  external_id TEXT NOT NULL,
  link_body JSONB,
  UNIQUE NULLS NOT DISTINCT(dirent_id, link_type, external_id)
);
CREATE INDEX fs_dirent_links_DIRENT_INDEX ON fs_dirent_links (dirent_id);

CREATE TABLE fs_dirent_remarks
(
  id VARCHAR(40) PRIMARY KEY,
  commit_id VARCHAR(40) NOT NULL,
  parent_id VARCHAR(40),
  created_commit_id VARCHAR(40) NOT NULL,
  dirent_id VARCHAR(40) NOT NULL,
  reporter_id VARCHAR(255) NOT NULL,
  remark_status VARCHAR(100),
  remark_type VARCHAR(100),
  remark_source VARCHAR(100),
  remark_text TEXT NOT NULL
);
ALTER TABLE fs_dirent_remarks
  ADD CONSTRAINT fs_dirent_remarks_PARENT_FK
  FOREIGN KEY (parent_id)
  REFERENCES fs_dirent_remarks (id);

CREATE INDEX fs_dirent_remarks_CREATED_INDEX ON fs_dirent_remarks (created_commit_id);
CREATE INDEX fs_dirent_remarks_DIRENT_INDEX ON fs_dirent_remarks (dirent_id);

CREATE TABLE fs_dirent_assignment
(
  id VARCHAR(40) PRIMARY KEY,
  commit_id VARCHAR(40) NOT NULL,
  dirent_id VARCHAR(40) NOT NULL,
  assignee VARCHAR(255) NOT NULL,
  assignment_type VARCHAR(100) NOT NULL,
  assignee_contact TEXT,
  UNIQUE NULLS NOT DISTINCT(dirent_id, assignee, assignment_type)
);
CREATE INDEX fs_dirent_assignment_DIRENT_INDEX ON fs_dirent_assignment (dirent_id);

--- constraints forfs_commits

ALTER TABLE fs_dirent_assignment
  ADD CONSTRAINT fs_dirent_assignment_COMMIT_FK
  FOREIGN KEY (commit_id)
  REFERENCES fs_commits (commit_id);


ALTER TABLE fs_commit_trees
  ADD CONSTRAINT fs_commit_trees_COMMIT_FK
  FOREIGN KEY (commit_id)
  REFERENCES fs_commits (commit_id);


ALTER TABLE fs_dirent_data
  ADD CONSTRAINT fs_dirent_data_COMMIT_FK
  FOREIGN KEY (commit_id)
  REFERENCES fs_commits (commit_id);


ALTER TABLE fs_dirent_labels
  ADD CONSTRAINT fs_dirent_labels_COMMIT_FK
  FOREIGN KEY (commit_id)
  REFERENCES fs_commits (commit_id);


ALTER TABLE fs_dirent_links
  ADD CONSTRAINT fs_dirent_links_COMMIT_FK
  FOREIGN KEY (commit_id)
  REFERENCES fs_commits (commit_id);


ALTER TABLE fs_dirent_remarks
  ADD CONSTRAINT fs_dirent_remarks_COMMIT_FK
  FOREIGN KEY (commit_id)
  REFERENCES fs_commits (commit_id);


ALTER TABLE fs_dirents
  ADD CONSTRAINT fs_dirents_COMMIT_FK
  FOREIGN KEY (commit_id)
  REFERENCES fs_commits (commit_id);


ALTER TABLE fs_dirents
  ADD CONSTRAINT fs_dirents_CREATED_COMMIT_ID_FK
  FOREIGN KEY (created_commit_id)
  REFERENCES fs_commits (commit_id);


ALTER TABLE fs_dirents
  ADD CONSTRAINT fs_dirents_UPDATED_TREE_COMMIT_ID_FK
  FOREIGN KEY (updated_tree_commit_id)
  REFERENCES fs_commits (commit_id);


ALTER TABLE fs_dirent_links
  ADD CONSTRAINT fs_dirent_links_CREATED_COMMIT_ID_FK
  FOREIGN KEY (created_commit_id)
  REFERENCES fs_commits (commit_id);


--- constraints forfs_commit_trees

--- constraints forfs_dirents

--- constraints forfs_dirent_data
ALTER TABLE fs_dirent_data
  ADD CONSTRAINT fs_dirent_labels_DIRENT_FK
  FOREIGN KEY (dirent_id)
  REFERENCES fs_dirents (id);


--- constraints forfs_dirent_labels
ALTER TABLE fs_dirent_labels
  ADD CONSTRAINT fs_dirent_labels_DIRENT_FK
  FOREIGN KEY (dirent_id)
  REFERENCES fs_dirents (id);


--- constraints forfs_dirent_links
ALTER TABLE fs_dirent_links
  ADD CONSTRAINT fs_dirent_links_DIRENT_FK
  FOREIGN KEY (dirent_id)
  REFERENCES fs_dirents (id);


ALTER TABLE fs_dirent_remarks
  ADD CONSTRAINT fs_dirent_remarks_DIRENT_FK
  FOREIGN KEY (dirent_id)
  REFERENCES fs_dirents (id);


--- constraints forfs_dirent_assignment
ALTER TABLE fs_dirent_assignment
  ADD CONSTRAINT fs_dirent_assignment_DIRENT_FK
  FOREIGN KEY (dirent_id)
  REFERENCES fs_dirents (id);

