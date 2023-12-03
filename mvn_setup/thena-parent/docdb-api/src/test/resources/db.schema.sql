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

