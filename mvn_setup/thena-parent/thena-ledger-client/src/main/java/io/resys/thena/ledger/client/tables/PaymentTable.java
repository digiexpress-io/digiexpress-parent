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
  order = 200,
  ddl = """
    CREATE TABLE IF NOT EXISTS {payment}
    (
      id UUID PRIMARY KEY,
      ledger_id UUID NOT NULL,
      external_id VARCHAR(255) NOT NULL,
      
      payment_type VARCHAR(100) NOT NULL,
      payment_sub_type VARCHAR(100),
      payment_description TEXT,
      payment_date DATE NOT NULL,
      payment_amount DECIMAL(15,2) NOT NULL,
      payment_body JSONB,
            
      created_commit_id UUID NOT NULL
    );

    CREATE INDEX IF NOT EXISTS {payment}_LEDGER_INDEX
      ON {payment} (ledger_id);
    CREATE INDEX IF NOT EXISTS {payment}_EXTERNAL_INDEX
      ON {payment} (external_id);
    CREATE INDEX IF NOT EXISTS {payment}_DATE_INDEX
      ON {payment} (payment_date);
  """,
  constraints = """
    ALTER TABLE {payment} ADD CONSTRAINT fk_payment_ledger 
      FOREIGN KEY (ledger_id) REFERENCES {ledger}(id);
  """,
  drop = """
    DROP TABLE IF EXISTS {payment} CASCADE;
  """
)
public interface PaymentTable {

  @TenantSql.FindAll(
    sql = """
      SELECT payment.*,
             created_commit.created_at as created_at
      FROM {payment} payment
      LEFT JOIN {commit} created_commit ON payment.created_commit_id = created_commit.commit_id
      LEFT JOIN {ledger} ledger ON payment.ledger_id = ledger.id
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
      LEFT JOIN {commit} created_commit ON payment.created_commit_id = created_commit.commit_id
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
      LEFT JOIN {commit} created_commit ON payment.created_commit_id = created_commit.commit_id
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
      LEFT JOIN {commit} created_commit ON payment.created_commit_id = created_commit.commit_id
      WHERE id = $1
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
      LEFT JOIN {commit} created_commit ON payment.created_commit_id = created_commit.commit_id
      WHERE external_id = $1
    """,
    rowMapper = PaymentMapper.class
  )
  SqlTuple findByExternalId(String externalId);

  @TenantSql.InsertAll(
    sql = """
      INSERT INTO {payment}
      (id, ledger_id, external_id, payment_type, payment_sub_type, 
       payment_description, payment_date, payment_amount, payment_body, created_commit_id)
       VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)
    """,
    propsMapper = PaymentInsertMapper.class
  )
  SqlTupleList insertMany(List<Payment> payments);

  // Mapper classes
  class PaymentMapper implements TenantSql.RowMapper<Payment> {
    @Override
    public Payment apply(Row row) {
      return ImmutablePayment.builder()
          .id(TableUtils.toStringUUID(row, "id"))
          .ledgerId(TableUtils.toStringUUID(row, "ledger_id"))
          .externalId(row.getString("external_id"))
          .paymentType(row.getString("payment_type"))
          .paymentSubType(Optional.ofNullable(row.getString("payment_sub_type")))
          .paymentDescription(Optional.ofNullable(row.getString("payment_description")))
          .paymentDate(row.getLocalDate("payment_date"))
          .paymentAmount(row.getBigDecimal("payment_amount"))
          .paymentBody(Optional.ofNullable(row.getJsonObject("payment_body")))
          
          .createdCommitId(TableUtils.toStringUUID(row, "created_commit_id"))
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
        doc.getPaymentType(),
        doc.getPaymentSubType().orElse(null),
        doc.getPaymentDescription().orElse(null),
        doc.getPaymentDate(),
        doc.getPaymentAmount(),
        doc.getPaymentBody().orElse(null),
        TableUtils.toUuid(doc.getCreatedCommitId())
      });
    }
  }
}