package io.resys.thena.contract.client.tables;

/*-
 * #%L
 * thena-contract-client
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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.resys.thena.api.annotations.TenantSql;
import io.resys.thena.contract.client.entities.Capability;
import io.resys.thena.contract.client.entities.ImmutableCapability;
import io.resys.thena.contract.client.entities.ImmutableCapabilityTransitives;
import io.resys.thena.contract.client.tables.ContractTable.ContractMapper;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
  name = "capability",
  order = 3,
  ddl = """
    CREATE TABLE IF NOT EXISTS {capability}
    (
      id                              UUID PRIMARY KEY,
      contract_id                     UUID NOT NULL,

      external_id                     VARCHAR(255),
      commit_id                       UUID NOT NULL,
      created_commit_id               UUID NOT NULL,

      capability_code                 VARCHAR(100) NOT NULL,
      capability_name                 VARCHAR(255) NOT NULL,
      capability_type                 VARCHAR(100) NOT NULL,
      capability_enabled              BOOLEAN NOT NULL DEFAULT TRUE
    );

    CREATE INDEX IF NOT EXISTS {capability}_TYPE_INDEX
      ON {capability} (capability_type);
    CREATE INDEX IF NOT EXISTS {capability}_CONTRACT_INDEX
      ON {capability} (contract_id);
    CREATE INDEX IF NOT EXISTS {capability}_COMMIT_INDEX
      ON {capability} (commit_id);
    CREATE INDEX IF NOT EXISTS {capability}_CREATED_COMMIT_INDEX
      ON {capability} (created_commit_id);
  """,
  constraints = """
    ALTER TABLE {capability} ADD CONSTRAINT fk_capability_contract 
      FOREIGN KEY (contract_id) REFERENCES {contract}(id);
  """,
  drop = """
    DROP TABLE {capability};
  """
)
public interface CapabilityTable {

  @TenantSql.FindAll(
    sql = """
      SELECT capability.*, 
             updated_commit.created_at as updated_at,
             created_commit.created_at as created_at
      FROM {capability} capability
      LEFT JOIN {commit} updated_commit ON capability.commit_id = updated_commit.commit_id
      LEFT JOIN {commit} created_commit ON capability.created_commit_id = created_commit.commit_id
      LEFT JOIN {contract} created_commit ON capability.contract_id = contract.id
    """,
    rowMapper = ContractMapper.class,
    sqlBuilder = ContractTableFilter.SQL.class
  )
  SqlTuple findAllByFilter(ContractTableFilter filter);
  
  
  @TenantSql.FindAll(
    sql = """
      SELECT c.*, 
             updated_commit.created_at as updated_at,
             created_commit.created_at as created_at
      FROM {capability} c
      LEFT JOIN {commit} updated_commit ON c.commit_id = updated_commit.commit_id
      LEFT JOIN {commit} created_commit ON c.created_commit_id = created_commit.commit_id
    """,
    rowMapper = CapabilityMapper.class
  )
  Sql findAll();

  @TenantSql.FindAll(
    sql = """
      SELECT c.*, 
             updated_commit.created_at as updated_at,
             created_commit.created_at as created_at
      FROM {capability} c
      LEFT JOIN {commit} updated_commit ON c.commit_id = updated_commit.commit_id
      LEFT JOIN {commit} created_commit ON c.created_commit_id = created_commit.commit_id
      WHERE c.contract_id = $1
    """,
    rowMapper = CapabilityMapper.class
  )
  SqlTuple findAllByContractId(UUID contractId);

  @TenantSql.Find(
    optional = false,
    sql = """
      SELECT c.*, 
             updated_commit.created_at as updated_at,
             created_commit.created_at as created_at
      FROM {capability} c
      LEFT JOIN {commit} updated_commit ON c.commit_id = updated_commit.commit_id
      LEFT JOIN {commit} created_commit ON c.created_commit_id = created_commit.commit_id
      WHERE c.id = $1
    """,
    rowMapper = CapabilityMapper.class
  )
  SqlTuple getById(UUID id);

  @TenantSql.InsertAll(
    sql = """
      INSERT INTO {capability}
      (id, contract_id, external_id, commit_id, created_commit_id,
       capability_code, capability_name, capability_type, capability_enabled)
       VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9)
    """,
    propsMapper = CapabilityInsertMapper.class
  )
  SqlTupleList insertMany(List<Capability> capabilities);

  @TenantSql.UpdateAll(
    sql = """
      UPDATE {capability}
       SET contract_id = $1, external_id = $2, commit_id = $3,
           capability_code = $4, capability_name = $5, capability_type = $6, capability_enabled = $7
       WHERE id = $8
    """,
    propsMapper = CapabilityUpdateMapper.class
  )
  SqlTupleList updateMany(List<Capability> capabilities);

  @TenantSql.DeleteAll(
    sql = "DELETE FROM {capability} WHERE id = $1",
    propsMapper = CapabilityDeleteMapper.class
  )
  SqlTupleList deleteAll(Collection<Capability> capabilities);

  // Mapper classes
  class CapabilityMapper implements TenantSql.RowMapper<Capability> {
    @Override
    public Capability apply(Row row) {
      final String external_id = row.getString("external_id");

      return ImmutableCapability.builder()
          .id(row.getString("id"))
          .contractId(row.getString("contract_id"))

          .externalId(Optional.ofNullable(external_id))
          .commitId(row.getString("commit_id"))
          .createdCommitId(row.getString("created_commit_id"))

          // Transitive data from joins
          .transitives(ImmutableCapabilityTransitives.builder()
              .createdAt(row.getOffsetDateTime("created_at"))
              .updatedAt(row.getOffsetDateTime("updated_at"))
              .build())

          .capabilityCode(row.getString("capability_code"))
          .capabilityName(row.getString("capability_name"))
          .capabilityType(row.getString("capability_type"))
          .capabilityEnabled(row.getBoolean("capability_enabled"))

          .build();
    }
  }

  class CapabilityInsertMapper implements TenantSql.PropsMapper<Capability> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Capability doc) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        TableUtils.toUuid(doc.getId()),
        TableUtils.toUuid(doc.getContractId()),
        doc.getExternalId().orElse(null),
        TableUtils.toUuid(doc.getCommitId()),
        TableUtils.toUuid(doc.getCreatedCommitId()),
        doc.getCapabilityCode(),
        doc.getCapabilityName(),
        doc.getCapabilityType(),
        doc.getCapabilityEnabled()
      });
    }
  }

  class CapabilityUpdateMapper implements TenantSql.PropsMapper<Capability> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Capability doc) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        TableUtils.toUuid(doc.getContractId()),
        doc.getExternalId().orElse(null),
        TableUtils.toUuid(doc.getCommitId()),
        doc.getCapabilityCode(),
        doc.getCapabilityName(),
        doc.getCapabilityType(),
        doc.getCapabilityEnabled(),
        TableUtils.toUuid(doc.getId())
      });
    }
  }

  class CapabilityDeleteMapper implements TenantSql.PropsMapper<Capability> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Capability capability) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[] {
        TableUtils.toUuid(capability.getId())
      });
    }
  }
}
