package io.resys.thena.storesql.builders;

import io.resys.thena.api.LogConstants;
import io.resys.thena.api.entities.doc.DocLog;
import io.resys.thena.datasource.SqlDataMapper;
import io.resys.thena.datasource.SqlQueryBuilder;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlFailed;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlTupleFailed;
import io.resys.thena.structures.doc.DocQueries.DocLogQuery;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.extern.slf4j.Slf4j;


@Slf4j(topic = LogConstants.SHOW_SQL)
public class DocLogQuerySqlPool implements DocLogQuery {
  private final ThenaSqlDataSource wrapper;
  private final SqlDataMapper sqlMapper;
  private final SqlQueryBuilder sqlBuilder;
  private final ThenaSqlDataSourceErrorHandler errorHandler;
  
  public DocLogQuerySqlPool(ThenaSqlDataSource dataSource) {
    this.wrapper = dataSource;
    this.sqlMapper = dataSource.getDataMapper();
    this.sqlBuilder = dataSource.getQueryBuilder();
    this.errorHandler = dataSource.getErrorHandler();
  }
  @Override
  public Uni<DocLog> getById(String id) {
    final var sql = sqlBuilder.docLogs().getById(id);
    if(log.isDebugEnabled()) {
      log.debug("DocLog byId query, with props: {} \r\n{}", 
          sql.getProps().deepToString(),
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.docLog(row))
        .execute(sql.getProps())
        .onItem()
        .transform((RowSet<DocLog> rowset) -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return it.next();
          }
          return null;
        })
        .onFailure(e -> errorHandler.notFound(e)).recoverWithNull()
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't get 'DOC_LOG' by 'id': '" + id + "'!", sql, e)));
  }
  @Override
  public Multi<DocLog> findAll() {
    final var sql = sqlBuilder.docLogs().findAll();
    if(log.isDebugEnabled()) {
      log.debug("DocLog findAll query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.docLog(row))
        .execute()
        .onItem()
        .transformToMulti((RowSet<DocLog> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlFailed("Can't find 'DOC_LOG'!", sql, e)));
  }
}
