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
import io.resys.thena.ledger.client.entities.ImmutableLedger;
import io.resys.thena.ledger.client.entities.Ledger;
import io.resys.thena.support.TableUtils;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
  name = "ledger",
  order = 200,
  ddl = """
    CREATE TABLE IF NOT EXISTS {ledger}
    (
      ledger_id UUID PRIMARY KEY,
      ledger_external_id VARCHAR(255) NOT NULL,
      ledger_name VARCHAR(255) NOT NULL,
      ledger_description TEXT,
      created_commit UUID NOT NULL,
      updated_commit UUID NOT NULL
    );

    CREATE INDEX IF NOT EXISTS {ledger}_EXTERNAL_INDEX
      ON {ledger} (ledger_external_id);
  """,
  constraints = """
    ALTER TABLE {ledger} ADD CONSTRAINT fk_ledger_created_commit 
      FOREIGN KEY (created_commit) REFERENCES {commit}(commit_id);
    ALTER TABLE {ledger} ADD CONSTRAINT fk_ledger_updated_commit 
      FOREIGN KEY (updated_commit) REFERENCES {commit}(commit_id);
  """,
  drop = """
    DROP TABLE {ledger};
  """
)
public interface LedgerTable {

  @TenantSql.FindAll(
    sql = """
      SELECT * FROM {ledger}
      ORDER BY ledger_name ASC
    """,
    rowMapper = LedgerMapper.class
  )
  Sql findAll();

  @TenantSql.Find(
    optional = false,
    sql = """
      SELECT * FROM {ledger}
      WHERE ledger_id = $1
    """,
    rowMapper = LedgerMapper.class
  )
  SqlTuple getById(String ledgerId);

  @TenantSql.Find(
    optional = true,
    sql = """
      SELECT * FROM {ledger}
      WHERE ledger_external_id = $1
    """,
    rowMapper = LedgerMapper.class
  )
  SqlTuple findByExternalId(String externalId);

  @TenantSql.InsertAll(
    sql = """
      INSERT INTO {ledger}
      (ledger_id, ledger_external_id, ledger_name, ledger_description, created_commit, updated_commit)
       VALUES($1, $2, $3, $4, $5, $6)
    """,
    propsMapper = LedgerInsertMapper.class
  )
  SqlTupleList insertMany(List<Ledger> ledgers);

  @TenantSql.UpdateAll(
    sql = """
      UPDATE {ledger}
       SET ledger_external_id = $1, ledger_name = $2, ledger_description = $3, updated_commit = $4
       WHERE ledger_id = $5
    """,
    propsMapper = LedgerUpdateMapper.class
  )
  SqlTupleList updateMany(List<Ledger> ledgers);

  // Mapper classes
  class LedgerMapper implements TenantSql.RowMapper<Ledger> {
    @Override
    public Ledger apply(Row row) {
      return ImmutableLedger.builder()
          .id(TableUtils.toStringUUID(row, "ledger_id"))
          .externalId(row.getString("ledger_external_id"))
          .name(row.getString("ledger_name"))
          .description(Optional.ofNullable(row.getString("ledger_description")))
          .createdCommit(TableUtils.toStringUUID(row, "created_commit"))
          .updatedCommit(TableUtils.toStringUUID(row, "updated_commit"))
          .build();
    }
  }

  class LedgerInsertMapper implements TenantSql.PropsMapper<Ledger> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Ledger doc) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        TableUtils.toUuid(doc.getId()),
        doc.getExternalId(),
        doc.getName(),
        doc.getDescription().orElse(null),
        TableUtils.toUuid(doc.getCreatedCommit()),
        TableUtils.toUuid(doc.getUpdatedCommit())
      });
    }
  }

  class LedgerUpdateMapper implements TenantSql.PropsMapper<Ledger> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Ledger doc) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        doc.getExternalId(),
        doc.getName(),
        doc.getDescription().orElse(null),
        TableUtils.toUuid(doc.getUpdatedCommit()),
        TableUtils.toUuid(doc.getId())
      });
    }
  }
}