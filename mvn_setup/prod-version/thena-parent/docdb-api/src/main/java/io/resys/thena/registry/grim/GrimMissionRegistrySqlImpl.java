package io.resys.thena.registry.grim;

import java.util.ArrayList;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.resys.thena.api.actions.GrimQueryActions.MissionOrderByType;
import io.resys.thena.api.entities.PageQuery.PageSortingOrder;
import io.resys.thena.api.entities.grim.GrimMission;
import io.resys.thena.api.entities.grim.ImmutableGrimMission;
import io.resys.thena.api.entities.grim.ImmutableGrimMissionTransitives;
import io.resys.thena.api.registry.grim.GrimMissionFilter;
import io.resys.thena.api.registry.grim.GrimMissionRegistry;
import io.resys.thena.datasource.ImmutableSql;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.ImmutableSqlTupleList;
import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.datasource.ThenaSqlClient;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.resys.thena.storesql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class GrimMissionRegistrySqlImpl implements GrimMissionRegistry {
  private final TenantTableNames options;
  
  
  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getGrimMission()).append(";").ln()
        .append("DROP SEQUENCE ").append(options.getGrimMissionRef()).append(";").ln()
        .build()).build();
  }
  @Override
  public Sql getNextRefSequence() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("select nextval('").append(options.getGrimMissionRef()).append("')")
        .build())
        .build();
  }

  @Override
  public SqlTuple getNextRefSequence(long howMany) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("select nextval('").append(options.getGrimMissionRef()).append("')").ln()
        .append(" from generate_series(1, $1)")
        .build())
        .props(Tuple.of(howMany))
        .build();
  }
  
  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT ")
        .append(" mission.*, ")
        .append(" updated_commit.created_at       as updated_at,").ln()
        .append(" created_commit.created_at       as created_at,").ln()
        
        .append(" updated_tree_commit.created_at     as tree_updated_at,").ln()
        .append(" updated_tree_commit.commit_author  as tree_updated_by,").ln()
        .append(" mission_data.data_extension        as data_extension ").ln()
        
        .append(" FROM ").append(options.getGrimMission()).append(" as mission ").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimCommit()).append(" as updated_commit").ln()
        .append(" ON(updated_commit.commit_id = mission.commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimCommit()).append(" as created_commit").ln()
        .append(" ON(created_commit.commit_id = mission.created_commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimCommit()).append(" as updated_tree_commit").ln()
        .append(" ON(updated_tree_commit.commit_id = mission.updated_tree_commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimMissionData()).append(" as mission_data").ln()
        .append(" ON(mission_data.mission_id = mission.id and objective_id is null and goal_id is null and remark_id is null)").ln()
        
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT ")
        .append(" mission.*, ")
        .append(" updated_commit.created_at       as updated_at,").ln()
        .append(" created_commit.created_at       as created_at,").ln()

        .append(" updated_tree_commit.created_at     as tree_updated_at,").ln()
        .append(" updated_tree_commit.commit_author  as tree_updated_by,").ln()
        
        .append(" mission_data.data_extension as data_extension ").ln()
        
        .append(" FROM ").append(options.getGrimMission()).append(" as mission ").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimCommit()).append(" as updated_commit").ln()
        .append(" ON(updated_commit.commit_id = mission.commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimCommit()).append(" as created_commit").ln()
        .append(" ON(created_commit.commit_id = mission.created_commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimCommit()).append(" as updated_tree_commit").ln()
        .append(" ON(updated_tree_commit.commit_id = mission.updated_tree_commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimMissionData()).append(" as mission_data").ln()
        .append(" ON(mission_data.mission_id = mission.id and objective_id is null and goal_id is null and remark_id is null)").ln()
        
        .append(" WHERE mission.id = $1").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public SqlTuple findAllByMissionIds(GrimMissionFilter filter) {
    final var where = new GrimMissionSqlFilterBuilder(options).where(filter);
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT ")
        .append(" mission.*, ")
        .append(" updated_commit.created_at       as updated_at,").ln()
        .append(" created_commit.created_at       as created_at,").ln()
        
        .append(" updated_tree_commit.created_at     as tree_updated_at,").ln()
        .append(" updated_tree_commit.commit_author  as tree_updated_by,").ln()
        
        .append(" mission_data.data_extension     as data_extension ").ln()
        
        .append(" FROM ").append(options.getGrimMission()).append(" as mission ").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimCommit()).append(" as updated_commit").ln()
        .append(" ON(updated_commit.commit_id = mission.commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimCommit()).append(" as created_commit").ln()
        .append(" ON(created_commit.commit_id = mission.created_commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimCommit()).append(" as updated_tree_commit").ln()
        .append(" ON(updated_tree_commit.commit_id = mission.updated_tree_commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimMissionData()).append(" as mission_data").ln()
        .append(" ON(mission_data.mission_id = mission.id and objective_id is null and goal_id is null and remark_id is null)").ln()

        .append(where.getValue()) 
        .build())
        .props(where.getProps())
        .build();
  }
  @Override
  public SqlTupleList insertAll(Collection<GrimMission> mission) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getGrimMission()).ln()
        .append(" (id,").ln()
        .append("  commit_id,").ln()

        .append("  parent_mission_id,").ln()
        .append("  external_id,").ln()
        .append("  reporter_id,").ln()
        
        .append("  mission_status,").ln()
        .append("  mission_priority,").ln()
        .append("  mission_start_date,").ln()        
        .append("  mission_due_date,").ln()
        
        .append("  mission_title,").ln()
        .append("  mission_description,").ln()
        .append("  mission_completed_at,").ln()
        
        .append("  archived_at,").ln()
        .append("  archived_status,").ln()
        .append("  mission_ref,").ln()
        .append("  questionnaire_id,").ln()
        
        .append("  created_commit_id,").ln()
        .append("  updated_tree_commit_id)").ln()
        
        .append(" VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14, $15, $16, $17, $18)").ln()
        .build())
        .props(mission.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getId(), 
                doc.getCommitId(),
                doc.getParentMissionId(),
                doc.getExternalId(),
                doc.getReporterId(),
                
                doc.getMissionStatus(),
                doc.getMissionPriority(),
                doc.getStartDate(),
                doc.getDueDate(),
                
                doc.getTitle(),
                doc.getDescription(),
                doc.getCompletedAt(),
                
                doc.getArchivedAt(),
                doc.getArchivedStatus(),
                
                doc.getRefId(),
                doc.getQuestionnaireId(),
                
                doc.getCreatedWithCommitId(),
                doc.getUpdatedTreeWithCommitId()
                
             }))
            .collect(Collectors.toList()))
        .build();
  }
  @Override
  public SqlTupleList updateAll(Collection<GrimMission> mission) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("UPDATE ").append(options.getGrimMission())
        .append(" SET").ln()
        .append("  commit_id = $1,").ln()
        .append("  parent_mission_id = $2,").ln()
        .append("  external_id = $3,").ln()
        .append("  reporter_id = $4,").ln()
        
        .append("  mission_status = $5,").ln()
        .append("  mission_priority = $6,").ln()
        .append("  mission_start_date = $7,").ln()        
        .append("  mission_due_date = $8,").ln()
        .append("  archived_at = $9,").ln()
        .append("  archived_status = $10,").ln()
        
        .append("  created_commit_id = $11,").ln()
        .append("  updated_tree_commit_id = $12,").ln()
        
        .append("  mission_title = $13,").ln()
        .append("  mission_description = $14,").ln()
        .append("  mission_ref = $15,").ln()
        .append("  questionnaire_id = $16").ln()
        .append("  mission_completed_at = $17").ln()
        
        .append(" WHERE id = $18")
        .build())
        .props(mission.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getCommitId(),
                doc.getParentMissionId(),
                doc.getExternalId(),
                doc.getReporterId(),
                doc.getMissionStatus(),
                doc.getMissionPriority(),
                doc.getStartDate(),
                doc.getDueDate(),
                
                doc.getArchivedAt(),
                doc.getArchivedStatus(),
                
                doc.getCreatedWithCommitId(),
                doc.getUpdatedTreeWithCommitId(),
                
                doc.getTitle(),
                doc.getDescription(),
                doc.getRefId(),
                
                doc.getQuestionnaireId(),
                
                doc.getCompletedAt(),
                
                doc.getId()
             }))
            .collect(Collectors.toList()))
        .build();
  }
  @Override
  public Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getGrimMission()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    .append("  created_commit_id VARCHAR(40) NOT NULL,").ln()
    .append("  updated_tree_commit_id VARCHAR(40) NOT NULL,").ln()
    
    .append("  questionnaire_id VARCHAR(40),")
    .append("  parent_mission_id VARCHAR(40),").ln()
    .append("  external_id VARCHAR(40) UNIQUE,").ln()
    .append("  reporter_id VARCHAR(255),").ln()
    
    .append("  mission_ref VARCHAR(40),").ln()
    .append("  mission_status VARCHAR(100),").ln()
    .append("  mission_priority VARCHAR(100),").ln()
    .append("  mission_start_date DATE,").ln()
    .append("  mission_due_date DATE,").ln()
    .append("  mission_title TEXT NOT NULL,").ln()
    .append("  mission_description TEXT,").ln()
    .append("  mission_completed_at TIMESTAMP WITH TIME ZONE,").ln()
    
    .append("  archived_at TIMESTAMP WITH TIME ZONE,").ln()
    .append("  archived_status VARCHAR(40)").ln()
    
    .append(");").ln()
    
    .append("CREATE SEQUENCE ").append(options.getGrimMissionRef()).append(" MINVALUE 1 MAXVALUE 999999 CYCLE;").ln()

    .append("CREATE INDEX ").append(options.getGrimMission()).append("_REF_INDEX")
    .append(" ON ").append(options.getGrimMission()).append(" (mission_ref);").ln()
    
    .append("CREATE INDEX ").append(options.getGrimMission()).append("_QUESTIONNAIRE_ID_INDEX")
    .append(" ON ").append(options.getGrimMission()).append(" (questionnaire_id);").ln()
    
    .append("CREATE INDEX ").append(options.getGrimMission()).append("_PARENT_INDEX")
    .append(" ON ").append(options.getGrimMission()).append(" (parent_mission_id);").ln()

    .append("CREATE INDEX ").append(options.getGrimMission()).append("_COMMIT_INDEX")
    .append(" ON ").append(options.getGrimMission()).append(" (commit_id);").ln()
    
    .append("CREATE INDEX ").append(options.getGrimMission()).append("_CREATED_INDEX")
    .append(" ON ").append(options.getGrimMission()).append(" (created_commit_id);").ln()
    
    .append("CREATE INDEX ").append(options.getGrimMission()).append("_TREE_UPDATED_INDEX")
    .append(" ON ").append(options.getGrimMission()).append(" (updated_tree_commit_id);").ln()
    
    .append("ALTER TABLE ").append(options.getGrimMission()).ln()
    .append("  ADD CONSTRAINT ").append(options.getGrimMission()).append("_PARENT_FK").ln()
    .append("  FOREIGN KEY (parent_mission_id)").ln()
    .append("  REFERENCES ").append(options.getGrimMission()).append(" (id);").ln()
    
    .build()).build();
  }


  @Override
  public Sql createConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
    .ln().append("--- constraints for").append(options.getGrimMission()).ln()    
    .build()).build();
  }


  @Override
  public Function<Row, GrimMission> defaultMapper() {
    return (row) -> {
      
      return ImmutableGrimMission.builder()
          .id(row.getString("id"))
          .commitId(row.getString("commit_id"))
          .createdWithCommitId(row.getString("created_commit_id"))
          .updatedTreeWithCommitId(row.getString("updated_tree_commit_id"))
          .title(row.getString("mission_title"))
          .description(row.getString("mission_description"))
          .refId(row.getString("commit_id"))
          .questionnaireId(row.getString("questionnaire_id"))
          
          .transitives(ImmutableGrimMissionTransitives.builder()
            .dataExtension(row.getJsonObject("data_extension"))
            .updatedAt(row.getOffsetDateTime("updated_at"))
            .createdAt(row.getOffsetDateTime("created_at"))
            .treeUpdatedAt(row.getOffsetDateTime("tree_updated_at"))
            .treeUpdatedBy(row.getString("tree_updated_by"))            
            .build()            
          )
          
          .parentMissionId(row.getString("parent_mission_id"))
          .externalId(row.getString("external_id"))
          .reporterId(row.getString("reporter_id"))
          
          .missionStatus(row.getString("mission_status"))
          .missionPriority(row.getString("mission_priority"))
          .startDate(row.getLocalDate("mission_start_date"))        
          .dueDate(row.getLocalDate("mission_due_date"))
          
          .archivedAt(row.getOffsetDateTime("archived_at"))
          .archivedStatus(row.getString("archived_status"))
          .build();
    };
  }
  @Override
  public SqlTuple count(GrimMissionFilter filter) {
    final var where = new GrimMissionSqlFilterBuilder(options).where(filter);
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT COUNT(mission.id) as mission_count").ln()
        
        .append(" FROM ").append(options.getGrimMission()).append(" as mission ").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimCommit()).append(" as updated_commit").ln()
        .append(" ON(updated_commit.commit_id = mission.commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimCommit()).append(" as created_commit").ln()
        .append(" ON(created_commit.commit_id = mission.created_commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimCommit()).append(" as updated_tree_commit").ln()
        .append(" ON(updated_tree_commit.commit_id = mission.updated_tree_commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimMissionData()).append(" as mission_data").ln()
        .append(" ON(mission_data.mission_id = mission.id and objective_id is null and goal_id is null and remark_id is null)").ln()

        .append(where.getValue()) 
        .build())
        .props(where.getProps())
        .build();
  }
  @Override
  public SqlTuple findAllIdentifiers(GrimMissionFilter filter, List<PageSortingOrder<MissionOrderByType>> orderBy, long offset, long limit) {
    final var params = new ArrayList<Object>();
    final var where = new GrimMissionSqlFilterBuilder(options, params).where(filter);
    final var sorting = new GrimMissionSqlSortingBuilder(options, params, offset, limit).orderBy(orderBy);
    
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT mission.id as mission_id").ln()
        
        .append(" FROM ").append(options.getGrimMission()).append(" as mission ").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimCommit()).append(" as updated_commit").ln()
        .append(" ON(updated_commit.commit_id = mission.commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimCommit()).append(" as created_commit").ln()
        .append(" ON(created_commit.commit_id = mission.created_commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimCommit()).append(" as updated_tree_commit").ln()
        .append(" ON(updated_tree_commit.commit_id = mission.updated_tree_commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimMissionData()).append(" as mission_data").ln()
        .append(" ON(mission_data.mission_id = mission.id and objective_id is null and goal_id is null and remark_id is null)").ln()

        .append(sorting.getOrderByJoins().getValue())
        
        .append(where.getValue()) 
        .append(sorting.getOrderByClause().getValue())
        
        .build())
        .props(Tuple.from(params))
        .build();
  }
  @Override
  public Function<Row, Long> countMapper() {
    return (row) -> row.getLong("mission_count");
  }
  @Override
  public Function<Row, String> idMapper() {
    return (row) -> row.getString("mission_id");
  }
}
