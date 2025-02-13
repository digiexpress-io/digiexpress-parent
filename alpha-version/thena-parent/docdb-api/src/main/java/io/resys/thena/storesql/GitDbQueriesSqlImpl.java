package io.resys.thena.storesql;

import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.storesql.builders.GitBlobHistoryQuerySqlPool;
import io.resys.thena.storesql.builders.GitBlobQuerySqlPool;
import io.resys.thena.storesql.builders.GitCommitQuerySqlPool;
import io.resys.thena.storesql.builders.GitRefQuerySqlPool;
import io.resys.thena.storesql.builders.GitTagQuerySqlPool;
import io.resys.thena.storesql.builders.GitTreeQuerySqlPool;
import io.resys.thena.structures.git.GitQueries;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class GitDbQueriesSqlImpl implements GitQueries {
  private final ThenaSqlDataSource dataSource;
  
  @Override
  public GitTagQuery tags() {
    return new GitTagQuerySqlPool(dataSource);
  }
  @Override
  public GitCommitQuery commits() {
    return new GitCommitQuerySqlPool(dataSource);
  }
  @Override
  public GitRefQuery refs() {
    return new GitRefQuerySqlPool(dataSource);
  }
  @Override
  public GitTreeQuery trees() {
    return new GitTreeQuerySqlPool(dataSource);
  }
  @Override
  public GitBlobQuery blobs() {
    return new GitBlobQuerySqlPool(dataSource);
  }
  @Override
  public GitBlobHistoryQuery blobHistory() {
    return new GitBlobHistoryQuerySqlPool(dataSource);
  }
}
