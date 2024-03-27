package io.resys.thena.storesql.statement;

import io.resys.thena.api.entities.git.Tag;
import io.resys.thena.datasource.ImmutableSql;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.SqlQueryBuilder.GitTagSqlBuilder;
import io.resys.thena.datasource.SqlQueryBuilder.Sql;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTuple;
import io.resys.thena.spi.DbCollections;
import io.resys.thena.storesql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GitTagSqlBuilderImpl implements GitTagSqlBuilder {
  
  private final DbCollections options;
  
  @Override
  public Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getTags())
        .build())
        .build();
  }
  @Override
  public SqlTuple getByName(String name) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getTags())
        .append(" WHERE id = $1")
        .append(" FETCH FIRST ROW ONLY")
        .build())
        .props(Tuple.of(name))
        .build();
  }
  @Override
  public SqlTuple deleteByName(String name) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("DELETE FROM ").append(options.getTags())
        .append(" WHERE id = $1")
        .build())
        .props(Tuple.of(name))
        .build();
  }
  @Override
  public Sql getFirst() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getTags())
        .append(" FETCH FIRST ROW ONLY")
        .build())
        .build();
  }
  @Override
  public SqlTuple insertOne(Tag newTag) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getTags())
        .append(" (id, commit, datetime, author, message) VALUES($1, $2, $3, $4, $5)")
        .build())
        .props(Tuple.of(newTag.getName(), newTag.getCommit(), newTag.getDateTime().toString(), newTag.getAuthor(), newTag.getMessage()))
        .build();
  }
}
