package io.resys.thena.grim.spi.sql;

/*-
 * #%L
 * thena-grim-client
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

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.function.Function;

import io.resys.thena.api.entities.grim.GrimProcess;
import io.resys.thena.api.entities.grim.ImmutableGrimProcess;
import io.resys.thena.datasource.ImmutableSql;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.grim.spi.datasource.GrimProcessRegistry;
import io.resys.thena.storesql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class GrimProcessRegistrySqlImpl implements GrimProcessRegistry {
  private final GrimTableNames options;

  @Override
  public SqlTuple findOnOrAfter(OffsetDateTime createdOnOrAfter) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT procs.*, mission.mission_ref").ln()        
        .append(" FROM ").append(options.getGrimProcesses()).append(" as procs ")
        
        .append(" LEFT JOIN ").append(options.getGrimMission()).append(" as mission").ln()
        .append(" ON(procs.task_id = mission.id)").ln()
        
        .append(" WHERE procs.created >= $1").ln()
        .build())
        .props(Tuple.of(createdOnOrAfter))
        .build();
  }

  @Override
  public Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT procs.*, mission.mission_ref").ln()        
        .append(" FROM ").append(options.getGrimProcesses()).append(" as procs ")
        
        .append(" LEFT JOIN ").append(options.getGrimMission()).append(" as mission").ln()
        .append(" ON(procs.task_id = mission.id)").ln()
        .build())
        .build();
  }

  @Override
  public SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT procs.*, mission.mission_ref").ln()        
        .append(" FROM ").append(options.getGrimProcesses()).append(" as procs ")
        
        .append(" LEFT JOIN ").append(options.getGrimMission()).append(" as mission").ln()
        .append(" ON(procs.task_id = mission.id)").ln()
        .append(" WHERE mission.mission_ref = $1 OR procs.id = $2 OR mission.id = $1").ln()
        
        .build())
        .props(Tuple.of(id, Long.parseLong(id)))
        .build();
  }

  // TODO NOT USED
  @Override
  public Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getGrimProcesses()).ln()
    .append("(").ln()
    .append(""" 
      id                  BIGSERIAL PRIMARY KEY GENERATED ALWAYS AS IDENTITY),
      article_name        VARCHAR(255) NULL,
      created             TIMESTAMPTZ NOT NULL,
      expires_at          TIMESTAMPTZ NULL,
      expires_in_seconds  INT8 NULL,
      flow_body           JSONB NULL,
      flow_name           VARCHAR(255) NULL,
      form_body           JSONB NULL,
      form_name           VARCHAR(255) NULL,
      form_tag_name       VARCHAR(255) NULL,
      parent_article_name TEXT NULL,
      questionnaire_id    VARCHAR(255) NULL,
      status              VARCHAR(255) NULL,
      stencil_tag_name    TEXT NULL,
      task_id             VARCHAR(255) NULL,
      updated             TIMESTAMPTZ NOT NULL,
      user_id             VARCHAR(255) NULL,
      workflow_name       VARCHAR(255) NOT NULL,
      wrench_tag_name     TEXT NULL,
      anon bool           NULL
    """)
    
    
    .append(");").ln()
    
    .append("CREATE INDEX ").append(options.getGrimProcesses()).append("_CREATED_INDEX")
    .append(" ON ").append(options.getGrimProcesses()).append(" (created);").ln()

    .append("CREATE INDEX ").append(options.getGrimProcesses()).append("_WK_NAME_INDEX")
    .append(" ON ").append(options.getGrimProcesses()).append(" (workflow_name);").ln()

    .append("CREATE INDEX ").append(options.getGrimProcesses()).append("_QUESTIONNAIRE_ID_INDEX")
    .append(" ON ").append(options.getGrimProcesses()).append(" (questionnaire_id);").ln()
    
    .append("CREATE INDEX ").append(options.getGrimProcesses()).append("_STATUS_INDEX")
    .append(" ON ").append(options.getGrimProcesses()).append(" (status);").ln()

    .append("CREATE INDEX ").append(options.getGrimProcesses()).append("_TASK_ID_INDEX")
    .append(" ON ").append(options.getGrimProcesses()).append(" (task_id);").ln()
    
    .append("CREATE INDEX ").append(options.getGrimProcesses()).append("_FORM_NAME_INDEX")
    .append(" ON ").append(options.getGrimProcesses()).append(" (form_name);").ln()
    
    .build()).build();
  }

  // TODO NOT USED
  @Override
  public Sql createConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
    // NONE ATM
    .build()).build();
  }

  // TODO NOT USED
  @Override
  public Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getGrimProcesses()).append(";").ln()
        .build()).build();
  }

  @Override
  public Function<Row, GrimProcess> defaultMapper() {
    return (row) -> {
      
      return ImmutableGrimProcess.builder()
          .id(row.getLong("id").toString())

          .articleName(row.getString("article_name"))
          .created(row.getOffsetDateTime("created"))
          .expiresAt(row.getOffsetDateTime("expires_at"))
          .expiresInSeconds(row.getLong("expires_in_seconds"))
          .flowBody(Optional.ofNullable(row.getJsonObject("flow_body")).map(e -> e.encode()).orElse(null))
          .flowName(row.getString("flow_name"))
          .formBody(Optional.ofNullable(row.getJsonObject("form_body")).map(e -> e.encode()).orElse(null))
          .formName(row.getString("form_name"))
          .formTagName(row.getString("form_tag_name"))
          .parentArticleName(row.getString("parent_article_name"))
          .questionnaireId(row.getString("questionnaire_id"))
          .status(row.getString("status"))
          .stencilTagName(row.getString("stencil_tag_name"))
          .taskId(row.getString("task_id"))
          .taskRef(row.getString("mission_ref"))
          
          
          .updated(row.getOffsetDateTime("updated"))
          .userId(row.getString("user_id"))
          .workflowName(row.getString("workflow_name"))
          .wrenchTagName(row.getString("wrench_tag_name"))
          .anon(row.getBoolean("anon"))

          .build();
    };
  }


}
