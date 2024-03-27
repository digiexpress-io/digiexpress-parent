package io.resys.thena.storesql;

import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.storesql.builders.DocDbInsertsSqlPool;
import io.resys.thena.structures.doc.DocInserts;
import io.resys.thena.structures.doc.DocQueries;
import io.resys.thena.structures.doc.DocState;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class DocDbStateImpl implements DocState {
  private final ThenaSqlDataSource dataSource;
 
  @Override
  public <R> Uni<R> withTransaction(TransactionFunction<R> callback) {
    return dataSource.getPool().withTransaction(conn -> callback.apply(new DocDbStateImpl(dataSource.withTx(conn))));
  }
  @Override
  public DocInserts insert() {
    return new DocDbInsertsSqlPool(dataSource);
  }
  @Override
  public DocQueries query() {
    return new DocDbQueriesSqlImpl(dataSource);
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
