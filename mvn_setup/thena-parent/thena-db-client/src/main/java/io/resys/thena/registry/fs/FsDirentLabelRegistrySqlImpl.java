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

import io.resys.thena.api.entities.fs.FsDirentLabel;
import io.resys.thena.api.entities.fs.FsUniqueDirentLabel;
import io.resys.thena.api.entities.fs.ImmutableFsDirentLabel;
import io.resys.thena.api.entities.fs.ImmutableFsUniqueDirentLabel;
import io.resys.thena.api.registry.fs.FsDirentFilter;
import io.resys.thena.api.registry.fs.FsDirentLabelRegistry;
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
public class FsDirentLabelRegistrySqlImpl implements FsDirentLabelRegistry {
  private final TenantTableNames options;
  
  
  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getFsDirentLabel()).append(";").ln()
        .build()).build();
  }
  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getFsDirentLabel())
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.Sql findAllUniques() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT distinct(label_type, label_value, label_body) FROM ").append(options.getFsDirentLabel()).ln()
        .append(" WHERE dirent_id is not null").ln()
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getFsDirentLabel()).ln()
        .append("  WHERE (id = $1)").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public SqlTupleList insertAll(Collection<FsDirentLabel> labels) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getFsDirentLabel()).ln()
        .append(" (id,").ln()
        .append("  commit_id,").ln()

        .append("  dirent_id,").ln()
        
        .append("  label_type,").ln()
        .append("  label_value,").ln()
        .append("  label_body)").ln()
        
        .append(" VALUES($1, $2, $3, $4, $5, $6)").ln()
        .build())
        .props(labels.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getId(), 
                doc.getCommitId(),
                doc.getDirentId(),
                    
                doc.getLabelType(),
                doc.getLabelValue(),
                doc.getLabelBody()
             }))
            .collect(Collectors.toList()))
        .build();
  }
  @Override
  public Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getFsDirentLabel()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()

    .append("  label_type VARCHAR(100) NOT NULL,").ln()
    .append("  label_value VARCHAR(255) NOT NULL,").ln()
    .append("  label_body JSONB,").ln()

    .append("  dirent_id VARCHAR(40) NOT NULL,").ln()
    
    .append("  UNIQUE NULLS NOT DISTINCT(dirent_id, label_type, label_value)").ln()    
    .append(");").ln()

    .append("CREATE INDEX ").append(options.getFsDirentLabel()).append("_DIRENT_INDEX")
    .append(" ON ").append(options.getFsDirentLabel()).append(" (dirent_id);").ln()
    

    .append("CREATE INDEX ").append(options.getFsDirentLabel()).append("_LABEL_INDEX")
    .append(" ON ").append(options.getFsDirentLabel()).append(" (label_value);").ln()
    
    
    .build()).build();
  }


  @Override
  public Sql createConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
        .ln().append("--- constraints for").append(options.getFsDirentLabel()).ln()
        .append("ALTER TABLE ").append(options.getFsDirentLabel()).ln()
        .append("  ADD CONSTRAINT ").append(options.getFsDirentLabel()).append("_DIRENT_FK").ln()
        .append("  FOREIGN KEY (dirent_id)").ln()
        .append("  REFERENCES ").append(options.getFsDirent()).append(" (id);").ln().ln()
        
    .build()).build();
  }


  @Override
  public Function<Row, FsDirentLabel> defaultMapper() {
    return (row) -> {
      
      return ImmutableFsDirentLabel.builder()
          .id(row.getString("id"))
          .commitId(row.getString("commit_id"))
          .direntId(row.getString("dirent_id"))
          .labelType(row.getString("label_type"))
          .labelValue(row.getString("label_value"))
          .labelBody(row.getJsonObject("label_body"))
          .build();
    };
  }
  @Override
  public SqlTuple findAll(FsDirentFilter filter) {
    final var where = new FsDirentSqlFilterBuilder(options).where(filter);
    
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT dirent_label.* ").ln()
        .append("  FROM ").append(options.getFsDirentLabel()).append(" as dirent_label").ln()
        
        .append("  LEFT JOIN ").append(options.getFsDirent()).append(" as dirent")
        .append("  ON(dirent_label.dirent_id = dirent.id)")
        .append(where.getValue()) 
        .build())
        .props(where.getProps())
        .build();
  }
  @Override
  public SqlTupleList deleteAll(Collection<FsDirentLabel> labels) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("DELETE FROM ").append(options.getFsDirentLabel())
        .append(" WHERE id = $1")
        .build())
        .props(labels.stream()
            .map(doc -> Tuple.from(new Object[]{doc.getId()}))
            .collect(Collectors.toList()))
        .build();
  }

  @Override
  public Function<Row, FsUniqueDirentLabel> uniqueLabelMapper() {
    return (row) -> {
      
      return ImmutableFsUniqueDirentLabel.builder()
          .labelType(row.getString("label_type"))
          .labelValue(row.getString("label_value"))
          .labelBody(row.getJsonObject("label_body"))
          .build();
    };
  }
}
