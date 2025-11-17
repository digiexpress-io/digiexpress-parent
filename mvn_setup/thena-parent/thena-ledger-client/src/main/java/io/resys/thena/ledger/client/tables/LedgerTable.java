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
import io.resys.thena.ledger.client.entities.ImmutableLedgerTransitives;
import io.resys.thena.ledger.client.entities.Ledger;
import io.resys.thena.support.TableUtils;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
  name = "ledger",
  order = 0,
  ddl = """
    CREATE TABLE IF NOT EXISTS {ledger}
    (
      id UUID PRIMARY KEY,
      external_id VARCHAR(255) NOT NULL,
      ledger_name VARCHAR(255) NOT NULL,
      ledger_description TEXT,
      
      commit_id UUID NOT NULL,
      created_commit_id UUID NOT NULL,
      updated_tree_commit_id UUID NOT NULL
    );

    CREATE INDEX IF NOT EXISTS {ledger}_EXTERNAL_INDEX
      ON {ledger} (external_id);
  """,
  constraints = """
    ALTER TABLE {ledger} ADD CONSTRAINT fk_ledger_commit 
      FOREIGN KEY (commit_id) REFERENCES {commit}(commit_id);
    ALTER TABLE {ledger} ADD CONSTRAINT fk_ledger_created_commit 
      FOREIGN KEY (created_commit_id) REFERENCES {commit}(commit_id);
    ALTER TABLE {ledger} ADD CONSTRAINT fk_ledger_updated_tree_commit 
      FOREIGN KEY (updated_tree_commit_id) REFERENCES {commit}(commit_id);
  """,
  drop = """
    DROP TABLE {ledger};
  """
)
public interface LedgerTable {

  @TenantSql.FindAll(
    sql = """
      SELECT ledger.*,
             updated_commit.created_at as updated_at,
             created_commit.created_at as created_at,
             updated_tree_commit.created_at as updated_tree_at
      FROM {ledger} ledger
      LEFT JOIN {commit} updated_commit ON ledger.commit_id = updated_commit.commit_id
      LEFT JOIN {commit} created_commit ON ledger.created_commit_id = created_commit.commit_id
      LEFT JOIN {commit} updated_tree_commit ON ledger.updated_tree_commit_id = updated_tree_commit.commit_id
    """,
    rowMapper = LedgerMapper.class,
    sqlBuilder = LedgerTableFilter.SQL.class
  )
  SqlTuple findAllByFilter(LedgerTableFilter filter);

  @TenantSql.FindAll(
    sql = """
      SELECT ledger.*,
             updated_commit.created_at as updated_at,
             created_commit.created_at as created_at,
             updated_tree_commit.created_at as updated_tree_at
      FROM {ledger} ledger
      LEFT JOIN {commit} updated_commit ON ledger.commit_id = updated_commit.commit_id
      LEFT JOIN {commit} created_commit ON ledger.created_commit_id = created_commit.commit_id
      LEFT JOIN {commit} updated_tree_commit ON ledger.updated_tree_commit_id = updated_tree_commit.commit_id
      ORDER BY ledger_name ASC
    """,
    rowMapper = LedgerMapper.class
  )
  Sql findAll();

  @TenantSql.Find(
    optional = false,
    sql = """
      SELECT ledger.*,
             updated_commit.created_at as updated_at,
             created_commit.created_at as created_at,
             updated_tree_commit.created_at as updated_tree_at
      FROM {ledger} ledger
      LEFT JOIN {commit} updated_commit ON ledger.commit_id = updated_commit.commit_id
      LEFT JOIN {commit} created_commit ON ledger.created_commit_id = created_commit.commit_id
      LEFT JOIN {commit} updated_tree_commit ON ledger.updated_tree_commit_id = updated_tree_commit.commit_id
      WHERE id = $1 or external_id = $1
    """,
    rowMapper = LedgerMapper.class
  )
  SqlTuple getById(String ledgerId);


  @TenantSql.InsertAll(
    sql = """
      INSERT INTO {ledger}
      (id, external_id, ledger_name, ledger_description, commit_id, created_commit_id, updated_tree_commit_id)
       VALUES($1, $2, $3, $4, $5, $6, $7)
    """,
    propsMapper = LedgerInsertMapper.class
  )
  SqlTupleList insertMany(List<Ledger> ledgers);

  @TenantSql.UpdateAll(
    sql = """
      UPDATE {ledger}
       SET external_id = $1, ledger_name = $2, ledger_description = $3, commit_id = $4, updated_tree_commit_id = $5
       WHERE id = $6
    """,
    propsMapper = LedgerUpdateMapper.class
  )
  SqlTupleList updateMany(List<Ledger> ledgers);

  // Mapper classes
  class LedgerMapper implements TenantSql.RowMapper<Ledger> {
    @Override
    public Ledger apply(Row row) {
      return ImmutableLedger.builder()
          .id(TableUtils.toStringUUID(row, "id"))
          .externalId(row.getString("external_id"))
          .name(row.getString("ledger_name"))
          .description(Optional.ofNullable(row.getString("ledger_description")))
          .commitId(TableUtils.toStringUUID(row, "commit_id"))
          .createdCommitId(TableUtils.toStringUUID(row, "created_commit_id"))
          .updatedTreeCommitId(TableUtils.toStringUUID(row, "updated_tree_commit_id"))
          .transitives(ImmutableLedgerTransitives.builder()
              .createdAt(row.getOffsetDateTime("created_at"))
              .updatedAt(row.getOffsetDateTime("updated_at"))
              .updatedTreeAt(row.getOffsetDateTime("updated_tree_at"))
              .build())
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
        TableUtils.toUuid(doc.getCommitId()),
        TableUtils.toUuid(doc.getCreatedCommitId()),
        TableUtils.toUuid(doc.getUpdatedTreeCommitId())
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
        TableUtils.toUuid(doc.getCommitId()),
        TableUtils.toUuid(doc.getUpdatedTreeCommitId()),
        TableUtils.toUuid(doc.getId())
      });
    }
  }
}