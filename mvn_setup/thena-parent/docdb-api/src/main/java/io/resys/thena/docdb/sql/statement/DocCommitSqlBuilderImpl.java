package io.resys.thena.docdb.sql.statement;

/*-
 * #%L
 * thena-docdb-pgsql
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
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

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import io.resys.thena.docdb.api.models.ThenaDocObject.DocCommit;
import io.resys.thena.docdb.spi.DbCollections;
import io.resys.thena.docdb.sql.ImmutableSql;
import io.resys.thena.docdb.sql.ImmutableSqlTuple;
import io.resys.thena.docdb.sql.ImmutableSqlTupleList;
import io.resys.thena.docdb.sql.SqlBuilder.DocCommitSqlBuilder;
import io.resys.thena.docdb.sql.SqlBuilder.Sql;
import io.resys.thena.docdb.sql.SqlBuilder.SqlTuple;
import io.resys.thena.docdb.sql.SqlBuilder.SqlTupleList;
import io.resys.thena.docdb.sql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DocCommitSqlBuilderImpl implements DocCommitSqlBuilder {
  private final DbCollections options;
 
  @Override
  public Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getDocCommits())
        .build())
        .build();
  }
  @Override
  public SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getDocCommits())
        .append(" WHERE id = $1")
        .append(" FETCH FIRST ROW ONLY")
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public SqlTuple insertOne(DocCommit commit) {
    final var message = getMessage(commit);
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getDocCommits())
        .append(" (id, datetime, author, message, branch_id, doc_id, parent) VALUES($1, $2, $3, $4, $5, $6, $7)")
        .build())
        .props(Tuple.from(Arrays.asList(
            commit.getId(), commit.getDateTime().toString(), commit.getAuthor(), message, 
            commit.getBranchId(), commit.getDocId(), commit.getParent().orElse(null))))
        .build();
  }
  

  @Override
  public SqlTupleList insertAll(Collection<DocCommit> commits) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getDocCommits())
        .append(" (id, datetime, author, message, branch_id, doc_id, parent) VALUES($1, $2, $3, $4, $5, $6, $7)")
        .build())
        .props(commits.stream().map(commit -> {
          final var message = getMessage(commit);
          return Tuple.from(Arrays.asList(
              commit.getId(), commit.getDateTime().toString(), commit.getAuthor(), message, 
              commit.getBranchId(), commit.getDocId(), commit.getParent().orElse(null)));
          
        }) .collect(Collectors.toList()))
        .build();
  }
  
  private String getMessage(DocCommit commit) {

    var message = commit.getMessage();
    if(commit.getMessage().length() > 254) {
      message = message.substring(0, 254);
    }
    return message;
  }
}
