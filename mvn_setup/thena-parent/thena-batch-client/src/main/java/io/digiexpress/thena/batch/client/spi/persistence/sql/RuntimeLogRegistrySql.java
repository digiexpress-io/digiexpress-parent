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

import io.digiexpress.thena.batch.client.api.entities.ImmutableRuntimeLog;
import io.digiexpress.thena.batch.client.api.entities.RuntimeLog;
import io.digiexpress.thena.batch.client.api.entities.RuntimeLog.ExecutionLogLevel;
import io.digiexpress.thena.batch.client.spi.persistence.BatchTableNames;
import io.digiexpress.thena.batch.client.spi.persistence.RuntimeLogRegistry;
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
public class RuntimeLogRegistrySql implements RuntimeLogRegistry {
  private final BatchTableNames options;
  
  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getRuntimeLogs())
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getRuntimeLogs()).ln()
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
        .append("  FROM ").append(options.getRuntimeLogs()).ln()
        .append("  WHERE (step_id = $1)").ln() 
        .build())
        .props(Tuple.of(stepId))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTupleList insertMany(List<RuntimeLog> logs) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getRuntimeLogs())
        .append("(").ln()
        .append("  id, runtime_id, step_id, step_row_id, ").ln()
        .append("  log_external_id, log_created_at, log_format, log_format_type, log_level, log_extra, log_stack").ln()
        .append(")").ln()
        .append(" VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11)").ln()
        .build())
        .props(logs.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getId(), 
                doc.getRuntimeId(), 
                doc.getStepId().orElse(null),
                doc.getRowId().orElse(null),
                doc.getExternalId().orElse(null),
                
                doc.getCreatedAt(),
                doc.getFormat(),
                doc.getFormatType(), 
                doc.getLevel().name(),
                
                
                doc.getExtra().orElse(null),
                doc.getStack().orElse(null)
             }))
            .collect(Collectors.toList()))
        .build();
  }


  @Override
  public ThenaSqlClient.Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("CREATE TABLE IF NOT EXISTS ").append(options.getRuntimeLogs()).ln()
        .append("(").ln()
        .append("  id             VARCHAR(40) PRIMARY KEY,").ln()
        .append("  runtime_id     VARCHAR(40) NOT NULL,").ln()
        .append("  step_id        VARCHAR(40),").ln()
        .append("  step_row_id    VARCHAR(40),").ln()
        
        .append("  log_external_id    TEXT,").ln()
        .append("  log_created_at     TIMESTAMP WITH TIME ZONE NOT NULL,").ln()
        .append("  log_format         TEXT NOT NULL,").ln()
        .append("  log_format_type    TEXT NOT NULL,").ln()
        .append("  log_level          VARCHAR(100) NOT NULL,").ln()
        .append("  log_extra          JSONB,").ln()
        .append("  log_stack          TEXT").ln()
        
        .append(");").ln()

        .append("CREATE INDEX IF NOT EXISTS ").append(options.getRuntimeLogs()).append("_RUNTIME_INDEX")
        .append(" ON ").append(options.getRuntimeLogs()).append(" (runtime_id);").ln()
        
        .append("CREATE INDEX IF NOT EXISTS ").append(options.getRuntimeLogs()).append("_STEP_INDEX")
        .append(" ON ").append(options.getRuntimeLogs()).append(" (step_id);").ln()
        
        .append("CREATE INDEX IF NOT EXISTS ").append(options.getRuntimeLogs()).append("_STEP_ROW_INDEX")
        .append(" ON ").append(options.getRuntimeLogs()).append(" (step_row_id);").ln()
        
        .append("CREATE INDEX IF NOT EXISTS ").append(options.getRuntimeLogs()).append("_EXT_INDEX")
        .append(" ON ").append(options.getRuntimeLogs()).append(" (log_external_id);").ln()

        .append("CREATE INDEX IF NOT EXISTS ").append(options.getRuntimeLogs()).append("_FORMAT_TYPE_INDEX")
        .append(" ON ").append(options.getRuntimeLogs()).append(" (log_format_type);").ln()
        
        .build()).build();
  }
  
  @Override
  public ThenaSqlClient.Sql createConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
        .ln().append("--- constraints for ").append(options.getRuntimeLogs()).ln()
        
        .append("ALTER TABLE ").append(options.getRuntimeLogs()).ln()
        .append("  ADD CONSTRAINT ").append(options.getRuntimeLogs()).append("_INSTANCE_FK").ln()
        .append("  FOREIGN KEY (runtime_id)").ln()
        .append("  REFERENCES ").append(options.getRuntimeInstances()).append(" (id);").ln().ln()

        
        .append("ALTER TABLE ").append(options.getRuntimeLogs()).ln()
        .append("  ADD CONSTRAINT ").append(options.getRuntimeLogs()).append("_STEP_FK").ln()
        .append("  FOREIGN KEY (step_id)").ln()
        .append("  REFERENCES ").append(options.getRuntimeSteps()).append(" (id);").ln().ln()


        .append("ALTER TABLE ").append(options.getRuntimeLogs()).ln()
        .append("  ADD CONSTRAINT ").append(options.getRuntimeLogs()).append("_STEP_ROW_FK").ln()
        .append("  FOREIGN KEY (step_row_id)").ln()
        .append("  REFERENCES ").append(options.getRuntimeStepRows()).append(" (id);").ln().ln()

        
        .build())
        .build();
  }

  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getRuntimeLogs()).append(";").ln()
        .build()).build();
  }

  @Override
  public Function<Row, RuntimeLog> defaultMapper() {
    return row -> {
      
      


      return ImmutableRuntimeLog.builder()
        .id(row.getString("id"))
        .runtimeId(row.getString("runtime_id"))
        .stepId(Optional.ofNullable(row.getString("step_id")))
        .rowId(Optional.ofNullable(row.getString("step_row_id")))
        
        .externalId(Optional.ofNullable(row.getString("log_external_id")))
        .createdAt(row.getOffsetDateTime("log_created_at"))
        
        .format(row.getString("log_format"))
        .formatType(row.getString("log_format_type"))
        .level(ExecutionLogLevel.valueOf(row.getString("log_level")))
        
        .stack(Optional.ofNullable(row.getString("log_stack")))
        .extra(Optional.ofNullable(row.getJsonObject("log_extra")))
        
        .build();
    };
  }
}
