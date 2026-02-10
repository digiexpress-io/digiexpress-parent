package io.resys.thena.fs.tables;

import java.util.UUID;

import io.resys.thena.api.annotations.TenantSql;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.fs.entities.ImmutableObjectIndex;
import io.resys.thena.fs.entities.ObjectIndex;
import io.resys.thena.fs.tables.ObjectIndexTable.ObjectIndexMapper;
import io.resys.thena.support.TableUtils;
import io.vertx.mutiny.sqlclient.Row;


@TenantSql.Table(
  name = "object_index",
  order = 800,
  ddl = """
CREATE TABLE {object_index} (
  object_id UUID PRIMARY KEY,
  
  created_by TEXT NOT NULL REFERENCES {commit}(id),
  updated_by TEXT NOT NULL REFERENCES {commit}(id)
);

CREATE INDEX {object_index}_created_by_idx ON {object_index}(created_by);
CREATE INDEX {object_index}_updated_by_idx ON {object_index}(updated_by);

COMMENT ON TABLE {object_index} IS 'Sideloaded state for nodes, tracking the birth and last mutation of a logical object independent of its content hash.';
COMMENT ON COLUMN {object_index}.object_id IS 'The stable identifier (UUID) that persists across multiple content versions (hashes).';
COMMENT ON COLUMN {object_index}.created_by IS 'Points to commit with what this object was created.';
COMMENT ON COLUMN {object_index}.updated_by IS 'Points to commit with what this object was updated.';
  """,
  constraints = """
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
  SqlTuple findById(UUID objectId);

  @TenantSql.FindMany(
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
  SqlTuple findByIds(UUID[] objectIds);

  @TenantSql.Insert(
    sql = """
      INSERT INTO {object_index} (object_id, created_by, updated_by)
      VALUES ($1, $2, $3)
    """
  )
  SqlTuple insertOne(UUID objectId, String createdBy, String updatedBy);

  @TenantSql.Update(
    sql = """
      UPDATE {object_index}
      SET updated_by = $2
      WHERE object_id = $1
    """
  )
  SqlTuple updateOne(UUID objectId, String updatedBy);

  @TenantSql.Delete(
    sql = """
      DELETE FROM {object_index}
      WHERE object_id = $1
    """
  )
  SqlTuple deleteById(UUID objectId);

  @TenantSql.DeleteMany(
    sql = """
      DELETE FROM {object_index}
      WHERE object_id = ANY($1)
    """
  )
  SqlTuple deleteByIds(UUID[] objectIds);

  public static class ObjectIndexMapper implements TenantSql.RowMapper<ObjectIndex> {
    @Override
    public ObjectIndex apply(Row row) {
      return ImmutableObjectIndex.builder()
          .objectId(TableUtils.toStringUUID(row, "object_id"))
          .createdBy(row.getString("created_by"))
          .updatedBy(row.getString("updated_by"))
          .createdAt(row.getOffsetDateTime("created_at"))
          .updatedAt(row.getOffsetDateTime("updated_at"))
          .build();
    }
  }
}
