package io.resys.thena.fs.tables;

import java.util.List;

import io.resys.thena.api.annotations.TenantSql;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.resys.thena.fs.entities.Blob;
import io.resys.thena.fs.entities.ImmutableBlob;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
  name = "blob",
  order = 200,
  ddl = """
    CREATE TABLE {blob} (
      id TEXT PRIMARY KEY,
      blob_type TEXT NOT NULL,
      blob_value JSONB NOT NULL
    );
    
    CREATE INDEX {blob}_type_idx ON {blob}(blob_type);
    
    COMMENT ON TABLE {blob} IS 'Content-addressable storage for file data. Each blob represents immutable file content identified by its hash.';
    COMMENT ON COLUMN {blob}.id IS 'Content hash (SHA-1) serving as unique identifier for this blob';
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
    sql = """
      SELECT id, blob_type, blob_value
      FROM {blob}
    """,
    rowMapper = BlobMapper.class
  )
  Sql findAll();

  @TenantSql.Find(
    optional = false,
    sql = """
      SELECT id, blob_type, blob_value
      FROM {blob}
      WHERE id = $1
    """,
    rowMapper = BlobMapper.class
  )
  SqlTuple getById(String id);

  @TenantSql.FindAll(
    sql = """
      SELECT id, blob_type, blob_value
      FROM {blob}
      WHERE blob_type = $1
    """,
    rowMapper = BlobMapper.class
  )
  SqlTuple findAllByType(String blobType);

  @TenantSql.InsertAll(
    sql = """
      INSERT INTO {blob}
      (id, blob_type, blob_value)
      VALUES($1, $2, $3)
    """,
    propsMapper = BlobInsertMapper.class
  )
  SqlTupleList insertMany(List<Blob> blobs);

  @TenantSql.UpdateAll(
    sql = """
      UPDATE {blob}
      SET blob_type = $1, blob_value = $2
      WHERE id = $3
    """,
    propsMapper = BlobUpdateMapper.class
  )
  SqlTupleList updateMany(List<Blob> blobs);

  @TenantSql.DeleteAll(
    sql = "DELETE FROM {blob} WHERE id = $1",
    propsMapper = BlobDeleteMapper.class
  )
  SqlTupleList deleteAll(List<Blob> blobs);

  class BlobMapper implements TenantSql.RowMapper<Blob> {
    @Override
    public Blob apply(Row row) {
      return ImmutableBlob.builder()
          .id(row.getString("id"))
          .blobType(row.getString("blob_type"))
          .blobValue(row.getJsonObject("blob_value"))
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

  class BlobUpdateMapper implements TenantSql.PropsMapper<Blob> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Blob blob) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        blob.getBlobType(),
        blob.getBlobValue(),
        blob.getId()
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