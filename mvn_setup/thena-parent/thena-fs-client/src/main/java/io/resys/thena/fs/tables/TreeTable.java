package io.resys.thena.fs.tables;

import java.util.ArrayList;

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
import java.util.UUID;

import io.resys.thena.api.annotations.TenantSql;
import io.resys.thena.api.annotations.TenantSql.SqlBuilder;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.resys.thena.fs.entities.ImmutableTree;
import io.resys.thena.fs.entities.Tree;
import io.resys.thena.fs.jackson.NodesAndBlobStdDeserializer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;

@TenantSql.Table(
  name = "tree",
  order = 400,
  ddl = """
    CREATE TABLE {tree} (
      tree_id UUID PRIMARY KEY,
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
    sql = """
      SELECT 
        tree.tree_id, 
        tree_view.tree_node_blob::TEXT as nodes_json 
      FROM {tree} as tree
      LEFT JOIN LATERAL {tree_view} (
        tree.tree_id,
        false,       -- hydrate_all
        '[]'::jsonb, -- hydrate_ids 
        '[]'::jsonb  -- hydrate_ids 
      ) AS tree_view ON TRUE
    """,
    rowMapper = TreeMapper.class
  )
  Sql findAll();

  record TreeFilter(UUID treeId, List<String> objectIds, List<String> blobType) {}
  @TenantSql.Find(
    optional = false,
    sql = """
      SELECT 
        tree.tree_id, tree_view.tree_node_blob::TEXT as nodes_json
      FROM {tree} as tree
      JOIN LATERAL {tree_view}(
        tree.tree_id,
        $2::boolean, -- hydrate_all
        $3::jsonb,   -- hydrate_ids 
        $4::jsonb    -- hydrate_types 
      ) AS tree_view ON TRUE
      WHERE tree.tree_id = $1
    """,
    rowMapper = TreeMapper.class,
    sqlBuilder = TreeTable.TREE_AND_NODES_SQL.class
  )
  SqlTuple getById(TreeFilter filter);

  @TenantSql.Find(
    optional = false,
    sql = """
      SELECT 
        tree.tree_id,
        jsonb_path_query_array(
          tree_view.tree_node_blob::jsonb, 
          '$[*] ? (@.object_id == $ids[*] || @.node_full_name == $ids[*])',
          jsonb_build_object('ids', to_jsonb($2))
        )::TEXT as nodes_json
      FROM {tree} as tree
      JOIN LATERAL {tree_view}(
        tree.tree_id,
        $2::boolean, -- hydrate_all
        $3::jsonb,   -- hydrate_ids 
        $4::jsonb    -- hydrate_types 
      ) AS tree_view ON TRUE
      WHERE tree.tree_id = $1
    """,
    rowMapper = TreeMapper.class,
    sqlBuilder = TreeTable.TREE_AND_NODES_SQL.class
  )
  SqlTuple getByIdWithOnlySpecifiedNodes(TreeFilter filter);
  
  
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
      final var uuid = row.getUUID("tree_id");
      final var nodes = row.getString("nodes_json");
      if(nodes != null) {
        final var tree_node_blob = NodesAndBlobStdDeserializer.deserialize(nodes);
        return tree_node_blob.toTreeBuilder().id(uuid).build();
      }
      return ImmutableTree.builder().id(uuid).build();
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
  
  
  class TREE_AND_NODES_SQL implements SqlBuilder<TreeFilter> {
    @Override
    public SqlTuple apply(Tenant tenant, String baseline, TreeFilter treeFilter) {
      final var params = new ArrayList<Object>();
      params.add(treeFilter.treeId);
      params.add(false);
      params.add(Optional.ofNullable(treeFilter.objectIds).map(JsonArray::new).orElse(new JsonArray()));
      params.add(Optional.ofNullable(treeFilter.blobType).map(JsonArray::new).orElse(new JsonArray()));      
      return ImmutableSqlTuple.builder()
          .value(baseline)
          .props(Tuple.from(params))
          .build();
    }
  }
}
