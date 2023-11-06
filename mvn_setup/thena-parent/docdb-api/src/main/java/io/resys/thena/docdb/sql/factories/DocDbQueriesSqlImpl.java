package io.resys.thena.docdb.sql.factories;

import io.resys.thena.docdb.spi.DocDbQueries;
import io.resys.thena.docdb.sql.factories.GitDbQueriesSqlImpl.ClientQuerySqlContext;
import io.resys.thena.docdb.sql.queries.doc.DocBranchQuerySqlPool;
import io.resys.thena.docdb.sql.queries.doc.DocCommitQuerySqlPool;
import io.resys.thena.docdb.sql.queries.doc.DocLogQuerySqlPool;
import io.resys.thena.docdb.sql.queries.doc.DocQuerySqlPool;
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
