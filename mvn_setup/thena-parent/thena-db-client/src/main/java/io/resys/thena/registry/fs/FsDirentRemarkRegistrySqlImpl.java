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

import io.resys.thena.api.entities.fs.FsDirentRemark;
import io.resys.thena.api.entities.fs.ImmutableFsDirentRemark;
import io.resys.thena.api.registry.fs.FsDirentFilter;
import io.resys.thena.api.registry.fs.FsDirentRemarkRegistry;
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
public class FsDirentRemarkRegistrySqlImpl implements FsDirentRemarkRegistry {
  private final TenantTableNames options;
    
  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getFsDirentRemark()).append(";").ln()
        .build()).build();
  }

  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT remark.*,").ln()
        .append(" updated_commit.created_at       as updated_at,").ln()
        .append(" created_commit.created_at       as created_at,").ln()
        .append(" created_commit.commit_author    as created_by").ln()

        .append(" FROM ").append(options.getFsDirentRemark()).append(" as remark ")
        
        .append(" LEFT JOIN ").append(options.getFsCommit()).append(" as updated_commit").ln()
        .append(" ON(updated_commit.commit_id = remark.commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getFsCommit()).append(" as created_commit").ln()
        .append(" ON(created_commit.commit_id = remark.created_commit_id)").ln()

        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT remark.*,").ln()
        .append(" updated_commit.created_at       as updated_at,").ln()
        .append(" created_commit.created_at       as created_at,").ln()
        .append(" created_commit.commit_author    as created_by").ln()
        
        .append(" FROM ").append(options.getFsDirentRemark()).append(" as remark ")
        
        .append(" LEFT JOIN ").append(options.getFsCommit()).append(" as updated_commit").ln()
        .append(" ON(updated_commit.commit_id = remark.commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getFsCommit()).append(" as created_commit").ln()
        .append(" ON(created_commit.commit_id = remark.created_commit_id)").ln()

        .append(" WHERE remark.id = $1").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public SqlTuple findAll(FsDirentFilter filter) {
    final var where = new FsDirentSqlFilterBuilder(options).where(filter);
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT remark.*,").ln()
        .append(" updated_commit.created_at       as updated_at,").ln()
        .append(" created_commit.created_at       as created_at,").ln()
        .append(" created_commit.commit_author    as created_by").ln()

        .append(" FROM ").append(options.getFsDirentRemark()).append(" as remark ").ln()
        
        .append(" LEFT JOIN ").append(options.getFsCommit()).append(" as updated_commit").ln()
        .append(" ON(updated_commit.commit_id = remark.commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getFsCommit()).append(" as created_commit").ln()
        .append(" ON(created_commit.commit_id = remark.created_commit_id)").ln()

        .append("  LEFT JOIN ").append(options.getFsDirent()).append(" as dirent")
        .append("  ON(remark.dirent_id = dirent.id)").ln()
        .append(where.getValue()) 
        .build())
        .props(where.getProps())
        .build();
  }
  @Override
  public SqlTupleList insertAll(Collection<FsDirentRemark> remarks) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getFsDirentRemark()).ln()
        .append(" (id,").ln()
        .append("  commit_id,").ln()
        .append("  created_commit_id,").ln()
        .append("  parent_id,").ln()
        
        .append("  dirent_id,").ln()
        
        .append("  reporter_id,").ln()
        .append("  remark_status,").ln()
        .append("  remark_type,").ln()
        .append("  remark_source,").ln()
        .append("  remark_text)").ln()
        
        .append(" VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)").ln()
        .build())
        .props(remarks.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getId(), 
                doc.getCommitId(),
                doc.getCreatedWithCommitId(),
                
                doc.getParentId(),
                doc.getDirentId(),
                    
                doc.getReporterId(),
                doc.getRemarkStatus(),
                doc.getRemarkType(),
                doc.getRemarkSource(),
                doc.getRemarkText(),
             }))
            .collect(Collectors.toList()))
        .build();
  }

  @Override
  public SqlTupleList updateAll(Collection<FsDirentRemark> remarks) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("UPDATE ").append(options.getFsDirentRemark())
        .append(" SET").ln()
        .append("  commit_id = $1,").ln()

        .append("  parent_id = $2,").ln()
        .append("  reporter_id = $3,").ln()
        .append("  remark_status = $4,").ln()
        .append("  remark_text = $5").ln()
        .append(" WHERE id = $6")
        .build())
        .props(remarks.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getCommitId(),
                doc.getParentId(),
                doc.getReporterId(),
                doc.getRemarkStatus(),
                doc.getRemarkText(),
                doc.getId(), 
             }))
            .collect(Collectors.toList()))
        .build();
  }
  @Override
  public Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getFsDirentRemark()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    .append("  parent_id VARCHAR(40),").ln()
    .append("  created_commit_id VARCHAR(40) NOT NULL,").ln()
    
    .append("  dirent_id VARCHAR(40) NOT NULL,").ln()
    .append("  reporter_id VARCHAR(255) NOT NULL,").ln()
    .append("  remark_status VARCHAR(100),").ln()
    
    .append("  remark_type VARCHAR(100),").ln()
    .append("  remark_source VARCHAR(100),").ln()
    
    .append("  remark_text TEXT NOT NULL").ln()
    
    
    .append(");").ln()
    
    .append("ALTER TABLE ").append(options.getFsDirentRemark()).ln()
    .append("  ADD CONSTRAINT ").append(options.getFsDirentRemark()).append("_EXTRA_FK").ln()
    .append("  FOREIGN KEY (remark_id)").ln()
    .append("  REFERENCES ").append(options.getFsDirentRemark()).append(" (id);").ln().ln()

    .append("ALTER TABLE ").append(options.getFsDirentRemark()).ln()
    .append("  ADD CONSTRAINT ").append(options.getFsDirentRemark()).append("_PARENT_FK").ln()
    .append("  FOREIGN KEY (parent_id)").ln()
    .append("  REFERENCES ").append(options.getFsDirentRemark()).append(" (id);").ln().ln()
    
    .append("CREATE INDEX ").append(options.getFsDirentRemark()).append("_CREATED_INDEX")
    .append(" ON ").append(options.getFsDirentRemark()).append(" (created_commit_id);").ln()
    
    .append("CREATE INDEX ").append(options.getFsDirentRemark()).append("_DIRENT_INDEX")
    .append(" ON ").append(options.getFsDirentRemark()).append(" (dirent_id);").ln()
    
    .build()).build();
  }


  @Override
  public Sql createConstraints() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
      
      .append("ALTER TABLE ").append(options.getFsDirentRemark()).ln()
      .append("  ADD CONSTRAINT ").append(options.getFsDirentRemark()).append("_DIRENT_FK").ln()
      .append("  FOREIGN KEY (dirent_id)").ln()
      .append("  REFERENCES ").append(options.getFsDirent()).append(" (id);").ln().ln()
      
      
      .build()).build();
  }


  @Override
  public Function<Row, FsDirentRemark> defaultMapper() {
    return (row) -> {
      
      
      return ImmutableFsDirentRemark.builder()
          .id(row.getString("id"))
          .commitId(row.getString("commit_id"))
          .parentId(row.getString("parent_id"))
          
        
          .createdWithCommitId(row.getString("created_commit_id"))
          
          .direntId(row.getString("dirent_id"))
          .remarkText(row.getString("remark_text"))
          .remarkStatus(row.getString("remark_status"))
          .reporterId(row.getString("reporter_id"))
          .remarkType(row.getString("remark_type"))
          .remarkSource(row.getString("remark_source"))
          .build();
    };
  }

  @Override
  public SqlTupleList deleteAll(Collection<FsDirentRemark> remarks) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("DELETE FROM ").append(options.getFsDirentRemark())
        .append(" WHERE id = $1")
        .build())
        .props(remarks.stream()
            .map(doc -> Tuple.from(new Object[]{doc.getId()}))
            .collect(Collectors.toList()))
        .build();
  }

}
