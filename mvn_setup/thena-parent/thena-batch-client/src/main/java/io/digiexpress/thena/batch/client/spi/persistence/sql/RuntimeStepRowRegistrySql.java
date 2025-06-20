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

import io.digiexpress.thena.batch.client.api.entities.ImmutableRuntimeStepRow;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance.RuntimeExecutionStatus;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance.RuntimeStatus;
import io.digiexpress.thena.batch.client.api.entities.RuntimeStepRow;
import io.digiexpress.thena.batch.client.spi.persistence.BatchTableNames;
import io.digiexpress.thena.batch.client.spi.persistence.RuntimeStepRowRegistry;
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
public class RuntimeStepRowRegistrySql implements RuntimeStepRowRegistry {
  private final BatchTableNames options;
  
  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getRuntimeStepRows())
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getRuntimeStepRows()).ln()
        .append("  WHERE (id = $1)").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public SqlTuple findAllByStepId(String stepId) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getRuntimeStepRows()).ln()
        .append("  WHERE (step_id = $1)").ln() 
        .build())
        .props(Tuple.of(stepId))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTupleList insertMany(List<RuntimeStepRow> docs) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getRuntimeStepRows())
        .append("(")
        .append("  id, step_id, runtime_id, external_id,").ln()
        .append("  row_number, row_created_at, row_ended_at, row_execution_status, row_input, row_output, row_comment ").ln()
        .append(")")
        .append("VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11)").ln()
        .build())
        .props(docs.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getId(), 
                doc.getStepId(),
                doc.getRuntimeId(),
                doc.getExternalId(),
                
                doc.getRowNumber(),
                doc.getCreatedAt(),
                doc.getEndedAt().orElse(null),
                doc.getExecutionStatus().name(),
                
                doc.getInput().orElse(null),
                doc.getOutput().orElse(null),
                
                doc.getComment().orElse(null)
             }))
            .collect(Collectors.toList()))
        .build();
  }


  @Override
  public ThenaSqlClient.Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("CREATE TABLE IF NOT EXISTS ").append(options.getRuntimeStepRows()).ln()
        .append("(").ln()
        .append("  id               VARCHAR(40) PRIMARY KEY,").ln()
        .append("  step_id          VARCHAR(40) NOT NULL,").ln()
        .append("  runtime_id       VARCHAR(40) NOT NULL,").ln()
        .append("  external_id      VARCHAR(40) NOT NULL,").ln()
        
        .append("  row_number       BIGSERIAL NOT NULL,").ln()
        .append("  row_created_at   TIMESTAMP WITH TIME ZONE NOT NULL,").ln()
        .append("  row_ended_at     TIMESTAMP WITH TIME ZONE,").ln()
        
        .append("  row_execution_status VARCHAR(100) NOT NULL,").ln()
        
        .append("  row_input        JSONB,").ln()
        .append("  row_output       JSONB,").ln()
        .append("  row_comment      TEXT,").ln()
        
        .append("  UNIQUE(step_id, row_number)")
        .append(");").ln()

        .append("CREATE INDEX IF NOT EXISTS ").append(options.getRuntimeStepRows()).append("_STEP_INDEX")
        .append(" ON ").append(options.getRuntimeStepRows()).append(" (step_id);").ln()
        
        .append("CREATE INDEX IF NOT EXISTS ").append(options.getRuntimeStepRows()).append("_INSTANCE_INDEX")
        .append(" ON ").append(options.getRuntimeStepRows()).append(" (runtime_id);").ln()
        
        .append("CREATE INDEX IF NOT EXISTS ").append(options.getRuntimeStepRows()).append("_EXT_INDEX")
        .append(" ON ").append(options.getRuntimeStepRows()).append(" (external_id);").ln()

        .append("CREATE INDEX IF NOT EXISTS ").append(options.getRuntimeStepRows()).append("_CREATED_INDEX")
        .append(" ON ").append(options.getRuntimeStepRows()).append(" (row_created_at);").ln()
        
        .append("CREATE INDEX IF NOT EXISTS ").append(options.getRuntimeStepRows()).append("_ROW_NUMBER_INDEX")
        .append(" ON ").append(options.getRuntimeStepRows()).append(" (row_number);").ln()

        .append("CREATE INDEX IF NOT EXISTS ").append(options.getRuntimeStepRows()).append("_ROW_STATUS_INDEX")
        .append(" ON ").append(options.getRuntimeStepRows()).append(" (row_execution_status);").ln()
        
        .build()).build();
  }
  
  @Override
  public ThenaSqlClient.Sql createConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
        .ln().append("--- constraints for ").append(options.getRuntimeStepRows()).ln()
        

        .append("ALTER TABLE ").append(options.getRuntimeStepRows()).ln()
        .append("  ADD CONSTRAINT ").append(options.getRuntimeStepRows()).append("_STEP_FK").ln()
        .append("  FOREIGN KEY (step_id)").ln()
        .append("  REFERENCES ").append(options.getRuntimeSteps()).append(" (id);").ln().ln()
        
        
        .append("ALTER TABLE ").append(options.getRuntimeStepRows()).ln()
        .append("  ADD CONSTRAINT ").append(options.getRuntimeStepRows()).append("_INSTANCE_FK").ln()
        .append("  FOREIGN KEY (runtime_id)").ln()
        .append("  REFERENCES ").append(options.getRuntimeInstances()).append(" (id);").ln().ln()

        
        .build())
        .build();
  }

  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getRuntimeStepRows()).append(";").ln()
        .build()).build();
  }

  @Override
  public Function<Row, RuntimeStepRow> defaultMapper() {
    return row -> ImmutableRuntimeStepRow.builder()
        
      .id(row.getString("id"))
      .stepId(row.getString("step_id"))
      .runtimeId(row.getString("runtime_id"))
      .externalId(row.getString("external_id"))

      .rowNumber(row.getLong("row_number"))
      .createdAt(row.getOffsetDateTime("row_created_at"))

      .input(Optional.ofNullable(row.getJsonObject("row_input")))
      .output(Optional.ofNullable(row.getJsonObject("row_output")))
      .comment(Optional.ofNullable(row.getString("row_comment")))

      .executionStatus(RuntimeExecutionStatus.valueOf(row.getString("row_execution_status")))
      .endedAt(Optional.ofNullable(row.getOffsetDateTime("row_ended_at")))
      
      
      .build();
  }
  
  @Override
  public SqlTuple findAllByInstanceStatus(List<RuntimeStatus> status) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT rows.* ").ln()
        .append("  FROM ").append(options.getRuntimeStepRows()).append(" as rows").ln()
        .append("  LEFT JOIN ").append(options.getRuntimeInstances()).append(" as instances").ln()
        .append("  ON(rows.runtime_id = instances.id)").ln()
        .append("  WHERE (instances.instance_status = ANY($1) OR $1 IS NULL)").ln() 
        .build())
        .props(Tuple.of(status.isEmpty() ? null : status.stream().map(e -> e.name()).toArray()))
        .build();
  }

}
