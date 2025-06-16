package io.digiexpress.thena.batch.client.spi.persistence.sql;

/*-
 * #%L
 * thena-mq-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.digiexpress.thena.batch.client.api.entities.Batch;
import io.digiexpress.thena.batch.client.api.entities.BatchStatus;
import io.digiexpress.thena.batch.client.api.entities.ImmutableBatch;
import io.digiexpress.thena.batch.client.spi.persistence.BatchRegistry;
import io.digiexpress.thena.batch.client.spi.persistence.BatchTableNames;
import io.resys.thena.datasource.ImmutableSql;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.ImmutableSqlTupleList;
import io.resys.thena.datasource.ThenaSqlClient;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.storesql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BatchRegistrySql implements BatchRegistry {
  private final BatchTableNames options;
  
  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getBatches())
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple findAllByAppId(String appId, boolean lockForUpdate) {
    //WHERE name = $1 FOR UPDATE NOWAIT
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT batches.* ").ln()
        .append("  FROM ").append(options.getBatches()).append(" AS batches ").ln()
        .append("  WHERE ").ln() 
        .append("    batches.app_id = $1").ln()
        .append(lockForUpdate ? "     FOR UPDATE" : "").ln()  //FOR UPDATE NOWAIT
        
        .build())
        .props(Tuple.of(appId))
        .build();

  }
  @Override
  public ThenaSqlClient.SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getBatches()).ln()
        .append("  WHERE (id = $1 OR batch_name = $1 OR batch_external_id = $1)").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  
  @Override
  public SqlTuple findOneByAppIdAndName(String appId, String batchName) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getBatches()).ln()
        .append("  WHERE app_id = $2 AND (id = $1 OR batch_name = $1 OR batch_external_id = $1)").ln() 
        .build())
        .props(Tuple.of(batchName, appId))
        .build();
  }
  
  @Override
  public SqlTuple findOneByName(String batchName) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getBatches()).ln()
        .append("  WHERE id = $1 OR batch_name = $1 OR batch_external_id = $1").ln() 
        .build())
        .props(Tuple.of(batchName))
        .build();
  }
  
  @Override
  public ThenaSqlClient.SqlTupleList insertMany(List<Batch> users) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getBatches())
        .append(" (").ln()
        .append(" id, app_id,").ln()
        .append(" batch_name, batch_comment, batch_external_id, batch_status, ").ln()
        .append(" batch_created_at, batch_created_by,").ln()
        .append(" batch_updated_at, batch_updated_by").ln()
        .append(" )").ln()
        .append(" VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)").ln()
        .build())
        .props(users.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getId(), 
                doc.getAppId(),
                
                doc.getBatchName(), 
                doc.getComment(),
                doc.getExternalId().orElse(null),
                doc.getStatus(),
                
                doc.getCreatedAt(), 
                doc.getCreatedBy(),
                
                doc.getUpdatedAt().orElse(null), 
                doc.getUpdatedBy().orElse(null),
             }))
            .collect(Collectors.toList()))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTupleList updateMany(List<Batch> users) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("UPDATE ").append(options.getBatches())
        .append(" SET batch_name = $1, batch_comment = $2, batch_external_id = $3, batch_status = $4, batch_updated_at = $5, batch_updated_by = $6")
        .append(" WHERE id = $7")
        .build())
        .props(users.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                
                doc.getBatchName(), 
                doc.getComment(),
                doc.getExternalId().orElse(null),
                doc.getStatus(),
                
                doc.getUpdatedAt().orElse(null), 
                doc.getUpdatedBy().orElse(null),
                
                doc.getId()
             }))
            .collect(Collectors.toList()))
        .build();
  }


  @Override
  public ThenaSqlClient.Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("CREATE TABLE IF NOT EXISTS ").append(options.getBatches()).ln()
        .append("(").ln()
        .append("  id                   VARCHAR(40) PRIMARY KEY,").ln()
        .append("  app_id               TEXT NOT NULL,").ln()
        .append("  batch_name           TEXT NOT NULL,").ln()
        .append("  batch_external_id    TEXT,").ln()
        .append("  batch_created_at     TIMESTAMP WITH TIME ZONE NOT NULL,").ln()
        .append("  batch_created_by     TEXT NOT NULL,").ln()
        .append("  batch_comment        TEXT NOT NULL,").ln()
        .append("  batch_status         TEXT NOT NULL,").ln()
        .append("  batch_updated_at     TIMESTAMP,").ln()
        .append("  batch_updated_by     TEXT,").ln()

        .append("  UNIQUE(batch_name)").ln()
        .append(");").ln()

        .append("CREATE INDEX IF NOT EXISTS ").append(options.getBatches()).append("_NAME_INDEX")
        .append(" ON ").append(options.getBatches()).append(" (batch_name);").ln()

        .append("CREATE INDEX IF NOT EXISTS ").append(options.getBatches()).append("_APP_INDEX")
        .append(" ON ").append(options.getBatches()).append(" (app_id);").ln()
        
        .append("CREATE INDEX IF NOT EXISTS ").append(options.getBatches()).append("_EXT_ID_INDEX")
        .append(" ON ").append(options.getBatches()).append(" (batch_external_id);").ln()
        
        .build()).build();
  }
  
  @Override
  public SqlTuple deleteById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("DELETE FROM ").append(options.getBatches())
        .append(" WHERE id = $1")
        .build())
        .props(Tuple.of(id))
        .build();
  }

  @Override
  public ThenaSqlClient.Sql createConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
        .ln().append("--- constraints for ").append(options.getBatches()).ln()
        .build())
        .build();
  }

  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getBatches()).append(";").ln()
        .build()).build();
  }
  
  @Override
  public Function<Row, Batch> defaultMapper() {
    return row -> ImmutableBatch.builder()
        .id(row.getString("id"))
        .appId(row.getString("app_id"))
        .batchName(row.getString("batch_name"))
        .externalId(Optional.ofNullable(row.getString("batch_external_id")))
        
        .createdBy(row.getString("batch_created_by"))
        .createdAt(row.getOffsetDateTime("batch_created_at"))

        .updatedBy(Optional.ofNullable(row.getString("batch_created_by")))
        .updatedAt(Optional.ofNullable(row.getOffsetDateTime("batch_created_at")))
        
        .status(BatchStatus.valueOf(row.getString("batch_status")))
        
        .comment(row.getString("batch_comment"))
        .build();
  }
}
