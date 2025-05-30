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
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.fs.FsDirent;
import io.resys.thena.api.entities.fs.FsDirent.DirentType;
import io.resys.thena.api.entities.fs.ImmutableFsDirent;
import io.resys.thena.api.entities.fs.ImmutableFsDirentTransitives;
import io.resys.thena.api.registry.fs.FsDirentFilter;
import io.resys.thena.api.registry.fs.FsDirentRegistry;
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
public class FsDirentRegistrySqlImpl implements FsDirentRegistry {
  private final FsTableNames options;
  
  
  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getFsDirent()).append(";").ln()
        .append("DROP SEQUENCE ").append(options.getFsDirentRef()).append(";").ln()
        .build()).build();
  }
  @Override
  public Sql getNextRefSequence() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("select nextval('").append(options.getFsDirentRef()).append("')")
        .build())
        .build();
  }

  @Override
  public SqlTuple getNextRefSequence(long howMany) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("select nextval('").append(options.getFsDirentRef()).append("')").ln()
        .append(" from generate_series(1, $1)")
        .build())
        .props(Tuple.of(howMany))
        .build();
  }
  
  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT ")
        .append(" dirent.*, ")
        .append(" updated_commit.created_at       as updated_at,").ln()
        .append(" created_commit.created_at       as created_at,").ln()
        
        .append(" updated_tree_commit.created_at     as tree_updated_at,").ln()
        .append(" updated_tree_commit.commit_author  as tree_updated_by,").ln()
        .append(" dirent_data.data_extension        as data_extension ").ln()
        
        .append(" FROM ").append(options.getFsDirent()).append(" as dirent ").ln()
        
        .append(" LEFT JOIN ").append(options.getFsCommit()).append(" as updated_commit").ln()
        .append(" ON(updated_commit.commit_id = dirent.commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getFsCommit()).append(" as created_commit").ln()
        .append(" ON(created_commit.commit_id = dirent.created_commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getFsCommit()).append(" as updated_tree_commit").ln()
        .append(" ON(updated_tree_commit.commit_id = dirent.updated_tree_commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getFsDirentData()).append(" as dirent_data").ln()
        .append(" ON(dirent_data.dirent_id = dirent.id)").ln()
        
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT ")
        .append(" dirent.*, ")
        .append(" updated_commit.created_at       as updated_at,").ln()
        .append(" created_commit.created_at       as created_at,").ln()

        .append(" updated_tree_commit.created_at     as tree_updated_at,").ln()
        .append(" updated_tree_commit.commit_author  as tree_updated_by,").ln()
        
        .append(" dirent_data.data_extension as data_extension ").ln()
        
        .append(" FROM ").append(options.getFsDirent()).append(" as dirent ").ln()
        
        .append(" LEFT JOIN ").append(options.getFsCommit()).append(" as updated_commit").ln()
        .append(" ON(updated_commit.commit_id = dirent.commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getFsCommit()).append(" as created_commit").ln()
        .append(" ON(created_commit.commit_id = dirent.created_commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getFsCommit()).append(" as updated_tree_commit").ln()
        .append(" ON(updated_tree_commit.commit_id = dirent.updated_tree_commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getFsDirentData()).append(" as dirent_data").ln()
        .append(" ON(dirent_data.dirent_id = dirent.id)").ln()
        
        .append(" WHERE dirent.id = $1").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public SqlTuple findAll(FsDirentFilter filter) {
    final var where = new FsDirentSqlFilterBuilder(options).where(filter);
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT ")
        .append(" dirent.*, ")
        .append(" updated_commit.created_at       as updated_at,").ln()
        .append(" created_commit.created_at       as created_at,").ln()
        
        .append(" updated_tree_commit.created_at     as tree_updated_at,").ln()
        .append(" updated_tree_commit.commit_author  as tree_updated_by,").ln()
        
        .append(" dirent_data.data_extension     as data_extension ").ln()
        
        .append(" FROM ").append(options.getFsDirent()).append(" as dirent ").ln()
        
        .append(" LEFT JOIN ").append(options.getFsCommit()).append(" as updated_commit").ln()
        .append(" ON(updated_commit.commit_id = dirent.commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getFsCommit()).append(" as created_commit").ln()
        .append(" ON(created_commit.commit_id = dirent.created_commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getFsCommit()).append(" as updated_tree_commit").ln()
        .append(" ON(updated_tree_commit.commit_id = dirent.updated_tree_commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getFsDirentData()).append(" as dirent_data").ln()
        .append(" ON(dirent_data.dirent_id = dirent.id )").ln()

        .append(where.getValue()) 
        .build())
        .props(where.getProps())
        .build();
  }
  @Override
  public SqlTupleList insertAll(Collection<FsDirent> dirent) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getFsDirent()).ln()
        .append(" (id,").ln()
        .append("  commit_id,").ln()
        .append("  external_id,").ln()
        
        .append("  dirent_parent_id,").ln()
        .append("  dirent_type,").ln()
        .append("  dirent_name,").ln()
        .append("  dirent_description,").ln()        
        .append("  dirent_user_type,").ln()
        .append("  dirent_ref,").ln()
        
        .append("  archived_at,").ln()
        .append("  archived_status,").ln()
        
        .append("  created_commit_id,").ln()
        .append("  updated_tree_commit_id)").ln()
        
        .append(" VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13)").ln()
        .build())
        .props(dirent.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getId(), 
                doc.getCommitId(),
                doc.getExternalId(),
                
                doc.getDirentParentId(),
                doc.getDirentType(),
                doc.getDirentName(),
                doc.getDirentDescription(),
                doc.getDirentUserType(),
                doc.getDirentRef(),

                doc.getArchivedAt(),
                doc.getArchivedStatus(),
                
                doc.getCreatedWithCommitId(),
                doc.getUpdatedTreeWithCommitId()
                
             }))
            .collect(Collectors.toList()))
        .build();
  }
  
  
  
  @Override
  public SqlTupleList updateAll(Collection<FsDirent> dirent) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("UPDATE ").append(options.getFsDirent())
        .append(" SET").ln()
        .append("  commit_id = $1,").ln()
        .append("  external_id = $2,").ln()

        .append("  dirent_parent_id = $3,").ln()
        .append("  dirent_type = $4,").ln()
        .append("  dirent_name = $5,").ln()
        .append("  dirent_description = $6,").ln()        
        .append("  dirent_user_type = $7,").ln()
        
        .append("  archived_at = $8,").ln()
        .append("  archived_status = $9,").ln()
        
        .append("  created_commit_id = $10,").ln()
        .append("  updated_tree_commit_id = $11").ln()
        
        .append(" WHERE id = $12")
        .build())
        .props(dirent.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getCommitId(),
                doc.getExternalId(),
                
                doc.getDirentParentId(),
                doc.getDirentType(),
                doc.getDirentName(),
                doc.getDirentDescription(),
                doc.getDirentUserType(),
                
                doc.getArchivedAt(),
                doc.getArchivedStatus(),
                
                doc.getCreatedWithCommitId(),
                doc.getUpdatedTreeWithCommitId(),
                
                doc.getId()
             }))
            .collect(Collectors.toList()))
        .build();
  }
  @Override
  public Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getFsDirent()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    .append("  created_commit_id VARCHAR(40) NOT NULL,").ln()
    .append("  updated_tree_commit_id VARCHAR(40) NOT NULL,").ln()

    .append("  external_id VARCHAR(40) UNIQUE,").ln()

    .append("  dirent_parent_id VARCHAR(40),").ln()
    .append("  dirent_ref VARCHAR(40) NOT NULL,").ln()
    .append("  dirent_type VARCHAR(100) NOT NULL,").ln()
    .append("  dirent_name TEXT NOT NULL,").ln()
    .append("  dirent_description TEXT NOT NULL,").ln()
    .append("  dirent_user_type VARCHAR(100),").ln()
    
    .append("  archived_at TIMESTAMP WITH TIME ZONE,").ln()
    .append("  archived_status VARCHAR(40)").ln()
    
    .append(");").ln()
    
    .append("CREATE SEQUENCE ").append(options.getFsDirentRef()).append(" MINVALUE 1 MAXVALUE 999999 CYCLE;").ln()

    .append("CREATE INDEX ").append(options.getFsDirent()).append("_REF_INDEX")
    .append(" ON ").append(options.getFsDirent()).append(" (dirent_ref);").ln()
    
    .append("CREATE INDEX ").append(options.getFsDirent()).append("_EXT_ID_INDEX")
    .append(" ON ").append(options.getFsDirent()).append(" (external_id);").ln()
    
    .append("CREATE INDEX ").append(options.getFsDirent()).append("_NAME_INDEX")
    .append(" ON ").append(options.getFsDirent()).append(" (dirent_name);").ln()
    
    .append("CREATE INDEX ").append(options.getFsDirent()).append("_PARENT_INDEX")
    .append(" ON ").append(options.getFsDirent()).append(" (dirent_parent_id);").ln()

    .append("CREATE INDEX ").append(options.getFsDirent()).append("_COMMIT_INDEX")
    .append(" ON ").append(options.getFsDirent()).append(" (commit_id);").ln()
    
    .append("CREATE INDEX ").append(options.getFsDirent()).append("_CREATED_INDEX")
    .append(" ON ").append(options.getFsDirent()).append(" (created_commit_id);").ln()
    
    .append("CREATE INDEX ").append(options.getFsDirent()).append("_TREE_UPDATED_INDEX")
    .append(" ON ").append(options.getFsDirent()).append(" (updated_tree_commit_id);").ln()
    
    .append("ALTER TABLE ").append(options.getFsDirent()).ln()
    .append("  ADD CONSTRAINT ").append(options.getFsDirent()).append("_PARENT_FK").ln()
    .append("  FOREIGN KEY (dirent_parent_id)").ln()
    .append("  REFERENCES ").append(options.getFsDirent()).append(" (id);").ln()
    
    .build()).build();
  }


  @Override
  public Sql createConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
    .ln().append("--- constraints for").append(options.getFsDirent()).ln()    
    .build()).build();
  }


  @Override
  public Function<Row, FsDirent> defaultMapper() {
    return (row) -> {
      
      return ImmutableFsDirent.builder()
          .id(row.getString("id"))
          .commitId(row.getString("commit_id"))
          .createdWithCommitId(row.getString("created_commit_id"))
          .updatedTreeWithCommitId(row.getString("updated_tree_commit_id"))

          .externalId(row.getString("external_id"))
          
          .direntParentId(row.getString("dirent_parent_id"))
          .direntName(row.getString("dirent_name"))
          .direntDescription(row.getString("dirent_description"))
          .direntType(DirentType.valueOf(row.getString("dirent_type")))
          .direntRef(Optional.ofNullable(row.getString("dirent_ref")).orElse(""))
          .direntUserType(row.getString("dirent_user_type"))
          
          .transitives(ImmutableFsDirentTransitives.builder()
            .dataExtension(row.getJsonObject("data_extension"))
            .updatedAt(row.getOffsetDateTime("updated_at"))
            .createdAt(row.getOffsetDateTime("created_at"))
            .treeUpdatedAt(row.getOffsetDateTime("tree_updated_at"))
            .treeUpdatedBy(row.getString("tree_updated_by"))            
            .build()            
          )          
          
          .archivedAt(row.getOffsetDateTime("archived_at"))
          .archivedStatus(row.getString("archived_status"))
          .build();
    };
  }
}
