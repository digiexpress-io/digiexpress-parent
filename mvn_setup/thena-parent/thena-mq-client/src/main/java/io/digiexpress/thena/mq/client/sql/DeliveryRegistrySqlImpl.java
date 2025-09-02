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

import io.digiexpress.thena.mq.client.api.entities.Delivery;
import io.digiexpress.thena.mq.client.api.entities.Delivery.DeliveryStatus;
import io.digiexpress.thena.mq.client.api.entities.ImmutableDelivery;
import io.digiexpress.thena.mq.client.api.persistence.DeliveryRegistry;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqTableNames;
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
public class DeliveryRegistrySqlImpl implements DeliveryRegistry {
  private final ThenaMqTableNames options;
  
  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getDelivery())
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getByIdOrName(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getDelivery()).ln()
        .append("  WHERE id = $1").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple findAllByMessageId(List<String> messageId) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getDelivery()).ln()
        .append("  WHERE message_id = ANY($1)").ln() 
        .build())
        .props(Tuple.of(messageId.toArray()))
        .build();
  }
  @Override
  public SqlTuple findLastNEntries(long entries) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getDelivery()).ln()
        .append("  ORDER BY created_at DESC").ln() 
        .append("  LIMIT $1").ln() 
        .build())
        .props(Tuple.of(entries))
        .build();
  }
  @Override
  public SqlTuple findAllByAppIdAndStatus(String appId, DeliveryStatus status, boolean lockForUpdate) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT deliveries.* ").ln()
        .append(" FROM ").append(options.getDelivery()).append(" AS deliveries").ln()
        .append(" LEFT JOIN ").append(options.getQueueConsumers()).append(" AS consumers").ln()
        .append(" ON(deliveries.consumer_id = consumers.id)").ln()
        .append(" WHERE deliveries.status = $1 AND consumers.app_id = $2").ln()
        .append(lockForUpdate ? "FOR UPDATE" : "").ln()  //FOR UPDATE NOWAIT
        
        .build())
        .props(Tuple.of(status, appId))
        .build();
  }
  
  @Override
  public ThenaSqlClient.SqlTupleList insertMany(List<Delivery> users) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getDelivery())
        .append(" (id, message_id, queue_id, consumer_id, status, created_at, starts_at, expires_at, completed_at)").ln()
        .append(" VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9)").ln()
        .build())
        .props(users.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getId(), 
                doc.getMessageId(),
                doc.getQueueId(),
                doc.getConsumerId(),
                doc.getStatus().name(), 
                doc.getCreatedAt(), 
                doc.getStartsAt(), 
                doc.getExpiresAt(), 
                doc.getCompletedAt() 
             }))
            .collect(Collectors.toList()))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTupleList updateMany(List<Delivery> users) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("UPDATE ").append(options.getDelivery())
        .append(" SET status = $1, completed_at = $2")
        .append(" WHERE id = $3")
        .build())
        .props(users.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getStatus().name(), 
                doc.getCompletedAt(),
                doc.getId() 
             }))
            .collect(Collectors.toList()))
        .build();
  }

  @Override
  public ThenaSqlClient.Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("CREATE TABLE IF NOT EXISTS ").append(options.getDelivery()).ln()
        .append("(").ln()
        .append("  id             VARCHAR(40) PRIMARY KEY,").ln()
        .append("  message_id     VARCHAR(40) NOT NULL,").ln()
        .append("  queue_id       VARCHAR(40) NOT NULL,").ln()
        .append("  consumer_id    VARCHAR(40) NOT NULL,").ln()
        .append("  status         VARCHAR(100) NOT NULL,").ln()
        .append("  created_at     TIMESTAMP WITH TIME ZONE NOT NULL,").ln()
        .append("  starts_at      TIMESTAMP WITH TIME ZONE NOT NULL,").ln()
        .append("  expires_at     TIMESTAMP WITH TIME ZONE NOT NULL,").ln()
        .append("  completed_at   TIMESTAMP WITH TIME ZONE").ln()
        
        .append(");").ln()

        .append("CREATE INDEX IF NOT EXISTS ").append(options.getDelivery()).append("_MSG_INDEX")
        .append(" ON ").append(options.getDelivery()).append(" (message_id);").ln()

        .append("CREATE INDEX IF NOT EXISTS ").append(options.getDelivery()).append("_STATUS_INDEX")
        .append(" ON ").append(options.getDelivery()).append(" (status);").ln()

        .append("CREATE INDEX IF NOT EXISTS ").append(options.getDelivery()).append("_QUEUE_INDEX")
        .append(" ON ").append(options.getDelivery()).append(" (queue_id);").ln()

        .append("CREATE INDEX IF NOT EXISTS ").append(options.getDelivery()).append("_STATUS_INDEX")
        .append(" ON ").append(options.getDelivery()).append(" (status);").ln()

        .append("CREATE INDEX IF NOT EXISTS ").append(options.getDelivery()).append("_CONSUMER_INDEX")
        .append(" ON ").append(options.getDelivery()).append(" (consumer_id);").ln()
        
        .build()).build();
  }

  @Override
  public ThenaSqlClient.Sql createConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
        .ln().append("--- constraints for ").append(options.getDelivery()).ln()
        
        .append("ALTER TABLE ").append(options.getDelivery()).ln()
        .append("  ADD CONSTRAINT ").append(options.getDelivery()).append("_MSG_FK").ln()
        .append("  FOREIGN KEY (message_id)").ln()
        .append("  REFERENCES ").append(options.getMessages()).append(" (id);").ln()
        
        .append("ALTER TABLE ").append(options.getDelivery()).ln()
        .append("  ADD CONSTRAINT ").append(options.getDelivery()).append("_QUEUE_FK").ln()
        .append("  FOREIGN KEY (queue_id)").ln()
        .append("  REFERENCES ").append(options.getQueues()).append(" (id);").ln()

        .append("ALTER TABLE ").append(options.getDelivery()).ln()
        .append("  ADD CONSTRAINT ").append(options.getDelivery()).append("_CON_FK").ln()
        .append("  FOREIGN KEY (consumer_id)").ln()
        .append("  REFERENCES ").append(options.getQueueConsumers()).append(" (id);").ln()
        
        .build())
        .build();
  }

  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getDelivery()).append(";").ln()
        .build()).build();
  }

  @Override
  public Function<Row, Delivery> defaultMapper() {
    return row -> ImmutableDelivery.builder()
        .id(row.getString("id"))
        .messageId(row.getString("message_id"))
        .status(DeliveryStatus.valueOf(row.getString("status")))
        .queueId(row.getString("queue_id"))
        .consumerId(row.getString("consumer_id"))
        .createdAt(row.getOffsetDateTime("created_at"))
        .startsAt(row.getOffsetDateTime("starts_at"))
        .expiresAt(row.getOffsetDateTime("expires_at"))
        .completedAt(row.getOffsetDateTime("completed_at"))
        .build();
  }

}
