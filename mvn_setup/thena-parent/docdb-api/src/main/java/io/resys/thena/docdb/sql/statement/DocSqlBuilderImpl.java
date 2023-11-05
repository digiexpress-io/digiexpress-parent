package io.resys.thena.docdb.sql.statement;

import io.resys.thena.docdb.api.models.ThenaDocObject.Doc;
import io.resys.thena.docdb.spi.DbCollections;
import io.resys.thena.docdb.sql.ImmutableSql;
import io.resys.thena.docdb.sql.ImmutableSqlTuple;
import io.resys.thena.docdb.sql.SqlBuilder.DocSqlBuilder;
import io.resys.thena.docdb.sql.SqlBuilder.Sql;
import io.resys.thena.docdb.sql.SqlBuilder.SqlTuple;
import io.resys.thena.docdb.sql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DocSqlBuilderImpl implements DocSqlBuilder {
  private final DbCollections options;
  
  @Override
  public Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getDoc())
        .build())
        .build();
  }
  @Override
  public SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getDoc()).ln()
        .append("  WHERE (id = $1 OR external_id = $2)").ln()
        .build())
        .props(Tuple.of(id, id))
        .build();
  }
  @Override
  public SqlTuple insertOne(Doc doc) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getDoc())
        .append(" (id, external_id) VALUES($1, $2)").ln()
        .build())
        .props(Tuple.of(doc.getId(), doc.getExternalId()))
        .build();
  }
  @Override
  public SqlTuple deleteById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("DELETE FROM ").append(options.getDoc())
        .append(" WHERE (id = $1 OR external_id = $2)")
        .build())
        .props(Tuple.of(id, id))
        .build();
  }

}
