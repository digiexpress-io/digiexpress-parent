package io.resys.thena.docdb.store.sql.factories;

import io.resys.thena.docdb.models.org.OrgQueries;
import io.resys.thena.docdb.store.sql.factories.GitDbQueriesSqlImpl.ClientQuerySqlContext;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class OrgDbQueriesSqlImpl implements OrgQueries {
  
  protected final ClientQuerySqlContext context;


}
