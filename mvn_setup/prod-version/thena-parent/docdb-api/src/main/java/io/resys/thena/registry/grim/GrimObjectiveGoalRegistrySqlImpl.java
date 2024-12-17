package io.resys.thena.registry.grim;

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
import java.util.function.Function;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.grim.GrimObjectiveGoal;
import io.resys.thena.api.entities.grim.ImmutableGrimObjectiveGoal;
import io.resys.thena.api.entities.grim.ImmutableGrimObjectiveGoalTransitives;
import io.resys.thena.api.registry.grim.GrimMissionFilter;
import io.resys.thena.api.registry.grim.GrimObjectiveGoalRegistry;
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
public class GrimObjectiveGoalRegistrySqlImpl implements GrimObjectiveGoalRegistry {
  private final TenantTableNames options;
  
  
  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getGrimObjectiveGoal()).append(";").ln()
        .build()).build();
  }
  @Override
  public ThenaSqlClient.Sql findAll() {

    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT goal.*,").ln()
        .append(" objective.mission_id            as mission_id,").ln()
        .append(" updated_commit.created_at       as updated_at,").ln()
        .append(" created_commit.created_at       as created_at,").ln()
        .append(" mission_data.data_extension as data_extension ").ln()
        
        .append(" FROM ").append(options.getGrimObjectiveGoal()).append(" as goal ").ln()
        .append(" LEFT JOIN ").append(options.getGrimObjective()).append(" as objective").ln()
        .append(" ON(goal.objective_id = objective.id)").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimCommit()).append(" as updated_commit").ln()
        .append(" ON(updated_commit.commit_id = goal.commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimCommit()).append(" as created_commit").ln()
        .append(" ON(created_commit.commit_id = goal.created_commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimMissionData()).append(" as mission_data").ln()
        .append(" ON(mission_data.goal_id = goal.id)").ln()
        
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT goal.*,").ln()
        .append(" objective.mission_id            as mission_id,").ln()
        .append(" updated_commit.created_at       as updated_at,").ln()
        .append(" created_commit.created_at       as created_at,").ln()
        .append(" mission_data.data_extension as data_extension ").ln()
        
        .append(" FROM ").append(options.getGrimObjectiveGoal()).append(" as goal ").ln()
        .append(" LEFT JOIN ").append(options.getGrimObjective()).append(" as objective").ln()
        .append(" ON(goal.objective_id = objective.id)").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimCommit()).append(" as updated_commit").ln()
        .append(" ON(updated_commit.commit_id = goal.commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimCommit()).append(" as created_commit").ln()
        .append(" ON(created_commit.commit_id = goal.created_commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimMissionData()).append(" as mission_data").ln()
        .append(" ON(mission_data.goal_id = goal.id)").ln()
        
        .append(" WHERE goal.id = $1").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public SqlTuple findAllByMissionIds(GrimMissionFilter filter) {
    final var where = new GrimMissionSqlFilterBuilder(options).where(filter);
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT goal.*,").ln()
        .append(" objective.mission_id            as mission_id,").ln()
        .append(" updated_commit.created_at       as updated_at,").ln()
        .append(" created_commit.created_at       as created_at,").ln()
        .append(" mission_data.data_extension as data_extension ").ln()
        
        .append(" FROM ").append(options.getGrimObjectiveGoal()).append(" as goal ").ln()
        .append(" LEFT JOIN ").append(options.getGrimObjective()).append(" as objective").ln()
        .append(" ON(goal.objective_id = objective.id)").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimCommit()).append(" as updated_commit").ln()
        .append(" ON(updated_commit.commit_id = goal.commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimCommit()).append(" as created_commit").ln()
        .append(" ON(created_commit.commit_id = goal.created_commit_id)").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimMissionData()).append(" as mission_data").ln()
        .append(" ON(mission_data.goal_id = goal.id)").ln()
        
        .append(" LEFT JOIN ").append(options.getGrimMission()).append(" as mission")
        .append(" ON(objective.mission_id = mission.id)")
        .append(where.getValue()) 
        .build())
        .props(where.getProps())
        .build();
  }
  @Override
  public SqlTupleList insertAll(Collection<GrimObjectiveGoal> goals) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getGrimObjectiveGoal()).ln()
        .append(" (id,").ln()
        .append("  commit_id,").ln()
        .append("  created_commit_id,").ln()
        
        .append("  objective_id,").ln()
        .append("  goal_status,").ln()
        .append("  goal_start_date,").ln()
        
        .append("  goal_title,").ln()
        .append("  goal_description,").ln()
        
        .append("  goal_due_date)").ln()
        
        .append(" VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9)").ln()
        .build())
        .props(goals.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getId(), 
                doc.getCommitId(),
                doc.getCreatedWithCommitId(),
                doc.getObjectiveId(),
                doc.getGoalStatus(),
                doc.getStartDate(),
                doc.getTitle(),
                doc.getDescription(),
                doc.getDueDate()
             }))
            .collect(Collectors.toList()))
        .build();
  }
  @Override
  public SqlTupleList updateAll(Collection<GrimObjectiveGoal> goals) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("UPDATE ").append(options.getGrimObjectiveGoal())
        .append(" SET").ln()
        .append("  commit_id = $1,").ln()

        .append("  goal_status = $2,").ln()
        .append("  goal_start_date = $3,").ln()
        .append("  goal_due_date = $4,").ln()
        
        .append("  goal_title = $5,").ln()
        .append("  goal_description = $6").ln()
        
        .append(" WHERE id = $7")
        .build())
        .props(goals.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getCommitId(),
                doc.getGoalStatus(),
                doc.getStartDate(),
                doc.getDueDate(),
                doc.getTitle(),
                doc.getDescription(),
                
                doc.getId(), 
             }))
            .collect(Collectors.toList()))
        .build();
  }
  @Override
  public Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getGrimObjectiveGoal()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    .append("  created_commit_id VARCHAR(40) NOT NULL,").ln()
    .append("  objective_id VARCHAR(40) NOT NULL,").ln()
    .append("  goal_status VARCHAR(100),").ln()
    .append("  goal_start_date DATE,").ln()
    .append("  goal_due_date DATE,").ln()
    .append("  goal_title TEXT NOT NULL,").ln()
    .append("  goal_description TEXT").ln()
    .append(");").ln()    
    
    .append("CREATE INDEX ").append(options.getGrimObjectiveGoal()).append("_CREATED_INDEX")
    .append(" ON ").append(options.getGrimObjectiveGoal()).append(" (created_commit_id);").ln()
    
    .append("CREATE INDEX ").append(options.getGrimObjectiveGoal()).append("_OBJECTIVE_INDEX")
    .append(" ON ").append(options.getGrimObjectiveGoal()).append(" (objective_id);").ln()
    
    .build()).build();
  }


  @Override
  public Sql createConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
    .ln().append("--- constraints for").append(options.getGrimObjectiveGoal()).ln()
    
    .append("ALTER TABLE ").append(options.getGrimObjectiveGoal()).ln()
    .append("  ADD CONSTRAINT ").append(options.getGrimObjectiveGoal()).append("_OBJECTIVE_FK").ln()
    .append("  FOREIGN KEY (objective_id)").ln()
    .append("  REFERENCES ").append(options.getGrimObjective()).append(" (id);").ln().ln()

    .build()).build();
  }


  @Override
  public Function<Row, GrimObjectiveGoal> defaultMapper() {
    return (row) -> {

      
      return ImmutableGrimObjectiveGoal.builder()
          .id(row.getString("id"))
          .commitId(row.getString("commit_id"))
          .createdWithCommitId(row.getString("created_commit_id"))
          .title(row.getString("goal_title"))
          .description(row.getString("goal_description"))
          
          .transitives(ImmutableGrimObjectiveGoalTransitives.builder()
            .missionId(row.getString("mission_id"))
            .dataExtension(row.getJsonObject("data_extension"))
            .updatedAt(row.getOffsetDateTime("updated_at"))
            .createdAt(row.getOffsetDateTime("created_at"))
            .build()
          )
          
          .objectiveId(row.getString("objective_id"))
          .goalStatus(row.getString("goal_status"))
          .startDate(row.getLocalDate("goal_start_date"))
          .dueDate(row.getLocalDate("goal_due_date"))
          
          .build();
    };
  }

  @Override
  public SqlTupleList deleteAll(Collection<GrimObjectiveGoal> goals) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("DELETE FROM ").append(options.getGrimObjectiveGoal())
        .append(" WHERE id = $1")
        .build())
        .props(goals.stream()
            .map(doc -> Tuple.from(new Object[]{doc.getId()}))
            .collect(Collectors.toList()))
        .build();
  }

}
