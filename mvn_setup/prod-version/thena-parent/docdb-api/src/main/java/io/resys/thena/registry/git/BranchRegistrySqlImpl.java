package io.resys.thena.registry.git;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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

import java.util.function.Function;

import io.resys.thena.api.entities.git.Branch;
import io.resys.thena.api.entities.git.Commit;
import io.resys.thena.api.entities.git.ImmutableBranch;
import io.resys.thena.api.registry.git.BranchRegistry;
import io.resys.thena.datasource.ImmutableSql;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.datasource.ThenaSqlClient;
import io.resys.thena.storesql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BranchRegistrySqlImpl implements BranchRegistry {
  private final TenantTableNames options;

  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getRefs())
        .build())
        .build();
  }

  @Override
  public ThenaSqlClient.SqlTuple getByName(String name) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getRefs())
        .append(" WHERE name = $1")
        .append(" FETCH FIRST ROW ONLY")
        .build())
        .props(Tuple.of(name))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getByNameOrCommit(String refNameOrCommit) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getRefs())
        .append(" WHERE name = $1 OR commit = $1")
        .append(" FETCH FIRST ROW ONLY")
        .build())
        .props(Tuple.of(refNameOrCommit))
        .build();
  }

  @Override
  public ThenaSqlClient.Sql getFirst() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getRefs())
        .append(" FETCH FIRST ROW ONLY")
        .build())
        .build();
  }

  @Override
  public ThenaSqlClient.SqlTuple insertOne(Branch ref) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getRefs())
        .append(" (name, commit) VALUES($1, $2)")
        .build())
        .props(Tuple.of(ref.getName(), ref.getCommit()))
        .build();
  }

  @Override
  public ThenaSqlClient.SqlTuple updateOne(Branch ref, Commit commit) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("UPDATE ").append(options.getRefs())
        .append(" SET commit = $1")
        .append(" WHERE name = $2 AND commit = $3")
        .build())
        .props(Tuple.of(ref.getCommit(), ref.getName(), commit.getParent().get()))
        .build();
  }

  @Override
  public ThenaSqlClient.SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getRefs())
        .append(" WHERE id = $1 OR name = $1")
        .append(" FETCH FIRST ROW ONLY")
        .build())
        .props(Tuple.of(id))
        .build();
  }

  @Override
  public Function<Row, Branch> defaultMapper() {
    return BranchRegistrySqlImpl::ref;
  }
  private static Branch ref(Row row) {
    return ImmutableBranch.builder()
        .name(row.getString("name"))
        .commit(row.getString("commit"))
        .build();
  }
  @Override
  public ThenaSqlClient.Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getRefs()).ln()
    .append("(").ln()
    .append("  name VARCHAR(100) PRIMARY KEY,").ln()
    .append("  commit VARCHAR(40) NOT NULL").ln()
    .append(");").ln()
    .build()).build();
  }

  @Override
  public ThenaSqlClient.Sql createConstraints() {
    return ImmutableSql.builder()
        .value(new SqlStatement().ln()
        .append("ALTER TABLE ").append(options.getRefs()).ln()
        .append("  ADD CONSTRAINT ").append(options.getRefs()).append("_REF_COMMIT_FK").ln()
        .append("  FOREIGN KEY (commit)").ln()
        .append("  REFERENCES ").append(options.getCommits()).append(" (id);").ln()
        .build())
        .build();
  }

  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getRefs()).append(";").ln()
        .build()).build();
  }
}
