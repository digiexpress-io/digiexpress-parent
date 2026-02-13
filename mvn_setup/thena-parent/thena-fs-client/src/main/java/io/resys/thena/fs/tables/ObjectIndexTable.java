package io.resys.thena.fs.tables;

import java.util.List;
import java.util.UUID;

import io.resys.thena.api.annotations.TenantSql;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.resys.thena.fs.entities.ImmutableObjectIndex;
import io.resys.thena.fs.entities.ObjectIndex;
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
    FOREIGN KEY (created_by) REFERENCES {commit}(id);
    
  ALTER TABLE {object_index} ADD CONSTRAINT fk_{object_index}_updated_by
    FOREIGN KEY (updated_by) REFERENCES {commit}(id);

  """,
  drop = """
DROP TABLE IF EXISTS {object_index} CASCADE;
  """
)
public interface ObjectIndexTable {

  @TenantSql.FindAll(
    sql = """
      SELECT object_index.object_id, object_index.created_by, object_index.updated_by,
             created_commit.commit_created_at as created_at,
             updated_commit.commit_created_at as updated_at
      FROM {object_index} as object_index
      LEFT JOIN {commit} as created_commit ON object_index.created_by = created_commit.id
      LEFT JOIN {commit} as updated_commit ON object_index.updated_by = updated_commit.id
    """,
    rowMapper = ObjectIndexMapper.class
  )
  Sql findAll();

  @TenantSql.Find(
    optional = false,
    sql = """
      SELECT object_index.object_id, object_index.created_by, object_index.updated_by,
             created_commit.commit_created_at as created_at,
             updated_commit.commit_created_at as updated_at
      FROM {object_index} as object_index
      LEFT JOIN {commit} as created_commit ON object_index.created_by = created_commit.id
      LEFT JOIN {commit} as updated_commit ON object_index.updated_by = updated_commit.id
      WHERE object_index.object_id = $1
    """,
    rowMapper = ObjectIndexMapper.class
  )
  SqlTuple getById(UUID objectId);

  @TenantSql.Find(
    optional = true,
    sql = """
      SELECT object_index.object_id, object_index.created_by, object_index.updated_by,
             created_commit.commit_created_at as created_at,
             updated_commit.commit_created_at as updated_at
      FROM {object_index} as object_index
      LEFT JOIN {commit} as created_commit ON object_index.created_by = created_commit.id
      LEFT JOIN {commit} as updated_commit ON object_index.updated_by = updated_commit.id
      WHERE object_index.object_id = $1
    """,
    rowMapper = ObjectIndexMapper.class
  )
  SqlTuple findById(String objectId);

  @TenantSql.FindAll(
    sql = """
      SELECT object_index.object_id, object_index.created_by, object_index.updated_by,
             created_commit.commit_created_at as created_at,
             updated_commit.commit_created_at as updated_at
      FROM {object_index} as object_index
      LEFT JOIN {commit} as created_commit ON object_index.created_by = created_commit.id
      LEFT JOIN {commit} as updated_commit ON object_index.updated_by = updated_commit.id
      WHERE object_index.object_id = ANY($1)
    """,
    rowMapper = ObjectIndexMapper.class
  )
  SqlTuple findByIds(String[] objectIds);

  @TenantSql.InsertAll(
    sql = """
      INSERT INTO {object_index} (object_id, created_by, updated_by)
      VALUES ($1, $2, $3)
    """,
    propsMapper = ObjectIndexInsertMapper.class
  )
  SqlTupleList insertMany(List<ObjectIndex> objectIndices);

  @TenantSql.UpdateAll(
    sql = """
      UPDATE {object_index}
      SET created_by = $2, updated_by = $3
      WHERE object_id = $1
    """,
    propsMapper = ObjectIndexUpdateMapper.class
  )
  SqlTupleList updateMany(List<ObjectIndex> objectIndices);

  public static class ObjectIndexMapper implements TenantSql.RowMapper<ObjectIndex> {
    @Override
    public ObjectIndex apply(Row row) {
      return ImmutableObjectIndex.builder()
          .objectId(row.getString("object_id"))
          .createdBy(row.getString("created_by"))
          .updatedBy(row.getString("updated_by"))
          .createdAt(row.getOffsetDateTime("created_at"))
          .updatedAt(row.getOffsetDateTime("updated_at"))
          .build();
    }
  }

  public static class ObjectIndexInsertMapper implements TenantSql.PropsMapper<ObjectIndex> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(ObjectIndex objectIndex) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        objectIndex.getObjectId(),
        objectIndex.getCreatedBy(),
        objectIndex.getUpdatedBy()
      });
    }
  }

  public static class ObjectIndexUpdateMapper implements TenantSql.PropsMapper<ObjectIndex> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(ObjectIndex objectIndex) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        objectIndex.getObjectId(),
        objectIndex.getCreatedBy(),
        objectIndex.getUpdatedBy()
      });
    }
  }
}
