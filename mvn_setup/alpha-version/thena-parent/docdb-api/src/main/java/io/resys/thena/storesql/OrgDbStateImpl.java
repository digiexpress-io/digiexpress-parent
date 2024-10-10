package io.resys.thena.storesql;

import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.structures.org.OrgInserts;
import io.resys.thena.structures.org.OrgQueries;
import io.resys.thena.structures.org.OrgState;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class OrgDbStateImpl implements OrgState {
  private final ThenaSqlDataSource dataSource;
  
  @Override
  public <R> Uni<R> withTransaction(TransactionFunction<R> callback) {
    return dataSource.getPool().withTransaction(conn -> callback.apply(new OrgDbStateImpl(dataSource.withTx(conn))));
  }
  @Override
  public OrgInserts insert() {
    return new OrgDbInsertsSqlPool(dataSource);
  }
  @Override
  public OrgQueries query() {
    return new OrgDbQueriesSqlImpl(dataSource);
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
