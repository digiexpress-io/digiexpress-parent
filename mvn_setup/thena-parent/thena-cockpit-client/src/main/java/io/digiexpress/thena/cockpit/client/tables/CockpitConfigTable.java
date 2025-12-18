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
import java.util.UUID;

import io.resys.thena.api.annotations.TenantSql;
import io.digiexpress.thena.cockpit.client.api.entities.CockpitConfig;
import io.digiexpress.thena.cockpit.client.api.entities.ImmutableCockpitConfig;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.resys.thena.support.TableUtils;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
  name = "cockpit_config",
  order = 0,
  ddl = """
    CREATE TABLE IF NOT EXISTS {cockpit_config}
    (
      id                              UUID PRIMARY KEY,
      commit_id                       UUID NOT NULL,
      created_commit_id               UUID NOT NULL,
      updated_tree_commit_id          UUID NOT NULL,

      external_id                     VARCHAR(255),

      cockpit_config_name             VARCHAR(255) NOT NULL,
      cockpit_config_desc             TEXT
    );

    CREATE INDEX IF NOT EXISTS {cockpit_config}_COMMIT_INDEX
      ON {cockpit_config} (commit_id);
    CREATE INDEX IF NOT EXISTS {cockpit_config}_CREATED_COMMIT_INDEX
      ON {cockpit_config} (created_commit_id);
    CREATE INDEX IF NOT EXISTS {cockpit_config}_UPDATED_TREE_COMMIT_INDEX
      ON {cockpit_config} (updated_tree_commit_id);
    CREATE INDEX IF NOT EXISTS {cockpit_config}_NAME_INDEX
      ON {cockpit_config} (cockpit_config_name);
  """,
  constraints = """
    ALTER TABLE {cockpit_config} ADD CONSTRAINT fk_cockpit_config_commit
      FOREIGN KEY (commit_id) REFERENCES {cockpit_config_commit}(id);
    ALTER TABLE {cockpit_config} ADD CONSTRAINT fk_cockpit_config_created_commit
      FOREIGN KEY (created_commit_id) REFERENCES {cockpit_config_commit}(id);
    ALTER TABLE {cockpit_config} ADD CONSTRAINT fk_cockpit_config_updated_tree_commit
      FOREIGN KEY (updated_tree_commit_id) REFERENCES {cockpit_config_commit}(id);
  """,
  drop = """
    DROP TABLE IF EXISTS {cockpit_config} CASCADE;
  """
)
public interface CockpitConfigTable {

  @TenantSql.FindAll(
    sql = """
      SELECT cockpit_config.*
      FROM {cockpit_config} cockpit_config
      LEFT JOIN {cockpit_config_commit} updated_commit ON cockpit_config.commit_id = updated_commit.id
      LEFT JOIN {cockpit_config_commit} created_commit ON cockpit_config.created_commit_id = created_commit.id  
      LEFT JOIN {cockpit_config_commit} updated_tree_commit ON cockpit_config.updated_tree_commit_id = updated_tree_commit.id
    """,
    rowMapper = CockpitConfigMapper.class,
    sqlBuilder = CockpitTableFilter.SQL.class
  )
  SqlTuple findAllByFilter(CockpitTableFilter filter);
  
  @TenantSql.FindAll(
    sql = """
      SELECT c.*
      FROM {cockpit_config} c
      LEFT JOIN {cockpit_config_commit} updated_commit ON c.commit_id = updated_commit.id
      LEFT JOIN {cockpit_config_commit} created_commit ON c.created_commit_id = created_commit.id
      LEFT JOIN {cockpit_config_commit} updated_tree_commit ON c.updated_tree_commit_id = updated_tree_commit.id
      ORDER BY c.cockpit_config_name
    """,
    rowMapper = CockpitConfigMapper.class
  )
  Sql findAll();

  @TenantSql.Find(
    optional = false,
    sql = """
      SELECT c.*
      FROM {cockpit_config} c
      LEFT JOIN {cockpit_config_commit} updated_commit ON c.commit_id = updated_commit.id
      LEFT JOIN {cockpit_config_commit} created_commit ON c.created_commit_id = created_commit.id  
      LEFT JOIN {cockpit_config_commit} updated_tree_commit ON c.updated_tree_commit_id = updated_tree_commit.id
      WHERE c.id = $1
    """,
    rowMapper = CockpitConfigMapper.class
  )
  SqlTuple getById(UUID id);

  @TenantSql.InsertAll(
    sql = """
      INSERT INTO {cockpit_config}
      (id, commit_id, created_commit_id, updated_tree_commit_id, external_id, cockpit_config_name, cockpit_config_desc)
       VALUES($1, $2, $3, $4, $5, $6, $7)
    """,
    propsMapper = CockpitConfigInsertMapper.class
  )
  SqlTupleList insertMany(List<CockpitConfig> configs);

  @TenantSql.UpdateAll(
    sql = """
      UPDATE {cockpit_config}
       SET commit_id = $1, updated_tree_commit_id = $2, external_id = $3, cockpit_config_name = $4, cockpit_config_desc = $5
       WHERE id = $6
    """,
    propsMapper = CockpitConfigUpdateMapper.class
  )
  SqlTupleList updateMany(List<CockpitConfig> configs);

  // Mapper classes
  class CockpitConfigMapper implements TenantSql.RowMapper<CockpitConfig> {
    @Override
    public CockpitConfig apply(Row row) {
      final String external_id = row.getString("external_id");

      return ImmutableCockpitConfig.builder()
          .id(TableUtils.toStringUUID(row, "id"))
          .commitId(TableUtils.toStringUUID(row, "commit_id"))
          .createdCommitId(TableUtils.toStringUUID(row, "created_commit_id"))
          .updatedTreeCommitId(TableUtils.toStringUUID(row, "updated_tree_commit_id"))
          .externalId(Optional.ofNullable(external_id))
          .cockpitConfigName(row.getString("cockpit_config_name"))
          .cockpitConfigDesc(row.getString("cockpit_config_desc"))
          .build();
    }
  }

  class CockpitConfigInsertMapper implements TenantSql.PropsMapper<CockpitConfig> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(CockpitConfig doc) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        TableUtils.toUuid(doc.getId()),
        TableUtils.toUuid(doc.getCommitId()),
        TableUtils.toUuid(doc.getCreatedCommitId()),
        TableUtils.toUuid(doc.getUpdatedTreeCommitId()),
        doc.getExternalId().orElse(null),
        doc.getCockpitConfigName(),
        doc.getCockpitConfigDesc()
      });
    }
  }

  class CockpitConfigUpdateMapper implements TenantSql.PropsMapper<CockpitConfig> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(CockpitConfig doc) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        TableUtils.toUuid(doc.getCommitId()),
        TableUtils.toUuid(doc.getUpdatedTreeCommitId()),
        doc.getExternalId().orElse(null),
        doc.getCockpitConfigName(),
        doc.getCockpitConfigDesc(),
        TableUtils.toUuid(doc.getId())
      });
    }
  }
}