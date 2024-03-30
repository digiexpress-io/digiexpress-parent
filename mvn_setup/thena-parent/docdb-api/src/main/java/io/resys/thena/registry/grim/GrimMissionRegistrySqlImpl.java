package io.resys.thena.registry.grim;

import java.util.function.Function;

import io.resys.thena.api.entities.grim.GrimMission;
import io.resys.thena.api.entities.grim.ImmutableGrimMission;
import io.resys.thena.api.registry.grim.GrimMissionRegistry;
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
public class GrimMissionRegistrySqlImpl implements GrimMissionRegistry {
  private final TenantTableNames options;
  
  
  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getGrimMission()).append(";").ln()
        .build()).build();
  }
  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getGrimMission())
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getGrimMission()).ln()
        .append("  WHERE (id = $1)").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getGrimMission()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    .append("  parent_id VARCHAR(40),").ln()
    .append("  external_id VARCHAR(40) UNIQUE,").ln()
    .append("  reporter_id VARCHAR(255),").ln()
    
    .append("  mission_status VARCHAR(100),").ln()
    .append("  mission_priority VARCHAR(100),").ln()
    .append("  mission_start_date TIMESTAMP,").ln()
    .append("  mission_due_date TIMESTAMP").ln()
    
    .append("ALTER TABLE ").append(options.getGrimMission()).ln()
    .append("  ADD CONSTRAINT ").append(options.getGrimMission()).append("_PARENT_FK").ln()
    .append("  FOREIGN KEY (parent_id)").ln()
    .append("  REFERENCES ").append(options.getGrimMission()).append(" (id);").ln()
    
    .append(");").ln()
    .build()).build();
  }


  @Override
  public Sql createConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
        
    .build()).build();
  }


  @Override
  public Function<Row, GrimMission> defaultMapper() {
    return (row) -> {
      
      return ImmutableGrimMission.builder().build();
    };
  }

}