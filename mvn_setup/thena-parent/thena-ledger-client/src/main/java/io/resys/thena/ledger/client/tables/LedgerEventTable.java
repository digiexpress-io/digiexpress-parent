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
import io.resys.thena.ledger.client.entities.ImmutableLedgerEventTransitives;
import io.resys.thena.ledger.client.entities.LedgerEvent;
import io.resys.thena.support.TableUtils;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
  name = "ledger_event",
  order = 1100,
  ddl = """
    CREATE TABLE IF NOT EXISTS {ledger_event}
    (
      id UUID PRIMARY KEY,
      ledger_id UUID NOT NULL,
      external_id VARCHAR(255) NOT NULL,
      ledger_event_type VARCHAR(100) NOT NULL,
      ledger_event_sub_type VARCHAR(100),
      ledger_event_description TEXT,
      ledger_event_date DATE NOT NULL,
      ledger_event_body JSONB,
      created_commit_id UUID NOT NULL
    );

    CREATE INDEX IF NOT EXISTS {ledger_event}_LEDGER_INDEX
      ON {ledger_event} (ledger_id);
    CREATE INDEX IF NOT EXISTS {ledger_event}_EXTERNAL_INDEX
      ON {ledger_event} (external_id);
    CREATE INDEX IF NOT EXISTS {ledger_event}_TYPE_INDEX
      ON {ledger_event} (ledger_event_type);
    CREATE INDEX IF NOT EXISTS {ledger_event}_DATE_INDEX
      ON {ledger_event} (ledger_event_date);
  """,
  constraints = """
    ALTER TABLE {ledger_event} ADD CONSTRAINT fk_ledger_event_ledger 
      FOREIGN KEY (ledger_id) REFERENCES {ledger}(id);
  """,
  drop = """
    DROP TABLE IF EXISTS {ledger_event} CASCADE;
  """
)
public interface LedgerEventTable {

  @TenantSql.FindAll(
    sql = """
      SELECT ledger_event.*,
             created_commit.created_at as created_at
      FROM {ledger_event} ledger_event
      LEFT JOIN {commit} created_commit ON ledger_event.created_commit_id = created_commit.commit_id
      LEFT JOIN {ledger} ledger ON ledger_event.ledger_id = ledger.id
    """,
    rowMapper = LedgerEventMapper.class,
    sqlBuilder = LedgerTableFilter.SQL.class
  )
  SqlTuple findAllByFilter(LedgerTableFilter filter);

  @TenantSql.FindAll(
    sql = """
      SELECT ledger_event.*,
             created_commit.created_at as created_at
      FROM {ledger_event} ledger_event
      LEFT JOIN {commit} created_commit ON ledger_event.created_commit_id = created_commit.commit_id
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
      LEFT JOIN {commit} created_commit ON ledger_event.created_commit_id = created_commit.commit_id
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
      LEFT JOIN {commit} created_commit ON ledger_event.created_commit_id = created_commit.commit_id
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
      LEFT JOIN {commit} created_commit ON ledger_event.created_commit_id = created_commit.commit_id
      WHERE id = $1
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
      LEFT JOIN {commit} created_commit ON ledger_event.created_commit_id = created_commit.commit_id
      WHERE external_id = $1
    """,
    rowMapper = LedgerEventMapper.class
  )
  SqlTuple findByExternalId(String externalId);

  @TenantSql.InsertAll(
    sql = """
      INSERT INTO {ledger_event}
      (id, ledger_id, external_id, ledger_event_type, ledger_event_sub_type, 
       ledger_event_description, ledger_event_date, ledger_event_body, created_commit_id)
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
          .id(TableUtils.toStringUUID(row, "id"))
          .ledgerId(TableUtils.toStringUUID(row, "ledger_id"))
          .externalId(row.getString("external_id"))
          .eventType(row.getString("ledger_event_type"))
          .eventSubType(Optional.ofNullable(row.getString("ledger_event_sub_type")))
          .eventDescription(Optional.ofNullable(row.getString("ledger_event_description")))
          .eventDate(row.getLocalDate("ledger_event_date"))
          .body(Optional.ofNullable(ledger_event_body))
          .createdCommitId(TableUtils.toStringUUID(row, "created_commit_id"))
          .transitives(ImmutableLedgerEventTransitives.builder()
              .createdAt(row.getOffsetDateTime("created_at"))
              .build())
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
        doc.getEventType(),
        doc.getEventSubType().orElse(null),
        doc.getEventDescription().orElse(null),
        doc.getEventDate(),
        doc.getBody().orElse(null),
        TableUtils.toUuid(doc.getCreatedCommitId())
      });
    }
  }
}