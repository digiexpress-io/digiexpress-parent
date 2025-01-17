package io.digiexpress.thena.mq.client.sql;

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
import java.util.function.Function;
import java.util.stream.Collectors;

import io.digiexpress.thena.mq.client.api.entities.ImmutableQueueConsumer;
import io.digiexpress.thena.mq.client.api.entities.QueueConsumer;
import io.digiexpress.thena.mq.client.api.entities.QueueConsumer.QueueConsumerStatus;
import io.digiexpress.thena.mq.client.api.persistence.QueueConsumerRegistry;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqTableNames;
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
public class QueueConsumerRegistrySqlImpl implements QueueConsumerRegistry {
  private final ThenaMqTableNames options;
  
  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getQueueConsumers())
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getByIdOrName(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getQueueConsumers()).ln()
        .append("  WHERE id = $1").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public Sql findAllEnabled() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getQueueConsumers())
        .append(" WHERE consumer_status = '").append(QueueConsumerStatus.ENABLED.name()).append("'").ln()
        .build())
        .build();
  }
  @Override
  public SqlTuple findAllEnabledByAppId(String appId)  {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getQueueConsumers()).ln()
        .append("WHERE").ln()
        .append("  consumer_status = '").append(QueueConsumerStatus.ENABLED.name()).append("'").ln()
        .append("  AND app_id = $1").ln()
        .build())
        .props(Tuple.of(appId))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTupleList insertMany(List<QueueConsumer> users) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getQueueConsumers())
        .append(" (id, app_id, consumer_name, qualified_java_name, consumer_status, routing_key, created_at, updated_at, comment)").ln()
        .append(" VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9)").ln()
        .build())
        .props(users.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getId(), 
                doc.getAppId(),
                doc.getConsumerName(),
                doc.getQualifiedJavaName(), 
                doc.getConsumerStatus().name(), 
                doc.getRoutingKey(),

                doc.getCreatedAt(),
                doc.getUpdatedAt(),
                doc.getComment()
             }))
            .collect(Collectors.toList()))
        .build();
  }
  @Override
  public SqlTupleList updateMany(List<QueueConsumer> queue) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
            .append("UPDATE ").append(options.getQueueConsumers())
            .append(" SET qualified_java_name = $1, consumer_status = $2, routing_key = $3, comment = $4, updated_at = $5 ")
            .append(" WHERE id = $6")
        .build())
        .props(queue.stream()
            .map(doc -> Tuple.from(new Object[]{ 

                doc.getQualifiedJavaName(), 
                doc.getConsumerStatus().name(), 
                doc.getRoutingKey(), 
                doc.getComment(),
                doc.getUpdatedAt(),
                
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
        .append("  FROM ").append(options.getQueueConsumers()).append(" AS consumers ").ln()
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
        .append("CREATE TABLE IF NOT EXISTS ").append(options.getQueueConsumers()).ln()
        .append("(").ln()
        .append("  id                   VARCHAR(40) PRIMARY KEY,").ln()
        .append("  app_id               TEXT NOT NULL,").ln()
        .append("  consumer_name        TEXT NOT NULL,").ln()
        .append("  qualified_java_name  TEXT NOT NULL,").ln()
        .append("  comment              TEXT NOT NULL,").ln()
        .append("  consumer_status      VARCHAR(100) NOT NULL,").ln()
        .append("  routing_key          TEXT NOT NULL,").ln()
        .append("  created_at           TIMESTAMP WITH TIME ZONE NOT NULL,").ln()
        .append("  updated_at           TIMESTAMP WITH TIME ZONE,").ln()
        
        .append("  UNIQUE(routing_key, app_id, consumer_name)")
        .append(");").ln()

        .append("CREATE INDEX IF NOT EXISTS ").append(options.getQueueConsumers()).append("_APP_INDEX")
        .append(" ON ").append(options.getQueueConsumers()).append(" (app_id);").ln()
        
        .append("CREATE INDEX IF NOT EXISTS ").append(options.getQueueConsumers()).append("_RKEY_INDEX")
        .append(" ON ").append(options.getQueueConsumers()).append(" (routing_key);").ln()

        .build()).build();
  }
  
  @Override
  public SqlTuple deleteById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("DELETE FROM ").append(options.getQueueConsumers())
        .append(" WHERE id = $1")
        .build())
        .props(Tuple.of(id))
        .build();
  }

  @Override
  public ThenaSqlClient.Sql createConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
        .ln().append("--- constraints for ").append(options.getQueueConsumers()).ln()
        
        
        .build())
        .build();
  }

  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getQueueConsumers()).append(";").ln()
        .build()).build();
  }

  @Override
  public Function<Row, QueueConsumer> defaultMapper() {
    return row -> ImmutableQueueConsumer.builder()
        .id(row.getString("id"))
        .appId(row.getString("app_id"))
        .consumerName(row.getString("consumer_name"))
        .qualifiedJavaName(row.getString("qualified_java_name"))
        .consumerStatus(QueueConsumerStatus.valueOf(row.getString("consumer_status")))        
        .routingKey(row.getString("routing_key"))
        .comment(row.getString("comment"))
        .createdAt(row.getOffsetDateTime("created_at"))
        .updatedAt(row.getOffsetDateTime("updated_at"))
        
        .build();
  }
}
