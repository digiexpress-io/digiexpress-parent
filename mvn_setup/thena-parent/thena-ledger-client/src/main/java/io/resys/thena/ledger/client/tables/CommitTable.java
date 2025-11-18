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
import io.resys.thena.ledger.client.entities.Commit;
import io.resys.thena.ledger.client.entities.ImmutableCommit;
import io.resys.thena.support.TableUtils;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
  name = "commit",
  order = 2000,
  ddl = """
    CREATE TABLE IF NOT EXISTS {commit}
    (
      commit_id UUID PRIMARY KEY,
      parent_id UUID,
      ledger_id UUID,
      created_at TIMESTAMP WITH TIME ZONE NOT NULL,
      commit_log TEXT NOT NULL,
      commit_author VARCHAR(255) NOT NULL,
      commit_message VARCHAR(255) NOT NULL
    );

    CREATE INDEX IF NOT EXISTS {commit}_PARENT_INDEX
      ON {commit} (parent_id);
    CREATE INDEX IF NOT EXISTS {commit}_LEDGER_INDEX
      ON {commit} (ledger_id);
    CREATE INDEX IF NOT EXISTS {commit}_AUTH_INDEX
      ON {commit} (commit_author);
  """,
  constraints = """
    ALTER TABLE {commit} ADD CONSTRAINT fk_commit_parent 
      FOREIGN KEY (parent_id) REFERENCES {commit}(commit_id);
  """,
  drop = """
    DROP TABLE {commit};
  """
)
public interface CommitTable {

  @TenantSql.FindAll(
    sql = """
      SELECT * FROM {commit}
      ORDER BY created_at DESC
    """,
    rowMapper = CommitMapper.class
  )
  Sql findAll();

  @TenantSql.FindAll(
    sql = """
      SELECT * FROM {commit}
      WHERE ledger_id = $1
      ORDER BY created_at DESC
    """,
    rowMapper = CommitMapper.class
  )
  SqlTuple findAllByLedgerId(String ledgerId);

  @TenantSql.Find(
    optional = false,
    sql = """
      SELECT * FROM {commit}
      WHERE commit_id = $1
    """,
    rowMapper = CommitMapper.class
  )
  SqlTuple getByCommitId(String commitId);

  @TenantSql.InsertAll(
    sql = """
      INSERT INTO {commit}
      (commit_id, parent_id, ledger_id, created_at, commit_log, commit_author, commit_message)
       VALUES($1, $2, $3, $4, $5, $6, $7)
    """,
    propsMapper = CommitInsertMapper.class
  )
  SqlTupleList insertMany(List<Commit> commits);

  // Mapper classes
  class CommitMapper implements TenantSql.RowMapper<Commit> {
    @Override
    public Commit apply(Row row) {
      return ImmutableCommit.builder()
          .id(TableUtils.toStringUUID(row, "commit_id"))
          .parentId(Optional.ofNullable(TableUtils.toStringUUID(row, "parent_id")))
          .ledgerId(Optional.ofNullable(TableUtils.toStringUUID(row, "ledger_id")))
          .createdAt(row.getOffsetDateTime("created_at"))
          .commitLog(row.getString("commit_log"))
          .commitAuthor(row.getString("commit_author"))
          .commitMessage(row.getString("commit_message"))
          .build();
    }
  }

  class CommitInsertMapper implements TenantSql.PropsMapper<Commit> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Commit doc) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        TableUtils.toUuid(doc.getId()),
        doc.getParentId().map(TableUtils::toUuid).orElse(null),
        doc.getLedgerId().map(TableUtils::toUuid).orElse(null),
        doc.getCreatedAt(),
        doc.getCommitLog(),
        doc.getCommitAuthor(),
        doc.getCommitMessage()
      });
    }
  }
}