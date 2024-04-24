package io.resys.thena.storesql.builders;

import java.util.Collection;

import io.resys.thena.api.LogConstants;
import io.resys.thena.api.entities.doc.DocCommands;
import io.resys.thena.api.registry.DocRegistry;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlFailed;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlTupleFailed;
import io.resys.thena.structures.doc.DocQueries.DocCommandsQuery;
import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.extern.slf4j.Slf4j;


@Slf4j(topic = LogConstants.SHOW_SQL)
public class DocCommandsQuerySqlPool implements DocCommandsQuery {
  private final ThenaSqlDataSource wrapper;
  private final DocRegistry registry;
  private final ThenaSqlDataSourceErrorHandler errorHandler;
  
  public DocCommandsQuerySqlPool(ThenaSqlDataSource dataSource) {
    this.wrapper = dataSource;
    this.registry = dataSource.getRegistry().doc();
    this.errorHandler = dataSource.getErrorHandler();
  }
  @Override
  public Multi<DocCommands> findAllByDocIdsAndBranch(Collection<String> id, String branchId) {
    final var sql = registry.docCommands().findAllByDocIdsAndBranch(id, branchId);
    if(log.isDebugEnabled()) {
      log.debug("DocCommands byId query, with props: {} \r\n{}", 
          sql.getProps().deepToString(),
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(registry.docCommands().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transformToMulti((RowSet<DocCommands> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't get 'DOC_COMMANDS' by id: '" + id + "'!", sql, e)));
  }
  @Override
  public Multi<DocCommands> findAll() {
    final var sql = registry.docCommands().findAll();
    if(log.isDebugEnabled()) {
      log.debug("DocCommands findAll query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(registry.docCommands().defaultMapper())
        .execute()
        .onItem()
        .transformToMulti((RowSet<DocCommands> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlFailed("Can't find 'DOC_COMMANDS'!", sql, e)));
  }
}
