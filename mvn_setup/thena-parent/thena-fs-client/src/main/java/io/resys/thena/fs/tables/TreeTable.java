package io.resys.thena.fs.tables;

import java.util.List;

import io.resys.thena.api.annotations.TenantSql;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.resys.thena.fs.entities.Tree;
import io.resys.thena.fs.entities.ImmutableTree;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
  name = "fs_tree",
  order = 400,
  ddl = """
    CREATE TABLE {fs_tree} (
      id TEXT PRIMARY KEY,
      tree_nodes {fs_node}[] NOT NULL
    );
    
    COMMENT ON TABLE {fs_tree} IS 'Directory structure snapshots. Each tree represents the complete filesystem hierarchy state at a specific point in time. Referential integrity for fs_node array elements is enforced via triggers.';
    COMMENT ON COLUMN {fs_tree}.id IS 'Content hash of the tree structure, enabling deduplication of identical directory states';
    COMMENT ON COLUMN {fs_tree}.tree_nodes IS 'Array of fs_node entries representing files and subdirectories in this tree';
  """,
  constraints = """
    CREATE TRIGGER {fs_tree}_validation_trigger
      BEFORE INSERT OR UPDATE ON {fs_tree}
      FOR EACH ROW EXECUTE FUNCTION {fs_tree}_validate_tree();

    ALTER TABLE {tree_name}
      ADD CONSTRAINT uq_tree_name_path UNIQUE (name, path)
      WHERE name IS NOT NULL AND path IS NOT NULL;
  """,
  drop = """
    DROP TRIGGER IF EXISTS {fs_tree}_validation_trigger ON {fs_tree};
    DROP TABLE IF EXISTS {fs_tree} CASCADE;
  """
)
public interface TreeTable {

  @TenantSql.FindAll(
    sql = """
      SELECT id, tree_nodes
      FROM {fs_tree}
    """,
    rowMapper = TreeMapper.class
  )
  Sql findAll();

  @TenantSql.Find(
    optional = false,
    sql = """
      SELECT id, tree_nodes
      FROM {fs_tree}
      WHERE id = $1
    """,
    rowMapper = TreeMapper.class
  )
  SqlTuple getById(String id);

  @TenantSql.InsertAll(
    sql = """
      INSERT INTO {fs_tree}
      (id, tree_nodes)
      VALUES($1, $2)
    """,
    propsMapper = TreeInsertMapper.class
  )
  SqlTupleList insertMany(List<Tree> trees);

  @TenantSql.UpdateAll(
    sql = """
      UPDATE {fs_tree}
      SET tree_nodes = $1
      WHERE id = $2
    """,
    propsMapper = TreeUpdateMapper.class
  )
  SqlTupleList updateMany(List<Tree> trees);

  @TenantSql.DeleteAll(
    sql = "DELETE FROM {fs_tree} WHERE id = $1",
    propsMapper = TreeDeleteMapper.class
  )
  SqlTupleList deleteAll(List<Tree> trees);

  class TreeMapper implements TenantSql.RowMapper<Tree> {
    @Override
    public Tree apply(Row row) {
      // Note: Complex mapping for fs_node[] array would need custom logic
      // This is a simplified version - actual implementation would need to parse the array
      return ImmutableTree.builder()
          .id(row.getString("id"))
          .treeNodes(List.of()) // TODO: Parse fs_node[] array
          .build();
    }
  }

  class TreeInsertMapper implements TenantSql.PropsMapper<Tree> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Tree tree) {
      // Note: Complex mapping for fs_node[] array would need custom logic
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        tree.getId(),
        null // TODO: Convert List<FsNode> to PostgreSQL fs_node[] array
      });
    }
  }

  class TreeUpdateMapper implements TenantSql.PropsMapper<Tree> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Tree tree) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        null, // TODO: Convert List<FsNode> to PostgreSQL fs_node[] array
        tree.getId()
      });
    }
  }

  class TreeDeleteMapper implements TenantSql.PropsMapper<Tree> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Tree tree) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        tree.getId()
      });
    }
  }
}