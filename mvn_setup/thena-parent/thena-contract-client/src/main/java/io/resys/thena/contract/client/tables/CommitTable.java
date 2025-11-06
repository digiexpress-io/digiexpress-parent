package io.resys.thena.contract.client.tables;

/*-
 * #%L
 * thena-contract-client
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
import io.resys.thena.contract.client.entities.Commit;
import io.resys.thena.contract.client.entities.ImmutableCommit;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
  name = "commit",
  order = 100,
  ddl = """
    CREATE TABLE IF NOT EXISTS {commit}
    (
      commit_id UUID PRIMARY KEY,
      parent_id UUID,
      contract_id UUID,
      created_at TIMESTAMP WITH TIME ZONE NOT NULL,
      commit_log TEXT NOT NULL,
      commit_author VARCHAR(255) NOT NULL,
      commit_message VARCHAR(255) NOT NULL
    );

    CREATE INDEX IF NOT EXISTS {commit}_PARENT_INDEX
      ON {commit} (parent_id);
    CREATE INDEX IF NOT EXISTS {commit}_CONTRACT_INDEX
      ON {commit} (contract_id);
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
      WHERE contract_id = $1
      ORDER BY created_at DESC
    """,
    rowMapper = CommitMapper.class
  )
  SqlTuple findAllByContractId(String contractId);

  @TenantSql.FindAll(
    sql = """
      SELECT commit.*
      FROM {commit} commit
      LEFT JOIN {contract} contract ON commit.contract_id = contract.id
      ORDER BY commit.created_at DESC
    """,
    rowMapper = CommitMapper.class,
    sqlBuilder = ContractTableFilter.SQL.class
  )
  SqlTuple findAllByFilter(ContractTableFilter filter);

  @TenantSql.Find(
    optional = false,
    sql = """
      SELECT * FROM {commit}
      WHERE commit_id = $1
    """,
    rowMapper = CommitMapper.class
  )
  SqlTuple getById(String commitId);

  @TenantSql.InsertAll(
    sql = """
      INSERT INTO {commit}
      (commit_id, parent_id, contract_id, created_at, commit_log, commit_author, commit_message)
       VALUES($1, $2, $3, $4, $5, $6, $7)
    """,
    propsMapper = CommitInsertMapper.class
  )
  SqlTupleList insertMany(List<Commit> commits);

  @TenantSql.UpdateAll(
    sql = """
      UPDATE {commit}
       SET parent_id = $1, contract_id = $2, created_at = $3, commit_log = $4, commit_author = $5, commit_message = $6
       WHERE commit_id = $7
    """,
    propsMapper = CommitUpdateMapper.class
  )
  SqlTupleList updateMany(List<Commit> commits);

  // Mapper classes
  class CommitMapper implements TenantSql.RowMapper<Commit> {
    @Override
    public Commit apply(Row row) {
      final String parent_id = row.getString("parent_id");
      final String contract_id = row.getString("contract_id");

      return ImmutableCommit.builder()
          .commitId(row.getString("commit_id"))
          .parentCommitId(Optional.ofNullable(parent_id))
          .contractId(Optional.ofNullable(contract_id))
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
        TableUtils.toUuid(doc.getCommitId()),
        doc.getParentCommitId().map(TableUtils::toUuid).orElse(null),
        doc.getContractId().map(TableUtils::toUuid).orElse(null),
        doc.getCreatedAt(),
        doc.getCommitLog(),
        doc.getCommitAuthor(),
        doc.getCommitMessage()
      });
    }
  }

  class CommitUpdateMapper implements TenantSql.PropsMapper<Commit> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Commit doc) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        doc.getParentCommitId().map(TableUtils::toUuid).orElse(null),
        doc.getContractId().map(TableUtils::toUuid).orElse(null),
        doc.getCreatedAt(),
        doc.getCommitLog(),
        doc.getCommitAuthor(),
        doc.getCommitMessage(),
        TableUtils.toUuid(doc.getCommitId())
      });
    }
  }
}