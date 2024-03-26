package io.resys.thena.storesql.statement;

import io.resys.thena.api.models.ThenaGitObject.Branch;
import io.resys.thena.api.models.ThenaGitObject.Commit;
import io.resys.thena.spi.DbCollections;
import io.resys.thena.storesql.ImmutableSql;
import io.resys.thena.storesql.ImmutableSqlTuple;
import io.resys.thena.storesql.SqlBuilder.GitRefSqlBuilder;
import io.resys.thena.storesql.SqlBuilder.Sql;
import io.resys.thena.storesql.SqlBuilder.SqlTuple;
import io.resys.thena.storesql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GitRefSqlBuilderImpl implements GitRefSqlBuilder {
  private final DbCollections options;

  @Override
  public Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getRefs())
        .build())
        .build();
  }

  @Override
  public SqlTuple getByName(String name) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getRefs())
        .append(" WHERE name = $1")
        .append(" FETCH FIRST ROW ONLY")
        .build())
        .props(Tuple.of(name))
        .build();
  }
  @Override
  public SqlTuple getByNameOrCommit(String refNameOrCommit) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getRefs())
        .append(" WHERE name = $1 OR commit = $1")
        .append(" FETCH FIRST ROW ONLY")
        .build())
        .props(Tuple.of(refNameOrCommit))
        .build();
  }

  @Override
  public Sql getFirst() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getRefs())
        .append(" FETCH FIRST ROW ONLY")
        .build())
        .build();
  }

  @Override
  public SqlTuple insertOne(Branch ref) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getRefs())
        .append(" (name, commit) VALUES($1, $2)")
        .build())
        .props(Tuple.of(ref.getName(), ref.getCommit()))
        .build();
  }

  @Override
  public SqlTuple updateOne(Branch ref, Commit commit) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("UPDATE ").append(options.getRefs())
        .append(" SET commit = $1")
        .append(" WHERE name = $2 AND commit = $3")
        .build())
        .props(Tuple.of(ref.getCommit(), ref.getName(), commit.getParent().get()))
        .build();
  }
  
}
