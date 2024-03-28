package io.resys.thena.datasource.vertx;

import java.util.List;
import java.util.function.Function;

import io.resys.thena.datasource.ThenaSqlClient.ThenaPreparedQuery;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.SqlClient;
import io.vertx.mutiny.sqlclient.Tuple;

public class ThenaPreparedQueryVertx<T> implements ThenaPreparedQuery<T> {
  
  private final io.vertx.mutiny.sqlclient.SqlClient realClient;
  private final String sql;
  private final Function<Row, ?> mapper;
  
  public ThenaPreparedQueryVertx(SqlClient realClient, String sql) {
    super();
    this.realClient = realClient;
    this.mapper = null;
    this.sql = sql;
  }

  public <U> ThenaPreparedQueryVertx(SqlClient realClient, String sql, Function<Row, U> mapper) {
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
    final var delegate = this.realClient.preparedQuery(sql);
    if(this.mapper == null) {
      return (Uni<T>) delegate.execute();
    }
    return (Uni<T>) delegate.mapping(mapper).execute();
  }
  @SuppressWarnings("unchecked")
  @Override
  public Uni<T> execute(Tuple props) {
    final var delegate = this.realClient.preparedQuery(sql);
    if(this.mapper == null) {
      return (Uni<T>) delegate.execute(props);
    }
    return (Uni<T>) delegate.mapping(mapper).execute(props);
  }
  @SuppressWarnings("unchecked")
  @Override
  public Uni<T> executeBatch(List<Tuple> batch) {
    final var delegate = this.realClient.preparedQuery(sql);
    if(this.mapper == null) {
      return (Uni<T>) delegate.executeBatch(batch);
    }
    return (Uni<T>) delegate.mapping(mapper).executeBatch(batch);
  }
  


}
