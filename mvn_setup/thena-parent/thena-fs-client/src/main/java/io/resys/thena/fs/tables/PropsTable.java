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
