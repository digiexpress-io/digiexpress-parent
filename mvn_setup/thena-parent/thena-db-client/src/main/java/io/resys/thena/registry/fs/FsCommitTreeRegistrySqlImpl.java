package io.resys.thena.registry.fs;

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

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.fs.FsCommitTree;
import io.resys.thena.api.entities.fs.FsCommitTree.FsCommitTreeOperation;
import io.resys.thena.api.entities.fs.ImmutableFsCommitTree;
import io.resys.thena.api.registry.fs.FsCommitTreeRegistry;
import io.resys.thena.datasource.ImmutableSql;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.ImmutableSqlTupleList;
import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.datasource.ThenaSqlClient;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.resys.thena.storesql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class FsCommitTreeRegistrySqlImpl implements FsCommitTreeRegistry {
  private final TenantTableNames options;
  
  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getFsCommitTree()).append(";").ln()
        .build()).build();
  }
  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT tree.*, commit.dirent_id as dirent_id").ln()
        .append(" FROM ").append(options.getFsCommitTree()).append(" as tree ").ln()
        .append(" RIGHT JOIN ").append(options.getFsCommit()).append(" as commit").ln()
        .append(" ON(tree.commit_id = commit.commit_id)").ln()
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT tree.*, commit.dirent_id as dirent_id").ln()
        .append(" FROM ").append(options.getFsCommitTree()).append(" as tree ").ln()
        .append(" RIGHT JOIN ").append(options.getFsCommit()).append(" as commit").ln()
        .append(" WHERE (tree.id = $1)").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public SqlTuple findAllByCommitIds(List<String> commitId) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT tree.*, commit.dirent_id as dirent_id").ln()
        .append(" FROM ").append(options.getFsCommitTree()).append(" as tree ").ln()
        .append(" RIGHT JOIN ").append(options.getFsCommit()).append(" as commit").ln()
        .append(" WHERE (tree.commit_id = ANY($1))").ln() 
        .build())
        .props(Tuple.of(commitId.toArray()))
        .build();    
  }
  @Override
  public SqlTuple findAllByDirentIdAndCommitId(String direntId, String commitId) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT tree.*, commit.dirent_id as dirent_id").ln()
        .append("  FROM ").append(options.getFsCommitTree()).append(" AS tree").ln()
        .append("  RIGHT JOIN ").append(options.getFsCommit()).append(" AS commit ON(commit.commit_id = tree.commit_id)").ln() 
        .append("  WHERE tree.commit_id = $1").ln() 
        .append("  AND commit.dirent_id = $2").ln() 
        .build())
        .props(Tuple.of(commitId, direntId))
        .build();    
  }
  
  @Override
  public SqlTuple findAllByDirentId(String direntId) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT tree.*, commit.dirent_id as dirent_id").ln()
        .append("  FROM ").append(options.getFsCommitTree()).append(" AS tree").ln()
        .append("  LEFT JOIN ").append(options.getFsCommit()).append(" AS commit ON(commit.commit_id = tree.commit_id)").ln() 
        .append("  WHERE commit.dirent_id = $1").ln()
        .build())
        .props(Tuple.of(direntId))
        .build();    
  }
  
  
  @Override
  public Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getFsCommitTree()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    
    .append("  operation_type VARCHAR(40),").ln()
    .append("  body_after JSONB,").ln()
    .append("  body_before JSONB").ln()
    .append(");").ln()
    
    .append("CREATE INDEX ").append(options.getFsCommitTree()).append("_COMMIT_INDEX")
    .append(" ON ").append(options.getFsCommitTree()).append(" (commit_id);").ln()

    
    .build()).build();
  }
  @Override
  public SqlTupleList insertAll(Collection<FsCommitTree> commits) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getFsCommitTree()).ln()
        .append(" (id,").ln()
        .append("  commit_id,").ln()
        .append("  operation_type,").ln()
        .append("  body_after,").ln()
        .append("  body_before)").ln()
        
        .append(" VALUES($1, $2, $3, $4, $5)").ln()
        .build())
        .props(commits.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getId(), 
                doc.getCommitId(),
                doc.getOperationType().name(),
                doc.getBodyAfter(),
                doc.getBodyBefore(),
             }))
            .collect(Collectors.toList()))
        .build();
  }

  @Override
  public Sql createConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
    .ln().append("--- constraints for").append(options.getFsCommitTree()).ln()
    .build()).build();
  }

  @Override
  public Function<Row, FsCommitTree> defaultMapper() {
    return (row) -> {
      
      return ImmutableFsCommitTree.builder()
          .id(row.getString("id"))
          .commitId(row.getString("commit_id"))
          .direntId(row.getString("dirent_id"))
          .operationType(FsCommitTreeOperation.valueOf(row.getString("operation_type")))
          .bodyBefore(row.getJsonObject("body_before"))
          .bodyAfter(row.getJsonObject("body_after"))
          .build();
    };
  }
}
