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
import java.util.function.Function;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.fs.FsDirentAssignment;
import io.resys.thena.api.entities.fs.ImmutableFsDirentAssignment;
import io.resys.thena.api.registry.fs.FsDirentAssignmentRegistry;
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
public class FsDirentAssignmentRegistrySqlImpl implements FsDirentAssignmentRegistry {
  private final TenantTableNames options;
  
  
  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getFsDirentAssignment()).append(";").ln()
        .build()).build();
  }


  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getFsDirentAssignment())
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getFsDirentAssignment()).ln()
        .append("  WHERE (id = $1)").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  
  @Override
  public SqlTuple findAll(FsDirentFilter filter) {
    final var where = new FsDirentSqlFilterBuilder(options).where(filter);
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT assignment.* ").ln()
        .append("  FROM ").append(options.getFsDirentAssignment()).append(" as assignment").ln()
        
        .append("  LEFT JOIN ").append(options.getFsDirent()).append(" as dirent")
        .append("  ON(assignment.dirent_id = dirent.id)")
        .append(where.getValue())
        .build())
        .props(where.getProps())
        .build();
  }

  @Override
  public SqlTupleList deleteAll(Collection<FsDirentAssignment> assignments) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("DELETE FROM ").append(options.getFsDirentAssignment())
        .append(" WHERE id = $1")
        .build())
        .props(assignments.stream()
            .map(doc -> Tuple.from(new Object[]{doc.getId()}))
            .collect(Collectors.toList()))
        .build();
  }


  @Override
  public SqlTupleList insertAll(Collection<FsDirentAssignment> asssignments) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getFsDirentAssignment()).ln()
        .append(" (id,").ln()
        .append("  commit_id,").ln()
        .append("  dirent_id,").ln()
        .append("  assignee, ").ln()
        .append("  assignment_type,").ln()
        .append("  assignee_contact)").ln()
        
        .append(" VALUES($1, $2, $3, $4, $5, $6)").ln()
        .build())
        .props(asssignments.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getId(), 
                doc.getCommitId(),
                doc.getDirentId(),
                doc.getAssignee(),
                doc.getAssignmentType(),
                doc.getAssigneeContact()
             }))
            .collect(Collectors.toList()))
        .build();
  }

  @Override
  public Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getFsDirentAssignment()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    
    .append("  dirent_id VARCHAR(40) NOT NULL,").ln()
    .append("  assignee VARCHAR(255) NOT NULL,").ln()
    .append("  assignment_type VARCHAR(100) NOT NULL,").ln()
    .append("  assignee_contact TEXT,").ln()
    
    .append("  UNIQUE NULLS NOT DISTINCT(dirent_id, assignee, assignment_type)").ln()
    
    .append(");").ln()
    
    
    .append("CREATE INDEX ").append(options.getFsDirentAssignment()).append("_DIRENT_INDEX")
    .append(" ON ").append(options.getFsDirentAssignment()).append(" (dirent_id);").ln()
    
    
    .build()).build();
  }


  @Override
  public Sql createConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
    .ln().append("--- constraints for").append(options.getFsDirentAssignment()).ln()
    
    .append("ALTER TABLE ").append(options.getFsDirentAssignment()).ln()
    .append("  ADD CONSTRAINT ").append(options.getFsDirentAssignment()).append("_DIRENT_FK").ln()
    .append("  FOREIGN KEY (dirent_id)").ln()
    .append("  REFERENCES ").append(options.getFsDirent()).append(" (id);").ln().ln()
    

    .build()).build();
  }
  @Override
  public Function<Row, FsDirentAssignment> defaultMapper() {
    return (row) -> {      
      
      return ImmutableFsDirentAssignment.builder()
          .id(row.getString("id"))
          .commitId(row.getString("commit_id"))
          .direntId(row.getString("dirent_id"))
          
          .assigneeContact(row.getString("assignee_contact"))
          .assignee(row.getString("assignee"))
          .assignmentType(row.getString("assignment_type"))
          .build();
    };
  }

}
