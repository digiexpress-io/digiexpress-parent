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

import io.resys.thena.api.entities.fs.FsDirentData;
import io.resys.thena.api.entities.fs.ImmutableFsDirentData;
import io.resys.thena.api.registry.fs.FsDirentDataRegistry;
import io.resys.thena.api.registry.fs.FsDirentFilter;
import io.resys.thena.datasource.FsTableNames;
import io.resys.thena.datasource.ImmutableSql;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.ImmutableSqlTupleList;
import io.resys.thena.datasource.ThenaSqlClient;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.resys.thena.storesql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class FsDirentDataRegistrySqlImpl implements FsDirentDataRegistry {
  private final FsTableNames options;
  
  
  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getFsDirentData()).append(";").ln()
        .build()).build();
  }
  @Override
  public SqlTuple findAll(FsDirentFilter filter) {
    final var where = new FsDirentSqlFilterBuilder(options).where(filter);
    
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT ").ln()
        .append(" dirent_data.*,").ln()
        .append(" updated_commit.created_at       as updated_at,").ln()
        .append(" created_commit.created_at       as created_at").ln()
        
        .append(" FROM ").append(options.getFsDirentData()).append(" as dirent_data ").ln()
        
        .append(" LEFT JOIN ").append(options.getFsCommit()).append(" as updated_commit").ln()
        .append(" ON(updated_commit.commit_id = dirent_data.commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getFsCommit()).append(" as created_commit").ln()
        .append(" ON(created_commit.commit_id = dirent_data.created_commit_id)").ln()

        .append(" LEFT JOIN ").append(options.getFsDirent()).append(" as dirent")
        .append(" ON(dirent_data.dirent_id = dirent.id)")
        .append(where.getValue()) 
        .build())
        .props(where.getProps())
        
        .build();
  }
  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT ").ln()
        .append(" dirent_data.*,").ln()
        .append(" updated_commit.created_at       as updated_at,").ln()
        .append(" created_commit.created_at       as created_at").ln()
        
        .append(" FROM ").append(options.getFsDirentData()).append(" as dirent_data ").ln()
        
        .append(" LEFT JOIN ").append(options.getFsCommit()).append(" as updated_commit").ln()
        .append(" ON(updated_commit.commit_id = dirent_data.commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getFsCommit()).append(" as created_commit").ln()
        .append(" ON(created_commit.commit_id = dirent_data.created_commit_id)").ln()
        
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT ").ln()
        .append(" dirent_data.*,").ln()
        .append(" updated_commit.created_at       as updated_at,").ln()
        .append(" created_commit.created_at       as created_at").ln()
        
        .append(" FROM ").append(options.getFsDirentData()).append(" as dirent_data ").ln()
        
        .append(" LEFT JOIN ").append(options.getFsCommit()).append(" as updated_commit").ln()
        .append(" ON(updated_commit.commit_id = dirent_data.commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getFsCommit()).append(" as created_commit").ln()
        .append(" ON(created_commit.commit_id = dirent_data.created_commit_id)").ln()
        
        .append(" WHERE dirent_data.id = $1").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public SqlTupleList insertAll(Collection<FsDirentData> labels) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getFsDirentData()).ln()
        .append(" (id,").ln()
        .append("  commit_id,").ln()
        .append("  created_commit_id,").ln()
        .append("  dirent_id,").ln()
        
        .append("  data_extension)").ln()
        
        .append(" VALUES($1, $2, $3, $4, $5)").ln()
        .build())
        .props(labels.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getId(), 
                doc.getCommitId(),
                doc.getCreatedWithCommitId(),
                doc.getDirentId(),

                doc.getDataExtension()
             }))
            .collect(Collectors.toList()))
        .build();
  }
  @Override
  public SqlTupleList updateAll(Collection<FsDirentData> data) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("UPDATE ").append(options.getFsDirentData())
        .append(" SET").ln()
        .append("  commit_id = $1,").ln()
        .append("  data_extension = $2").ln()
        .append(" WHERE id = $3")
        .build())
        .props(data.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getCommitId(),
                doc.getDataExtension(),
                doc.getId(), 
             }))
            .collect(Collectors.toList()))
        .build();
  }
  @Override
  public Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getFsDirentData()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    .append("  created_commit_id VARCHAR(40) NOT NULL,").ln()
    
    .append("  data_extension JSONB,").ln()
    
    .append("  dirent_id VARCHAR(40) NOT NULL,").ln()
    
    .append("  UNIQUE NULLS NOT DISTINCT(dirent_id)").ln()
    
    .append(");").ln()
    
    .append("CREATE INDEX ").append(options.getFsDirentData()).append("_CREATED_INDEX")
    .append(" ON ").append(options.getFsDirentData()).append(" (created_commit_id);").ln()

    .append("CREATE INDEX ").append(options.getFsDirentData()).append("_DIRENT_INDEX")
    .append(" ON ").append(options.getFsDirentData()).append(" (dirent_id);").ln()
    
    
    .build()).build();
  }


  @Override
  public Sql createConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
        .ln().append("--- constraints for").append(options.getFsDirentData()).ln()
        .append("ALTER TABLE ").append(options.getFsDirentData()).ln()
        .append("  ADD CONSTRAINT ").append(options.getFsDirentLabel()).append("_DIRENT_FK").ln()
        .append("  FOREIGN KEY (dirent_id)").ln()
        .append("  REFERENCES ").append(options.getFsDirent()).append(" (id);").ln().ln()


    .build()).build();
  }


  @Override
  public Function<Row, FsDirentData> defaultMapper() {
    return (row) -> {
      
      return ImmutableFsDirentData.builder()
          .id(row.getString("id"))
          .commitId(row.getString("commit_id"))
          .direntId(row.getString("dirent_id"))
          
          .updatedAt(row.getOffsetDateTime("updated_at"))
          .createdAt(row.getOffsetDateTime("created_at"))
          .createdWithCommitId(row.getString("created_commit_id"))
          
          .dataExtension(row.getJsonObject("data_extension"))
          .build();
    };
  }
  @Override
  public SqlTupleList deleteAll(Collection<FsDirentData> links) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("DELETE FROM ").append(options.getFsDirentData())
        .append(" WHERE id = $1")
        .build())
        .props(links.stream()
            .map(doc -> Tuple.from(new Object[]{doc.getId()}))
            .collect(Collectors.toList()))
        .build();
  }

}
