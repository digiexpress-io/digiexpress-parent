package io.resys.thena.registry.grim;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.grim.GrimObjectiveGoal;
import io.resys.thena.api.entities.grim.ImmutableGrimObjectiveGoal;
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
        .append("SELECT goal.*, objective.mission_id as mission_id ").ln()
        .append(" FROM ").append(options.getGrimObjectiveGoal()).append(" as goal ").ln()
        .append(" RIGHT JOIN ").append(options.getGrimObjective()).append(" as objective").ln()
        .append(" ON(goal.objective_id = objective.id)").ln()
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT goal.*, objective.mission_id as mission_id ").ln()
        .append(" FROM ").append(options.getGrimObjectiveGoal()).append(" as goal ").ln()
        .append(" RIGHT JOIN ").append(options.getGrimObjective()).append(" as objective").ln()
        .append(" ON(goal.objective_id = objective.id)").ln()
        .append(" WHERE (goal.id = $1)").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public SqlTuple findAllByMissionIds(Collection<String> id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT goal.*, objective.mission_id as mission_id ").ln()
        .append(" FROM ").append(options.getGrimObjectiveGoal()).append(" as goal ").ln()
        .append(" RIGHT JOIN ").append(options.getGrimObjective()).append(" as objective").ln()
        .append(" ON(goal.objective_id = objective.id)").ln()
        .append(" WHERE (objective.mission_id = ANY($1))").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public SqlTupleList insertAll(Collection<GrimObjectiveGoal> goals) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getGrimObjectiveGoal()).ln()
        .append(" (id,").ln()
        .append("  commit_id,").ln()

        .append("  objective_id,").ln()
        .append("  goal_status,").ln()
        .append("  goal_start_date,").ln()
        .append("  goal_due_date)").ln()
        
        .append(" VALUES($1, $2, $3, $4, $5, $6)").ln()
        .build())
        .props(goals.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getId(), 
                doc.getCommitId(),
                doc.getObjectiveId(),
                doc.getGoalStatus(),
                doc.getStartDate(),
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
        .append("  goal_due_date = $4").ln()
        .append(" WHERE id = $5")
        .build())
        .props(goals.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getCommitId(),
                doc.getGoalStatus(),
                doc.getStartDate(),
                doc.getDueDate(),
                
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
    .append("  objective_id VARCHAR(40) NOT NULL,").ln()
    .append("  goal_status VARCHAR(100),").ln()
    .append("  goal_start_date DATE,").ln()
    .append("  goal_due_date DATE").ln()
    .append(");").ln()    
    
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
          .missionId(row.getString("mission_id"))
          
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