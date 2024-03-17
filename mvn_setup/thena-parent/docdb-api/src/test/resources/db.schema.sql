CREATE TABLE IF NOT EXISTS repos
(
  id VARCHAR(40) PRIMARY KEY,
  rev VARCHAR(40) NOT NULL,
  prefix VARCHAR(40) NOT NULL,
  type VARCHAR(3) NOT NULL,
  name VARCHAR(255) NOT NULL,
  UNIQUE(name), UNIQUE(rev), UNIQUE(prefix)
)

CREATE TABLE blobs
(
  id VARCHAR(40) PRIMARY KEY,
  value jsonb NOT NULL
);

CREATE TABLE commits
(
  id VARCHAR(40) PRIMARY KEY,
  datetime VARCHAR(29) NOT NULL,
  author VARCHAR(40) NOT NULL,
  message VARCHAR(255) NOT NULL,
  tree VARCHAR(40) NOT NULL,
  parent VARCHAR(40),
  merge VARCHAR(40)
);

CREATE TABLE treeItems(  id SERIAL PRIMARY KEY,  name VARCHAR(255) NOT NULL,  blob VARCHAR(40) NOT NULL,  tree VARCHAR(40) NOT NULL);
CREATE TABLE trees
(
  id VARCHAR(40) PRIMARY KEY
);

CREATE TABLE refs
(
  name VARCHAR(100) PRIMARY KEY,
  commit VARCHAR(40) NOT NULL
);

CREATE TABLE tags
(
  id VARCHAR(40) PRIMARY KEY,
  commit VARCHAR(40) NOT NULL,
  datetime VARCHAR(29) NOT NULL,
  author VARCHAR(40) NOT NULL,
  message VARCHAR(100) NOT NULL
);

ALTER TABLE commits
  ADD CONSTRAINT commits_COMMIT_PARENT_FK
  FOREIGN KEY (parent)
  REFERENCES commits (id);
ALTER TABLE commits
  ADD CONSTRAINT commits_COMMIT_TREE_FK
  FOREIGN KEY (tree)
  REFERENCES trees (id);
CREATE INDEX commits_TREE_INDEX ON treeItems (tree);
CREATE INDEX commits_PARENT_INDEX ON treeItems (tree);

ALTER TABLE refs
  ADD CONSTRAINT refs_REF_COMMIT_FK
  FOREIGN KEY (commit)
  REFERENCES commits (id);

ALTER TABLE tags
  ADD CONSTRAINT tags_TAG_COMMIT_FK
  FOREIGN KEY (commit)
  REFERENCES commits (id);

ALTER TABLE treeItems
  ADD CONSTRAINT treeItems_TREE_ITEM_BLOB_FK
  FOREIGN KEY (blob)
  REFERENCES blobs (id);
ALTER TABLE treeItems
  ADD CONSTRAINT treeItems_TREE_ITEM_PARENT_FK
  FOREIGN KEY (tree)
  REFERENCES trees (id);
ALTER TABLE treeItems
  ADD CONSTRAINT treeItems_TREE_NAME_BLOB_UNIQUE
  UNIQUE (tree, name, blob);
CREATE INDEX treeItems_TREE_INDEX ON treeItems (tree);

CREATE TABLE doc
(
  id VARCHAR(40) PRIMARY KEY,
  external_id VARCHAR(40) UNIQUE,
  external_id_deleted VARCHAR(40),
  owner_id VARCHAR(40),
  doc_parent_id VARCHAR(40),
  doc_type VARCHAR(40) NOT NULL,
  doc_status VARCHAR(8) NOT NULL,
  doc_meta jsonb
);
CREATE INDEX doc_DOC_EXT_ID_INDEX ON doc (external_id);
CREATE INDEX doc_DOC_PARENT_ID_INDEX ON doc (doc_parent_id);
CREATE INDEX doc_DOC_TYPE_INDEX ON doc (doc_type);
CREATE INDEX doc_DOC_OWNER_INDEX ON doc (owner_id);
ALTER TABLE doc
  ADD CONSTRAINT doc_DOC_PARENT_FK
  FOREIGN KEY (doc_parent_id)
  REFERENCES doc (id);


CREATE TABLE doc_branch
(
  branch_name VARCHAR(255) NOT NULL,
  branch_name_deleted VARCHAR(255),
  branch_id VARCHAR(40) NOT NULL,
  commit_id VARCHAR(40) NOT NULL,
  branch_status VARCHAR(8) NOT NULL,
  doc_id VARCHAR(40),
  value jsonb NOT NULL,
  PRIMARY KEY (branch_id),
  UNIQUE (doc_id, branch_name)
);
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
  branch_id VARCHAR(40) NOT NULL,
  doc_id VARCHAR(40) NOT NULL,
  datetime VARCHAR(29) NOT NULL,
  author VARCHAR(40) NOT NULL,
  message VARCHAR(255) NOT NULL,
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
  value jsonb NOT NULL
);
CREATE INDEX doc_log_DOC_LOG_COMMIT_ID_INDEX ON doc_log (commit_id);

ALTER TABLE doc_log
  ADD CONSTRAINT doc_log_DOC_LOG_COMMIT_FK
  FOREIGN KEY (commit_id)
  REFERENCES doc_commits (id);


CREATE TABLE org_roles
(
  id VARCHAR(40) PRIMARY KEY,
  commit_id VARCHAR(40) NOT NULL,
  external_id VARCHAR(40) UNIQUE,
  role_name VARCHAR(255) UNIQUE NOT NULL,
  role_description VARCHAR(255) NOT NULL
);
CREATE INDEX org_roles_NAME_INDEX ON org_roles (role_name);
CREATE INDEX org_roles_COMMIT_INDEX ON org_roles (commit_id);
CREATE INDEX org_roles_EXTERNAL_INDEX ON org_roles (external_id);

CREATE TABLE org_groups
(
  id VARCHAR(40) PRIMARY KEY,
  commit_id VARCHAR(40) NOT NULL,
  external_id VARCHAR(40) UNIQUE,
  parent_id VARCHAR(40),
  group_name VARCHAR(255) UNIQUE NOT NULL,
  group_description VARCHAR(255) NOT NULL
);

ALTER TABLE org_groups
  ADD CONSTRAINT org_groups_PARENT_FK
  FOREIGN KEY (parent_id)
  REFERENCES org_groups (id);
CREATE INDEX org_groups_NAME_INDEX ON org_groups (group_name);
CREATE INDEX org_groups_COMMIT_INDEX ON org_groups (commit_id);
CREATE INDEX org_groups_EXTERNAL_INDEX ON org_groups (external_id);

CREATE TABLE org_group_roles
(
  id VARCHAR(40) PRIMARY KEY,
  commit_id VARCHAR(40) NOT NULL,
  group_id VARCHAR(40) NOT NULL,
  role_id VARCHAR(40) NOT NULL,
  UNIQUE (role_id, group_id)
);
CREATE INDEX org_group_roles_COMMIT_INDEX ON org_group_roles (commit_id);
CREATE INDEX org_group_roles_GROUP_INDEX ON org_group_roles (group_id);
CREATE INDEX org_group_roles_ROLE_INDEX ON org_group_roles (role_id);

CREATE TABLE org_users
(
  id VARCHAR(40) PRIMARY KEY,
  commit_id VARCHAR(40) NOT NULL,
  external_id VARCHAR(40) UNIQUE,
  username VARCHAR(255) UNIQUE NOT NULL,
  email VARCHAR(255) NOT NULL
);
CREATE INDEX org_users_COMMIT_INDEX ON org_users (commit_id);
CREATE INDEX org_users_EXTERNAL_INDEX ON org_users (external_id);
CREATE INDEX org_users_USER_NAME_INDEX ON org_users (username);

CREATE TABLE org_user_roles
(
  id VARCHAR(40) PRIMARY KEY,
  commit_id VARCHAR(40) NOT NULL,
  user_id VARCHAR(40) NOT NULL,
  role_id VARCHAR(40) NOT NULL,
  group_id VARCHAR(40),
  UNIQUE (user_id, role_id, group_id)
);
CREATE INDEX org_user_roles_COMMIT_INDEX ON org_user_roles (commit_id);
CREATE INDEX org_user_roles_ROLE_INDEX ON org_user_roles (role_id);
CREATE INDEX org_user_roles_USER_INDEX ON org_user_roles (user_id);
CREATE INDEX org_user_roles_GROUP_INDEX ON org_user_roles (group_id);
CREATE INDEX org_user_roles_REF_INDEX ON org_user_roles (role_id, user_id);
CREATE INDEX org_user_roles_REF_2_INDEX ON org_user_roles (role_id, user_id, group_id);

CREATE TABLE org_user_memberships
(
  id VARCHAR(40) PRIMARY KEY,
  commit_id VARCHAR(40) NOT NULL,
  user_id VARCHAR(40) NOT NULL,
  group_id VARCHAR(40) NOT NULL,
  UNIQUE (user_id, group_id)
);
CREATE INDEX org_user_memberships_COMMIT_INDEX ON org_user_memberships (commit_id);
CREATE INDEX org_user_memberships_USER_INDEX ON org_user_memberships (user_id);
CREATE INDEX org_user_memberships_GROUP_INDEX ON org_user_memberships (group_id);
CREATE INDEX org_user_memberships_REF_INDEX ON org_user_memberships (group_id, user_id);

CREATE TABLE org_actor_status
(
  id VARCHAR(40) PRIMARY KEY,
  commit_id VARCHAR(40) NOT NULL,
  user_id VARCHAR(40),
  role_id VARCHAR(40),
  group_id VARCHAR(40),
  actor_status VARCHAR(100) NOT NULL,
  UNIQUE (user_id, role_id, group_id)
);
CREATE INDEX org_actor_status_COMMIT_INDEX ON org_actor_status (commit_id);
CREATE INDEX org_actor_status_ROLE_INDEX ON org_actor_status (role_id);
CREATE INDEX org_actor_status_USER_INDEX ON org_actor_status (user_id);
CREATE INDEX org_actor_status_GROUP_INDEX ON org_actor_status (group_id);

CREATE TABLE org_commits
(
  commit_id VARCHAR(40) PRIMARY KEY,
  parent_id VARCHAR(40),
  created_at TIMESTAMP WITH TIME ZONE NOT NULL,
  commit_log TEXT NOT NULL,
  commit_author VARCHAR(255) NOT NULL,
  commit_message VARCHAR(255) NOT NULL
);

CREATE TABLE org_commit_trees
(
  id VARCHAR(40) PRIMARY KEY,
  commit_id VARCHAR(40) NOT NULL,
  parent_commit_id VARCHAR(40),
  actor_id VARCHAR(40) NOT NULL,
  actor_type VARCHAR(40) NOT NULL,
  value JSONB NOT NULL
);

ALTER TABLE org_commits
  ADD CONSTRAINT org_commits_PARENT_FK
  FOREIGN KEY (parent_id)
  REFERENCES org_commits (commit_id);
CREATE INDEX org_commits_PARENT_INDEX ON org_commits (parent_id);
ALTER TABLE org_commit_trees
  ADD CONSTRAINT org_commit_trees_COMMIT_FK
  FOREIGN KEY (commit_id)
  REFERENCES org_commits (commit_id);
ALTER TABLE org_commit_trees
  ADD CONSTRAINT org_commit_trees_PARENT_FK
  FOREIGN KEY (parent_commit_id)
  REFERENCES org_commits (commit_id);
CREATE INDEX org_commit_trees_ACTOR_INDEX ON org_commit_trees (actor_type, actor_id);
CREATE INDEX org_commit_trees_COMMIT_INDEX ON org_commit_trees (commit_id);
CREATE INDEX org_commit_trees_PARENT_INDEX ON org_commit_trees (parent_commit_id);

CREATE TABLE org_actor_data
(
  id VARCHAR(40) PRIMARY KEY,
  commit_id VARCHAR(40) NOT NULL,
  parent_id VARCHAR(40),
  external_id VARCHAR(40) UNIQUE,
  user_id VARCHAR(40),
  role_id VARCHAR(40),
  group_id VARCHAR(40),
  data_type VARCHAR(255) NOT NULL,
  value JSONB NOT NULL,
  commit_author VARCHAR(255) NOT NULL,
  commit_message VARCHAR(255) NOT NULL
);

ALTER TABLE org_actor_data
  ADD CONSTRAINT org_actor_data_PARENT_FK
  FOREIGN KEY (parent_id)
  REFERENCES org_actor_data (id);

ALTER TABLE org_actor_data
  ADD CONSTRAINT org_actor_data_ROLE_FK
  FOREIGN KEY (role_id)
  REFERENCES org_roles (id);



ALTER TABLE org_actor_status
  ADD CONSTRAINT org_actor_status_ROLE_FK
  FOREIGN KEY (role_id)
  REFERENCES org_roles (id);



ALTER TABLE org_user_roles
  ADD CONSTRAINT org_user_roles_ROLE_FK
  FOREIGN KEY (role_id)
  REFERENCES org_roles (id);



ALTER TABLE org_group_roles
  ADD CONSTRAINT org_group_roles_ROLE_FK
  FOREIGN KEY (role_id)
  REFERENCES org_roles (id);



ALTER TABLE org_user_memberships
  ADD CONSTRAINT org_user_memberships_USER_FK
  FOREIGN KEY (user_id)
  REFERENCES org_users (id);



ALTER TABLE org_user_roles
  ADD CONSTRAINT org_user_roles_USER_FK
  FOREIGN KEY (user_id)
  REFERENCES org_users (id);



ALTER TABLE org_actor_data
  ADD CONSTRAINT org_actor_data_USER_FK
  FOREIGN KEY (user_id)
  REFERENCES org_users (id);



ALTER TABLE org_actor_status
  ADD CONSTRAINT org_actor_status_USER_FK
  FOREIGN KEY (user_id)
  REFERENCES org_users (id);



ALTER TABLE org_actor_data
  ADD CONSTRAINT org_actor_data_GROUP_FK
  FOREIGN KEY (group_id)
  REFERENCES org_groups (id);



ALTER TABLE org_actor_status
  ADD CONSTRAINT org_actor_status_GROUP_FK
  FOREIGN KEY (group_id)
  REFERENCES org_groups (id);



ALTER TABLE org_group_roles
  ADD CONSTRAINT org_group_roles_GROUP_FK
  FOREIGN KEY (group_id)
  REFERENCES org_groups (id);



ALTER TABLE org_user_roles
  ADD CONSTRAINT org_user_roles_GROUP_FK
  FOREIGN KEY (group_id)
  REFERENCES org_groups (id);



ALTER TABLE org_user_memberships
  ADD CONSTRAINT org_user_memberships_GROUP_FK
  FOREIGN KEY (group_id)
  REFERENCES org_groups (id);


ALTER TABLE org_user_roles
  ADD CONSTRAINT org_user_roles_GROUP_MEMBER_FK
  FOREIGN KEY (group_id, user_id)
  REFERENCES org_user_memberships (group_id, user_id);


ALTER TABLE org_user_memberships
  ADD CONSTRAINT org_user_memberships_COMMIT_FK
  FOREIGN KEY (commit_id)
  REFERENCES org_commits (commit_id);

