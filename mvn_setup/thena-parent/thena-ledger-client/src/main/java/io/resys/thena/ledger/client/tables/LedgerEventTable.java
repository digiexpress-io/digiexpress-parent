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
import io.resys.thena.ledger.client.entities.ImmutableLedgerEvent;
import io.resys.thena.ledger.client.entities.LedgerEvent;
import io.resys.thena.support.TableUtils;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
  name = "ledger_event",
  order = 900,
  ddl = """
    CREATE TABLE IF NOT EXISTS {ledger_event}
    (
      ledger_event_id UUID PRIMARY KEY,
      ledger_id UUID NOT NULL,
      ledger_event_external_id VARCHAR(255) NOT NULL,
      ledger_event_type VARCHAR(100) NOT NULL,
      ledger_event_sub_type VARCHAR(100),
      ledger_event_description TEXT,
      ledger_event_date DATE NOT NULL,
      ledger_event_body JSONB,
      created_commit UUID NOT NULL
    );

    CREATE INDEX IF NOT EXISTS {ledger_event}_LEDGER_INDEX
      ON {ledger_event} (ledger_id);
    CREATE INDEX IF NOT EXISTS {ledger_event}_EXTERNAL_INDEX
      ON {ledger_event} (ledger_event_external_id);
    CREATE INDEX IF NOT EXISTS {ledger_event}_TYPE_INDEX
      ON {ledger_event} (ledger_event_type);
    CREATE INDEX IF NOT EXISTS {ledger_event}_DATE_INDEX
      ON {ledger_event} (ledger_event_date);
  """,
  constraints = """
    ALTER TABLE {ledger_event} ADD CONSTRAINT fk_ledger_event_ledger 
      FOREIGN KEY (ledger_id) REFERENCES {ledger}(ledger_id);
    ALTER TABLE {ledger_event} ADD CONSTRAINT fk_ledger_event_created_commit 
      FOREIGN KEY (created_commit) REFERENCES {commit}(commit_id);
  """,
  drop = """
    DROP TABLE {ledger_event};
  """
)
public interface LedgerEventTable {

  @TenantSql.FindAll(
    sql = """
      SELECT ledger_event.*,
             created_commit.created_at as created_at
      FROM {ledger_event} ledger_event
      LEFT JOIN {commit} created_commit ON ledger_event.created_commit = created_commit.commit_id
      ORDER BY ledger_event_date DESC
    """,
    rowMapper = LedgerEventMapper.class
  )
  Sql findAll();

  @TenantSql.FindAll(
    sql = """
      SELECT ledger_event.*,
             created_commit.created_at as created_at
      FROM {ledger_event} ledger_event
      LEFT JOIN {commit} created_commit ON ledger_event.created_commit = created_commit.commit_id
      WHERE ledger_id = $1
      ORDER BY ledger_event_date DESC
    """,
    rowMapper = LedgerEventMapper.class
  )
  SqlTuple findAllByLedgerId(String ledgerId);

  @TenantSql.FindAll(
    sql = """
      SELECT ledger_event.*,
             created_commit.created_at as created_at
      FROM {ledger_event} ledger_event
      LEFT JOIN {commit} created_commit ON ledger_event.created_commit = created_commit.commit_id
      WHERE ledger_event_type = $1
      ORDER BY ledger_event_date DESC
    """,
    rowMapper = LedgerEventMapper.class
  )
  SqlTuple findAllByType(String type);

  @TenantSql.Find(
    optional = false,
    sql = """
      SELECT ledger_event.*,
             created_commit.created_at as created_at
      FROM {ledger_event} ledger_event
      LEFT JOIN {commit} created_commit ON ledger_event.created_commit = created_commit.commit_id
      WHERE ledger_event_id = $1
    """,
    rowMapper = LedgerEventMapper.class
  )
  SqlTuple getById(String ledgerEventId);

  @TenantSql.Find(
    optional = true,
    sql = """
      SELECT ledger_event.*,
             created_commit.created_at as created_at
      FROM {ledger_event} ledger_event
      LEFT JOIN {commit} created_commit ON ledger_event.created_commit = created_commit.commit_id
      WHERE ledger_event_external_id = $1
    """,
    rowMapper = LedgerEventMapper.class
  )
  SqlTuple findByExternalId(String externalId);

  @TenantSql.InsertAll(
    sql = """
      INSERT INTO {ledger_event}
      (ledger_event_id, ledger_id, ledger_event_external_id, ledger_event_type, ledger_event_sub_type, 
       ledger_event_description, ledger_event_date, ledger_event_body, created_commit)
       VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9)
    """,
    propsMapper = LedgerEventInsertMapper.class
  )
  SqlTupleList insertMany(List<LedgerEvent> ledgerEvents);

  // Mapper classes
  class LedgerEventMapper implements TenantSql.RowMapper<LedgerEvent> {
    @Override
    public LedgerEvent apply(Row row) {
      final JsonObject ledger_event_body = row.getJsonObject("ledger_event_body");

      return ImmutableLedgerEvent.builder()
          .id(TableUtils.toStringUUID(row, "ledger_event_id"))
          .ledgerId(TableUtils.toStringUUID(row, "ledger_id"))
          .externalId(row.getString("ledger_event_external_id"))
          .type(row.getString("ledger_event_type"))
          .subType(Optional.ofNullable(row.getString("ledger_event_sub_type")))
          .description(Optional.ofNullable(row.getString("ledger_event_description")))
          .date(row.getLocalDate("ledger_event_date"))
          .body(Optional.ofNullable(ledger_event_body))
          .createdCommit(TableUtils.toStringUUID(row, "created_commit"))
          .build();
    }
  }

  class LedgerEventInsertMapper implements TenantSql.PropsMapper<LedgerEvent> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(LedgerEvent doc) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        TableUtils.toUuid(doc.getId()),
        TableUtils.toUuid(doc.getLedgerId()),
        doc.getExternalId(),
        doc.getType(),
        doc.getSubType().orElse(null),
        doc.getDescription().orElse(null),
        doc.getDate(),
        doc.getBody().orElse(null),
        TableUtils.toUuid(doc.getCreatedCommit())
      });
    }
  }
}