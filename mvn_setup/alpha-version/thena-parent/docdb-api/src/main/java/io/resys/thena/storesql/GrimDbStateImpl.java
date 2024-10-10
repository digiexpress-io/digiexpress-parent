package io.resys.thena.storesql;

import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.structures.grim.GrimInserts;
import io.resys.thena.structures.grim.GrimQueries;
import io.resys.thena.structures.grim.GrimState;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GrimDbStateImpl implements GrimState {
  private final ThenaSqlDataSource dataSource;
  
  @Override
  public <R> Uni<R> withTransaction(TransactionFunction<R> callback) {
    return dataSource.getPool().withTransaction(conn -> callback.apply(new GrimDbStateImpl(dataSource.withTx(conn))));
  }
  @Override
  public GrimInserts insert() {
    return new GrimInsertsSqlImpl(dataSource);
  }
  @Override
  public GrimQueries query() {
    return new GrimQueriesSqlImpl(dataSource);
  }
  @Override
  public ThenaSqlDataSource getDataSource() {
    return dataSource;
  }
  @Override
  public String getTenantId() {
    return dataSource.getTenant().getId();
  }
}
