package io.resys.thena.ledger.client.tables;

/*-
 * #%L
 * thena-ledger-client
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
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.resys.thena.ledger.client.entities.ImmutableSettlement;
import io.resys.thena.ledger.client.entities.ImmutableSettlementTransitives;
import io.resys.thena.ledger.client.entities.Settlement;
import io.resys.thena.support.TableUtils;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
  name = "settlement",
  order = 400,
  ddl = """
    CREATE TABLE IF NOT EXISTS {settlement}
    (
      id UUID PRIMARY KEY,
      ledger_id UUID NOT NULL,
      external_id VARCHAR(255) NOT NULL,
      
      settlement_type VARCHAR(100) NOT NULL,
      settlement_sub_type VARCHAR(100),
      settlement_description TEXT,
      settlement_date DATE NOT NULL,
      settlement_amount DECIMAL(15,2) NOT NULL,
      
      created_commit_id UUID NOT NULL
    );

    CREATE INDEX IF NOT EXISTS {settlement}_LEDGER_INDEX
      ON {settlement} (ledger_id);
    CREATE INDEX IF NOT EXISTS {settlement}_EXTERNAL_INDEX
      ON {settlement} (external_id);
    CREATE INDEX IF NOT EXISTS {settlement}_DATE_INDEX
      ON {settlement} (settlement_date);
  """,
  constraints = """
    ALTER TABLE {settlement} ADD CONSTRAINT fk_settlement_ledger 
      FOREIGN KEY (ledger_id) REFERENCES {ledger}(id);
  """,
  drop = """
    DROP TABLE IF EXISTS {settlement} CASCADE;
  """
)
public interface SettlementTable {

  @TenantSql.FindAll(
    sql = """
      SELECT settlement.*,
             created_commit.created_at as created_at
      FROM {settlement} settlement
      LEFT JOIN {commit} created_commit ON settlement.created_commit_id = created_commit.commit_id
      LEFT JOIN {ledger} ledger ON settlement.ledger_id = ledger.id
    """,
    rowMapper = SettlementMapper.class,
    sqlBuilder = LedgerTableFilter.SQL.class
  )
  SqlTuple findAllByFilter(LedgerTableFilter filter);

  @TenantSql.FindAll(
    sql = """
      SELECT settlement.*,
             created_commit.created_at as created_at
      FROM {settlement} settlement
      LEFT JOIN {commit} created_commit ON settlement.created_commit_id = created_commit.commit_id
      ORDER BY settlement_date DESC
    """,
    rowMapper = SettlementMapper.class
  )
  Sql findAll();

  @TenantSql.FindAll(
    sql = """
      SELECT settlement.*,
             created_commit.created_at as created_at
      FROM {settlement} settlement
      LEFT JOIN {commit} created_commit ON settlement.created_commit_id = created_commit.commit_id
      WHERE ledger_id = $1
      ORDER BY settlement_date DESC
    """,
    rowMapper = SettlementMapper.class
  )
  SqlTuple findAllByLedgerId(String ledgerId);

  @TenantSql.Find(
    optional = false,
    sql = """
      SELECT settlement.*,
             created_commit.created_at as created_at
      FROM {settlement} settlement
      LEFT JOIN {commit} created_commit ON settlement.created_commit_id = created_commit.commit_id
      WHERE id = $1
    """,
    rowMapper = SettlementMapper.class
  )
  SqlTuple getById(String settlementId);

  @TenantSql.Find(
    optional = true,
    sql = """
      SELECT settlement.*,
             created_commit.created_at as created_at
      FROM {settlement} settlement
      LEFT JOIN {commit} created_commit ON settlement.created_commit_id = created_commit.commit_id
      WHERE external_id = $1
    """,
    rowMapper = SettlementMapper.class
  )
  SqlTuple findByExternalId(String externalId);

  @TenantSql.InsertAll(
    sql = """
      INSERT INTO {settlement}
      (id, ledger_id, external_id, settlement_type, settlement_sub_type, 
       settlement_description, settlement_date, settlement_amount, created_commit_id)
       VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9)
    """,
    propsMapper = SettlementInsertMapper.class
  )
  SqlTupleList insertMany(List<Settlement> settlements);

  // Mapper classes
  class SettlementMapper implements TenantSql.RowMapper<Settlement> {
    @Override
    public Settlement apply(Row row) {
      return ImmutableSettlement.builder()
          .id(TableUtils.toStringUUID(row, "id"))
          .ledgerId(TableUtils.toStringUUID(row, "ledger_id"))
          .externalId(row.getString("external_id"))
          .settlementType(row.getString("settlement_type"))
          .settlementSubType(Optional.ofNullable(row.getString("settlement_sub_type")))
          .settlementDescription(Optional.ofNullable(row.getString("settlement_description")))
          .settlementDate(row.getLocalDate("settlement_date"))
          .settlementAmount(row.getBigDecimal("settlement_amount"))
          .createdCommitId(TableUtils.toStringUUID(row, "created_commit_id"))
          .transitives(ImmutableSettlementTransitives.builder()
              .createdAt(row.getOffsetDateTime("created_at"))
              .build())
          .build();
    }
  }

  class SettlementInsertMapper implements TenantSql.PropsMapper<Settlement> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Settlement doc) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        TableUtils.toUuid(doc.getId()),
        TableUtils.toUuid(doc.getLedgerId()),
        doc.getExternalId(),
        doc.getSettlementType(),
        doc.getSettlementSubType().orElse(null),
        doc.getSettlementDescription().orElse(null),
        doc.getSettlementDate(),
        doc.getSettlementAmount(),
        TableUtils.toUuid(doc.getCreatedCommitId())
      });
    }
  }
}