package io.resys.thena.storesql.statement;

import io.resys.thena.api.entities.Tenant;
import io.resys.thena.spi.DbCollections;
import io.resys.thena.storesql.ImmutableSql;
import io.resys.thena.storesql.ImmutableSqlTuple;
import io.resys.thena.storesql.SqlBuilder.RepoSqlBuilder;
import io.resys.thena.storesql.SqlBuilder.Sql;
import io.resys.thena.storesql.SqlBuilder.SqlTuple;
import io.resys.thena.storesql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RepoSqlBuilderImpl implements RepoSqlBuilder {
  private final DbCollections options;
  

  @Override
  public SqlTuple exists() {
    return ImmutableSqlTuple.builder().value(new SqlStatement().ln()
        .append("SELECT EXISTS").ln()
        .append("(").ln()
        .append("  SELECT table_name").ln()
        .append("  FROM information_schema.tables").ln()
        .append("  WHERE table_name = ?1").ln()
        .append(")").ln().build())
        .props(Tuple.of(options.getRepos()))
        .build();
  }  
  @Override
  public Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getRepos())
        .build())
        .build();
  }
  @Override
  public SqlTuple getByName(String name) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getRepos())
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
        .append("SELECT * FROM ").append(options.getRepos())
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
        .append("INSERT INTO ").append(options.getRepos())
        .append(" (id, rev, prefix, name, type) VALUES($1, $2, $3, $4, $5)")
        .build())
        .props(Tuple.of(newRepo.getId(), newRepo.getRev(), newRepo.getPrefix(), newRepo.getName(), newRepo.getType()))
        .build();
  }
  
  @Override
  public SqlTuple deleteOne(Tenant newRepo) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("DELETE FROM ").append(options.getRepos())
        .append(" WHERE id = $1")
        .build())
        .props(Tuple.of(newRepo.getId()))
        .build();
  }
}
