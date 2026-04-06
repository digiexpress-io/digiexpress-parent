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
import java.util.UUID;

import org.immutables.value.Value;

import io.resys.thena.api.annotations.TenantSql;
import io.resys.thena.api.annotations.TenantSql.WrapperType;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.resys.thena.fs.entities.ImmutableIndex;
import io.resys.thena.fs.entities.Index;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;


@TenantSql.Table(
  name = "tree_index",
  order = 700,
  ddl = """
CREATE DOMAIN {tree_index}_required_text AS TEXT NOT NULL;
CREATE DOMAIN {tree_index}_required_uuid AS UUID NOT NULL;

CREATE TYPE {tree_index}_type AS (
  object_id {tree_index}_required_text,
  created_by {tree_index}_required_uuid,
  updated_by {tree_index}_required_uuid 
);

CREATE TABLE {tree_index} (  
  tree_id UUID PRIMARY KEY REFERENCES {tree}(tree_id),
  tree_ancestry {tree_index}_type[] NOT NULL
);

COMMENT ON TYPE {tree_index}_type IS 'Dedicated type for fast created/updated access';
COMMENT ON TABLE {tree_index} IS 'Sideloaded state for nodes, tracking the birth and last mutation of a logical object independent of its content hash.';
COMMENT ON COLUMN {tree_index}.tree_id IS 'Reference to the root tree representing the complete filesystem state';
COMMENT ON COLUMN {tree_index}.tree_ancestry IS 'Array of node entries representing files and subdirectories in this tree';
  """,
  constraints = "",
  drop = """
DROP TABLE IF EXISTS {tree_index} CASCADE;
DROP TYPE IF EXISTS {tree_index}_node CASCADE;
  """
)
public interface TreeIndexTable {

  public static final String BASELINE = """
    SELECT 
      tree_index.tree_id,
      
      tree_ancestry.object_id,
      tree_ancestry.created_by,
      tree_ancestry.updated_by,
      
      created_commit.commit_author AS created_by_author,
      created_commit.commit_created_at AS created_at,
      
      updated_commit.commit_author AS updated_by_author,
      updated_commit.commit_created_at AS updated_at

    FROM {tree_index} AS tree_index

    -- 1. Explode the array into individual rows
    CROSS JOIN LATERAL unnest(tree_index.tree_ancestry) AS tree_ancestry

    -- 2. Join the "created" commit metadata
    LEFT JOIN {commit} AS created_commit ON tree_ancestry.created_by = created_commit.commit_id

    -- 3. Join the "updated" commit metadata
    LEFT JOIN {commit} AS updated_commit ON tree_ancestry.updated_by = updated_commit.commit_id 
    
  """;
  
  @TenantSql.FindAll(
    sql = BASELINE,
    rowMapper = TreeIndexMapper.class
  )
  Sql findAll();

  @TenantSql.FindAll(
    sql = BASELINE + """
      LEFT JOIN {commit} AS commit ON commit.tree_id = tree_index.tree_id
      RIGHT JOIN {ref} as ref ON ref.commit_id = commit.commit_id
      WHERE ref.ref_id = $1 
    """,
    wrapper = WrapperType.MULTI,
    rowMapper = TreeIndexMapper.class
  )
  SqlTuple findAllByByBranchId(UUID branchId);


  @TenantSql.InsertAll(
    sql = """
      INSERT INTO {tree_index} (tree_id, tree_ancestry)
      VALUES($1, ARRAY(
        SELECT node::{tree_index}_type FROM jsonb_populate_recordset(NULL::{tree_index}_type, $2::jsonb) as node
      ))
      ON CONFLICT (tree_id) DO NOTHING
    """,
    propsMapper = TreeIndexInsertMapper.class
  )
  SqlTupleList insertMany(List<TreeIndex> objectIndices);
  
  @Value.Immutable
  interface TreeIndex {
    UUID getTreeId(); 
    List<Index> getObjectIndices();
  }
  

  public static class TreeIndexMapper implements TenantSql.RowMapper<Index> {
    @Override
    public Index apply(Row row) {
      return ImmutableIndex.builder()
          .treeId(row.getUUID("tree_id"))
          .objectId(row.getString("object_id"))

          .createdBy(row.getUUID("created_by"))
          .updatedBy(row.getUUID("updated_by"))
          
          .createdByAuthor(row.getString("created_by_author"))
          .updatedByAuthor(row.getString("updated_by_author"))
          
          .createdAt(row.getOffsetDateTime("created_at"))
          .updatedAt(row.getOffsetDateTime("updated_at"))
          
          .build();
    }
  }

  public static class TreeIndexInsertMapper implements TenantSql.PropsMapper<TreeIndex> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(TreeIndex objectIndex) {
      final var nodes = objectIndex.getObjectIndices().stream()
          .map(node -> JsonObject.of(
              "object_id", node.getObjectId(),
              "created_by", node.getCreatedBy(),
              "updated_by", node.getUpdatedBy()
            )
          )
          .toArray();
        return io.vertx.mutiny.sqlclient.Tuple.of(objectIndex.getTreeId(), JsonArray.of(nodes));
    }
  }
}
