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
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.resys.thena.fs.entities.Blob;
import io.resys.thena.fs.entities.ImmutableBlob;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
  name = "blob",
  order = 200,
  ddl = """
    CREATE TABLE {blob} (
      blob_id TEXT PRIMARY KEY,
      blob_type TEXT NOT NULL,
      blob_value JSONB NOT NULL
    );
    
    CREATE INDEX {blob}_type_idx ON {blob}(blob_type);
    
    COMMENT ON TABLE {blob} IS 'Content-addressable storage for file data. Each blob represents immutable file content identified by its hash.';
    COMMENT ON COLUMN {blob}.blob_id IS 'Content hash (SHA-1) serving as unique identifier for this blob';
    COMMENT ON COLUMN {blob}.blob_type IS 'Content type classification (e.g., MIME types, application types) for efficient querying without parsing JSONB content';
    COMMENT ON COLUMN {blob}.blob_value IS 'File content stored as JSONB for structured data support';
  """,
  constraints = "",
  drop = """
    DROP TABLE IF EXISTS {blob} CASCADE;
  """
)
public interface BlobTable {

  @TenantSql.FindAll(
    sql = "SELECT * FROM {blob}",
    rowMapper = BlobMapper.class
  )
  Sql findAll();

  @TenantSql.InsertAll(
    sql = """
      INSERT INTO {blob}
      (blob_id, blob_type, blob_value)
      VALUES($1, $2, $3)
      ON CONFLICT (blob_id) DO NOTHING
    """,
    propsMapper = BlobInsertMapper.class
  )
  SqlTupleList insertMany(List<Blob> blobs);

  @TenantSql.DeleteAll(
    sql = "DELETE FROM {blob} WHERE id = $1",
    propsMapper = BlobDeleteMapper.class
  )
  SqlTupleList deleteAll(List<Blob> blobs);

  class BlobMapper implements TenantSql.RowMapper<Blob> {
    @Override
    public Blob apply(Row row) {
      return ImmutableBlob.builder()
          .id(row.getString("blob_id"))
          .blobType(row.getString("blob_type"))
          .blobValue(row.getJsonObject("blob_value"))
          .build();
    }

    public static Blob fromJson(JsonObject json) {
      return ImmutableBlob.builder()
          .id(json.getString("blob_id"))
          .blobType(json.getString("blob_type"))
          .blobValue(json.getJsonObject("blob_value"))
          .build();
    } 
  }
  


  class BlobInsertMapper implements TenantSql.PropsMapper<Blob> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Blob blob) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        blob.getId(),
        blob.getBlobType(),
        blob.getBlobValue()
      });
    }
  }

  class BlobDeleteMapper implements TenantSql.PropsMapper<Blob> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Blob blob) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        blob.getId()
      });
    }
  }
}
