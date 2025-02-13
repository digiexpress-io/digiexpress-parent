package io.resys.thena.datasource;

import java.util.List;
import java.util.function.Function;

import org.immutables.value.Value;

import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlFailed;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlTupleFailed;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlTupleListFailed;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Tuple;

public interface ThenaSqlClient {
  Uni<Void> rollback();
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

  @Value.Immutable
  interface Sql {
    String getValue();
    
    default SqlFailed failed(Throwable t, String message, Object ...args) {
      return new SqlFailed(message.formatted(args), this, t);
    }
  }

  @Value.Immutable
  interface SqlTuple {
    String getValue();
    Tuple getProps();
    
    default String getPropsDeepString() {
      StringBuilder sb = new StringBuilder();
      sb.append("[");
      final int size = getProps().size();
      for (int i = 0; i < size; i++) {
        final var value = getProps().getValue(i);
        if(value instanceof String[]) {
          final var unwrapped = (String[]) value;
          sb.append("[")
          .append(String.join(",", unwrapped))
          .append("]");   
        } else {
          sb.append(value);
        }
  
        if (i + 1 < size)
          sb.append(",");
      }
      sb.append("]");
      return sb.toString();
    }
    
    default SqlTupleFailed failed(Throwable t, String message, Object ...args) {
      return new SqlTupleFailed(message.formatted(args), this, t);
    }
  }

  @Value.Immutable
  interface SqlTupleList {
    String getValue();
    List<Tuple> getProps();
    
    default SqlTupleListFailed failed(Throwable t, String message, Object ...args) {
      return new SqlTupleListFailed(message.formatted(args), this, t);
    }
  }
}