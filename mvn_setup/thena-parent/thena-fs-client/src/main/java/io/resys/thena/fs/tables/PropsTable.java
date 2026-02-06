package io.resys.thena.fs.tables;

import java.util.List;

import io.resys.thena.api.annotations.TenantSql;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.resys.thena.fs.entities.Props;
import io.resys.thena.fs.entities.ImmutableProps;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
  name = "props",
  order = 300,
  ddl = """
    CREATE TABLE {props} (
      id TEXT PRIMARY KEY,
      props_labels JSONB,
      props_comments JSONB,
      props_permissions JSONB,
      props_flags JSONB
    );
    
    COMMENT ON TABLE {props} IS 'Versioned metadata for files and directories. Content-addressable properties that can be attached to filesystem nodes.';
    COMMENT ON COLUMN {props}.id IS 'Content hash of the properties, enabling deduplication of identical metadata sets';
    COMMENT ON COLUMN {props}.props_labels IS 'User-defined labels and tags in JSONB format';
    COMMENT ON COLUMN {props}.props_comments IS 'Comments and annotations in JSONB format';
    COMMENT ON COLUMN {props}.props_permissions IS 'Access control and permission settings in JSONB format';
    COMMENT ON COLUMN {props}.props_flags IS 'Boolean flags like hidden, disabled, etc. in JSONB format';
  """,
  constraints = "",
  drop = """
    DROP TABLE IF EXISTS {props} CASCADE;
  """
)
public interface PropsTable {

  @TenantSql.FindAll(
    sql = """
      SELECT id, props_labels, props_comments, props_permissions, props_flags
      FROM {props}
    """,
    rowMapper = PropsMapper.class
  )
  Sql findAll();

  @TenantSql.Find(
    optional = false,
    sql = """
      SELECT id, props_labels, props_comments, props_permissions, props_flags
      FROM {props}
      WHERE id = $1
    """,
    rowMapper = PropsMapper.class
  )
  SqlTuple getById(String id);

  @TenantSql.InsertAll(
    sql = """
      INSERT INTO {props}
      (id, props_labels, props_comments, props_permissions, props_flags)
      VALUES($1, $2, $3, $4, $5)
    """,
    propsMapper = PropsInsertMapper.class
  )
  SqlTupleList insertMany(List<Props> props);

  @TenantSql.UpdateAll(
    sql = """
      UPDATE {props}
      SET props_labels = $1, props_comments = $2, props_permissions = $3, props_flags = $4
      WHERE id = $5
    """,
    propsMapper = PropsUpdateMapper.class
  )
  SqlTupleList updateMany(List<Props> props);

  @TenantSql.DeleteAll(
    sql = "DELETE FROM {props} WHERE id = $1",
    propsMapper = PropsDeleteMapper.class
  )
  SqlTupleList deleteAll(List<Props> props);

  class PropsMapper implements TenantSql.RowMapper<Props> {
    @Override
    public Props apply(Row row) {
      return ImmutableProps.builder()
          .id(row.getString("id"))
          .propsLabels(row.getJsonObject("props_labels"))
          .propsComments(row.getJsonObject("props_comments"))
          .propsPermissions(row.getJsonObject("props_permissions"))
          .propsFlags(row.getJsonObject("props_flags"))
          .build();
    }
  }

  class PropsInsertMapper implements TenantSql.PropsMapper<Props> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Props props) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        props.getId(),
        props.getPropsLabels(),
        props.getPropsComments(),
        props.getPropsPermissions(),
        props.getPropsFlags()
      });
    }
  }

  class PropsUpdateMapper implements TenantSql.PropsMapper<Props> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Props props) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        props.getPropsLabels(),
        props.getPropsComments(),
        props.getPropsPermissions(),
        props.getPropsFlags(),
        props.getId()
      });
    }
  }

  class PropsDeleteMapper implements TenantSql.PropsMapper<Props> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Props props) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        props.getId()
      });
    }
  }
}