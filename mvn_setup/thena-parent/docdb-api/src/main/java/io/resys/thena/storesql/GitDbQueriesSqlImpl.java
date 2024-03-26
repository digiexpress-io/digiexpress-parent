package io.resys.thena.storesql;

import org.immutables.value.Value;

import io.resys.thena.storesql.builders.GitBlobHistoryQuerySqlPool;
import io.resys.thena.storesql.builders.GitBlobQuerySqlPool;
import io.resys.thena.storesql.builders.GitCommitQuerySqlPool;
import io.resys.thena.storesql.builders.GitRefQuerySqlPool;
import io.resys.thena.storesql.builders.GitTagQuerySqlPool;
import io.resys.thena.storesql.builders.GitTreeQuerySqlPool;
import io.resys.thena.storesql.support.SqlClientWrapper;
import io.resys.thena.structures.git.GitQueries;
import io.resys.thena.support.ErrorHandler;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class GitDbQueriesSqlImpl implements GitQueries {
  
  protected final ClientQuerySqlContext context;
  
  @Value.Immutable
  public interface ClientQuerySqlContext {
    SqlClientWrapper getWrapper();
    SqlMapper getMapper();
    SqlBuilder getBuilder();
    ErrorHandler getErrorHandler();
  }
  
  @Override
  public GitTagQuery tags() {
    return new GitTagQuerySqlPool(context.getWrapper(), context.getMapper(), context.getBuilder(), context.getErrorHandler());
  }

  @Override
  public GitCommitQuery commits() {
    return new GitCommitQuerySqlPool(context.getWrapper(), context.getMapper(), context.getBuilder(), context.getErrorHandler());
  }

  @Override
  public GitRefQuery refs() {
    return new GitRefQuerySqlPool(context.getWrapper(), context.getMapper(), context.getBuilder(), context.getErrorHandler());
  }

  @Override
  public GitTreeQuery trees() {
    return new GitTreeQuerySqlPool(context.getWrapper(), context.getMapper(), context.getBuilder(), context.getErrorHandler());
  }

  @Override
  public GitBlobQuery blobs() {
    return new GitBlobQuerySqlPool(context.getWrapper(), context.getMapper(), context.getBuilder(), context.getErrorHandler());
  }
  
  @Override
  public GitBlobHistoryQuery blobHistory() {
    return new GitBlobHistoryQuerySqlPool(context);
  }
}
