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
import io.resys.thena.ledger.client.entities.CommitTree;
import io.resys.thena.ledger.client.entities.CommitTree.CommitTreeOperation;
import io.resys.thena.ledger.client.entities.ImmutableCommitTree;
import io.resys.thena.support.TableUtils;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
  name = "commit_tree",
  order = 2001,
  ddl = """
    CREATE TABLE IF NOT EXISTS {commit_tree}
    (
      id UUID PRIMARY KEY,
      commit_id UUID NOT NULL,
      operation_type VARCHAR(40),
      body_after JSONB,
      body_before JSONB
    );

    CREATE INDEX IF NOT EXISTS {commit_tree}_COMMIT_INDEX
      ON {commit_tree} (commit_id);
  """,
  constraints = """
    ALTER TABLE {commit_tree} ADD CONSTRAINT fk_commit_tree_commit 
      FOREIGN KEY (commit_id) REFERENCES {commit}(commit_id);
  """,
  drop = """
    DROP TABLE IF EXISTS {commit_tree} CASCADE;
  """
)
public interface CommitTreeTable {

  @TenantSql.FindAll(
    sql = """
      SELECT ct.*
      FROM {commit_tree} ct
      LEFT JOIN {commit} c ON ct.commit_id = c.commit_id
      ORDER BY c.created_at DESC
    """,
    rowMapper = CommitTreeMapper.class
  )
  Sql findAll();

  @TenantSql.FindAll(
    sql = """
      SELECT ct.*
      FROM {commit_tree} ct
      LEFT JOIN {commit} c ON ct.commit_id = c.commit_id
      WHERE c.ledger_id = $1
      ORDER BY c.created_at DESC
    """,
    rowMapper = CommitTreeMapper.class
  )
  SqlTuple findAllByLedgerId(String ledgerId);

  @TenantSql.FindAll(
    sql = """
      SELECT * FROM {commit_tree}
      WHERE commit_id = $1
    """,
    rowMapper = CommitTreeMapper.class
  )
  SqlTuple findAllByCommitId(String commitId);

  @TenantSql.Find(
    optional = false,
    sql = """
      SELECT * FROM {commit_tree}
      WHERE id = $1
    """,
    rowMapper = CommitTreeMapper.class
  )
  SqlTuple getById(String id);

  @TenantSql.InsertAll(
    sql = """
      INSERT INTO {commit_tree}
      (id, commit_id, operation_type, body_after, body_before)
       VALUES($1, $2, $3, $4, $5)
    """,
    propsMapper = CommitTreeInsertMapper.class
  )
  SqlTupleList insertMany(List<CommitTree> commitTrees);

  // Mapper classes
  class CommitTreeMapper implements TenantSql.RowMapper<CommitTree> {
    @Override
    public CommitTree apply(Row row) {
      final String operation_type = row.getString("operation_type");
      final JsonObject body_after = row.getJsonObject("body_after");
      final JsonObject body_before = row.getJsonObject("body_before");

      return ImmutableCommitTree.builder()
          .id(TableUtils.toStringUUID(row, "id"))
          .commitId(TableUtils.toStringUUID(row, "commit_id"))
          .operationType(CommitTreeOperation.valueOf(operation_type))
          .bodyAfter(Optional.ofNullable(body_after))
          .bodyBefore(Optional.ofNullable(body_before))
          .build();
    }
  }

  class CommitTreeInsertMapper implements TenantSql.PropsMapper<CommitTree> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(CommitTree doc) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        TableUtils.toUuid(doc.getId()),
        TableUtils.toUuid(doc.getCommitId()),
        doc.getOperationType().name(),
        doc.getBodyAfter().orElse(null),
        doc.getBodyBefore().orElse(null)
      });
    }
  }
}