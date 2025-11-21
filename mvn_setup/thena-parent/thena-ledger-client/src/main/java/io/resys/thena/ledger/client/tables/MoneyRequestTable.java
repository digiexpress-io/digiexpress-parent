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
import io.resys.thena.ledger.client.entities.ImmutableMoneyRequest;
import io.resys.thena.ledger.client.entities.ImmutableMoneyRequestTransitives;
import io.resys.thena.ledger.client.entities.MoneyRequest;
import io.resys.thena.ledger.client.entities.MoneyRequest.MoneyRequestStatus;
import io.resys.thena.ledger.client.entities.MoneyRequest.MoneyRequestType;
import io.resys.thena.support.TableUtils;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
  name = "money_request",
  order = 300,
  ddl = """
    CREATE TABLE IF NOT EXISTS {money_request}
    (
      id UUID PRIMARY KEY,
      ledger_id UUID NOT NULL,
      external_id VARCHAR(255),
      payment_id UUID,
      
      money_request_type VARCHAR(100) NOT NULL,
      money_request_target_date DATE NOT NULL,
      money_request_status VARCHAR(20) NOT NULL,
      money_request_amount DECIMAL(15,2) NOT NULL,
                  
      money_request_sub_type VARCHAR(100),
      money_request_description TEXT,

      commit_id UUID NOT NULL,
      created_commit_id UUID NOT NULL
    );

    CREATE INDEX IF NOT EXISTS {money_request}_LEDGER_INDEX
      ON {money_request} (ledger_id);
    CREATE INDEX IF NOT EXISTS {money_request}_EXTERNAL_INDEX
      ON {money_request} (external_id);
    CREATE INDEX IF NOT EXISTS {money_request}_PAYMENT_INDEX
      ON {money_request} (payment_id);
    CREATE INDEX IF NOT EXISTS {money_request}_STATUS_INDEX
      ON {money_request} (money_request_status);
    CREATE INDEX IF NOT EXISTS {money_request}_TARGET_DATE_INDEX
      ON {money_request} (money_request_target_date);
  """,
  constraints = """
    ALTER TABLE {money_request} ADD CONSTRAINT fk_money_request_ledger 
      FOREIGN KEY (ledger_id) REFERENCES {ledger}(id);
      
    ALTER TABLE {money_request} ADD CONSTRAINT fk_money_request_payment 
      FOREIGN KEY (payment_id) REFERENCES {payment}(id);
  """,
  drop = """
    DROP TABLE IF EXISTS {money_request} CASCADE;
  """
)
public interface MoneyRequestTable {

  @TenantSql.FindAll(
    sql = """
      SELECT money_request.*,
             commit.created_at as updated_at,
             created_commit.created_at as created_at
      FROM {money_request} money_request
      LEFT JOIN {commit} commit ON money_request.commit_id = commit.commit_id
      LEFT JOIN {commit} created_commit ON money_request.created_commit_id = created_commit.commit_id
      LEFT JOIN {ledger} ledger ON money_request.ledger_id = ledger.id
    """,
    rowMapper = MoneyRequestMapper.class,
    sqlBuilder = LedgerTableFilter.SQL.class
  )
  SqlTuple findAllByFilter(LedgerTableFilter filter);

  @TenantSql.FindAll(
    sql = """
      SELECT money_request.*,
             commit.created_at as updated_at,
             created_commit.created_at as created_at
      FROM {money_request} money_request
      LEFT JOIN {commit} commit ON money_request.commit_id = commit.commit_id
      LEFT JOIN {commit} created_commit ON money_request.created_commit_id = created_commit.commit_id
      ORDER BY money_request_target_date ASC
    """,
    rowMapper = MoneyRequestMapper.class
  )
  Sql findAll();

  @TenantSql.FindAll(
    sql = """
      SELECT money_request.*,
             commit.created_at as updated_at,
             created_commit.created_at as created_at
      FROM {money_request} money_request
      LEFT JOIN {commit} commit ON money_request.commit_id = commit.commit_id
      LEFT JOIN {commit} created_commit ON money_request.created_commit_id = created_commit.commit_id
      WHERE ledger_id = $1
      ORDER BY money_request_target_date ASC
    """,
    rowMapper = MoneyRequestMapper.class
  )
  SqlTuple findAllByLedgerId(String ledgerId);

  @TenantSql.FindAll(
    sql = """
      SELECT money_request.*,
             commit.created_at as updated_at,
             created_commit.created_at as created_at
      FROM {money_request} money_request
      LEFT JOIN {commit} commit ON money_request.commit_id = commit.commit_id
      LEFT JOIN {commit} created_commit ON money_request.created_commit_id = created_commit.commit_id
      WHERE money_request_status = $1
      ORDER BY money_request_target_date ASC
    """,
    rowMapper = MoneyRequestMapper.class
  )
  SqlTuple findAllByStatus(String status);

  @TenantSql.Find(
    optional = false,
    sql = """
      SELECT money_request.*,
             commit.created_at as updated_at,
             created_commit.created_at as created_at
      FROM {money_request} money_request
      LEFT JOIN {commit} commit ON money_request.commit_id = commit.commit_id
      LEFT JOIN {commit} created_commit ON money_request.created_commit_id = created_commit.commit_id
      WHERE id = $1
    """,
    rowMapper = MoneyRequestMapper.class
  )
  SqlTuple getById(String moneyRequestId);

  @TenantSql.Find(
    optional = true,
    sql = """
      SELECT money_request.*,
             commit.created_at as updated_at,
             created_commit.created_at as created_at
      FROM {money_request} money_request
      LEFT JOIN {commit} commit ON money_request.commit_id = commit.commit_id
      LEFT JOIN {commit} created_commit ON money_request.created_commit_id = created_commit.commit_id
      WHERE external_id = $1
    """,
    rowMapper = MoneyRequestMapper.class
  )
  SqlTuple findByExternalId(String externalId);

  @TenantSql.InsertAll(
    sql = """
      INSERT INTO {money_request}
      (id, ledger_id, external_id, payment_id, money_request_type, money_request_sub_type, 
       money_request_status, money_request_description, money_request_target_date, 
       money_request_amount, commit_id, created_commit_id)
       VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12)
    """,
    propsMapper = MoneyRequestInsertMapper.class
  )
  SqlTupleList insertMany(List<MoneyRequest> moneyRequests);

  @TenantSql.UpdateAll(
    sql = """
      UPDATE {money_request}
       SET ledger_id = $1, external_id = $2, payment_id = $3, money_request_type = $4, money_request_sub_type = $5,
           money_request_status = $6, money_request_description = $7, 
           money_request_target_date = $8, money_request_amount = $9, commit_id = $10
       WHERE id = $11
    """,
    propsMapper = MoneyRequestUpdateMapper.class
  )
  SqlTupleList updateMany(List<MoneyRequest> moneyRequests);

  // Mapper classes
  class MoneyRequestMapper implements TenantSql.RowMapper<MoneyRequest> {
    @Override
    public MoneyRequest apply(Row row) {
      return ImmutableMoneyRequest.builder()
          .id(TableUtils.toStringUUID(row, "id"))
          .ledgerId(TableUtils.toStringUUID(row, "ledger_id"))
          .externalId(Optional.ofNullable(row.getString("external_id")))
          .paymentId(Optional.ofNullable(TableUtils.toStringUUID(row, "payment_id")))
          .requestType(MoneyRequestType.valueOf(row.getString("money_request_type")))
          .requestSubType(Optional.ofNullable(row.getString("money_request_sub_type")))
          .requestStatus(MoneyRequestStatus.valueOf(row.getString("money_request_status")))
          .requestDescription(Optional.ofNullable(row.getString("money_request_description")))
          .requestTargetDate(row.getLocalDate("money_request_target_date"))
          .requestAmount(row.getBigDecimal("money_request_amount"))
          .commitId(TableUtils.toStringUUID(row, "commit_id"))
          .createdCommitId(TableUtils.toStringUUID(row, "created_commit_id"))
          .transitives(ImmutableMoneyRequestTransitives.builder()
              .createdAt(row.getOffsetDateTime("created_at"))
              .updatedAt(row.getOffsetDateTime("updated_at"))
              .build())
          .build();
    }
  }

  class MoneyRequestInsertMapper implements TenantSql.PropsMapper<MoneyRequest> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(MoneyRequest doc) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        TableUtils.toUuid(doc.getId()),
        TableUtils.toUuid(doc.getLedgerId()),
        doc.getExternalId().orElse(null),
        TableUtils.toUuid(doc.getPaymentId().orElse(null)),
        doc.getRequestType().name(),
        doc.getRequestSubType().orElse(null),
        doc.getRequestStatus().name(),
        doc.getRequestDescription().orElse(null),
        doc.getRequestTargetDate(),
        doc.getRequestAmount(),
        TableUtils.toUuid(doc.getCommitId()),
        TableUtils.toUuid(doc.getCreatedCommitId())
      });
    }
  }

  class MoneyRequestUpdateMapper implements TenantSql.PropsMapper<MoneyRequest> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(MoneyRequest doc) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        TableUtils.toUuid(doc.getLedgerId()),
        doc.getExternalId().orElse(null),
        TableUtils.toUuid(doc.getPaymentId().orElse(null)),
        doc.getRequestType().name(),
        doc.getRequestSubType().orElse(null),
        doc.getRequestStatus().name(),
        doc.getRequestDescription().orElse(null),
        doc.getRequestTargetDate(),
        doc.getRequestAmount(),
        TableUtils.toUuid(doc.getCommitId()),
        TableUtils.toUuid(doc.getId())
      });
    }
  }
}