package io.resys.thena.registry.org;

import java.util.function.Function;

import io.resys.thena.api.entities.org.OrgActorData;
import io.resys.thena.api.registry.org.OrgActorDataRegistry;
import io.resys.thena.datasource.ImmutableSql;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.SqlQueryBuilder.Sql;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTuple;
import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.storesql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrgActorDataRegistrySqlImpl implements OrgActorDataRegistry {
  private final TenantTableNames options;
  

  
  @Override
  public Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getOrgActorData())
        .build())
        .build();
  }
  @Override
  public SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getOrgActorData()).ln()
        .append("  WHERE (id = $1)").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  
  @Override
  public Function<Row, OrgActorData> defaultMapper() {
    throw new RuntimeException("not implemented");
  }

  @Override
  public Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getOrgActorData()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    .append("  parent_id VARCHAR(40),").ln()
    .append("  external_id VARCHAR(40) UNIQUE,").ln()
    .append("  member_id VARCHAR(40),").ln()
    .append("  right_id VARCHAR(40),").ln()
    .append("  party_id VARCHAR(40),").ln()

    .append("  data_type VARCHAR(255) NOT NULL,").ln()
    .append("  value JSONB NOT NULL,").ln()

    .append("  commit_author VARCHAR(255) NOT NULL,").ln()
    .append("  commit_message VARCHAR(255) NOT NULL").ln()
    .append(");").ln().ln()


    // parent id, references self
    .append("ALTER TABLE ").append(options.getOrgActorData()).ln()
    .append("  ADD CONSTRAINT ").append(options.getOrgActorData()).append("_PARENT_FK").ln()
    .append("  FOREIGN KEY (parent_id)").ln()
    .append("  REFERENCES ").append(options.getOrgActorData()).append(" (id);").ln()


    .build()).build();
  }

  @Override
  public Sql createConstraints() {
    return ImmutableSql.builder().value("").build();
  }

  @Override
  public Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getOrgActorData()).append(";").ln()
        .build()).build();
  }


}
