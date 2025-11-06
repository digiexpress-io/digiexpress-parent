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

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.resys.thena.api.annotations.TenantSql;
import io.resys.thena.contract.client.entities.Contract;
import io.resys.thena.contract.client.entities.ImmutableContract;
import io.resys.thena.contract.client.entities.ImmutableContractTransitives;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
  name = "contract",
  order = 0,
  ddl = """
    CREATE TABLE IF NOT EXISTS {contract}
    (
      id                              UUID PRIMARY KEY,
      parent_contract_id              UUID,
      contract_number                 VARCHAR(255) NOT NULL,

      external_id                     VARCHAR(255),
      commit_id                       UUID NOT NULL,
      created_commit_id               UUID NOT NULL,
      updated_tree_commit_id          UUID NOT NULL,

      contract_issue_date             DATE NOT NULL,
      contract_issue_date_interval    INTERVAL NOT NULL,
      contract_issue_date_type        VARCHAR(100) NOT NULL,

      contract_start_date             DATE NOT NULL,
      contract_start_date_interval    INTERVAL NOT NULL,
      contract_start_date_type        VARCHAR(100) NOT NULL,

      contract_maturity_date          DATE,
      contract_maturity_date_interval INTERVAL,
      contract_maturity_date_type     VARCHAR(100),

      contract_status                 VARCHAR(100) NOT NULL,
      contract_sub_status             VARCHAR(100),
      contract_type                   VARCHAR(100) NOT NULL,
      contract_sub_type               VARCHAR(100),
      contract_data                   JSONB
    );

    CREATE INDEX IF NOT EXISTS {contract}_STATUS_INDEX
      ON {contract} (contract_status);
    CREATE INDEX IF NOT EXISTS {contract}_TYPE_INDEX
      ON {contract} (contract_type);
    CREATE INDEX IF NOT EXISTS {contract}_SUB_STATUS_INDEX
      ON {contract} (contract_sub_status);
    CREATE INDEX IF NOT EXISTS {contract}_SUB_TYPE_INDEX
      ON {contract} (contract_sub_type);
    CREATE INDEX IF NOT EXISTS {contract}_PARENT_INDEX
      ON {contract} (parent_contract_id);
    CREATE INDEX IF NOT EXISTS {contract}_COMMIT_INDEX
      ON {contract} (commit_id);
    CREATE INDEX IF NOT EXISTS {contract}_CREATED_COMMIT_INDEX
      ON {contract} (created_commit_id);
    CREATE INDEX IF NOT EXISTS {contract}_UPDATED_TREE_COMMIT_INDEX
      ON {contract} (updated_tree_commit_id);
  """,
  constraints = """
    ALTER TABLE {contract} ADD CONSTRAINT fk_contract_parent
      FOREIGN KEY (parent_contract_id) REFERENCES {contract}(id);
  """,
  drop = """
    DROP TABLE {contract};
  """
)
public interface ContractTable {

  @TenantSql.FindAll(
    sql = """
      SELECT contract.*, 
             updated_commit.created_at as updated_at,
             created_commit.created_at as created_at,
             updated_tree_commit.created_at as updated_tree_at
      FROM {contract} contract
      LEFT JOIN {commit} updated_commit ON contract.commit_id = updated_commit.id
      LEFT JOIN {commit} created_commit ON contract.created_commit_id = created_commit.id  
      LEFT JOIN {commit} updated_tree_commit ON contract.updated_tree_commit_id = updated_tree_commit.id
    """,
    rowMapper = ContractMapper.class,
    sqlBuilder = ContractTableFilter.SQL.class
  )
  SqlTuple findAllByFilter(ContractTableFilter filter);
  
  @TenantSql.FindAll(
    sql = """
      SELECT c.*,
             updated_commit.created_at as updated_at,
             created_commit.created_at as created_at,
             updated_tree_commit.created_at as updated_tree_at
      FROM {contract} c
      LEFT JOIN {commit} updated_commit ON c.commit_id = updated_commit.id
      LEFT JOIN {commit} created_commit ON c.created_commit_id = created_commit.id
      LEFT JOIN {commit} updated_tree_commit ON c.updated_tree_commit_id = updated_tree_commit.id
    """,
    rowMapper = ContractMapper.class
  )
  Sql findAll();

  @TenantSql.FindAll(
    sql = """
      SELECT c.*, 
             updated_commit.created_at as updated_at,
             created_commit.created_at as created_at,
             updated_tree_commit.created_at as updated_tree_at
      FROM {contract} c
      LEFT JOIN {commit} updated_commit ON c.commit_id = updated_commit.id
      LEFT JOIN {commit} created_commit ON c.created_commit_id = created_commit.id  
      LEFT JOIN {commit} updated_tree_commit ON c.updated_tree_commit_id = updated_tree_commit.id
      WHERE c.id = $1
    """,
    rowMapper = ContractMapper.class
  )
  SqlTuple findAllByContractId(UUID contractId);


  @TenantSql.Find(
    optional = false,
    sql = """
      SELECT c.*, 
             updated_commit.created_at as updated_at,
             created_commit.created_at as created_at,
             updated_tree_commit.created_at as updated_tree_at
      FROM {contract} c
      LEFT JOIN {commit} updated_commit ON c.commit_id = updated_commit.id
      LEFT JOIN {commit} created_commit ON c.created_commit_id = created_commit.id  
      LEFT JOIN {commit} updated_tree_commit ON c.updated_tree_commit_id = updated_tree_commit.id
      WHERE c.id = $1
    """,
    rowMapper = ContractMapper.class
  )
  SqlTuple getById(UUID id);

  @TenantSql.InsertAll(
    sql = """
      INSERT INTO {contract}
      (id, parent_contract_id, contract_number, external_id, commit_id, created_commit_id, updated_tree_commit_id,
       contract_issue_date, contract_issue_date_interval, contract_issue_date_type,
       contract_start_date, contract_start_date_interval, contract_start_date_type,
       contract_maturity_date, contract_maturity_date_interval, contract_maturity_date_type,
       contract_status, contract_sub_status, contract_type, contract_sub_type, contract_data)
       VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14, $15, $16, $17, $18, $19, $20, $21)
    """,
    propsMapper = ContractInsertMapper.class
  )
  SqlTupleList insertMany(List<Contract> contracts);

  @TenantSql.UpdateAll(
    sql = """
      UPDATE {contract}
       SET parent_contract_id = $1, contract_number = $2, external_id = $3, commit_id = $4, updated_tree_commit_id = $5,
           contract_issue_date = $6, contract_issue_date_interval = $7, contract_issue_date_type = $8,
           contract_start_date = $9, contract_start_date_interval = $10, contract_start_date_type = $11,
           contract_maturity_date = $12, contract_maturity_date_interval = $13, contract_maturity_date_type = $14,
           contract_status = $15, contract_sub_status = $16, contract_type = $17, contract_sub_type = $18, contract_data = $19
       WHERE id = $20
    """,
    propsMapper = ContractUpdateMapper.class
  )
  SqlTupleList updateMany(List<Contract> contracts);

  // Mapper classes
  class ContractMapper implements TenantSql.RowMapper<Contract> {
    @Override
    public Contract apply(Row row) {
      final String parent_contract_id = row.getString("parent_contract_id");
      final String external_id = row.getString("external_id");
      final LocalDate contract_maturity_date = row.getLocalDate("contract_maturity_date");
      final Duration contract_maturity_date_interval = row.get(Duration.class, "contract_maturity_date_interval");
      final String contract_maturity_date_type = row.getString("contract_maturity_date_type");
      final String contract_sub_status = row.getString("contract_sub_status");
      final String contract_sub_type = row.getString("contract_sub_type");
      final JsonObject contract_data = row.getJsonObject("contract_data");

      return ImmutableContract.builder()
          .id(row.getString("id"))
          .parentContractId(Optional.ofNullable(parent_contract_id))
          .contractNumber(row.getString("contract_number"))

          .externalId(Optional.ofNullable(external_id))
          .commitId(row.getString("commit_id"))
          .createdCommitId(row.getString("created_commit_id"))
          .updatedTreeCommitId(row.getString("updated_tree_commit_id"))

          // Transitive data from joins
          .transitives(ImmutableContractTransitives.builder()
              .createdAt(row.getOffsetDateTime("created_at"))
              .updatedAt(row.getOffsetDateTime("updated_at"))
              .updatedTreeAt(row.getOffsetDateTime("updated_tree_at"))
              .build())

          // Business dates (expanded)
          .contractIssueDate(row.getLocalDate("contract_issue_date"))
          .contractIssueDateInterval(row.get(Duration.class, "contract_issue_date_interval"))
          .contractIssueDateType(row.getString("contract_issue_date_type"))
          
          .contractStartDate(row.getLocalDate("contract_start_date"))
          .contractStartDateInterval(row.get(Duration.class, "contract_start_date_interval"))
          .contractStartDateType(row.getString("contract_start_date_type"))
          
          .contractMaturityDate(Optional.ofNullable(contract_maturity_date))
          .contractMaturityDateInterval(Optional.ofNullable(contract_maturity_date_interval))
          .contractMaturityDateType(Optional.ofNullable(contract_maturity_date_type))
          
          .contractStatus(row.getString("contract_status"))
          .contractSubStatus(Optional.ofNullable(contract_sub_status))
          .contractType(row.getString("contract_type"))
          .contractSubType(Optional.ofNullable(contract_sub_type))
          .contractData(Optional.ofNullable(contract_data))
          .build();
      
    }
  }

  class ContractInsertMapper implements TenantSql.PropsMapper<Contract> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Contract doc) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        TableUtils.toUuid(doc.getId()),
        doc.getParentContractId().map(TableUtils::toUuid).orElse(null),
        doc.getContractNumber(),
        doc.getExternalId().orElse(null),
        TableUtils.toUuid(doc.getCommitId()),
        TableUtils.toUuid(doc.getCreatedCommitId()),
        TableUtils.toUuid(doc.getUpdatedTreeCommitId()),
        doc.getContractIssueDate(),
        TableUtils.toInterval(doc.getContractIssueDateInterval()),
        doc.getContractIssueDateType(),
        doc.getContractStartDate(),
        TableUtils.toInterval(doc.getContractStartDateInterval()),
        doc.getContractStartDateType(),
        doc.getContractMaturityDate().orElse(null),
        TableUtils.toIntervalOptional(doc.getContractMaturityDateInterval()),
        doc.getContractMaturityDateType().orElse(null),
        doc.getContractStatus(),
        doc.getContractSubStatus().orElse(null),
        doc.getContractType(),
        doc.getContractSubType().orElse(null),
        doc.getContractData().orElse(null)
      });
    }
  }

  class ContractUpdateMapper implements TenantSql.PropsMapper<Contract> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Contract doc) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        doc.getParentContractId().map(TableUtils::toUuid).orElse(null),
        doc.getContractNumber(),
        doc.getExternalId().orElse(null),
        TableUtils.toUuid(doc.getCommitId()),
        TableUtils.toUuid(doc.getUpdatedTreeCommitId()),
        doc.getContractIssueDate(),
        TableUtils.toInterval(doc.getContractIssueDateInterval()),
        doc.getContractIssueDateType(),
        doc.getContractStartDate(),
        TableUtils.toInterval(doc.getContractStartDateInterval()),
        doc.getContractStartDateType(),
        doc.getContractMaturityDate().orElse(null),
        TableUtils.toIntervalOptional(doc.getContractMaturityDateInterval()),
        doc.getContractMaturityDateType().orElse(null),
        doc.getContractStatus(),
        doc.getContractSubStatus().orElse(null),
        doc.getContractType(),
        doc.getContractSubType().orElse(null),
        doc.getContractData().orElse(null),
        TableUtils.toUuid(doc.getId())
      });
    }
  }
}
