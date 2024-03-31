package io.resys.thena.registry.grim;

import java.util.function.Function;

import io.resys.thena.api.entities.grim.GrimObjectiveGoal;
import io.resys.thena.api.entities.grim.ImmutableGrimObjectiveGoal;
import io.resys.thena.api.registry.grim.GrimObjectiveGoalRegistry;
import io.resys.thena.datasource.ImmutableSql;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.datasource.ThenaSqlClient;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
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
        .append("SELECT * FROM ").append(options.getGrimObjectiveGoal())
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getGrimObjectiveGoal()).ln()
        .append("  WHERE (id = $1)").ln() 
        .build())
        .props(Tuple.of(id))
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
    .append("  goal_start_date TIMESTAMP,").ln()
    .append("  goal_due_date TIMESTAMP").ln()
    
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
      
      return ImmutableGrimObjectiveGoal.builder().build();
    };
  }

}