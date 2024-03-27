package io.resys.thena.storesql.statement;

import io.resys.thena.api.entities.Tenant;
import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.datasource.ImmutableSql;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.SqlQueryBuilder.RepoSqlBuilder;
import io.resys.thena.datasource.SqlQueryBuilder.Sql;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTuple;
import io.resys.thena.storesql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RepoSqlBuilderImpl implements RepoSqlBuilder {
  private final TenantTableNames options;
  
  @Override
  public SqlTuple exists() {
    return ImmutableSqlTuple.builder().value(new SqlStatement().ln()
        .append("SELECT EXISTS").ln()
        .append("(").ln()
        .append("  SELECT table_name").ln()
        .append("  FROM information_schema.tables").ln()
        .append("  WHERE table_name = ?1").ln()
        .append(")").ln().build())
        .props(Tuple.of(options.getTenant()))
        .build();
  }  
  @Override
  public Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getTenant())
        .build())
        .build();
  }
  @Override
  public SqlTuple getByName(String name) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getTenant())
        .append(" WHERE name = $1")
        .append(" FETCH FIRST ROW ONLY")
        .build())
        .props(Tuple.of(name))
        .build();
  }
  @Override
  public SqlTuple getByNameOrId(String name) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getTenant())
        .append(" WHERE name = $1 OR id = $1")
        .append(" FETCH FIRST ROW ONLY")
        .build())
        .props(Tuple.of(name))
        .build();
  }
  @Override
  public SqlTuple insertOne(Tenant newRepo) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getTenant())
        .append(" (id, rev, prefix, name, type) VALUES($1, $2, $3, $4, $5)")
        .build())
        .props(Tuple.of(newRepo.getId(), newRepo.getRev(), newRepo.getPrefix(), newRepo.getName(), newRepo.getType()))
        .build();
  }
  
  @Override
  public SqlTuple deleteOne(Tenant newRepo) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("DELETE FROM ").append(options.getTenant())
        .append(" WHERE id = $1")
        .build())
        .props(Tuple.of(newRepo.getId()))
        .build();
  }
}
