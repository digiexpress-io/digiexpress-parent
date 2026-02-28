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
import java.util.Optional;

import io.resys.thena.api.annotations.TenantSql;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.resys.thena.fs.entities.ImmutableTree;
import io.resys.thena.fs.entities.Tree;
import io.resys.thena.fs.tables.NodeTable.NodeMapper;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
  name = "tree",
  order = 400,
  ddl = """
    CREATE TABLE {tree} (
      tree_id TEXT PRIMARY KEY,
      tree_nodes {node}[] NOT NULL
    );
    
    COMMENT ON TABLE {tree} IS 'Directory structure snapshots. Each tree represents the complete filesystem hierarchy state at a specific point in time. Referential integrity for node array elements is enforced via triggers.';
    COMMENT ON COLUMN {tree}.tree_id IS 'Content hash of the tree structure, enabling deduplication of identical directory states';
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
    sql = "SELECT tree_id, (" + NodeTable.BASELINE + ") as nodes_json FROM {tree} as tree",
    rowMapper = TreeMapper.class
    
  )
  Sql findAll();

  @TenantSql.InsertAll(
    sql = """
      INSERT INTO {tree} (tree_id, tree_nodes)
      VALUES($1, ARRAY(
        SELECT node::{node} FROM jsonb_populate_recordset(NULL::{node}, $2::jsonb) as node
      ))
      ON CONFLICT (tree_id) DO NOTHING
    """,
    propsMapper = TreeInsertMapper.class
  )
  SqlTupleList insertMany(List<Tree> trees);

  @TenantSql.DeleteAll(
    sql = "DELETE FROM {tree} WHERE tree_id = $1",
    propsMapper = TreeDeleteMapper.class
  )
  SqlTupleList deleteAll(List<Tree> trees);

  class TreeMapper implements TenantSql.RowMapper<Tree> {
    @Override
    public Tree apply(Row row) {
      return ImmutableTree.builder()
          .id(row.getString("tree_id"))
          .treeNodes(Optional.ofNullable(row.getJsonArray("nodes_json")).orElseGet(() -> new JsonArray()).stream()
              .map(e -> (JsonObject) e)
              .map(NodeMapper::fromJson)
              .toList()
          )
          .build();
    }
  }

  class TreeInsertMapper implements TenantSql.PropsMapper<Tree> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Tree tree) {
      final var nodes = tree.getTreeNodes().stream()
        .map(node -> JsonObject.of(
            "node_id", node.getId(),
            "object_id", node.getObjectId(),
            "node_path", node.getNodePath().orElse(null),
            "node_name", node.getNodeName(),
            "blob_id", node.getBlobId().orElse(null),
            "props_id", node.getPropsId().orElse(null)
          )
        )
        .toArray();
      return io.vertx.mutiny.sqlclient.Tuple.of(tree.getId(), JsonArray.of(nodes));
    }
  }

  class TreeDeleteMapper implements TenantSql.PropsMapper<Tree> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Tree tree) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{ tree.getId() });
    }
  }
}
