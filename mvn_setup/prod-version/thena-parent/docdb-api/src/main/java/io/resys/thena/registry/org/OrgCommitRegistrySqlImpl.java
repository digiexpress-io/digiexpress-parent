package io.resys.thena.registry.org;

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
import java.util.function.Function;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.org.OrgCommit;
import io.resys.thena.api.registry.org.OrgCommitRegistry;
import io.resys.thena.datasource.ImmutableSql;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.ImmutableSqlTupleList;
import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.datasource.ThenaSqlClient;
import io.resys.thena.storesql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrgCommitRegistrySqlImpl implements OrgCommitRegistry {
  private final TenantTableNames options;
  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getOrgMembers())
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getOrgCommits()).ln()
        .append("  WHERE commit_id = $1").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple insertOne(OrgCommit doc) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getOrgCommits())
        .append(" (commit_id, parent_id, created_at, commit_log, commit_author, commit_message) VALUES($1, $2, $3, $4, $5, $6)").ln()
        .build())
        .props(Tuple.from(new Object[]{ doc.getCommitId(), doc.getParentId(), doc.getCreatedAt(), doc.getCommitLog(), doc.getCommitAuthor(), doc.getCommitMessage() }))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTupleList insertAll(Collection<OrgCommit> docs) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getOrgCommits())
        .append(" (commit_id, parent_id, created_at, commit_log, commit_author, commit_message) VALUES($1, $2, $3, $4, $5, $6)").ln()
        .build())
        .props(docs.stream()
            .map(doc -> Tuple.from(new Object[]{ doc.getCommitId(), doc.getParentId(), doc.getCreatedAt(), doc.getCommitLog(), doc.getCommitAuthor(), doc.getCommitMessage() }))
            .collect(Collectors.toList()))
        .build();
  }

  @Override
  public Function<Row, OrgCommit> defaultMapper() {
    throw new RuntimeException("Not implemented");
  }
  @Override
  public ThenaSqlClient.Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
        .append("CREATE TABLE ").append(options.getOrgCommits()).ln()
        .append("(").ln()
        .append("  commit_id VARCHAR(40) PRIMARY KEY,").ln()
        .append("  parent_id VARCHAR(40),").ln()
        .append("  created_at TIMESTAMP WITH TIME ZONE NOT NULL,").ln()
        .append("  commit_log TEXT NOT NULL,").ln()
        
        .append("  commit_author VARCHAR(255) NOT NULL,").ln()
        .append("  commit_message VARCHAR(255) NOT NULL").ln()
        .append(");").ln().ln()
        
        


        // parent id, references self
        .append("ALTER TABLE ").append(options.getOrgCommits()).ln()
        .append("  ADD CONSTRAINT ").append(options.getOrgCommits()).append("_PARENT_FK").ln()
        .append("  FOREIGN KEY (parent_id)").ln()
        .append("  REFERENCES ").append(options.getOrgCommits()).append(" (commit_id);").ln()

        .append("CREATE INDEX ").append(options.getOrgCommits()).append("_PARENT_INDEX")
        .append(" ON ").append(options.getOrgCommits()).append(" (parent_id);").ln()


        .build()).build();
  }
  @Override
  public ThenaSqlClient.Sql createConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
        .ln().append("--- constraints for").append(options.getOrgCommits()).ln()
        
        .append(createOrgCommitFk(options.getOrgCommitTrees())).ln()
        .append(createOrgCommitFk(options.getOrgMembers())).ln()
        .append(createOrgCommitFk(options.getOrgMemberRights())).ln()        
        .append(createOrgCommitFk(options.getOrgMemberships())).ln()        
        .append(createOrgCommitFk(options.getOrgParties())).ln()
        .append(createOrgCommitFk(options.getOrgPartyRights())).ln()
        .append(createOrgCommitFk(options.getOrgRights())).ln()
        
        .append(createOrgCommitFk(options.getOrgMembers(), "created_commit_id")).ln()
        .append(createOrgCommitFk(options.getOrgParties(), "created_commit_id")).ln()
        .append(createOrgCommitFk(options.getOrgRights(), "created_commit_id")).ln()
        
        .build()).build();
  }
  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getOrgCommits()).append(";").ln()
        .build()).build();
  }
  
  
  private String createOrgCommitFk(String tableNameThatPointToCommits) {
    return  new SqlStatement().ln()
        .append("ALTER TABLE ").append(tableNameThatPointToCommits).ln()
        .append("  ADD CONSTRAINT ").append(tableNameThatPointToCommits).append("_COMMIT_FK").ln()
        .append("  FOREIGN KEY (commit_id)").ln()
        .append("  REFERENCES ").append(options.getOrgCommits()).append(" (commit_id);").ln().ln()
        .build();
  }
  private String createOrgCommitFk(String tableNameThatPointToCommits, String column) {
    return new SqlStatement().ln()
        .append("ALTER TABLE ").append(tableNameThatPointToCommits).ln()
        .append("  ADD CONSTRAINT ").append(tableNameThatPointToCommits).append("_").append(column.toUpperCase()).append("_FK").ln()
        .append("  FOREIGN KEY (" + column + ")").ln()
        .append("  REFERENCES ").append(options.getOrgCommits()).append(" (commit_id);").ln().ln()
        .build();
  }
}
