package io.resys.thena.docdb.sql.statement;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
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

import io.resys.thena.docdb.api.models.ThenaObject.Branch;
import io.resys.thena.docdb.api.models.ThenaObject.Commit;
import io.resys.thena.docdb.spi.ClientCollections;
import io.resys.thena.docdb.sql.ImmutableSql;
import io.resys.thena.docdb.sql.ImmutableSqlTuple;
import io.resys.thena.docdb.sql.SqlBuilder.RefSqlBuilder;
import io.resys.thena.docdb.sql.SqlBuilder.Sql;
import io.resys.thena.docdb.sql.SqlBuilder.SqlTuple;
import io.resys.thena.docdb.sql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultRefSqlBuilder implements RefSqlBuilder {
  private final ClientCollections options;

  @Override
  public Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getRefs())
        .build())
        .build();
  }

  @Override
  public SqlTuple getByName(String name) {
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
  public SqlTuple getByNameOrCommit(String refNameOrCommit) {
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
  public Sql getFirst() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getRefs())
        .append(" FETCH FIRST ROW ONLY")
        .build())
        .build();
  }

  @Override
  public SqlTuple insertOne(Branch ref) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getRefs())
        .append(" (name, commit) VALUES($1, $2)")
        .build())
        .props(Tuple.of(ref.getName(), ref.getCommit()))
        .build();
  }

  @Override
  public SqlTuple updateOne(Branch ref, Commit commit) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("UPDATE ").append(options.getRefs())
        .append(" SET commit = $1")
        .append(" WHERE name = $2 AND commit = $3")
        .build())
        .props(Tuple.of(ref.getCommit(), ref.getName(), commit.getParent().get()))
        .build();
  }
  
}
