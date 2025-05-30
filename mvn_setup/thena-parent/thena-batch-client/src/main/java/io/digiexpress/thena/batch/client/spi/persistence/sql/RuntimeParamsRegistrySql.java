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

import io.digiexpress.thena.batch.client.api.entities.ImmutableRuntimeParams;
import io.digiexpress.thena.batch.client.api.entities.RuntimeParams;
import io.digiexpress.thena.batch.client.spi.persistence.BatchTableNames;
import io.digiexpress.thena.batch.client.spi.persistence.RuntimeParamsRegistry;
import io.resys.thena.datasource.ImmutableSql;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.ImmutableSqlTupleList;
import io.resys.thena.datasource.ThenaSqlClient;
import io.resys.thena.storesql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class RuntimeParamsRegistrySql implements RuntimeParamsRegistry {
  private final BatchTableNames options;
  
  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getRuntimeParams())
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getRuntimeParams()).ln()
        .append("  WHERE (id = $1)").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }

  @Override
  public ThenaSqlClient.SqlTupleList insertMany(List<RuntimeParams> users) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getRuntimeParams())
        .append("(")
        .append("  id, runtime_id, step_id, step_row_id, ").ln()
        .append("  params_created_at, params_name, params_comment, params_body ").ln()
        .append(")")
        .append(" VALUES($1, $2, $3, $4, $5, $6, $7, $8)").ln()
        .build())
        .props(users.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getId(), 
                doc.getRuntimeId(), 
                doc.getStepId().orElse(null),
                doc.getRowId().orElse(null),
                
                doc.getCreatedAt(),
                doc.getName(),
                doc.getComment(),
                doc.getBody()
                
             }))
            .collect(Collectors.toList()))
        .build();
  }


  @Override
  public ThenaSqlClient.Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("CREATE TABLE IF NOT EXISTS ").append(options.getRuntimeParams()).ln()
        .append("(").ln()
        .append("  id             VARCHAR(40) PRIMARY KEY,").ln()
        .append("  runtime_id     VARCHAR(40) NOT NULL,").ln()
        .append("  step_id        VARCHAR(40),").ln()
        .append("  step_row_id    VARCHAR(40),").ln()
        
        .append("  params_created_at  TIMESTAMP WITH TIME ZONE NOT NULL,").ln()
        .append("  params_name        TEXT NOT NULL,").ln()
        .append("  params_comment     TEXT NOT NULL,").ln()
        .append("  params_body        JSONB NOT NULL,").ln()
        
        .append("  UNIQUE(runtime_id, step_id, params_name)")
        .append(");").ln()

        .append("CREATE INDEX IF NOT EXISTS ").append(options.getRuntimeParams()).append("_RUNTIME_INDEX")
        .append(" ON ").append(options.getRuntimeParams()).append(" (runtime_id);").ln()
        
        .append("CREATE INDEX IF NOT EXISTS ").append(options.getRuntimeParams()).append("_STEP_INDEX")
        .append(" ON ").append(options.getRuntimeParams()).append(" (step_id);").ln()

        .append("CREATE INDEX IF NOT EXISTS ").append(options.getRuntimeParams()).append("_STEP_ROW_INDEX")
        .append(" ON ").append(options.getRuntimeParams()).append(" (step_row_id);").ln()
        
        .append("CREATE INDEX IF NOT EXISTS ").append(options.getRuntimeParams()).append("_NAME_INDEX")
        .append(" ON ").append(options.getRuntimeParams()).append(" (params_name);").ln()
        

        .build()).build();
  }
  
  @Override
  public ThenaSqlClient.Sql createConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
        .ln().append("--- constraints for ").append(options.getRuntimeParams()).ln()
        
        .append("ALTER TABLE ").append(options.getRuntimeParams()).ln()
        .append("  ADD CONSTRAINT ").append(options.getRuntimeParams()).append("_INSTANCE_FK").ln()
        .append("  FOREIGN KEY (runtime_id)").ln()
        .append("  REFERENCES ").append(options.getRuntimeInstances()).append(" (id);").ln().ln()

        
        .append("ALTER TABLE ").append(options.getRuntimeParams()).ln()
        .append("  ADD CONSTRAINT ").append(options.getRuntimeParams()).append("_STEP_FK").ln()
        .append("  FOREIGN KEY (step_id)").ln()
        .append("  REFERENCES ").append(options.getRuntimeSteps()).append(" (id);").ln().ln()


        .append("ALTER TABLE ").append(options.getRuntimeParams()).ln()
        .append("  ADD CONSTRAINT ").append(options.getRuntimeParams()).append("_STEP_ROW_FK").ln()
        .append("  FOREIGN KEY (step_row_id)").ln()
        .append("  REFERENCES ").append(options.getRuntimeStepRows()).append(" (id);").ln().ln()

        .build())
        .build();
  }

  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getRuntimeParams()).append(";").ln()
        .build()).build();
  }

  @Override
  public Function<Row, RuntimeParams> defaultMapper() {
    return row -> ImmutableRuntimeParams.builder()
        
          .id(row.getString("id"))
          .runtimeId(row.getString("runtime_id"))
          .stepId(Optional.ofNullable(row.getString("step_id")))
          .rowId(Optional.ofNullable(row.getString("step_row_id")))

          .createdAt(row.getOffsetDateTime("params_created_at"))
          .name(row.getString("params_name"))
          .comment(row.getString("params_comment"))
          .body(row.getJsonObject("params_body"))
          
          .build()
    ;
  }

}
