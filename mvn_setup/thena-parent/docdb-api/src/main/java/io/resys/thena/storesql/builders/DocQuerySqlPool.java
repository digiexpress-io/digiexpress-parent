package io.resys.thena.storesql.builders;

import io.resys.thena.api.LogConstants;
import io.resys.thena.api.entities.doc.Doc;
import io.resys.thena.api.entities.doc.DocFlatted;
import io.resys.thena.datasource.SqlDataMapper;
import io.resys.thena.datasource.SqlQueryBuilder;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlFailed;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlTupleFailed;
import io.resys.thena.structures.doc.DocQueries.DocQuery;
import io.resys.thena.structures.doc.DocQueries.FlattedCriteria;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.extern.slf4j.Slf4j;


@Slf4j(topic = LogConstants.SHOW_SQL)
public class DocQuerySqlPool implements DocQuery {
  private final ThenaSqlDataSource wrapper;
  private final SqlDataMapper sqlMapper;
  private final SqlQueryBuilder sqlBuilder;
  private final ThenaSqlDataSourceErrorHandler errorHandler;
  
  public DocQuerySqlPool(ThenaSqlDataSource dataSource) {
    this.wrapper = dataSource;
    this.sqlMapper = dataSource.getDataMapper();
    this.sqlBuilder = dataSource.getQueryBuilder();
    this.errorHandler = dataSource.getErrorHandler();
  }
  @Override
  public Uni<Doc> getById(String id) {
    final var sql = sqlBuilder.docs().getById(id);
    if(log.isDebugEnabled()) {
      log.debug("Doc byId query, with props: {} \r\n{}", 
          sql.getProps().deepToString(),
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.doc(row))
        .execute(sql.getProps())
        .onItem()
        .transform((RowSet<Doc> rowset) -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return it.next();
          }
          return null;
        })
        .onFailure(e -> errorHandler.notFound(e)).recoverWithNull()
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't get 'DOC' by 'id': '" + id + "'!", sql, e)));
  }
  @Override
  public Multi<Doc> findAll() {
    final var sql = sqlBuilder.docs().findAll();
    if(log.isDebugEnabled()) {
      log.debug("Doc findAll query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.doc(row))
        .execute()
        .onItem()
        .transformToMulti((RowSet<Doc> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlFailed("Can't find 'DOC'!", sql, e)));
  }
  @Override
  public Multi<DocFlatted> findAllFlatted() {
    final var sql = sqlBuilder.docs().findAllFlatted();
    if(log.isDebugEnabled()) {
      log.debug("Doc findAllFlatted query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.docFlatted(row))
        .execute()
        .onItem()
        .transformToMulti((RowSet<DocFlatted> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlFailed("Can't find 'DOC FLATTED'!", sql, e)));
  }
  @Override
  public Multi<DocFlatted> findAllFlatted(FlattedCriteria criteria) {
    final var sql = sqlBuilder.docs().findAllFlatted(criteria);
    if(log.isDebugEnabled()) {
      log.debug("Doc findAllFlattedByAnyId query, with props: {} \r\n{}", 
          sql.getProps().deepToString(),
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.docFlatted(row))
        .execute(sql.getProps())
        .onItem()
        .transformToMulti((RowSet<DocFlatted> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't find 'DOC FLATTED' by any id!", sql, e)));
  }
}
