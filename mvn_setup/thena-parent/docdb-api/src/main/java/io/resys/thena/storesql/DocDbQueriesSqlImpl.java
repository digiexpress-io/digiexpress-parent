package io.resys.thena.storesql;

import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.storesql.builders.DocBranchQuerySqlPool;
import io.resys.thena.structures.doc.DocQueries;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class DocDbQueriesSqlImpl implements DocQueries {
  private final ThenaSqlDataSource dataSource;
  
  @Override
  public DocBranchQuery branches() {
    return new DocBranchQuerySqlPool(dataSource);
  }
}
