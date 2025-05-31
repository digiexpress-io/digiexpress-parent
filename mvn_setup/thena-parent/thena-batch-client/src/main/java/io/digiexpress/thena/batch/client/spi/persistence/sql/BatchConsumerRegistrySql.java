package io.digiexpress.thena.batch.client.spi.persistence.sql;

/*-
 * #%L
 * thena-batch-client
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
import java.util.function.Function;
import java.util.stream.Collectors;

import io.digiexpress.thena.batch.client.api.entities.BatchConsumer;
import io.digiexpress.thena.batch.client.api.entities.BatchStatus;
import io.digiexpress.thena.batch.client.api.entities.ImmutableBatchConsumer;
import io.digiexpress.thena.batch.client.spi.persistence.BatchConsumerRegistry;
import io.digiexpress.thena.batch.client.spi.persistence.BatchTableNames;
import io.resys.thena.datasource.ImmutableSql;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.ImmutableSqlTupleList;
import io.resys.thena.datasource.ThenaSqlClient;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.storesql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;



@RequiredArgsConstructor
public class BatchConsumerRegistrySql implements BatchConsumerRegistry {
  private final BatchTableNames options;
  
  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getBatchConsumers())
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getBatchConsumers()).ln()
        .append("  WHERE id = $1").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public Sql findAllEnabled() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getBatchConsumers())
        .append(" WHERE consumer_status = '").append(BatchStatus.ENABLED.name()).append("'").ln()
        .build())
        .build();
  }
  @Override
  public SqlTuple findAllEnabledByAppId(String appId)  {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getBatchConsumers()).ln()
        .append("WHERE").ln()
        .append("  consumer_status = '").append(BatchStatus.ENABLED.name()).append("'").ln()
        .append("  AND app_id = $1").ln()
        .build())
        .props(Tuple.of(appId))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTupleList insertMany(List<BatchConsumer> users) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getBatchConsumers())
        .append("(id, app_id, batch_name,").ln()
        .append(" consumer_name, consumer_qualified_java_name, consumer_status, consumer_comment,").ln()
        .append(" consumer_created_at, consumer_created_by, ").ln()
        .append(" consumer_updated_at, consumer_updated_by ").ln()
        .append(")").ln()
        .append(" VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11)").ln()
        .build())
        .props(users.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getId(), 
                doc.getAppId(),
                doc.getBatchName(),
                
                doc.getConsumerName(),
                doc.getQualifiedJavaName(), 
                doc.getStatus().name(), 
                doc.getComment(),

                doc.getCreatedAt(),
                doc.getCreatedBy(),
                doc.getUpdatedAt().orElse(null),
                doc.getUpdatedBy().orElse(null),

             }))
            .collect(Collectors.toList()))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTupleList updateMany(List<BatchConsumer> users) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("UPDATE ").append(options.getBatches())
        .append(" SET consumer_qualified_java_name = $1, consumer_status = $2, batch_name = $3, consumer_updated_at = $4, consumer_updated_by = $5, consumer_comment = $6")
        .append(" WHERE id = $7")
        .build())
        .props(users.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                
                doc.getQualifiedJavaName(),
                doc.getStatus().name(),
                doc.getBatchName(), 
                doc.getUpdatedAt().orElse(null), 
                doc.getUpdatedBy().orElse(null),
                doc.getComment(),
                doc.getId()
             }))
            .collect(Collectors.toList()))
        .build();
  }

  @Override
  public SqlTuple findAllByAppId(String appId, boolean lockForUpdate) {
    //WHERE name = $1 FOR UPDATE NOWAIT
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT consumers.* ").ln()
        .append("  FROM ").append(options.getBatchConsumers()).append(" AS consumers ").ln()
        .append("  WHERE ").ln() 
        .append("    consumers.app_id = $1").ln()
        .append(lockForUpdate ? "     FOR UPDATE" : "").ln()  //FOR UPDATE NOWAIT
        
        .build())
        .props(Tuple.of(appId))
        .build();
  }
  @Override
  public ThenaSqlClient.Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("CREATE TABLE IF NOT EXISTS ").append(options.getBatchConsumers()).ln()
        .append("(").ln()
        .append("  id                           VARCHAR(40) PRIMARY KEY,").ln()
        .append("  batch_name                   TEXT NOT NULL,").ln()
        .append("  app_id                       TEXT NOT NULL,").ln()
        
        .append("  consumer_name                TEXT NOT NULL,").ln()
        .append("  consumer_qualified_java_name TEXT NOT NULL,").ln()
        .append("  consumer_comment             TEXT NOT NULL,").ln()
        .append("  consumer_status              VARCHAR(100) NOT NULL,").ln()
        .append("  consumer_created_at          TIMESTAMP WITH TIME ZONE NOT NULL,").ln()
        .append("  consumer_created_by          TEXT NOT NULL,").ln()
        .append("  consumer_updated_at          TIMESTAMP WITH TIME ZONE,").ln()
        .append("  consumer_updated_by          TEXT,").ln()
        
        .append("  UNIQUE(batch_name, app_id, consumer_name)")
        .append(");").ln()

        .append("CREATE INDEX IF NOT EXISTS ").append(options.getBatchConsumers()).append("_APP_INDEX")
        .append(" ON ").append(options.getBatchConsumers()).append(" (app_id);").ln()
        
        .append("CREATE INDEX IF NOT EXISTS ").append(options.getBatchConsumers()).append("_BATCH_NAME_INDEX")
        .append(" ON ").append(options.getBatchConsumers()).append(" (batch_name);").ln()
        
        .append("CREATE INDEX IF NOT EXISTS ").append(options.getBatchConsumers()).append("_NAME_INDEX")
        .append(" ON ").append(options.getBatchConsumers()).append(" (consumer_name);").ln()

        .build()).build();
  }
  
  @Override
  public SqlTuple deleteById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("DELETE FROM ").append(options.getBatchConsumers())
        .append(" WHERE id = $1")
        .build())
        .props(Tuple.of(id))
        .build();
  }

  @Override
  public ThenaSqlClient.Sql createConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
        .ln().append("--- constraints for ").append(options.getBatchConsumers()).ln()
        
        .build())
        .build();
  }

  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getBatchConsumers()).append(";").ln()
        .build()).build();
  }

  @Override
  public Function<Row, BatchConsumer> defaultMapper() {
    return row -> ImmutableBatchConsumer.builder()
        
        .id(row.getString("id"))
        .appId(row.getString("app_id"))
        
        .consumerName(row.getString("consumer_name"))
        .qualifiedJavaName(row.getString("qualified_java_name"))

        .status(BatchStatus.valueOf(row.getString("consumer_status")))
        .comment(row.getString("comment"))
        
        .batchName(row.getString("batch_name"))
        .createdAt(row.getOffsetDateTime("created_at"))
        .updatedAt(row.getOffsetDateTime("updated_at"))
        
        .build();
  }
}
