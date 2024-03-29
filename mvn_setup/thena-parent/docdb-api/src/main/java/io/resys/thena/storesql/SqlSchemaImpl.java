package io.resys.thena.storesql;

import io.resys.thena.datasource.ImmutableSql;
import io.resys.thena.datasource.SqlQueryBuilder.Sql;
import io.resys.thena.datasource.SqlSchema;
import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.storesql.support.SqlStatement;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SqlSchemaImpl implements SqlSchema {

  protected final TenantTableNames options;
  
  @Override
  public Sql createTenant() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("CREATE TABLE IF NOT EXISTS ").append(options.getTenant()).ln()
        .append("(").ln()
        .append("  id VARCHAR(40) PRIMARY KEY,").ln()
        .append("  rev VARCHAR(40) NOT NULL,").ln()
        .append("  prefix VARCHAR(40) NOT NULL,").ln()
        .append("  type VARCHAR(3) NOT NULL,").ln()
        .append("  name VARCHAR(255) NOT NULL,").ln()
        .append("  external_id VARCHAR(255),").ln()
        .append("  UNIQUE(name), UNIQUE(rev), UNIQUE(prefix), UNIQUE(external_id)").ln()
        .append(");").ln()

        .append("CREATE INDEX IF NOT EXISTS ").append(options.getTenant()).append("_NAME_INDEX")
        .append(" ON ").append(options.getTenant()).append(" (name);").ln()
        .append("CREATE INDEX IF NOT EXISTS ").append(options.getTenant()).append("_EXT_INDEX")
        .append(" ON ").append(options.getTenant()).append(" (external_id);").ln()
        
        .build()).build();
  }
  
  @Override
  public SqlSchemaImpl withTenant(TenantTableNames options) {
    return new SqlSchemaImpl(options);
  }



  
  @Override public Sql dropRepo() { return dropTableIfNotExists(options.getTenant()); }
 
  
  private Sql dropTableIfNotExists(String tableName) {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(tableName).append(";").ln()
        .build()).build();
  }



}
