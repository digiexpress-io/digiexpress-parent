package io.resys.thena.datasource.vertx;

import java.util.function.Function;

import io.resys.thena.datasource.ThenaSqlClient.ThenaPreparedQuery;
import io.resys.thena.datasource.ThenaSqlClient.ThenaQuery;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.SqlClient;


public class ThenaQueryVertx<T> implements ThenaQuery<T> {
  
  private final io.vertx.mutiny.sqlclient.SqlClient realClient;
  
  private final String sql;
  private final Function<Row, ?> mapper;
  
  public ThenaQueryVertx(SqlClient realClient, String sql) {
    super();
    this.realClient = realClient;
    this.mapper = null;
    this.sql = sql;
  }

  public <U> ThenaQueryVertx(SqlClient realClient, String sql, Function<Row, U> mapper) {
    super();
    this.realClient = realClient;
    this.mapper = mapper;
    this.sql = sql;
  }

  
  @Override
  public <U> ThenaPreparedQuery<RowSet<U>> mapping(Function<Row, U> mapper) {
    return new ThenaPreparedQueryVertx<RowSet<U>>(realClient, sql, mapper);
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public Uni<T> execute() {
    final var delegate = this.realClient.query(sql);
    if(this.mapper == null) {
      return (Uni<T>) delegate.execute();
    }
    return (Uni<T>) delegate.mapping(mapper).execute();
  }


}
