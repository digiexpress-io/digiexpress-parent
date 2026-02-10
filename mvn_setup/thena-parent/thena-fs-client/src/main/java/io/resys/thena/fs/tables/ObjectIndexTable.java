package io.resys.thena.fs.tables;

import io.resys.thena.api.annotations.TenantSql;

@TenantSql.Table(
  name = "object_index",
  order = 800,
  ddl = """
CREATE TABLE {object_index} (
  object_id UUID PRIMARY KEY,
  
  created_by TEXT NOT NULL REFERENCES {commit}(id),
  updated_by TEXT NOT NULL REFERENCES {commit}(id)
);

CREATE INDEX {object_index}_updated_at_idx ON {object_index}(updated_at);

COMMENT ON TABLE {object_index} IS 'Sideloaded state for nodes, tracking the birth and last mutation of a logical object independent of its content hash.';
COMMENT ON COLUMN {object_index}.object_id IS 'The stable identifier (UUID) that persists across multiple content versions (hashes).';
COMMENT ON COLUMN {object_index}.created_at IS 'Points to commit with what this object was created.';
COMMENT ON COLUMN {object_index}.updated_at IS 'Points to commit with what this object was updated.';
  """,
  constraints = """
  """,
  drop = """
DROP TABLE IF EXISTS {object_index} CASCADE;
  """
)
public interface ObjectIndexTable {

}
