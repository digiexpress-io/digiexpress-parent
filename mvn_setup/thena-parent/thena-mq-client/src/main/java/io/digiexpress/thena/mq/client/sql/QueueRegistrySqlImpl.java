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

import io.digiexpress.thena.mq.client.api.entities.ImmutableQueue;
import io.digiexpress.thena.mq.client.api.entities.Queue;
import io.digiexpress.thena.mq.client.api.persistence.QueueRegistry;
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
public class QueueRegistrySqlImpl implements QueueRegistry {
  private final ThenaMqTableNames options;
  
  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getQueues())
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getByIdOrName(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getQueues()).ln()
        .append("  WHERE (id = $1 OR queue_name = $1)").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple findAllByIdOrName(List<String> id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getQueues()).ln()
        .append("  WHERE (id = ANY($1) OR queue_name = ANY($1))").ln() 
        .build())
        .props(Tuple.of(id.toArray()))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTupleList insertMany(List<Queue> users) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getQueues())
        .append(" (id, queue_name, created_at, created_by, comment)").ln()
        .append(" VALUES($1, $2, $3, $4, $5)").ln()
        .build())
        .props(users.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getId(), 
                doc.getQueueName(), 
                doc.getCreatedAt(), 
                doc.getCreatedBy(), 
                doc.getComment(),
             }))
            .collect(Collectors.toList()))
        .build();
  }


  @Override
  public ThenaSqlClient.Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("CREATE TABLE IF NOT EXISTS ").append(options.getQueues()).ln()
        .append("(").ln()
        .append("  id             VARCHAR(40) PRIMARY KEY,").ln()
        .append("  queue_name     TEXT NOT NULL,").ln()
        .append("  created_at     TIMESTAMP WITH TIME ZONE NOT NULL,").ln()
        .append("  created_by     TEXT NOT NULL,").ln()
        .append("  comment        TEXT NOT NULL,").ln()

        .append("  UNIQUE(queue_name)").ln()
        .append(");").ln()

        .append("CREATE INDEX IF NOT EXISTS ").append(options.getQueues()).append("_NAME_INDEX")
        .append(" ON ").append(options.getQueues()).append(" (queue_name);").ln()
        
        .build()).build();
  }
  
  @Override
  public SqlTuple deleteById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("DELETE FROM ").append(options.getQueues())
        .append(" WHERE id = $1")
        .build())
        .props(Tuple.of(id))
        .build();
  }

  @Override
  public ThenaSqlClient.Sql createConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
        .ln().append("--- constraints for ").append(options.getQueues()).ln()
        .build())
        .build();
  }

  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getQueues()).append(";").ln()
        .build()).build();
  }
  
  @Override
  public Function<Row, Queue> defaultMapper() {
    return row -> ImmutableQueue.builder()
        .id(row.getString("id"))
        .queueName(row.getString("queue_name"))
        .createdBy(row.getString("created_by"))
        .createdAt(row.getOffsetDateTime("created_at"))
        .comment(row.getString("comment"))
        .build();
  }
}
