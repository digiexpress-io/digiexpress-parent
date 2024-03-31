package io.resys.thena.registry.grim;

import java.util.Collection;
import java.util.function.Function;

import io.resys.thena.api.entities.grim.GrimMissionLink;
import io.resys.thena.api.entities.grim.ImmutableGrimMissionLink;
import io.resys.thena.api.registry.grim.GrimMissionLinkRegistry;
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
public class GrimMissionLinkRegistrySqlImpl implements GrimMissionLinkRegistry {
  private final TenantTableNames options;
  
  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getGrimMissionLink()).append(";").ln()
        .build()).build();
  }
  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getGrimMissionLink())
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getGrimMissionLink()).ln()
        .append("  WHERE (id = $1)").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getGrimMissionLink()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    
    .append("  mission_id VARCHAR(40) NOT NULL,").ln()
    .append("  objective_id VARCHAR(40),").ln()
    .append("  goal_id VARCHAR(40),").ln()
    .append("  remark_id VARCHAR(40),").ln()
    
    .append("  link_type VARCHAR(100) NOT NULL,").ln()
    .append("  external_id TEXT NOT NULL,").ln()
    .append("  link_body JSONB,").ln()
    
    .append("  UNIQUE NULLS NOT DISTINCT(mission_id, objective_id, goal_id, remark_id, link_type, external_id)").ln()
    
    .append(");").ln()
    
    .append("CREATE INDEX ").append(options.getGrimMissionLink()).append("_MISSION_INDEX")
    .append(" ON ").append(options.getGrimMissionLink()).append(" (mission_id);").ln()
    
    .append("CREATE INDEX ").append(options.getGrimMissionLink()).append("_OBJECTIVE_INDEX")
    .append(" ON ").append(options.getGrimMissionLink()).append(" (objective_id);").ln()
    
    .append("CREATE INDEX ").append(options.getGrimMissionLink()).append("_GOAL_INDEX")
    .append(" ON ").append(options.getGrimMissionLink()).append(" (goal_id);").ln()
    
    .append("CREATE INDEX ").append(options.getGrimMissionLink()).append("_REMARK_INDEX")
    .append(" ON ").append(options.getGrimMissionLink()).append(" (remark_id);").ln()
    
    
    .build()).build();
  }

  @Override
  public Sql createConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
    .ln().append("--- constraints for").append(options.getGrimMissionLink()).ln()
    
    .append("ALTER TABLE ").append(options.getGrimMissionLink()).ln()
    .append("  ADD CONSTRAINT ").append(options.getGrimMissionLink()).append("_MISSION_FK").ln()
    .append("  FOREIGN KEY (mission_id)").ln()
    .append("  REFERENCES ").append(options.getGrimMission()).append(" (id);").ln().ln()
    
    .append("ALTER TABLE ").append(options.getGrimMissionLink()).ln()
    .append("  ADD CONSTRAINT ").append(options.getGrimMissionLink()).append("_OBJECTIVE_FK").ln()
    .append("  FOREIGN KEY (objective_id)").ln()
    .append("  REFERENCES ").append(options.getGrimObjective()).append(" (id);").ln().ln()
    
    .append("ALTER TABLE ").append(options.getGrimMissionLink()).ln()
    .append("  ADD CONSTRAINT ").append(options.getGrimMissionLink()).append("_GOAL_FK").ln()
    .append("  FOREIGN KEY (goal_id)").ln()
    .append("  REFERENCES ").append(options.getGrimObjectiveGoal()).append(" (id);").ln().ln()
    
    .append("ALTER TABLE ").append(options.getGrimMissionLink()).ln()
    .append("  ADD CONSTRAINT ").append(options.getGrimMissionLink()).append("_REMARK_FK").ln()
    .append("  FOREIGN KEY (remark_id)").ln()
    .append("  REFERENCES ").append(options.getGrimRemark()).append(" (id);").ln().ln()
    
        
    .build()).build();
  }

  @Override
  public Function<Row, GrimMissionLink> defaultMapper() {
    return (row) -> {
      
      return ImmutableGrimMissionLink.builder().build();
    };
  }
  @Override
  public SqlTuple findAllByMissionIds(Collection<String> id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getGrimRemark()).ln()
        .append("  WHERE (mission_id = ANY($1))").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public SqlTupleList insertAll(Collection<GrimMissionLink> links) {
    // TODO Auto-generated method stub
    return null;
  }
  @Override
  public SqlTupleList deleteAll(Collection<GrimMissionLink> links) {
    // TODO Auto-generated method stub
    return null;
  }

}