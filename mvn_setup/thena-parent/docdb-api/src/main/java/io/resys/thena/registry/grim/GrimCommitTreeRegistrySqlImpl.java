package io.resys.thena.registry.grim;

import java.util.function.Function;

import io.resys.thena.api.entities.grim.GrimCommitTree;
import io.resys.thena.api.entities.grim.ImmutableGrimCommitTree;
import io.resys.thena.api.registry.grim.GrimCommitTreeRegistry;
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
public class GrimCommitTreeRegistrySqlImpl implements GrimCommitTreeRegistry {
  private final TenantTableNames options;
  
  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getGrimCommitTree()).append(";").ln()
        .build()).build();
  }
  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getGrimCommitTree())
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getGrimCommitTree()).ln()
        .append("  WHERE (id = $1)").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getGrimCommitTree()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    
    .append(");").ln()
    .build()).build();
  }

  @Override
  public Sql createConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
        
    .build()).build();
  }

  @Override
  public Function<Row, GrimCommitTree> defaultMapper() {
    return (row) -> {
      
      return ImmutableGrimCommitTree.builder().build();
    };
  }

}