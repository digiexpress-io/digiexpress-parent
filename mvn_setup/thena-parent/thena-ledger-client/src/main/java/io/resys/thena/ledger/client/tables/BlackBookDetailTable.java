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
import io.resys.thena.ledger.client.entities.BlackBookDetail;
import io.resys.thena.ledger.client.entities.ImmutableBlackBookDetail;
import io.resys.thena.ledger.client.entities.ImmutableBlackBookDetailTransitives;
import io.resys.thena.support.TableUtils;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
  name = "black_book_detail",
  order = 900,
  ddl = """
    CREATE TABLE IF NOT EXISTS {black_book_detail}
    (
      id UUID PRIMARY KEY,
      black_book_id UUID NOT NULL,
      external_id VARCHAR(255),
      target_id VARCHAR(255),
      payment_id UUID,
      
      detail_type VARCHAR(100) NOT NULL,
      detail_sub_type VARCHAR(100),
      detail_description TEXT,
      
      detail_start_date DATE,
      detail_end_date DATE,
      detail_amount DECIMAL(15,2) NOT NULL,
      detail_formula VARCHAR(255),
      detail_body JSONB,
      
      created_commit_id UUID NOT NULL
    );

    CREATE INDEX IF NOT EXISTS {black_book_detail}_BLACK_BOOK_INDEX
      ON {black_book_detail} (black_book_id);
    CREATE INDEX IF NOT EXISTS {black_book_detail}_EXTERNAL_INDEX
      ON {black_book_detail} (external_id);
    CREATE INDEX IF NOT EXISTS {black_book_detail}_PAYMENT_INDEX
      ON {black_book_detail} (payment_id);
    CREATE INDEX IF NOT EXISTS {black_book_detail}_TARGET_INDEX
      ON {black_book_detail} (target_id);
    CREATE INDEX IF NOT EXISTS {black_book_detail}_DATE_RANGE_INDEX
      ON {black_book_detail} (detail_start_date, detail_end_date);
  """,
  constraints = """
    ALTER TABLE {black_book_detail} ADD CONSTRAINT fk_black_book_detail_black_book 
      FOREIGN KEY (black_book_id) REFERENCES {black_book}(id);
      
    ALTER TABLE {black_book_detail} ADD CONSTRAINT fk_black_book_detail_payment 
      FOREIGN KEY (payment_id) REFERENCES {payment}(id);
  """,
  drop = """
    DROP TABLE IF EXISTS {black_book_detail} CASCADE;
  """
)
public interface BlackBookDetailTable {

  @TenantSql.FindAll(
    sql = """
      SELECT black_book_detail.*,
             created_commit.created_at as created_at
      FROM {black_book_detail} black_book_detail
      LEFT JOIN {commit} created_commit ON black_book_detail.created_commit_id = created_commit.commit_id
      LEFT JOIN {black_book} black_book ON black_book_detail.black_book_id = black_book.id
      LEFT JOIN {ledger} ledger ON black_book.ledger_id = ledger.id
    """,
    rowMapper = BlackBookDetailMapper.class,
    sqlBuilder = LedgerTableFilter.SQL.class
  )
  SqlTuple findAllByFilter(LedgerTableFilter filter);

  @TenantSql.FindAll(
    sql = """
      SELECT black_book_detail.*,
             created_commit.created_at as created_at
      FROM {black_book_detail} black_book_detail
      LEFT JOIN {commit} created_commit ON black_book_detail.created_commit_id = created_commit.commit_id
      ORDER BY detail_start_date DESC
    """,
    rowMapper = BlackBookDetailMapper.class
  )
  Sql findAll();

  @TenantSql.FindAll(
    sql = """
      SELECT black_book_detail.*,
             created_commit.created_at as created_at
      FROM {black_book_detail} black_book_detail
      LEFT JOIN {commit} created_commit ON black_book_detail.created_commit_id = created_commit.commit_id
      WHERE black_book_id = $1
      ORDER BY detail_start_date ASC
    """,
    rowMapper = BlackBookDetailMapper.class
  )
  SqlTuple findAllByBlackBookId(String blackBookId);

  @TenantSql.Find(
    optional = false,
    sql = """
      SELECT black_book_detail.*,
             created_commit.created_at as created_at
      FROM {black_book_detail} black_book_detail
      LEFT JOIN {commit} created_commit ON black_book_detail.created_commit_id = created_commit.commit_id
      WHERE id = $1
    """,
    rowMapper = BlackBookDetailMapper.class
  )
  SqlTuple getById(String detailId);

  @TenantSql.Find(
    optional = true,
    sql = """
      SELECT black_book_detail.*,
             created_commit.created_at as created_at
      FROM {black_book_detail} black_book_detail
      LEFT JOIN {commit} created_commit ON black_book_detail.created_commit_id = created_commit.commit_id
      WHERE external_id = $1
    """,
    rowMapper = BlackBookDetailMapper.class
  )
  SqlTuple findByExternalId(String externalId);

  @TenantSql.InsertAll(
    sql = """
      INSERT INTO {black_book_detail}
      (id, black_book_id, external_id, target_id, payment_id, detail_type, detail_sub_type, 
       detail_description, detail_start_date, detail_end_date, 
       detail_amount, detail_formula, detail_body, created_commit_id)
       VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14)
    """,
    propsMapper = BlackBookDetailInsertMapper.class
  )
  SqlTupleList insertMany(List<BlackBookDetail> details);

  // Mapper classes
  class BlackBookDetailMapper implements TenantSql.RowMapper<BlackBookDetail> {
    @Override
    public BlackBookDetail apply(Row row) {
      final JsonObject detail_body = row.getJsonObject("detail_body");

      return ImmutableBlackBookDetail.builder()
          .id(TableUtils.toStringUUID(row, "id"))
          .blackBookId(TableUtils.toStringUUID(row, "black_book_id"))
          .externalId(Optional.ofNullable(row.getString("external_id")))
          .detailType(row.getString("detail_type"))
          .detailSubType(Optional.ofNullable(row.getString("detail_sub_type")))
          .detailDescription(Optional.ofNullable(row.getString("detail_description")))
          .targetId(Optional.ofNullable(row.getString("target_id")))
          .paymentId(Optional.ofNullable(TableUtils.toStringUUID(row, "payment_id")))
          .detailStartDate(Optional.ofNullable(row.getLocalDate("detail_start_date")))
          .detailEndDate(Optional.ofNullable(row.getLocalDate("detail_end_date")))
          .detailAmount(row.getBigDecimal("detail_amount"))
          .detailFormula(Optional.ofNullable(row.getString("detail_formula")))
          .detailBody(Optional.ofNullable(detail_body))
          .createdCommitId(TableUtils.toStringUUID(row, "created_commit_id"))
          .transitives(ImmutableBlackBookDetailTransitives.builder()
              .createdAt(row.getOffsetDateTime("created_at"))
              .build())
          .build();
    }
  }

  class BlackBookDetailInsertMapper implements TenantSql.PropsMapper<BlackBookDetail> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(BlackBookDetail doc) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        TableUtils.toUuid(doc.getId()),
        TableUtils.toUuid(doc.getBlackBookId()),
        doc.getExternalId().orElse(null),
        doc.getTargetId().orElse(null),
        doc.getPaymentId().isPresent() ? TableUtils.toUuid(doc.getPaymentId().get()) : null,
        doc.getDetailType(),
        doc.getDetailSubType().orElse(null),
        doc.getDetailDescription().orElse(null),
        doc.getDetailStartDate().orElse(null),
        doc.getDetailEndDate().orElse(null),
        doc.getDetailAmount(),
        doc.getDetailFormula().orElse(null),
        doc.getDetailBody().orElse(null),
        TableUtils.toUuid(doc.getCreatedCommitId())
      });
    }
  }
}