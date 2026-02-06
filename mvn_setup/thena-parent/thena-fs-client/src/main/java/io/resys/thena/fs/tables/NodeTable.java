package io.resys.thena.fs.tables;

import java.util.Optional;

import io.resys.thena.api.annotations.TenantSql;
import io.resys.thena.fs.entities.Node;
import io.resys.thena.fs.entities.ImmutableNode;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
  name = "fs_node",
  order = 100,
  ddl = """
    CREATE TYPE {fs_node} AS (
      node_path TEXT,
      node_name TEXT,
      blob_id TEXT,
      props_id TEXT
    );
    
    COMMENT ON TYPE {fs_node} IS 'File or directory entry within a version tree. Represents a single item in the filesystem hierarchy with optional references to content and metadata. Referential integrity for blob_id and props_id is enforced via triggers since PostgreSQL cannot validate foreign keys within composite types.';
  """,
  constraints = """
    CREATE OR REPLACE FUNCTION {fs_tree}_validate_tree() 
    RETURNS TRIGGER AS $$
    DECLARE
        missing_count INTEGER;
    BEGIN
        -- Validate blob_id references
        SELECT count(*) INTO missing_count
        FROM unnest(NEW.tree_nodes) nodes
        LEFT JOIN {blob} b ON b.id = nodes.blob_id
        WHERE nodes.blob_id IS NOT NULL AND b.id IS NULL;

        IF missing_count > 0 THEN
            RAISE EXCEPTION 'Validation failed: % blob_id references do not exist', missing_count;
        END IF;

        -- Validate props_id references
        SELECT count(*) INTO missing_count
        FROM unnest(NEW.tree_nodes) nodes
        LEFT JOIN {props} p ON p.id = nodes.props_id
        WHERE nodes.props_id IS NOT NULL AND p.id IS NULL;

        IF missing_count > 0 THEN
            RAISE EXCEPTION 'Validation failed: % props_id references do not exist', missing_count;
        END IF;

        RETURN NEW;
    END;
    $$ LANGUAGE plpgsql;
  """,
  drop = """
    DROP FUNCTION IF EXISTS {fs_tree}_validate_tree() CASCADE;
    DROP TYPE IF EXISTS {fs_node} CASCADE;
  """
)
public interface NodeTable {

  class NodeMapper implements TenantSql.RowMapper<Node> {
    @Override
    public Node apply(Row row) {
      return ImmutableNode.builder()
          .id(row.getString("id"))
          .nodePath(row.getString("node_path"))
          .nodeName(row.getString("node_name"))
          .blobId(Optional.ofNullable(row.getString("blob_id")))
          .propsId(Optional.ofNullable(row.getString("props_id")))
          .build();
    }
  }
}