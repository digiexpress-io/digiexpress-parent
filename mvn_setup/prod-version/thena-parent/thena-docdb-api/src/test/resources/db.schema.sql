
CREATE TABLE blobs
(
  id VARCHAR(40) PRIMARY KEY,
  value JSONB NOT NULL
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
