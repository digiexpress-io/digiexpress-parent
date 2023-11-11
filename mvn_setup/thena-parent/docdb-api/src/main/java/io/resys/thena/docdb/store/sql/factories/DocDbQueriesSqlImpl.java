package io.resys.thena.docdb.store.sql.factories;

import io.resys.thena.docdb.models.doc.DocDbQueries;
import io.resys.thena.docdb.models.doc.store.sql.queries.DocBranchQuerySqlPool;
import io.resys.thena.docdb.models.doc.store.sql.queries.DocCommitQuerySqlPool;
import io.resys.thena.docdb.models.doc.store.sql.queries.DocLogQuerySqlPool;
import io.resys.thena.docdb.models.doc.store.sql.queries.DocQuerySqlPool;
import io.resys.thena.docdb.store.sql.factories.GitDbQueriesSqlImpl.ClientQuerySqlContext;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class DocDbQueriesSqlImpl implements DocDbQueries {
  
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
