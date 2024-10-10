package io.resys.thena.datasource.vertx;

import java.util.function.Function;

import io.resys.thena.datasource.ThenaSqlClient;
import io.resys.thena.datasource.ThenaSqlClient.ThenaSqlPool;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ThenaSqlPoolVertx implements ThenaSqlPool {
  
  private final io.vertx.mutiny.sqlclient.Pool realPool;
  
  @Override
  public ThenaPreparedQuery<RowSet<Row>> preparedQuery(String sql) {
    return new ThenaPreparedQueryVertx<RowSet<Row>>(realPool, sql);
  }
  @Override
  public ThenaQuery<RowSet<Row>> query(String sql) {
    return new ThenaQueryVertx<RowSet<Row>>(realPool, sql);
  }
  @Override
  public <T> Uni<T> withTransaction(Function<ThenaSqlClient, Uni<T>> function) {
    return realPool.withTransaction(realTxClient -> doInTx(realTxClient, function));
  }
  public <T> Uni<T> doInTx(io.vertx.mutiny.sqlclient.SqlConnection realTxClient, Function<ThenaSqlClient, Uni<T>> function) {
    final var delegate = new ThenaSqlClientVertx(realTxClient);
    final Uni<T> result = function.apply(delegate);
    return result;
  }
  @Override
  public Uni<Void> rollback() {
    return Uni.createFrom().voidItem();
  }
}
