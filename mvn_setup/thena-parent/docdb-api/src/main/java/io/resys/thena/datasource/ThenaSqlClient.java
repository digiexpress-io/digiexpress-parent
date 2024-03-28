package io.resys.thena.datasource;

import java.util.List;
import java.util.function.Function;

import io.smallrye.mutiny.Uni;

public interface ThenaSqlClient {
  ThenaPreparedQuery<io.vertx.mutiny.sqlclient.RowSet<io.vertx.mutiny.sqlclient.Row>> preparedQuery(String sql);
  ThenaQuery<io.vertx.mutiny.sqlclient.RowSet<io.vertx.mutiny.sqlclient.Row>> query(String sql);

  interface ThenaQuery<T> {
    <U> ThenaQuery<io.vertx.mutiny.sqlclient.RowSet<U>> mapping(Function<io.vertx.mutiny.sqlclient.Row, U> mapper);
    Uni<T> execute(); 
  }
  
  interface ThenaPreparedQuery<T> extends ThenaQuery<T> {
    <U> ThenaPreparedQuery<io.vertx.mutiny.sqlclient.RowSet<U>> mapping(Function<io.vertx.mutiny.sqlclient.Row, U> mapper);
    Uni<T> execute();
    Uni<T> execute(io.vertx.mutiny.sqlclient.Tuple props);
    Uni<T> executeBatch(List<io.vertx.mutiny.sqlclient.Tuple> batch);
  }
  // Starts transaction
  interface ThenaSqlPool extends ThenaSqlClient {
    <T> Uni<T> withTransaction(Function<ThenaSqlClient, Uni<T>> function); 
  }
}