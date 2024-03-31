package io.resys.thena.registry.grim;

import java.util.function.Function;

import io.resys.thena.api.entities.grim.GrimMissionData;
import io.resys.thena.api.entities.grim.ImmutableGrimMissionData;
import io.resys.thena.api.registry.grim.GrimMissionDataRegistry;
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
public class GrimMissionDataRegistrySqlImpl implements GrimMissionDataRegistry {
  private final TenantTableNames options;
  
  
  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getGrimMissionData()).append(";").ln()
        .build()).build();
  }
  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getGrimMissionData())
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getGrimMissionData()).ln()
        .append("  WHERE (id = $1)").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getGrimMissionData()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    
    .append("  title TEXT NOT NULL,").ln()
    .append("  description TEXT NOT NULL,").ln()
    .append("  data_extension JSONB,").ln()
    
    .append("  mission_id VARCHAR(40) NOT NULL,").ln()
    .append("  objective_id VARCHAR(40),").ln()
    .append("  goal_id VARCHAR(40),").ln()
    .append("  remark_id VARCHAR(40),").ln()
    
    .append("  UNIQUE NULLS NOT DISTINCT(mission_id, objective_id, goal_id, remark_id)").ln()
    
    .append(");").ln()
    

    .append("CREATE INDEX ").append(options.getGrimMissionData()).append("_MISSION_INDEX")
    .append(" ON ").append(options.getGrimMissionData()).append(" (mission_id);").ln()
    
    .append("CREATE INDEX ").append(options.getGrimMissionData()).append("_OBJECTIVE_INDEX")
    .append(" ON ").append(options.getGrimMissionData()).append(" (objective_id);").ln()
    
    .append("CREATE INDEX ").append(options.getGrimMissionData()).append("_GOAL_INDEX")
    .append(" ON ").append(options.getGrimMissionData()).append(" (goal_id);").ln()
    
    .append("CREATE INDEX ").append(options.getGrimMissionData()).append("_REMARK_INDEX")
    .append(" ON ").append(options.getGrimMissionData()).append(" (remark_id);").ln()
    
    .build()).build();
  }


  @Override
  public Sql createConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
        .ln().append("--- constraints for").append(options.getGrimMissionData()).ln()
        .append("ALTER TABLE ").append(options.getGrimMissionData()).ln()
        .append("  ADD CONSTRAINT ").append(options.getGrimMissionLabel()).append("_MISSION_FK").ln()
        .append("  FOREIGN KEY (mission_id)").ln()
        .append("  REFERENCES ").append(options.getGrimMission()).append(" (id);").ln().ln()
        
        .append("ALTER TABLE ").append(options.getGrimMissionData()).ln()
        .append("  ADD CONSTRAINT ").append(options.getGrimMissionData()).append("_OBJECTIVE_FK").ln()
        .append("  FOREIGN KEY (objective_id)").ln()
        .append("  REFERENCES ").append(options.getGrimObjective()).append(" (id);").ln().ln()
        
        .append("ALTER TABLE ").append(options.getGrimMissionData()).ln()
        .append("  ADD CONSTRAINT ").append(options.getGrimMissionData()).append("_GOAL_FK").ln()
        .append("  FOREIGN KEY (goal_id)").ln()
        .append("  REFERENCES ").append(options.getGrimObjectiveGoal()).append(" (id);").ln().ln()
        
        .append("ALTER TABLE ").append(options.getGrimMissionData()).ln()
        .append("  ADD CONSTRAINT ").append(options.getGrimMissionData()).append("_REMARK_FK").ln()
        .append("  FOREIGN KEY (remark_id)").ln()
        .append("  REFERENCES ").append(options.getGrimRemark()).append(" (id);").ln().ln()

    .build()).build();
  }


  @Override
  public Function<Row, GrimMissionData> defaultMapper() {
    return (row) -> {
      
      return ImmutableGrimMissionData.builder().build();
    };
  }

}