package io.resys.thena.docdb.store.sql.factories;

import org.immutables.value.Value;

import io.resys.thena.docdb.models.git.GitQueries;
import io.resys.thena.docdb.models.git.store.sql.GitBlobHistoryQuerySqlPool;
import io.resys.thena.docdb.models.git.store.sql.GitBlobQuerySqlPool;
import io.resys.thena.docdb.models.git.store.sql.GitCommitQuerySqlPool;
import io.resys.thena.docdb.models.git.store.sql.GitRefQuerySqlPool;
import io.resys.thena.docdb.models.git.store.sql.GitTagQuerySqlPool;
import io.resys.thena.docdb.models.git.store.sql.GitTreeQuerySqlPool;
import io.resys.thena.docdb.store.sql.SqlBuilder;
import io.resys.thena.docdb.store.sql.SqlMapper;
import io.resys.thena.docdb.store.sql.support.SqlClientWrapper;
import io.resys.thena.docdb.support.ErrorHandler;
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
