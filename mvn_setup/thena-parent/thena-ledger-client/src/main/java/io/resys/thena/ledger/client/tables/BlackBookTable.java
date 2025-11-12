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
import io.resys.thena.ledger.client.entities.BlackBook;
import io.resys.thena.ledger.client.entities.ImmutableBlackBook;
import io.resys.thena.support.TableUtils;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
  name = "black_book",
  order = 600,
  ddl = """
    CREATE TABLE IF NOT EXISTS {black_book}
    (
      black_book_id UUID PRIMARY KEY,
      ledger_id UUID NOT NULL,
      black_book_external_id VARCHAR(255) NOT NULL,
      black_book_type VARCHAR(100) NOT NULL,
      black_book_sub_type VARCHAR(100),
      black_book_description TEXT,
      black_book_date DATE NOT NULL,
      black_book_amount DECIMAL(15,2) NOT NULL,
      black_book_currency VARCHAR(3) NOT NULL,
      created_commit UUID NOT NULL
    );

    CREATE INDEX IF NOT EXISTS {black_book}_LEDGER_INDEX
      ON {black_book} (ledger_id);
    CREATE INDEX IF NOT EXISTS {black_book}_EXTERNAL_INDEX
      ON {black_book} (black_book_external_id);
    CREATE INDEX IF NOT EXISTS {black_book}_DATE_INDEX
      ON {black_book} (black_book_date);
  """,
  constraints = """
    ALTER TABLE {black_book} ADD CONSTRAINT fk_black_book_ledger 
      FOREIGN KEY (ledger_id) REFERENCES {ledger}(ledger_id);
    ALTER TABLE {black_book} ADD CONSTRAINT fk_black_book_created_commit 
      FOREIGN KEY (created_commit) REFERENCES {commit}(commit_id);
  """,
  drop = """
    DROP TABLE {black_book};
  """
)
public interface BlackBookTable {

  @TenantSql.FindAll(
    sql = """
      SELECT * FROM {black_book}
      ORDER BY black_book_date DESC
    """,
    rowMapper = BlackBookMapper.class
  )
  Sql findAll();

  @TenantSql.FindAll(
    sql = """
      SELECT * FROM {black_book}
      WHERE ledger_id = $1
      ORDER BY black_book_date DESC
    """,
    rowMapper = BlackBookMapper.class
  )
  SqlTuple findAllByLedgerId(String ledgerId);

  @TenantSql.Find(
    optional = false,
    sql = """
      SELECT * FROM {black_book}
      WHERE black_book_id = $1
    """,
    rowMapper = BlackBookMapper.class
  )
  SqlTuple getById(String blackBookId);

  @TenantSql.Find(
    optional = true,
    sql = """
      SELECT * FROM {black_book}
      WHERE black_book_external_id = $1
    """,
    rowMapper = BlackBookMapper.class
  )
  SqlTuple findByExternalId(String externalId);

  @TenantSql.InsertAll(
    sql = """
      INSERT INTO {black_book}
      (black_book_id, ledger_id, black_book_external_id, black_book_type, black_book_sub_type, 
       black_book_description, black_book_date, black_book_amount, black_book_currency, created_commit)
       VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)
    """,
    propsMapper = BlackBookInsertMapper.class
  )
  SqlTupleList insertMany(List<BlackBook> blackBooks);

  // Mapper classes
  class BlackBookMapper implements TenantSql.RowMapper<BlackBook> {
    @Override
    public BlackBook apply(Row row) {
      return ImmutableBlackBook.builder()
          .id(TableUtils.toStringUUID(row, "black_book_id"))
          .ledgerId(TableUtils.toStringUUID(row, "ledger_id"))
          .externalId(row.getString("black_book_external_id"))
          .type(row.getString("black_book_type"))
          .subType(Optional.ofNullable(row.getString("black_book_sub_type")))
          .description(Optional.ofNullable(row.getString("black_book_description")))
          .date(row.getLocalDate("black_book_date"))
          .amount(row.getBigDecimal("black_book_amount"))
          .currency(row.getString("black_book_currency"))
          .createdCommit(TableUtils.toStringUUID(row, "created_commit"))
          .build();
    }
  }

  class BlackBookInsertMapper implements TenantSql.PropsMapper<BlackBook> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(BlackBook doc) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        TableUtils.toUuid(doc.getId()),
        TableUtils.toUuid(doc.getLedgerId()),
        doc.getExternalId(),
        doc.getType(),
        doc.getSubType().orElse(null),
        doc.getDescription().orElse(null),
        doc.getDate(),
        doc.getAmount(),
        doc.getCurrency(),
        TableUtils.toUuid(doc.getCreatedCommit())
      });
    }
  }
}