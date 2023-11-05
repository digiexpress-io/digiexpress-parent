package io.resys.thena.docdb.sql.factories;

import io.resys.thena.docdb.spi.DocDbQueries;
import io.resys.thena.docdb.sql.factories.GitDbQueriesSqlImpl.ClientQuerySqlContext;
import io.resys.thena.docdb.sql.queries.doc.DocCommitQuerySqlPool;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class DocDbQueriesSqlImpl implements DocDbQueries {
  
  protected final ClientQuerySqlContext context;

  @Override
  public DocCommitQuery commits() {
    return new DocCommitQuerySqlPool(context.getWrapper(), context.getMapper(), context.getBuilder(), context.getErrorHandler());
  }
}
