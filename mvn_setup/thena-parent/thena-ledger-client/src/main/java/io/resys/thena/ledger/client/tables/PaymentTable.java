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
import io.resys.thena.ledger.client.entities.ImmutablePayment;
import io.resys.thena.ledger.client.entities.ImmutablePaymentTransitives;
import io.resys.thena.ledger.client.entities.Payment;
import io.resys.thena.support.TableUtils;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
  name = "payment",
  order = 400,
  ddl = """
    CREATE TABLE IF NOT EXISTS {payment}
    (
      payment_id UUID PRIMARY KEY,
      ledger_id UUID NOT NULL,
      payment_external_id VARCHAR(255) NOT NULL,
      payment_type VARCHAR(100) NOT NULL,
      payment_sub_type VARCHAR(100),
      payment_description TEXT,
      payment_date DATE NOT NULL,
      payment_amount DECIMAL(15,2) NOT NULL,
      created_commit UUID NOT NULL
    );

    CREATE INDEX IF NOT EXISTS {payment}_LEDGER_INDEX
      ON {payment} (ledger_id);
    CREATE INDEX IF NOT EXISTS {payment}_EXTERNAL_INDEX
      ON {payment} (payment_external_id);
    CREATE INDEX IF NOT EXISTS {payment}_DATE_INDEX
      ON {payment} (payment_date);
  """,
  constraints = """
    ALTER TABLE {payment} ADD CONSTRAINT fk_payment_ledger 
      FOREIGN KEY (ledger_id) REFERENCES {ledger}(ledger_id);
    ALTER TABLE {payment} ADD CONSTRAINT fk_payment_created_commit 
      FOREIGN KEY (created_commit) REFERENCES {commit}(commit_id);
  """,
  drop = """
    DROP TABLE {payment};
  """
)
public interface PaymentTable {

  @TenantSql.FindAll(
    sql = """
      SELECT payment.*,
             created_commit.created_at as created_at
      FROM {payment} payment
      LEFT JOIN {commit} created_commit ON payment.created_commit = created_commit.commit_id
      LEFT JOIN {ledger} ledger ON payment.ledger_id = ledger.ledger_id
    """,
    rowMapper = PaymentMapper.class,
    sqlBuilder = LedgerTableFilter.SQL.class
  )
  SqlTuple findAllByFilter(LedgerTableFilter filter);

  @TenantSql.FindAll(
    sql = """
      SELECT payment.*,
             created_commit.created_at as created_at
      FROM {payment} payment
      LEFT JOIN {commit} created_commit ON payment.created_commit = created_commit.commit_id
      ORDER BY payment_date DESC
    """,
    rowMapper = PaymentMapper.class
  )
  Sql findAll();

  @TenantSql.FindAll(
    sql = """
      SELECT payment.*,
             created_commit.created_at as created_at
      FROM {payment} payment
      LEFT JOIN {commit} created_commit ON payment.created_commit = created_commit.commit_id
      WHERE ledger_id = $1
      ORDER BY payment_date DESC
    """,
    rowMapper = PaymentMapper.class
  )
  SqlTuple findAllByLedgerId(String ledgerId);

  @TenantSql.Find(
    optional = false,
    sql = """
      SELECT payment.*,
             created_commit.created_at as created_at
      FROM {payment} payment
      LEFT JOIN {commit} created_commit ON payment.created_commit = created_commit.commit_id
      WHERE payment_id = $1
    """,
    rowMapper = PaymentMapper.class
  )
  SqlTuple getById(String paymentId);

  @TenantSql.Find(
    optional = true,
    sql = """
      SELECT payment.*,
             created_commit.created_at as created_at
      FROM {payment} payment
      LEFT JOIN {commit} created_commit ON payment.created_commit = created_commit.commit_id
      WHERE payment_external_id = $1
    """,
    rowMapper = PaymentMapper.class
  )
  SqlTuple findByExternalId(String externalId);

  @TenantSql.InsertAll(
    sql = """
      INSERT INTO {payment}
      (payment_id, ledger_id, payment_external_id, payment_type, payment_sub_type, 
       payment_description, payment_date, payment_amount, created_commit)
       VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9)
    """,
    propsMapper = PaymentInsertMapper.class
  )
  SqlTupleList insertMany(List<Payment> payments);

  // Mapper classes
  class PaymentMapper implements TenantSql.RowMapper<Payment> {
    @Override
    public Payment apply(Row row) {
      return ImmutablePayment.builder()
          .id(TableUtils.toStringUUID(row, "payment_id"))
          .ledgerId(TableUtils.toStringUUID(row, "ledger_id"))
          .externalId(row.getString("payment_external_id"))
          .type(row.getString("payment_type"))
          .subType(Optional.ofNullable(row.getString("payment_sub_type")))
          .description(Optional.ofNullable(row.getString("payment_description")))
          .date(row.getLocalDate("payment_date"))
          .amount(row.getBigDecimal("payment_amount"))
          .createdCommit(TableUtils.toStringUUID(row, "created_commit"))
          .transitives(ImmutablePaymentTransitives.builder()
              .createdAt(row.getOffsetDateTime("created_at"))
              .build())
          .build();
    }
  }

  class PaymentInsertMapper implements TenantSql.PropsMapper<Payment> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Payment doc) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        TableUtils.toUuid(doc.getId()),
        TableUtils.toUuid(doc.getLedgerId()),
        doc.getExternalId(),
        doc.getType(),
        doc.getSubType().orElse(null),
        doc.getDescription().orElse(null),
        doc.getDate(),
        doc.getAmount(),
        TableUtils.toUuid(doc.getCreatedCommit())
      });
    }
  }
}