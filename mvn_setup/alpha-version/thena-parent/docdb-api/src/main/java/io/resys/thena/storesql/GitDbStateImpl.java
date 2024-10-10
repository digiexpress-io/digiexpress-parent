package io.resys.thena.storesql;

import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.structures.git.GitInserts;
import io.resys.thena.structures.git.GitQueries;
import io.resys.thena.structures.git.GitState;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GitDbStateImpl implements GitState {
  private final ThenaSqlDataSource dataSource;
  
  @Override
  public <R> Uni<R> withTransaction(TransactionFunction<R> callback) {
    return dataSource.getPool().withTransaction(conn -> callback.apply(new GitDbStateImpl(dataSource.withTx(conn))));
  }
  @Override
  public GitInserts insert() {
    return new GitDbInsertsSqlPool(dataSource);
  }
  @Override
  public GitQueries query() {
    return new GitDbQueriesSqlImpl(dataSource);
  }
  @Override
  public ThenaSqlDataSource getDataSource() {
    return dataSource;
  }
}
