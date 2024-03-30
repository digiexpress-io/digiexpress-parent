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
    .build()).build();
  }


  @Override
  public Sql createConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
        
    .build()).build();
  }


  @Override
  public Function<Row, GrimMissionData> defaultMapper() {
    return (row) -> {
      
      return ImmutableGrimMissionData.builder().build();
    };
  }

}