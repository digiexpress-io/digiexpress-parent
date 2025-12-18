package io.digiexpress.thena.cockpit.client.tables;

/*-
 * #%L
 * thena-cockpit-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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
import io.digiexpress.thena.cockpit.client.api.entities.CockpitConfigProps;
import io.digiexpress.thena.cockpit.client.api.entities.ImmutableCockpitConfigProps;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.resys.thena.support.TableUtils;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
  name = "cockpit_config_props",
  order = 3,
  ddl = """
    CREATE TABLE IF NOT EXISTS {cockpit_config_props}
    (
      id UUID PRIMARY KEY,
      cockpit_config_id UUID NOT NULL,
      commit_id UUID NOT NULL,
      created_commit_id UUID NOT NULL,

      cockpit_config_props_type VARCHAR(255) NOT NULL,
      cockpit_config_props_extension JSONB
    );

    CREATE INDEX IF NOT EXISTS {cockpit_config_props}_CONFIG_INDEX
      ON {cockpit_config_props} (cockpit_config_id);
    CREATE INDEX IF NOT EXISTS {cockpit_config_props}_COMMIT_INDEX
      ON {cockpit_config_props} (commit_id);
    CREATE INDEX IF NOT EXISTS {cockpit_config_props}_CREATED_COMMIT_INDEX
      ON {cockpit_config_props} (created_commit_id);
    CREATE INDEX IF NOT EXISTS {cockpit_config_props}_TYPE_INDEX
      ON {cockpit_config_props} (cockpit_config_props_type);
  """,
  constraints = """
    ALTER TABLE {cockpit_config_props} ADD CONSTRAINT fk_cockpit_config_props_config
      FOREIGN KEY (cockpit_config_id) REFERENCES {cockpit_config}(id);
    ALTER TABLE {cockpit_config_props} ADD CONSTRAINT fk_cockpit_config_props_commit
      FOREIGN KEY (commit_id) REFERENCES {cockpit_config_commit}(id);
    ALTER TABLE {cockpit_config_props} ADD CONSTRAINT fk_cockpit_config_props_created_commit
      FOREIGN KEY (created_commit_id) REFERENCES {cockpit_config_commit}(id);
  """,
  drop = """
    DROP TABLE IF EXISTS {cockpit_config_props} CASCADE;
  """
)
public interface CockpitConfigPropsTable {

  @TenantSql.FindAll(
    sql = """
      SELECT * FROM {cockpit_config_props}
      ORDER BY cockpit_config_props_type
    """,
    rowMapper = CockpitConfigPropsMapper.class
  )
  Sql findAll();

  @TenantSql.FindAll(
    sql = """
      SELECT * FROM {cockpit_config_props}
      WHERE cockpit_config_id = $1
      ORDER BY cockpit_config_props_type
    """,
    rowMapper = CockpitConfigPropsMapper.class
  )
  SqlTuple findAllByConfigId(String configId);

  @TenantSql.FindAll(
    sql = """
      SELECT * FROM {cockpit_config_props}
      WHERE cockpit_config_id = $1 AND cockpit_config_props_type = $2
    """,
    rowMapper = CockpitConfigPropsMapper.class
  )
  SqlTuple findAllByConfigIdAndType(String configId, String type);

  @TenantSql.Find(
    optional = false,
    sql = """
      SELECT * FROM {cockpit_config_props}
      WHERE id = $1
    """,
    rowMapper = CockpitConfigPropsMapper.class
  )
  SqlTuple getById(String id);

  @TenantSql.InsertAll(
    sql = """
      INSERT INTO {cockpit_config_props}
      (id, cockpit_config_id, commit_id, created_commit_id, cockpit_config_props_type, cockpit_config_props_extension)
       VALUES($1, $2, $3, $4, $5, $6)
    """,
    propsMapper = CockpitConfigPropsInsertMapper.class
  )
  SqlTupleList insertMany(List<CockpitConfigProps> props);

  @TenantSql.UpdateAll(
    sql = """
      UPDATE {cockpit_config_props}
       SET cockpit_config_id = $1, commit_id = $2, cockpit_config_props_type = $3, cockpit_config_props_extension = $4
       WHERE id = $5
    """,
    propsMapper = CockpitConfigPropsUpdateMapper.class
  )
  SqlTupleList updateMany(List<CockpitConfigProps> props);

  // Mapper classes
  class CockpitConfigPropsMapper implements TenantSql.RowMapper<CockpitConfigProps> {
    @Override
    public CockpitConfigProps apply(Row row) {
      final JsonObject cockpit_config_props_extension = row.getJsonObject("cockpit_config_props_extension");

      return ImmutableCockpitConfigProps.builder()
          .id(TableUtils.toStringUUID(row, "id"))
          .cockpitConfigId(TableUtils.toStringUUID(row, "cockpit_config_id"))
          .commitId(TableUtils.toStringUUID(row, "commit_id"))
          .createdCommitId(TableUtils.toStringUUID(row, "created_commit_id"))
          .cockpitConfigPropsType(row.getString("cockpit_config_props_type"))
          .cockpitConfigPropsExtension(Optional.ofNullable(cockpit_config_props_extension))
          .build();
    }
  }

  class CockpitConfigPropsInsertMapper implements TenantSql.PropsMapper<CockpitConfigProps> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(CockpitConfigProps doc) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        TableUtils.toUuid(doc.getId()),
        TableUtils.toUuid(doc.getCockpitConfigId()),
        TableUtils.toUuid(doc.getCommitId()),
        TableUtils.toUuid(doc.getCreatedCommitId()),
        doc.getCockpitConfigPropsType(),
        doc.getCockpitConfigPropsExtension().orElse(null)
      });
    }
  }

  class CockpitConfigPropsUpdateMapper implements TenantSql.PropsMapper<CockpitConfigProps> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(CockpitConfigProps doc) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        TableUtils.toUuid(doc.getCockpitConfigId()),
        TableUtils.toUuid(doc.getCommitId()),
        doc.getCockpitConfigPropsType(),
        doc.getCockpitConfigPropsExtension().orElse(null),
        TableUtils.toUuid(doc.getId())
      });
    }
  }
}