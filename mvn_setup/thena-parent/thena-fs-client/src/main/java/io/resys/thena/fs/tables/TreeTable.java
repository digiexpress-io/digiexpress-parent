package io.resys.thena.fs.tables;

/*-
 * #%L
 * thena-fs-client
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.List;

import io.resys.thena.api.annotations.TenantSql;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.resys.thena.fs.entities.ImmutableTree;
import io.resys.thena.fs.entities.Tree;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
  name = "tree",
  order = 400,
  ddl = """
    CREATE TABLE {tree} (
      id TEXT PRIMARY KEY,
      tree_nodes {node}[] NOT NULL
    );
    
    COMMENT ON TABLE {tree} IS 'Directory structure snapshots. Each tree represents the complete filesystem hierarchy state at a specific point in time. Referential integrity for node array elements is enforced via triggers.';
    COMMENT ON COLUMN {tree}.id IS 'Content hash of the tree structure, enabling deduplication of identical directory states';
    COMMENT ON COLUMN {tree}.tree_nodes IS 'Array of node entries representing files and subdirectories in this tree';
  """,
  constraints = """
    CREATE TRIGGER {tree}_validation_trigger
      BEFORE INSERT OR UPDATE ON {tree}
      FOR EACH ROW EXECUTE FUNCTION {tree}_validate_tree();

  """,
  drop = """
    DROP TRIGGER IF EXISTS {tree}_validation_trigger ON {tree};
    DROP TABLE IF EXISTS {tree} CASCADE;
  """
)
public interface TreeTable {

  @TenantSql.FindAll(
    sql = """
      SELECT id, tree_nodes
      FROM {tree}
    """,
    rowMapper = TreeMapper.class
  )
  Sql findAll();

  @TenantSql.Find(
    optional = false,
    sql = """
      SELECT id, tree_nodes
      FROM {tree}
      WHERE id = $1
    """,
    rowMapper = TreeMapper.class
  )
  SqlTuple getById(String id);

  @TenantSql.InsertAll(
    sql = """
      INSERT INTO {tree}
      (id, tree_nodes)
      VALUES($1,
      
        ARRAY(
          SELECT node::{node} FROM jsonb_populate_recordset(NULL::{node}, $2::jsonb) as node
        )
      )
    """,
    propsMapper = TreeInsertMapper.class
  )
  SqlTupleList insertMany(List<Tree> trees);

  @TenantSql.UpdateAll(
    sql = """
      UPDATE {tree}
      SET tree_nodes = $1
      WHERE id = $2
    """,
    propsMapper = TreeUpdateMapper.class
  )
  SqlTupleList updateMany(List<Tree> trees);

  @TenantSql.DeleteAll(
    sql = "DELETE FROM {tree} WHERE id = $1",
    propsMapper = TreeDeleteMapper.class
  )
  SqlTupleList deleteAll(List<Tree> trees);

  class TreeMapper implements TenantSql.RowMapper<Tree> {
    @Override
    public Tree apply(Row row) {
      // Note: Complex mapping for node[] array would need custom logic
      // This is a simplified version - actual implementation would need to parse the array
      return ImmutableTree.builder()
          .id(row.getString("id"))
          .treeNodes(List.of()) // TODO: Parse node[] array
          .build();
    }
  }

  class TreeInsertMapper implements TenantSql.PropsMapper<Tree> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Tree tree) {
      // 
      /**
       * Note: Complex mapping for node[] array would need custom logic
       *   id TEXT,
       *   node_id TEXT, -- not part of hash calculation, technical id of the object (user api generated)
       *   node_path TEXT,
       *   node_name TEXT,
       *   blob_id TEXT,
       *   props_id TEXT
       */
      
      final var nodes = tree.getTreeNodes().stream()
          .map(node -> JsonObject.of(
              "id", node.getId(),
              "node_id", node.getNodeId(),
              "node_path", node.getNodePath().orElse(null),
              "node_name", node.getNodeName(),
              "blob_id", node.getBlobId().orElse(null),
              "props_id", node.getPropsId().orElse(null)
              )
          )
          .toArray();
      
      return io.vertx.mutiny.sqlclient.Tuple.of(
        tree.getId(),
        JsonArray.of(nodes)
        
        //tree.getTreeNodes().stream().toArray(Tuple[]::new)
      );
    }
  }

  
  
  class TreeUpdateMapper implements TenantSql.PropsMapper<Tree> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Tree tree) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        null, // TODO: Convert List<FsNode> to PostgreSQL node[] array
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
