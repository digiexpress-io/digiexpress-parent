package io.resys.thena.storesql;

import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.storesql.builders.DocBranchQuerySqlPool;
import io.resys.thena.storesql.builders.DocCommandsQuerySqlPool;
import io.resys.thena.storesql.builders.DocCommitQuerySqlPool;
import io.resys.thena.storesql.builders.DocLogQuerySqlPool;
import io.resys.thena.storesql.builders.DocQuerySqlPool;
import io.resys.thena.structures.doc.DocQueries;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class DocDbQueriesSqlImpl implements DocQueries {
  private final ThenaSqlDataSource dataSource;
  
  @Override
  public DocBranchQuery branches() {
    return new DocBranchQuerySqlPool(dataSource);
  }

  @Override
  public DocQuery docs() {
    return new DocQuerySqlPool(dataSource);
  }

  @Override
  public DocCommitQuery commits() {
    return new DocCommitQuerySqlPool(dataSource);
  }

  @Override
  public DocCommitTreeQuery trees() {
    return new DocLogQuerySqlPool(dataSource);
  }

  @Override
  public DocCommandsQuery commands() {
    return new DocCommandsQuerySqlPool(dataSource);
  }
}
