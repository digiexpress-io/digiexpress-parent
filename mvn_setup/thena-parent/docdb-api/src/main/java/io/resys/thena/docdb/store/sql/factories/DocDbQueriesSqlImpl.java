package io.resys.thena.docdb.store.sql.factories;

import io.resys.thena.docdb.models.doc.DocQueries;
import io.resys.thena.docdb.store.sql.factories.GitDbQueriesSqlImpl.ClientQuerySqlContext;
import io.resys.thena.docdb.store.sql.queries.DocBranchQuerySqlPool;
import io.resys.thena.docdb.store.sql.queries.DocCommitQuerySqlPool;
import io.resys.thena.docdb.store.sql.queries.DocLogQuerySqlPool;
import io.resys.thena.docdb.store.sql.queries.DocQuerySqlPool;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class DocDbQueriesSqlImpl implements DocQueries {
  
  protected final ClientQuerySqlContext context;

  @Override
  public DocCommitQuery commits() {
    return new DocCommitQuerySqlPool(context.getWrapper(), context.getMapper(), context.getBuilder(), context.getErrorHandler());
  }

  @Override
  public DocQuery docs() {
    return new DocQuerySqlPool(context.getWrapper(), context.getMapper(), context.getBuilder(), context.getErrorHandler());
  }

  @Override
  public DocBranchQuery branches() {
    return new DocBranchQuerySqlPool(context.getWrapper(), context.getMapper(), context.getBuilder(), context.getErrorHandler());
  }

  @Override
  public DocLogQuery logs() {
    return new DocLogQuerySqlPool(context.getWrapper(), context.getMapper(), context.getBuilder(), context.getErrorHandler());
  }
}
