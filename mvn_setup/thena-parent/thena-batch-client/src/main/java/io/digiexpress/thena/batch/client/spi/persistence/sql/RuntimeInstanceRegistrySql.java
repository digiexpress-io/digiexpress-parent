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

import io.digiexpress.thena.batch.client.api.entities.ImmutableRuntimeInstance;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance.RuntimeExecutionStatus;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance.RuntimeStatus;
import io.digiexpress.thena.batch.client.spi.persistence.BatchTableNames;
import io.digiexpress.thena.batch.client.spi.persistence.RuntimeInstanceRegistry;
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
public class RuntimeInstanceRegistrySql implements RuntimeInstanceRegistry {
  private final BatchTableNames options;
  
  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getRuntimeInstances())
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getRuntimeInstances()).ln()
        .append("  WHERE (id = $1)").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public SqlTuple getById(String id, boolean lockForUpdate) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getRuntimeInstances()).ln()
        .append("  WHERE (id = $1)").ln() 
        .append(lockForUpdate ? "     FOR UPDATE" : "").ln()
        .build())
        .props(Tuple.of(id))
        .build();
  }

  @Override
  public ThenaSqlClient.SqlTupleList insertMany(List<RuntimeInstance> users) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getRuntimeInstances())
        .append(" (id, batch_id, instance_status, instance_execution_status, instance_name, instance_created_at, instance_ended_at, instance_comment)").ln()
        .append(" VALUES($1, $2, $3, $4, $5, $6, $7, $8)").ln()
        .build())
        .props(users.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getId(), 
                doc.getBatchId(), 
                doc.getStatus().name(),
                doc.getExecutionStatus().name(),
                
                doc.getName(),
                doc.getCreatedAt(),
                doc.getEndedAt().orElse(null),
                doc.getComment()
             }))
            .collect(Collectors.toList()))
        .build();
  }
  @Override
  public SqlTupleList updateMany(List<RuntimeInstance> instances) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("UPDATE ").append(options.getRuntimeInstances())
        .append(" SET").ln()
        .append(" instance_status = $1,").ln()
        .append(" instance_execution_status = $2,").ln()
        .append(" instance_ended_at = $3,").ln()
        .append(" instance_comment = $4").ln()
        .append(" WHERE id = $5")
        .build())
        .props(instances.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                 
                doc.getStatus().name(),
                doc.getExecutionStatus().name(),
                doc.getEndedAt().orElse(null),
                doc.getComment(),
                doc.getId()
             }))
            .collect(Collectors.toList()))
        .build();
  }


  @Override
  public ThenaSqlClient.Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("CREATE TABLE IF NOT EXISTS ").append(options.getRuntimeInstances()).ln()
        .append("(").ln()
        .append("  id             VARCHAR(40) PRIMARY KEY,").ln()
        .append("  batch_id       VARCHAR(40) NOT NULL,").ln()
        
        .append("  instance_status                VARCHAR(100) NOT NULL,").ln()
        .append("  instance_execution_status      VARCHAR(100) NOT NULL,").ln()
        
        .append("  instance_name        TEXT NOT NULL,").ln()
        .append("  instance_created_at  TIMESTAMP WITH TIME ZONE NOT NULL,").ln()
        .append("  instance_ended_at    TIMESTAMP WITH TIME ZONE,").ln()
        .append("  instance_comment     TEXT NOT NULL,").ln()
        
        .append("  UNIQUE(instance_name)")
        .append(");").ln()

        .append("CREATE INDEX IF NOT EXISTS ").append(options.getRuntimeInstances()).append("_BATCH_INDEX")
        .append(" ON ").append(options.getRuntimeInstances()).append(" (batch_id);").ln()
        
        .append("CREATE INDEX IF NOT EXISTS ").append(options.getRuntimeInstances()).append("_NAME")
        .append(" ON ").append(options.getRuntimeInstances()).append(" (instance_name);").ln()
        
        
        .append("CREATE INDEX IF NOT EXISTS ").append(options.getRuntimeInstances()).append("_STATUS")
        .append(" ON ").append(options.getRuntimeInstances()).append(" (instance_status);").ln()
        
        
        .append("CREATE SEQUENCE ").append(options.getRuntimeInstancesRef()).append(" MINVALUE 1 MAXVALUE 999999999 CYCLE;").ln()
        
        .build()).build();
  }
  
  @Override
  public ThenaSqlClient.Sql createConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
        .ln().append("--- constraints for ").append(options.getRuntimeInstances()).ln()

        .append("ALTER TABLE ").append(options.getRuntimeInstances()).ln()
        .append("  ADD CONSTRAINT ").append(options.getRuntimeInstances()).append("_BATCH_FK").ln()
        .append("  FOREIGN KEY (batch_id)").ln()
        .append("  REFERENCES ").append(options.getBatches()).append(" (id);").ln().ln()

        
        .build())
        .build();
  }

  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getRuntimeInstances()).append(";").ln()
        .append("DROP SEQUENCE ").append(options.getRuntimeInstancesRef()).append(";").ln()
        .build()).build();
  }
  
  @Override
  public Sql getNextRefSequence() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("select nextval('").append(options.getRuntimeInstancesRef()).append("')")
        .build())
        .build();
  }

  @Override
  public SqlTuple getNextRefSequence(long howMany) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("select nextval('").append(options.getRuntimeInstancesRef()).append("')").ln()
        .append(" from generate_series(1, $1)")
        .build())
        .props(Tuple.of(howMany))
        .build();
  }

  @Override
  public Function<Row, RuntimeInstance> defaultMapper() {
    return row -> ImmutableRuntimeInstance.builder()
          .id(row.getString("id"))
          .batchId(row.getString("batch_id"))
          
          .status(RuntimeStatus.valueOf(row.getString("instance_status")))
          .executionStatus(RuntimeExecutionStatus.valueOf(row.getString("instance_execution_status")))
          .endedAt(Optional.ofNullable(row.getOffsetDateTime("instance_ended_at")))
          
          .name(row.getString("instance_name"))
          .comment(row.getString("instance_comment"))
          .createdAt(row.getOffsetDateTime("instance_created_at"))
          .build()
    ;
  }
  @Override
  public SqlTuple findAllByStatus(List<RuntimeStatus> status) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getRuntimeInstances()).ln()
        .append("  WHERE (instance_status = ANY($1) OR $1 IS NULL)").ln() 
        .build())
        .props(Tuple.of(status.isEmpty() ? null : status.stream().map(e -> e.name()).toArray()))
        .build();
  }
}
