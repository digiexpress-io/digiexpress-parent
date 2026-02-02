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
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.grim.GrimProcess;
import io.resys.thena.api.entities.grim.GrimProcess.GrimProcessStatus;
import io.resys.thena.api.entities.grim.GrimProcess.GrimProcessType;
import io.resys.thena.api.entities.grim.ImmutableGrimProcess;
import io.resys.thena.datasource.ImmutableSql;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.ImmutableSqlTupleList;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.resys.thena.grim.spi.datasource.GrimProcessRegistry;
import io.resys.thena.storesql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class GrimProcessRegistrySqlImpl implements GrimProcessRegistry {
  private final GrimTableNames options;


  @Override
  public Sql findAll() {
    return findAll(false);
  }

  @Override
  public SqlTuple getById(String id) {
    return getById(id, false);
  }

  @Override
  public SqlTuple findOneByMissionId(String missionId, boolean includeFormBody) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT ").append(getColumns(includeFormBody)).ln()        
        .append(" FROM ").append(options.getGrimProcesses()).append(" as procs ")
        
        .append(" LEFT JOIN ").append(options.getGrimMission()).append(" as mission").ln()
        .append(" ON(procs.task_id = mission.id)").ln()

        .append(" WHERE (procs.task_id = $1 OR mission.mission_ref = $1) and procs.type is null").ln()
        .build())
        .props(Tuple.of(missionId))
        .build();
  }
  
  @Override
  public SqlTuple findNotArchivedByUserId(String userId, boolean includeFormBody) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT ").append(getColumns(includeFormBody)).ln()        
        .append(" FROM ").append(options.getGrimProcesses()).append(" as procs ")
        
        .append(" LEFT JOIN ").append(options.getGrimMission()).append(" as mission").ln()
        .append(" ON(procs.task_id = mission.id)").ln()
        
        .append(" WHERE procs.user_id = $1 and mission.archived_at is null").ln()
        .append(" AND procs.status <> $2").ln()
        .build())
        .props(Tuple.of(userId, "EXPIRED"))
        .build();
  }
  
  @Override
  public SqlTupleList updateAll(Collection<GrimProcess> procs) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("UPDATE ").append(options.getGrimProcesses()).append(" ").ln()   
        .append("""
SET
  updated = $1,
  status = $2,
  expires_at = $3,
  expires_in_seconds = $4,
  task_id = $5,
  flow_name = $6,
  flow_body = COALESCE($7, flow_body),
  form_body = COALESCE($8, form_body)
WHERE id = $9""").ln()
        .build())
        .props(procs.stream()
            .map(proc -> Tuple.from(new Object[]{ 
                proc.getUpdated(),
                Optional.ofNullable(proc.getStatus()).map(e -> e.name()).orElse(null),
                proc.getExpiresAt(),
                proc.getExpiresInSeconds(),
                proc.getMissionId(),
                proc.getFlowName(),
                proc.getFlowBody(),
                proc.getFormBody(),
                Long.parseLong(proc.getId())
             }))
            .collect(Collectors.toList()))
        .build();
  }

  @Override
  public SqlTupleList insertAll(Collection<GrimProcess> procs) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getGrimProcesses()).append(" ").ln()        
        .append("""
  (
  id,
  type,
  created,
  updated,
  flow_name,
  flow_body,
  workflow_name,
  status,
  expires_at,
  expires_in_seconds,
  questionnaire_id,
  cockpit_id,
  user_id,
  anon,
  article_name,
  parent_article_name,
  form_name,
  form_body,
  form_tag_name,
  stencil_tag_name,
  wrench_tag_name)""").ln()
        .append(" VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14, $15, $16, $17, $18, $19, $20, $21)").ln()
        .build())
        .props(procs.stream()
            .map(proc -> Tuple.from(new Object[]{
                Long.parseLong(proc.getId()),
                Optional.ofNullable(proc.getType()).map(e -> e.name()).orElse(null),
                proc.getCreated(),
                proc.getUpdated(),
                proc.getFlowName(),
                proc.getFlowBody(),
                proc.getWorkflowName(),
                Optional.ofNullable(proc.getStatus()).map(e -> e.name()).orElse(null),
                proc.getExpiresAt(),
                proc.getExpiresInSeconds(),
                proc.getQuestionnaireId(),
                proc.getCockpitId(),
                proc.getUserId(),
                proc.getAnon(),
                proc.getArticleName(),
                proc.getParentArticleName(),
                proc.getFormName(),
                proc.getFormBody(),
                proc.getFormTagName(),
                proc.getStencilTagName(),
                proc.getWrenchTagName()
             }))
            .collect(Collectors.toList()))
        .build();
  }
  
  @Override
  public SqlTuple findOnOrAfter(OffsetDateTime createdOnOrAfter, boolean includeFormBody) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT ").append(getColumns(includeFormBody)).ln()        
        .append(" FROM ").append(options.getGrimProcesses()).append(" as procs ")
        
        .append(" LEFT JOIN ").append(options.getGrimMission()).append(" as mission").ln()
        .append(" ON(procs.task_id = mission.id)").ln()
        
        .append(" WHERE procs.created >= $1").ln()
        .build())
        .props(Tuple.of(createdOnOrAfter))
        .build();
  }

  @Override
  public Sql findAll(boolean includeFormBody) {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT ").append(getColumns(includeFormBody)).ln()        
        .append(" FROM ").append(options.getGrimProcesses()).append(" as procs ")
        
        .append(" LEFT JOIN ").append(options.getGrimMission()).append(" as mission").ln()
        .append(" ON(procs.task_id = mission.id)").ln()
        .build())
        .build();
  }

  @Override
  public SqlTuple findOnOrBeforeWithoutMission(OffsetDateTime onOrBefore, boolean includeFormBody) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT ").append(getColumns(includeFormBody)).ln()        
        .append(" FROM ").append(options.getGrimProcesses()).append(" AS procs ")
        
        .append(" WHERE procs.created <= $1 ").ln()
        .append(" AND procs.task_id IS NULL").ln()
        .append(" AND (procs.status IN('CREATED', 'ANSWERING') OR procs.status IS NULL)").ln()

        .build())
        .props(Tuple.of(onOrBefore))
        .build();
  }

  
  @Override
  public SqlTuple getById(String id, boolean includeFormBody) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT ").append(getColumns(includeFormBody)).ln()        
        .append(" FROM ").append(options.getGrimProcesses()).append(" as procs ")
        
        .append(" LEFT JOIN ").append(options.getGrimMission()).append(" as mission").ln()
        .append(" ON(procs.task_id = mission.id)").ln()
        .append(" WHERE mission.mission_ref = $1 OR procs.id = $2 OR mission.id = $1").ln()
        
        .build())
        .props(Tuple.of(id, Long.parseLong(id)))
        .build();
  }
  @Override
  public SqlTuple getOneByIdWithLock(String id, boolean includeFormBody) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT ").append(getColumns(includeFormBody)).ln()        
        .append(" FROM ").append(options.getGrimProcesses()).append(" as procs ")
        
        .append(" LEFT JOIN ").append(options.getGrimMission()).append(" as mission").ln()
        .append(" ON(procs.task_id = mission.id)").ln()
        .append(" WHERE mission.mission_ref = $1 OR procs.id = $2 OR mission.id = $1").ln()
        .append(" FOR UPDATE OF procs").ln()
        
        .build())
        .props(Tuple.of(id, Long.parseLong(id)))
        .build();
  }
  
  

  @Override
  public Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    
    .append("CREATE SEQUENCE IF NOT EXISTS ").append(options.getGrimProcessSeq()).ln()
    .append("""
      INCREMENT BY 1
      MINVALUE 1
      MAXVALUE 9223372036854775807
      START 1
      CACHE 1
      NO CYCLE;
    """).ln()
        
    .append("CREATE TABLE IF NOT EXISTS ").append(options.getGrimProcesses()).ln()
    .append("""
    (
      id                  BIGINT PRIMARY KEY DEFAULT nextval('process_id_seq'::regclass),
      type                VARCHAR(255) NULL,
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
      cockpit_id          VARCHAR(255) NULL,
      updated             TIMESTAMPTZ NOT NULL,
      user_id             VARCHAR(255) NULL,
      workflow_name       VARCHAR(255) NOT NULL,
      wrench_tag_name     TEXT NULL,
      anon                BOOL NULL
    );
    """).ln()
    
    .append("CREATE INDEX IF NOT EXISTS ").append(options.getGrimProcesses()).append("_CREATED_INDEX")
    .append(" ON ").append(options.getGrimProcesses()).append(" (created);").ln()

    .append("CREATE INDEX IF NOT EXISTS ").append(options.getGrimProcesses()).append("_TYPE_INDEX")
    .append(" ON ").append(options.getGrimProcesses()).append(" (type);").ln()
    
    .append("CREATE INDEX IF NOT EXISTS ").append(options.getGrimProcesses()).append("_WK_NAME_INDEX")
    .append(" ON ").append(options.getGrimProcesses()).append(" (workflow_name);").ln()

    .append("CREATE INDEX IF NOT EXISTS ").append(options.getGrimProcesses()).append("_QUESTIONNAIRE_ID_INDEX")
    .append(" ON ").append(options.getGrimProcesses()).append(" (questionnaire_id);").ln()
    
    .append("CREATE INDEX IF NOT EXISTS ").append(options.getGrimProcesses()).append("_STATUS_INDEX")
    .append(" ON ").append(options.getGrimProcesses()).append(" (status);").ln()

    .append("CREATE INDEX IF NOT EXISTS ").append(options.getGrimProcesses()).append("_TASK_ID_INDEX")
    .append(" ON ").append(options.getGrimProcesses()).append(" (task_id);").ln()
    
    .append("CREATE INDEX IF NOT EXISTS ").append(options.getGrimProcesses()).append("_FORM_NAME_INDEX")
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
        .append("DROP TABLE ").append(options.getGrimProcessSeq()).append(";").ln()
        .build()).build();
  }
  
  private String getColumns(boolean includeFormBody) {
    final var optional = includeFormBody ? 
        "flow_body, form_body," : 
        "null as flow_body, null as form_body,";
    
    return optional + 
"""
  procs.id,
  procs.type,
  procs.article_name,
  procs.created,
  procs.expires_at,
  procs.expires_in_seconds,
  procs.flow_name,
  procs.form_name,
  procs.form_tag_name,
  procs.parent_article_name,
  procs.questionnaire_id,
  procs.status,
  procs.stencil_tag_name,
  procs.task_id,
  procs.cockpit_id,
  procs.updated,
  procs.user_id,
  procs.workflow_name,
  procs.wrench_tag_name,
  procs.anon,
  mission.mission_ref
""";
  }

  @Override
  public Function<Row, GrimProcess> defaultMapper() {
    return (row) -> {
      
      return ImmutableGrimProcess.builder()
          .id(row.getLong("id").toString())

          .articleName(row.getString("article_name"))
          .created(row.getOffsetDateTime("created"))
          .type(Optional.ofNullable(row.getString("type")).map(e -> GrimProcessType.valueOf(e)).orElse(null))
          .expiresAt(row.getOffsetDateTime("expires_at"))
          .expiresInSeconds(row.getLong("expires_in_seconds"))
          .flowBody(Optional.ofNullable(row.getJsonObject("flow_body")).orElse(null))
          .flowName(row.getString("flow_name"))
          .formBody(Optional.ofNullable(row.getJsonObject("form_body")).orElse(null))
          .formName(row.getString("form_name"))
          .formTagName(row.getString("form_tag_name"))
          .parentArticleName(row.getString("parent_article_name"))
          .questionnaireId(row.getString("questionnaire_id"))
          .status(GrimProcessStatus.valueOf(row.getString("status")))
          .stencilTagName(row.getString("stencil_tag_name"))
          .missionId(row.getString("task_id"))
          .cockpitId(row.getString("cockpit_id"))
          .missionRef(row.getString("mission_ref"))
          
          
          .updated(row.getOffsetDateTime("updated"))
          .userId(row.getString("user_id"))
          .workflowName(row.getString("workflow_name"))
          .wrenchTagName(row.getString("wrench_tag_name"))
          .anon(row.getBoolean("anon"))

          .build();
    };
  }

  @Override
  public Sql getNextSequence() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("select nextval('").append(options.getGrimProcessSeq()).append("')")
        .build())
        .build();
  }

  @Override
  public SqlTuple getNextSequence(long howMany) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("select nextval('").append(options.getGrimProcessSeq()).append("')").ln()
        .append(" from generate_series(1, $1)")
        .build())
        .props(Tuple.of(howMany))
        .build();
  }
  
  
  

  @Override
  public SqlTuple deleteOneById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("DELETE FROM ").append(options.getGrimProcesses())
        .append(" WHERE id = $1")
        .build())
        .props(Tuple.of(id))
        .build();
  }

  @Override
  public SqlTuple findAllExpired(boolean includeFormBody) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT ").append(getColumns(includeFormBody)).ln()        
        .append(" FROM ").append(options.getGrimProcesses()).append(" AS procs ")
        .append(" LEFT JOIN ").append(options.getGrimMission()).append(" as mission").ln()
        .append(" ON(procs.task_id = mission.id)").ln()
        .append(
"""
  WHERE procs.expires_in_seconds is not null
  and procs.created + make_interval(secs => procs.expires_in_seconds) < procs.expires_at
  and procs.status IN($1)            
"""
        ).ln()
        .build())
        .props(Tuple.of(
            new String[] {GrimProcessStatus.ANSWERED.name(), GrimProcessStatus.CREATED.name()}
        ))
        .build();
  }

  @Override
  public SqlTuple findAllAnsweredFrom(OffsetDateTime pickupFrom, boolean includeFormBody) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT ").append(getColumns(includeFormBody)).ln()
        .append(" FROM ").append(options.getGrimProcesses()).append(" AS procs ")
        
        .append(" LEFT JOIN ").append(options.getGrimMission()).append(" as mission").ln()
        .append(" ON(procs.task_id = mission.id)").ln()
        
        .append(" WHERE procs.status = ANY($1)").ln()
        .append(" AND procs.task_id IS NULL").ln()
        .append(" AND procs.created >= $2").ln()
        
        .build())
        .props(Tuple.of(
            new String[] {GrimProcessStatus.ANSWERED.name(), GrimProcessStatus.CREATED.name()},
            pickupFrom
        ))
        .build();
  }

  @Override
  public SqlTuple findOneByQuestionnaireId(String questionnaireId, boolean includeFormBody) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT ").append(getColumns(includeFormBody)).ln()
        .append(" FROM ").append(options.getGrimProcesses()).append(" AS procs ")
        
        .append(" LEFT JOIN ").append(options.getGrimMission()).append(" as mission").ln()
        .append(" ON(procs.task_id = mission.id)").ln()
        
        .append(" WHERE procs.questionnaire_id = $1").ln()
        
        .build())
        .props(Tuple.of(
            questionnaireId
        ))
        .build();
  }

}
