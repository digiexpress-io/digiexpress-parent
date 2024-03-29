package io.resys.thena.storesql.builders;

import io.resys.thena.api.LogConstants;
import io.resys.thena.api.entities.doc.DocCommit;
import io.resys.thena.api.registry.DocRegistry;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlFailed;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlTupleFailed;
import io.resys.thena.structures.doc.DocQueries.DocCommitQuery;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.extern.slf4j.Slf4j;


@Slf4j(topic = LogConstants.SHOW_SQL)
public class DocCommitQuerySqlPool implements DocCommitQuery {
  private final ThenaSqlDataSource wrapper;
  private final DocRegistry registry;
  private final ThenaSqlDataSourceErrorHandler errorHandler;
  public DocCommitQuerySqlPool(ThenaSqlDataSource dataSource) {
    this.wrapper = dataSource;
    this.registry = dataSource.getRegistry().doc();
    this.errorHandler = dataSource.getErrorHandler();
  }

  @Override
  public Uni<DocCommit> getById(String commit) {
    final var sql = registry.docCommits().getById(commit);
    if(log.isDebugEnabled()) {
      log.debug("DocCommit byId query, with props: {} \r\n{}", 
          sql.getProps().deepToString(),
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(registry.docCommits().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transform((RowSet<DocCommit> rowset) -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return it.next();
          }
          return null;
        })
        .onFailure(e -> errorHandler.notFound(e)).recoverWithNull()
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't get 'DOC_COMMIT' by 'id': '" + commit + "'!", sql, e)));
  }
  @Override
  public Multi<DocCommit> findAll() {
    final var sql = registry.docCommits().findAll();
    if(log.isDebugEnabled()) {
      log.debug("DocCommit findAll query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(registry.docCommits().defaultMapper())
        .execute()
        .onItem()
        .transformToMulti((RowSet<DocCommit> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlFailed("Can't find 'DOC_COMMIT'!", sql, e)));
  }

}
