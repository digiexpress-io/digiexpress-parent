package io.resys.thena.registry.grim;

import java.util.function.Function;

import io.resys.thena.api.entities.grim.GrimLabel;
import io.resys.thena.api.entities.grim.ImmutableGrimLabel;
import io.resys.thena.api.registry.grim.GrimLabelRegistry;
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
public class GrimLabelRegistrySqlImpl implements GrimLabelRegistry {
  private final TenantTableNames options;
  
  
  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getGrimLabel()).append(";").ln()
        .build()).build();
  }

  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getGrimLabel())
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getGrimLabel()).ln()
        .append("  WHERE (id = $1)").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getGrimLabel()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    
    .append("  label_type VARCHAR(100) NOT NULL,").ln()
    .append("  label_value TEXT NOT NULL,").ln()
    .append("  label_body JSONB,").ln()
    
    .append("  UNIQUE NULLS NOT DISTINCT(label_type, label_value)").ln()
    
    
    .append(");").ln()
    

    .append("CREATE INDEX ").append(options.getGrimLabel()).append("_TYPE_VALUE_INDEX")
    .append(" ON ").append(options.getGrimLabel()).append(" (label_type, label_value);").ln()
    
    
    .build()).build();
  }


  @Override
  public Sql createConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
    .ln().append("--- constraints for").append(options.getGrimLabel()).ln()

    .build()).build();
  }


  @Override
  public Function<Row, GrimLabel> defaultMapper() {
    return (row) -> {
      
      return ImmutableGrimLabel.builder().build();
    };
  }

}