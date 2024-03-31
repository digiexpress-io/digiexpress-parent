package io.resys.thena.registry.grim;

import java.util.Collection;
import java.util.function.Function;

import io.resys.thena.api.entities.grim.GrimAssignment;
import io.resys.thena.api.entities.grim.ImmutableGrimAssignment;
import io.resys.thena.api.registry.grim.GrimAssignmentRegistry;
import io.resys.thena.datasource.ImmutableSql;
import io.resys.thena.datasource.ImmutableSqlTuple;
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
public class GrimAssignmentRegistrySqlImpl implements GrimAssignmentRegistry {
  private final TenantTableNames options;
  
  
  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getGrimAssignment()).append(";").ln()
        .build()).build();
  }


  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getGrimAssignment())
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getGrimAssignment()).ln()
        .append("  WHERE (id = $1)").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  
  @Override
  public SqlTuple findAllByMissionIds(Collection<String> id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getGrimAssignment()).ln()
        .append("  WHERE (mission_id = ANY($1))").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }


  @Override
  public SqlTupleList insertAll(Collection<GrimAssignment> users) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SqlTupleList deleteAll(Collection<GrimAssignment> assignments) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getGrimAssignment()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    
    .append("  mission_id VARCHAR(40) NOT NULL,").ln()
    .append("  objective_id VARCHAR(40),").ln()
    .append("  goal_id VARCHAR(40),").ln()
    .append("  remark_id VARCHAR(40),").ln()

    .append("  assignee VARCHAR(255) NOT NULL,").ln()
    .append("  assignment_type VARCHAR(100) NOT NULL,").ln()
    .append("  UNIQUE NULLS NOT DISTINCT(mission_id, objective_id, goal_id, remark_id, assignee, assignment_type)").ln()
    
    .append(");").ln()
    
    
    .append("CREATE INDEX ").append(options.getGrimAssignment()).append("_MISSION_INDEX")
    .append(" ON ").append(options.getGrimAssignment()).append(" (mission_id);").ln()
    
    .append("CREATE INDEX ").append(options.getGrimAssignment()).append("_OBJECTIVE_INDEX")
    .append(" ON ").append(options.getGrimAssignment()).append(" (objective_id);").ln()
    
    .append("CREATE INDEX ").append(options.getGrimAssignment()).append("_GOAL_INDEX")
    .append(" ON ").append(options.getGrimAssignment()).append(" (goal_id);").ln()
    
    .append("CREATE INDEX ").append(options.getGrimAssignment()).append("_REMARK_INDEX")
    .append(" ON ").append(options.getGrimAssignment()).append(" (remark_id);").ln()
    
    
    .build()).build();
  }


  @Override
  public Sql createConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
    .ln().append("--- constraints for").append(options.getGrimAssignment()).ln()
    
    .append("ALTER TABLE ").append(options.getGrimAssignment()).ln()
    .append("  ADD CONSTRAINT ").append(options.getGrimAssignment()).append("_MISSION_FK").ln()
    .append("  FOREIGN KEY (mission_id)").ln()
    .append("  REFERENCES ").append(options.getGrimMission()).append(" (id);").ln().ln()
    
    .append("ALTER TABLE ").append(options.getGrimAssignment()).ln()
    .append("  ADD CONSTRAINT ").append(options.getGrimAssignment()).append("_OBJECTIVE_FK").ln()
    .append("  FOREIGN KEY (objective_id)").ln()
    .append("  REFERENCES ").append(options.getGrimObjective()).append(" (id);").ln().ln()
    
    .append("ALTER TABLE ").append(options.getGrimAssignment()).ln()
    .append("  ADD CONSTRAINT ").append(options.getGrimAssignment()).append("_GOAL_FK").ln()
    .append("  FOREIGN KEY (goal_id)").ln()
    .append("  REFERENCES ").append(options.getGrimObjectiveGoal()).append(" (id);").ln().ln()
    
    .append("ALTER TABLE ").append(options.getGrimAssignment()).ln()
    .append("  ADD CONSTRAINT ").append(options.getGrimAssignment()).append("_REMARK_FK").ln()
    .append("  FOREIGN KEY (remark_id)").ln()
    .append("  REFERENCES ").append(options.getGrimRemark()).append(" (id);").ln().ln()

    
    .build()).build();
  }
  @Override
  public Function<Row, GrimAssignment> defaultMapper() {
    return (row) -> {      
      return ImmutableGrimAssignment.builder().build();
    };
  }

}