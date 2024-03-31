package io.resys.thena.registry.grim;

import java.util.function.Function;

import io.resys.thena.api.entities.grim.GrimRemark;
import io.resys.thena.api.entities.grim.ImmutableGrimRemark;
import io.resys.thena.api.registry.grim.GrimRemarkRegistry;
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
public class GrimRemarkRegistrySqlImpl implements GrimRemarkRegistry {
  private final TenantTableNames options;
    
  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getGrimRemark()).append(";").ln()
        .build()).build();
  }

  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getGrimRemark())
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getGrimRemark()).ln()
        .append("  WHERE (id = $1)").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getGrimRemark()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
  
    .append("  reporter_id VARCHAR(255) NOT NULL,").ln()
    .append("  remark_status VARCHAR(100),").ln()
    .append("  remark_text TEXT NOT NULL,").ln()
    
    .append("  mission_id VARCHAR(40) NOT NULL,").ln()
    .append("  objective_id VARCHAR(40),").ln()
    .append("  goal_id VARCHAR(40)").ln()
    
    .append(");").ln()
    
    .append("CREATE INDEX ").append(options.getGrimRemark()).append("_MISSION_INDEX")
    .append(" ON ").append(options.getGrimRemark()).append(" (mission_id);").ln()
    
    .append("CREATE INDEX ").append(options.getGrimRemark()).append("_OBJECTIVE_INDEX")
    .append(" ON ").append(options.getGrimRemark()).append(" (objective_id);").ln()
    
    .append("CREATE INDEX ").append(options.getGrimRemark()).append("_GOAL_INDEX")
    .append(" ON ").append(options.getGrimRemark()).append(" (goal_id);").ln()
    
    
    .build()).build();
  }


  @Override
  public Sql createConstraints() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
      
      .append("ALTER TABLE ").append(options.getGrimRemark()).ln()
      .append("  ADD CONSTRAINT ").append(options.getGrimRemark()).append("_MISSION_FK").ln()
      .append("  FOREIGN KEY (mission_id)").ln()
      .append("  REFERENCES ").append(options.getGrimMission()).append(" (id);").ln().ln()
      
      .append("ALTER TABLE ").append(options.getGrimRemark()).ln()
      .append("  ADD CONSTRAINT ").append(options.getGrimRemark()).append("_OBJECTIVE_FK").ln()
      .append("  FOREIGN KEY (objective_id)").ln()
      .append("  REFERENCES ").append(options.getGrimObjective()).append(" (id);").ln().ln()
      
      .append("ALTER TABLE ").append(options.getGrimRemark()).ln()
      .append("  ADD CONSTRAINT ").append(options.getGrimRemark()).append("_GOAL_FK").ln()
      .append("  FOREIGN KEY (goal_id)").ln()
      .append("  REFERENCES ").append(options.getGrimObjectiveGoal()).append(" (id);").ln().ln()
      
      .build()).build();
  }


  @Override
  public Function<Row, GrimRemark> defaultMapper() {
    return (row) -> {
      
      return ImmutableGrimRemark.builder().build();
    };
  }

}