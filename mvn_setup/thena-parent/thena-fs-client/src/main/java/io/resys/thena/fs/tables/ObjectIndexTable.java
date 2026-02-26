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

import java.time.OffsetDateTime;
import java.util.List;

import io.resys.thena.api.annotations.TenantSql;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.resys.thena.fs.entities.ImmutableIndex;
import io.resys.thena.fs.entities.Index;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;


@TenantSql.Table(
  name = "object_index",
  order = 700,
  ddl = """
CREATE TABLE {object_index} (
  object_id TEXT PRIMARY KEY,
  
  created_by TEXT NOT NULL,
  updated_by TEXT NOT NULL 
);

CREATE INDEX {object_index}_created_by_idx ON {object_index}(created_by);
CREATE INDEX {object_index}_updated_by_idx ON {object_index}(updated_by);

COMMENT ON TABLE {object_index} IS 'Sideloaded state for nodes, tracking the birth and last mutation of a logical object independent of its content hash.';
COMMENT ON COLUMN {object_index}.object_id IS 'The stable identifier (UUID) that persists across multiple content versions (hashes).';
COMMENT ON COLUMN {object_index}.created_by IS 'Points to commit with what this object was created.';
COMMENT ON COLUMN {object_index}.updated_by IS 'Points to commit with what this object was updated.';
  """,
  constraints = """
      
ALTER TABLE {object_index} ADD CONSTRAINT fk_{object_index}_created_by
  FOREIGN KEY (created_by) REFERENCES {commit}(commit_id);
  
ALTER TABLE {object_index} ADD CONSTRAINT fk_{object_index}_updated_by
  FOREIGN KEY (updated_by) REFERENCES {commit}(commit_id);

  """,
  drop = """
DROP TABLE IF EXISTS {object_index} CASCADE;
  """
)
public interface ObjectIndexTable {

  @TenantSql.FindAll(
    sql = """
      SELECT object_index.*,
             created_commit.commit_created_at as created_at,
             updated_commit.commit_created_at as updated_at
      FROM {object_index} as object_index
      LEFT JOIN {commit} as created_commit ON object_index.created_by = created_commit.commit_id
      LEFT JOIN {commit} as updated_commit ON object_index.updated_by = updated_commit.commit_id
    """,
    rowMapper = ObjectIndexMapper.class
  )
  Sql findAll();


  @TenantSql.InsertAll(
    sql = """
      INSERT INTO {object_index} (object_id, created_by, updated_by)
      VALUES ($1, $2, $3)
    """,
    propsMapper = ObjectIndexInsertMapper.class
  )
  SqlTupleList insertMany(List<Index> objectIndices);

  @TenantSql.UpdateAll(
    sql = """
      UPDATE {object_index}
      SET created_by = $2, updated_by = $3
      WHERE object_id = $1
    """,
    propsMapper = ObjectIndexUpdateMapper.class
  )
  SqlTupleList updateMany(List<Index> objectIndices);

  public static class ObjectIndexMapper implements TenantSql.RowMapper<Index> {
    @Override
    public Index apply(Row row) {
      return ImmutableIndex.builder()
          .objectId(row.getString("object_id"))
          .createdBy(row.getString("created_by"))
          .updatedBy(row.getString("updated_by"))
          .createdAt(row.getOffsetDateTime("created_at"))
          .updatedAt(row.getOffsetDateTime("updated_at"))
          .build();
    }
    
    public static Index fromJson(JsonObject node_json) {
      return ImmutableIndex.builder()
        .objectId(node_json.getString("object_id"))
        
        .createdAt(OffsetDateTime.parse(node_json.getString("created_at")))
        .updatedAt(OffsetDateTime.parse(node_json.getString("updated_at")))
        
        .createdBy(node_json.getString("created_by"))
        .updatedBy(node_json.getString("updated_by"))
        
        .build();
    }
  }

  public static class ObjectIndexInsertMapper implements TenantSql.PropsMapper<Index> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Index objectIndex) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        objectIndex.getObjectId(),
        objectIndex.getCreatedBy(),
        objectIndex.getUpdatedBy()
      });
    }
  }

  public static class ObjectIndexUpdateMapper implements TenantSql.PropsMapper<Index> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Index objectIndex) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        objectIndex.getObjectId(),
        objectIndex.getCreatedBy(),
        objectIndex.getUpdatedBy()
      });
    }
  }
}
