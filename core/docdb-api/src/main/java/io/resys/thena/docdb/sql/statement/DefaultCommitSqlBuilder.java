package io.resys.thena.docdb.sql.statement;

import java.util.ArrayList;

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
import java.util.List;

import io.resys.thena.docdb.api.models.Objects.Commit;
import io.resys.thena.docdb.spi.ClientCollections;
import io.resys.thena.docdb.spi.ClientQuery.LockCriteria;
import io.resys.thena.docdb.sql.ImmutableSql;
import io.resys.thena.docdb.sql.ImmutableSqlTuple;
import io.resys.thena.docdb.sql.SqlBuilder.CommitSqlBuilder;
import io.resys.thena.docdb.sql.SqlBuilder.Sql;
import io.resys.thena.docdb.sql.SqlBuilder.SqlTuple;
import io.resys.thena.docdb.sql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultCommitSqlBuilder implements CommitSqlBuilder {
  private final ClientCollections options;
 
  @Override
  public Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getCommits())
        .build())
        .build();
  }
  @Override
  public SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getCommits())
        .append(" WHERE id = $1")
        .append(" FETCH FIRST ROW ONLY")
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public SqlTuple insertOne(Commit commit) {
    
    var message = commit.getMessage();
    if(commit.getMessage().length() > 100) {
      message = message.substring(0, 100);
    }
    
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getCommits())
        .append(" (id, datetime, author, message, tree, parent, merge) VALUES($1, $2, $3, $4, $5, $6, $7)")
        .build())
        .props(Tuple.from(Arrays.asList(
            commit.getId(), commit.getDateTime().toString(), commit.getAuthor(), message, 
            commit.getTree(), commit.getParent().orElse(null), commit.getMerge().orElse(null))))
        .build();
  }
  @Override
  public SqlTuple getLock(LockCriteria crit) {
    final var commitId = crit.getCommitId(); 
    final var headName = crit.getHeadName();
    List<Object> props = new ArrayList<>();
    props.add(headName);

    StringBuilder where = new StringBuilder();
    if(commitId != null) {
      where.append(" WHERE commits.id = $2 ");
      props.add(commitId);
    }
    
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT ")
        .append("  refs.name as ref_name,").ln()
        .append("  blobs.value as blob_value,").ln()
        .append("  treeValues.name as blob_name,").ln()
        .append("  treeValues.blob as blob_id,").ln()
        .append("  treeValues.tree as tree_id,").ln()
        .append("  commits.author as author,").ln()
        .append("  commits.datetime as datetime,").ln()
        .append("  commits.message as message,").ln()
        .append("  commits.merge as merge,").ln()
        .append("  commits.parent as commit_parent,").ln()
        .append("  commits.id as commit_id").ln()
        .append(" FROM (SELECT * FROM ").append(options.getRefs()).append(" WHERE name = $1 FOR UPDATE NOWAIT) as refs").ln()
        .append("  JOIN ").append(options.getCommits()).append(" as commits ON(commits.id = refs.commit)").ln()
        .append("  JOIN ").append(options.getTrees()).append(" as trees ON(trees.id = commits.tree)").ln()
        .append("  JOIN ").append(options.getTreeItems()).append(" as treeValues ON(trees.id = treeValues.tree)").ln()
        .append("  JOIN ").append(options.getBlobs()).append(" as blobs ON(blobs.id = treeValues.blob)").ln()
        .append(where.toString())
        .build())
        .props(Tuple.from(props))
        .build();
  }  
  
}
