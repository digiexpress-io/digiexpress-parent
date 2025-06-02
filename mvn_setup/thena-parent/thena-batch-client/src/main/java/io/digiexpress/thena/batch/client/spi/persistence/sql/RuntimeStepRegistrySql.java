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
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.digiexpress.thena.batch.client.api.entities.ImmutableRuntimeStep;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance.RuntimeExecutionStatus;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance.RuntimeStatus;
import io.digiexpress.thena.batch.client.api.entities.RuntimeStep;
import io.digiexpress.thena.batch.client.spi.persistence.BatchTableNames;
import io.digiexpress.thena.batch.client.spi.persistence.RuntimeStepRegistry;
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
public class RuntimeStepRegistrySql implements RuntimeStepRegistry {
  private final BatchTableNames options;
  
  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getRuntimeSteps())
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getRuntimeSteps()).ln()
        .append("  WHERE (id = $1)").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  
  @Override
  public ThenaSqlClient.SqlTupleList insertMany(List<RuntimeStep> docs) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getRuntimeSteps())
        .append("(")
        .append("  id, runtime_id, consumer_id,").ln()
        .append("  step_created_at, step_ended_at, step_status, step_execution_status, step_name, step_comment ").ln()
        .append(")")
        .append(" VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9)").ln()
        .build())
        .props(docs.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getId(), 
                doc.getRuntimeId(), 
                doc.getConsumerId(),
                
                doc.getCreatedAt(),
                doc.getEndedAt().orElse(null),
                
                doc.getStatus().name(),
                doc.getExecutionStatus().name(),
                
                doc.getName(),
                doc.getComment()
                
             }))
            .collect(Collectors.toList()))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTupleList updateMany(List<RuntimeStep> docs) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("UPDATE ").append(options.getRuntimeSteps())
        .append(" SET ")
        .append("  step_ended_at = $1, step_status = $2, ").ln()
        .append("  step_execution_status = $3, step_name = $4, step_comment = $5").ln()
        .append(" WHERE id = $6").ln()
        .build())
        .props(docs.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getEndedAt().orElse(null),
                
                doc.getStatus().name(),
                doc.getExecutionStatus().name(),
                
                doc.getName(),
                doc.getComment(),
                doc.getId()
             }))
            .collect(Collectors.toList()))
        .build();
  }


  @Override
  public ThenaSqlClient.Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("CREATE TABLE IF NOT EXISTS ").append(options.getRuntimeSteps()).ln()
        .append("(").ln()
        .append("  id             VARCHAR(40) PRIMARY KEY,").ln()
        .append("  consumer_id    VARCHAR(40) NOT NULL,").ln()
        .append("  runtime_id     VARCHAR(40) NOT NULL,").ln()
        
        .append("  step_created_at  TIMESTAMP WITH TIME ZONE NOT NULL,").ln()
        .append("  step_ended_at    TIMESTAMP WITH TIME ZONE,").ln()
        
        .append("  step_status            VARCHAR(100) NOT NULL,").ln()
        .append("  step_execution_status  VARCHAR(100) NOT NULL,").ln()

        .append("  step_name        TEXT NOT NULL,").ln()
        .append("  step_comment     TEXT NOT NULL,").ln()

        .append("  UNIQUE(runtime_id, consumer_id, step_name)")
        .append(");").ln()

        .append("CREATE INDEX IF NOT EXISTS ").append(options.getRuntimeSteps()).append("_RUNTIME_INDEX")
        .append(" ON ").append(options.getRuntimeSteps()).append(" (runtime_id);").ln()
        
        .append("CREATE INDEX IF NOT EXISTS ").append(options.getRuntimeSteps()).append("_CONSUMER_INDEX")
        .append(" ON ").append(options.getRuntimeSteps()).append(" (consumer_id);").ln()
        
        .append("CREATE INDEX IF NOT EXISTS ").append(options.getRuntimeSteps()).append("_NAME_INDEX")
        .append(" ON ").append(options.getRuntimeSteps()).append(" (step_name);").ln()

        .build()).build();
  }
  
  @Override
  public ThenaSqlClient.Sql createConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
        .ln().append("--- constraints for ").append(options.getRuntimeSteps()).ln()
        
        .append("ALTER TABLE ").append(options.getRuntimeSteps()).ln()
        .append("  ADD CONSTRAINT ").append(options.getRuntimeSteps()).append("_INSTANCE_FK").ln()
        .append("  FOREIGN KEY (runtime_id)").ln()
        .append("  REFERENCES ").append(options.getRuntimeInstances()).append(" (id);").ln().ln()

        .append("ALTER TABLE ").append(options.getRuntimeSteps()).ln()
        .append("  ADD CONSTRAINT ").append(options.getRuntimeSteps()).append("_CONSUMER_FK").ln()
        .append("  FOREIGN KEY (consumer_id)").ln()
        .append("  REFERENCES ").append(options.getBatchConsumers()).append(" (id);").ln().ln()
        
        .build())
        .build();
  }

  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getRuntimeSteps()).append(";").ln()
        .build()).build();
  }

  @Override
  public Function<Row, RuntimeStep> defaultMapper() {
    return row -> ImmutableRuntimeStep.builder()
          .id(row.getString("id"))
          .runtimeId(row.getString("runtime_id"))
          .consumerId(row.getString("consumer_id"))
          
          .createdAt(row.getOffsetDateTime("step_created_at"))

          .name(row.getString("step_name"))
          .comment(row.getString("step_comment"))

          .status(RuntimeStatus.valueOf(row.getString("step_status")))
          .executionStatus(RuntimeExecutionStatus.valueOf(row.getString("step_execution_status")))
          .endedAt(Optional.ofNullable(row.getOffsetDateTime("step_ended_at")))
          
          .build()
    ;
  }
  @Override
  public SqlTuple getById(String id, boolean lockForUpdate) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getRuntimeSteps()).ln()
        .append("  WHERE (id = $1)").ln() 
        .append(lockForUpdate ? "     FOR UPDATE" : "").ln()
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public SqlTuple findAllByInstanceId(String instanceId) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getRuntimeSteps()).ln()
        .append("  WHERE (runtime_id = $1)").ln() 
        .build())
        .props(Tuple.of(instanceId))
        .build();
  }
  @Override
  public SqlTuple findAllByInstanceStatus(List<RuntimeStatus> status) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT steps.* ").ln()
        .append("  FROM ").append(options.getRuntimeSteps()).append(" as steps").ln()
        .append("  LEFT JOIN ").append(options.getRuntimeInstances()).append(" as instances").ln()
        .append("  ON(steps.runtime_id = instances.id)").ln()
        .append("  WHERE (instances.instance_status = ANY($1) OR $1 IS NULL)").ln() 
        .build())
        .props(Tuple.of(status.isEmpty() ? null : status.stream().map(e -> e.name()).toArray()))
        .build();
  }
}
