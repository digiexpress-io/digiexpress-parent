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

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.fs.FsCommit;
import io.resys.thena.api.entities.fs.ImmutableFsCommit;
import io.resys.thena.api.registry.fs.FsCommitRegistry;
import io.resys.thena.api.registry.fs.FsDirentFilter;
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
public class FsCommitRegistrySqlImpl implements FsCommitRegistry {
  private final TenantTableNames options;
  
  
  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getFsCommit()).append(";").ln()
        .build()).build();
  }

  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getFsCommit())
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getFsCommit()).ln()
        .append("  WHERE (id = $1)").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public SqlTuple findAllByDirentId(String direntId) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getFsCommit()).ln()
        .append("  WHERE dirent_id = $1").ln() 
        .build())
        .props(Tuple.of(direntId))
        .build();
  }

  @Override
  public SqlTupleList insertAll(Collection<FsCommit> commits) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getFsCommit()).ln()
        .append(" (commit_id,").ln()
        .append("  parent_id,").ln()
        .append("  dirent_id,").ln()
        .append("  created_at,").ln()
        .append("  commit_log, ").ln()
        .append("  commit_author, ").ln()
        .append("  commit_message)").ln()
        
        .append(" VALUES($1, $2, $3, $4, $5, $6, $7)").ln()
        .build())
        .props(commits.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getCommitId(), 
                doc.getParentCommitId(),
                doc.getDirentId(),
                doc.getCreatedAt(),
                doc.getCommitLog(),
                doc.getCommitAuthor(),
                doc.getCommitMessage(),
             }))
            .collect(Collectors.toList()))
        .build();
  }
  @Override
  public Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getFsCommit()).ln()
    .append("(").ln()
    .append("  commit_id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  parent_id VARCHAR(40),").ln()
    .append("  dirent_id VARCHAR(40),").ln()
    .append("  created_at TIMESTAMP WITH TIME ZONE NOT NULL,").ln()
    .append("  commit_log TEXT NOT NULL,").ln()
    
    .append("  commit_author VARCHAR(255) NOT NULL,").ln()
    .append("  commit_message VARCHAR(255) NOT NULL").ln()
    
    .append(");").ln()
    

    .append("CREATE INDEX ").append(options.getFsCommit()).append("_PARENT_INDEX")
    .append(" ON ").append(options.getFsCommit()).append(" (parent_id);").ln()

    .append("CREATE INDEX ").append(options.getFsCommit()).append("_DIRENT_INDEX")
    .append(" ON ").append(options.getFsCommit()).append(" (dirent_id);").ln()
    
    .append("CREATE INDEX ").append(options.getFsCommit()).append("_AUTH_INDEX")
    .append(" ON ").append(options.getFsCommit()).append(" (commit_author);").ln()
    

    // parent id, references self
    .append("ALTER TABLE ").append(options.getFsCommit()).ln()
    .append("  ADD CONSTRAINT ").append(options.getFsCommit()).append("_PARENT_FK").ln()
    .append("  FOREIGN KEY (parent_id)").ln()
    .append("  REFERENCES ").append(options.getFsCommit()).append(" (commit_id);").ln()

    
    .build()).build();
  }


  @Override
  public Sql createConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
      .ln().append("--- constraints for").append(options.getFsCommit()).ln()
     .append(createFsCommitFk(options.getFsDirentAssignment()))
     .append(createFsCommitFk(options.getFsCommitTree()))
          
     .append(createFsCommitFk(options.getFsDirentData()))
     .append(createFsCommitFk(options.getFsDirentLabel()))
     .append(createFsCommitFk(options.getFsDirentLink()))
     .append(createFsCommitFk(options.getFsDirentRemark()))
     .append(createFsCommitFk(options.getFsDirent()))
     
     
     .append(createFsCommitFk(options.getFsDirent(), "created_commit_id"))
     .append(createFsCommitFk(options.getFsDirent(), "updated_tree_commit_id"))
     .append(createFsCommitFk(options.getFsDirentLink(), "created_commit_id"))

    .build()).build();
  }


  @Override
  public Function<Row, FsCommit> defaultMapper() {
    return (row) -> {
      
      return ImmutableFsCommit.builder()
          .commitId(row.getString("commit_id"))
          .direntId(row.getString("dirent_id"))

          .parentCommitId(row.getString("parent_id"))
          .createdAt(row.getOffsetDateTime("created_at"))
          .commitLog(row.getString("commit_log"))
          .commitAuthor(row.getString("commit_author"))
          .commitMessage(row.getString("commit_message"))
          
          .build();
    };
  }
  private String createFsCommitFk(String tableNameThatPointToCommits, String column) {
    return new SqlStatement().ln()
        .append("ALTER TABLE ").append(tableNameThatPointToCommits).ln()
        .append("  ADD CONSTRAINT ").append(tableNameThatPointToCommits).append("_").append(column.toUpperCase()).append("_FK").ln()
        .append("  FOREIGN KEY (" + column + ")").ln()
        .append("  REFERENCES ").append(options.getFsCommit()).append(" (commit_id);").ln().ln()
        .build();
  }
  private String createFsCommitFk(String tableNameThatPointToCommits) {
    return new SqlStatement().ln()
        .append("ALTER TABLE ").append(tableNameThatPointToCommits).ln()
        .append("  ADD CONSTRAINT ").append(tableNameThatPointToCommits).append("_COMMIT_FK").ln()
        .append("  FOREIGN KEY (commit_id)").ln()
        .append("  REFERENCES ").append(options.getFsCommit()).append(" (commit_id);").ln().ln()
        .build();
  }

  @Override
  public SqlTuple findAllByIds(Collection<String> id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getFsCommit()).ln()
        .append("  WHERE (id = ANY($1))").ln() 
        .build())
        .props(Tuple.from(new ArrayList<>(id)))
        .build();
  }

  @Override
  public SqlTuple findAll(FsDirentFilter filter) {
    final var where = new FsDirentSqlFilterBuilder(options).where(filter);
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT commits.* ").ln()
        .append("  FROM ").append(options.getFsCommit()).append(" as commits").ln()
        
        .append("  LEFT JOIN ").append(options.getFsDirent()).append(" as dirent")
        .append("  ON(commits.dirent_id = dirent.id)")
        .append(where.getValue()) 
        .build())
        .props(where.getProps())
        .build();
  }
}
