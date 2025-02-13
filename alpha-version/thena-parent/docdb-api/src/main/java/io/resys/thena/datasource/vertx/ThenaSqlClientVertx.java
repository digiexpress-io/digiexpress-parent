package io.resys.thena.datasource.vertx;

import io.resys.thena.datasource.ThenaSqlClient;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ThenaSqlClientVertx implements ThenaSqlClient {
  private final io.vertx.mutiny.sqlclient.SqlConnection realTxClient;
 
  @Override
  public ThenaPreparedQuery<RowSet<Row>> preparedQuery(String sql) {
    return new ThenaPreparedQueryVertx<RowSet<Row>>(realTxClient, sql);
  }
  @Override
  public ThenaQuery<RowSet<Row>> query(String sql) {
    return new ThenaQueryVertx<RowSet<Row>>(realTxClient, sql);
  }
  @Override
  public Uni<Void> rollback() {
    // no transaction
    if(realTxClient.transaction() == null) {
      return Uni.createFrom().voidItem();
    }
    
    return realTxClient.transaction().rollback();
  }
}
