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
import io.digiexpress.thena.cockpit.client.api.entities.CockpitConfigTenant;
import io.digiexpress.thena.cockpit.client.api.entities.ImmutableCockpitConfigTenant;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.resys.thena.support.TableUtils;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
  name = "cockpit_config_tenant",
  order = 2,
  ddl = """
    CREATE TABLE IF NOT EXISTS {cockpit_config_tenant}
    (
      id UUID PRIMARY KEY,
      cockpit_config_id UUID NOT NULL,
      commit_id UUID NOT NULL,
      created_commit_id UUID NOT NULL,

      external_id VARCHAR(255) NOT NULL,
      external_branch VARCHAR(255) NOT NULL,

      cockpit_config_tenant_desc TEXT,
      cockpit_config_tenant_extension JSONB
    );

    CREATE INDEX IF NOT EXISTS {cockpit_config_tenant}_CONFIG_INDEX
      ON {cockpit_config_tenant} (cockpit_config_id);
    CREATE INDEX IF NOT EXISTS {cockpit_config_tenant}_COMMIT_INDEX
      ON {cockpit_config_tenant} (commit_id);
    CREATE INDEX IF NOT EXISTS {cockpit_config_tenant}_CREATED_COMMIT_INDEX
      ON {cockpit_config_tenant} (created_commit_id);
    CREATE INDEX IF NOT EXISTS {cockpit_config_tenant}_EXTERNAL_ID_INDEX
      ON {cockpit_config_tenant} (external_id);
    CREATE INDEX IF NOT EXISTS {cockpit_config_tenant}_EXTERNAL_BRANCH_INDEX
      ON {cockpit_config_tenant} (external_branch);
  """,
  constraints = """
    ALTER TABLE {cockpit_config_tenant} ADD CONSTRAINT fk_cockpit_config_tenant_config
      FOREIGN KEY (cockpit_config_id) REFERENCES {cockpit_config}(id);
    ALTER TABLE {cockpit_config_tenant} ADD CONSTRAINT fk_cockpit_config_tenant_commit
      FOREIGN KEY (commit_id) REFERENCES {cockpit_config_commit}(id);
    ALTER TABLE {cockpit_config_tenant} ADD CONSTRAINT fk_cockpit_config_tenant_created_commit
      FOREIGN KEY (created_commit_id) REFERENCES {cockpit_config_commit}(id);
  """,
  drop = """
    DROP TABLE IF EXISTS {cockpit_config_tenant} CASCADE;
  """
)
public interface CockpitConfigTenantTable {

  @TenantSql.FindAll(
    sql = """
      SELECT * FROM {cockpit_config_tenant}
      ORDER BY external_id
    """,
    rowMapper = CockpitConfigTenantMapper.class
  )
  Sql findAll();

  @TenantSql.FindAll(
    sql = """
      SELECT * FROM {cockpit_config_tenant}
      WHERE cockpit_config_id = $1
      ORDER BY external_id
    """,
    rowMapper = CockpitConfigTenantMapper.class
  )
  SqlTuple findAllByConfigId(String configId);

  @TenantSql.Find(
    optional = false,
    sql = """
      SELECT * FROM {cockpit_config_tenant}
      WHERE id = $1
    """,
    rowMapper = CockpitConfigTenantMapper.class
  )
  SqlTuple getById(String id);

  @TenantSql.Find(
    optional = true,
    sql = """
      SELECT * FROM {cockpit_config_tenant}
      WHERE external_id = $1 AND external_branch = $2
    """,
    rowMapper = CockpitConfigTenantMapper.class
  )
  SqlTuple findByExternalIdAndBranch(String externalId, String externalBranch);

  @TenantSql.InsertAll(
    sql = """
      INSERT INTO {cockpit_config_tenant}
      (id, cockpit_config_id, commit_id, created_commit_id, external_id, external_branch, cockpit_config_tenant_desc, cockpit_config_tenant_extension)
       VALUES($1, $2, $3, $4, $5, $6, $7, $8)
    """,
    propsMapper = CockpitConfigTenantInsertMapper.class
  )
  SqlTupleList insertMany(List<CockpitConfigTenant> tenants);

  @TenantSql.UpdateAll(
    sql = """
      UPDATE {cockpit_config_tenant}
       SET cockpit_config_id = $1, commit_id = $2, external_id = $3, external_branch = $4, cockpit_config_tenant_desc = $5, cockpit_config_tenant_extension = $6
       WHERE id = $7
    """,
    propsMapper = CockpitConfigTenantUpdateMapper.class
  )
  SqlTupleList updateMany(List<CockpitConfigTenant> tenants);

  // Mapper classes
  class CockpitConfigTenantMapper implements TenantSql.RowMapper<CockpitConfigTenant> {
    @Override
    public CockpitConfigTenant apply(Row row) {
      final JsonObject cockpit_config_tenant_extension = row.getJsonObject("cockpit_config_tenant_extension");

      return ImmutableCockpitConfigTenant.builder()
          .id(TableUtils.toStringUUID(row, "id"))
          .cockpitConfigId(TableUtils.toStringUUID(row, "cockpit_config_id"))
          .commitId(TableUtils.toStringUUID(row, "commit_id"))
          .createdCommitId(TableUtils.toStringUUID(row, "created_commit_id"))
          .externalId(row.getString("external_id"))
          .externalBranch(row.getString("external_branch"))
          .cockpitConfigTenantDesc(row.getString("cockpit_config_tenant_desc"))
          .cockpitConfigTenantExtension(Optional.ofNullable(cockpit_config_tenant_extension))
          .build();
    }
  }

  class CockpitConfigTenantInsertMapper implements TenantSql.PropsMapper<CockpitConfigTenant> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(CockpitConfigTenant doc) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        TableUtils.toUuid(doc.getId()),
        TableUtils.toUuid(doc.getCockpitConfigId()),
        TableUtils.toUuid(doc.getCommitId()),
        TableUtils.toUuid(doc.getCreatedCommitId()),
        doc.getExternalId(),
        doc.getExternalBranch(),
        doc.getCockpitConfigTenantDesc(),
        doc.getCockpitConfigTenantExtension().orElse(null)
      });
    }
  }

  class CockpitConfigTenantUpdateMapper implements TenantSql.PropsMapper<CockpitConfigTenant> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(CockpitConfigTenant doc) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        TableUtils.toUuid(doc.getCockpitConfigId()),
        TableUtils.toUuid(doc.getCommitId()),
        doc.getExternalId(),
        doc.getExternalBranch(),
        doc.getCockpitConfigTenantDesc(),
        doc.getCockpitConfigTenantExtension().orElse(null),
        TableUtils.toUuid(doc.getId())
      });
    }
  }
}