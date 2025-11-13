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

import io.resys.thena.api.annotations.TenantSql;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.resys.thena.ledger.client.entities.ImmutableSettlementPayment;
import io.resys.thena.ledger.client.entities.SettlementPayment;
import io.resys.thena.support.TableUtils;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
  name = "settlement_payment",
  order = 501,
  ddl = """
    CREATE TABLE IF NOT EXISTS {settlement_payment}
    (
      settlement_payment_id UUID PRIMARY KEY,
      settlement_id UUID NOT NULL,
      payment_id UUID NOT NULL,
      allocation_amount DECIMAL(15,2) NOT NULL,
      created_commit UUID NOT NULL
    );

    CREATE INDEX IF NOT EXISTS {settlement_payment}_SETTLEMENT_INDEX
      ON {settlement_payment} (settlement_id);
    CREATE INDEX IF NOT EXISTS {settlement_payment}_PAYMENT_INDEX
      ON {settlement_payment} (payment_id);
  """,
  constraints = """
    ALTER TABLE {settlement_payment} ADD CONSTRAINT fk_settlement_payment_settlement 
      FOREIGN KEY (settlement_id) REFERENCES {settlement}(settlement_id);
    ALTER TABLE {settlement_payment} ADD CONSTRAINT fk_settlement_payment_payment 
      FOREIGN KEY (payment_id) REFERENCES {payment}(payment_id);
    ALTER TABLE {settlement_payment} ADD CONSTRAINT fk_settlement_payment_created_commit 
      FOREIGN KEY (created_commit) REFERENCES {commit}(commit_id);
  """,
  drop = """
    DROP TABLE {settlement_payment};
  """
)
public interface SettlementPaymentTable {

  @TenantSql.FindAll(
    sql = """
      SELECT settlement_payment.*,
             created_commit.created_at as created_at
      FROM {settlement_payment} settlement_payment
      LEFT JOIN {commit} created_commit ON settlement_payment.created_commit = created_commit.commit_id
      ORDER BY settlement_payment_id ASC
    """,
    rowMapper = SettlementPaymentMapper.class
  )
  Sql findAll();

  @TenantSql.FindAll(
    sql = """
      SELECT settlement_payment.*,
             created_commit.created_at as created_at
      FROM {settlement_payment} settlement_payment
      LEFT JOIN {commit} created_commit ON settlement_payment.created_commit = created_commit.commit_id
      WHERE settlement_id = $1
    """,
    rowMapper = SettlementPaymentMapper.class
  )
  SqlTuple findAllBySettlementId(String settlementId);

  @TenantSql.FindAll(
    sql = """
      SELECT settlement_payment.*,
             created_commit.created_at as created_at
      FROM {settlement_payment} settlement_payment
      LEFT JOIN {commit} created_commit ON settlement_payment.created_commit = created_commit.commit_id
      WHERE payment_id = $1
    """,
    rowMapper = SettlementPaymentMapper.class
  )
  SqlTuple findAllByPaymentId(String paymentId);

  @TenantSql.Find(
    optional = false,
    sql = """
      SELECT settlement_payment.*,
             created_commit.created_at as created_at
      FROM {settlement_payment} settlement_payment
      LEFT JOIN {commit} created_commit ON settlement_payment.created_commit = created_commit.commit_id
      WHERE settlement_payment_id = $1
    """,
    rowMapper = SettlementPaymentMapper.class
  )
  SqlTuple getById(String settlementPaymentId);

  @TenantSql.InsertAll(
    sql = """
      INSERT INTO {settlement_payment}
      (settlement_payment_id, settlement_id, payment_id, allocation_amount, created_commit)
       VALUES($1, $2, $3, $4, $5)
    """,
    propsMapper = SettlementPaymentInsertMapper.class
  )
  SqlTupleList insertMany(List<SettlementPayment> settlementPayments);

  // Mapper classes
  class SettlementPaymentMapper implements TenantSql.RowMapper<SettlementPayment> {
    @Override
    public SettlementPayment apply(Row row) {
      return ImmutableSettlementPayment.builder()
          .id(TableUtils.toStringUUID(row, "settlement_payment_id"))
          .settlementId(TableUtils.toStringUUID(row, "settlement_id"))
          .paymentId(TableUtils.toStringUUID(row, "payment_id"))
          .allocationAmount(row.getBigDecimal("allocation_amount"))
          .createdCommit(TableUtils.toStringUUID(row, "created_commit"))
          .build();
    }
  }

  class SettlementPaymentInsertMapper implements TenantSql.PropsMapper<SettlementPayment> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(SettlementPayment doc) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        TableUtils.toUuid(doc.getId()),
        TableUtils.toUuid(doc.getSettlementId()),
        TableUtils.toUuid(doc.getPaymentId()),
        doc.getAllocationAmount(),
        TableUtils.toUuid(doc.getCreatedCommit())
      });
    }
  }
}