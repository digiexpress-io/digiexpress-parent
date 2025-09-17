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
