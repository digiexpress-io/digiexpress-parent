package io.resys.thena.registry.grim;

import java.util.function.Function;

import io.resys.thena.api.entities.grim.GrimAssignment;
import io.resys.thena.api.registry.grim.GrimAssignmentRegistry;
import io.resys.thena.datasource.ImmutableSql;
import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.datasource.ThenaSqlClient;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.storesql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Row;
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
  public Sql findAll() {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public SqlTuple getById(String id) {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public Sql createTable() {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public Sql createConstraints() {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public Function<Row, GrimAssignment> defaultMapper() {
    // TODO Auto-generated method stub
    return null;
  }

}