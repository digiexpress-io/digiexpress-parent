package io.resys.thena.storesql.builders;

import io.resys.thena.api.LogConstants;
import io.resys.thena.api.models.ThenaDocObject.Doc;
import io.resys.thena.api.models.ThenaDocObject.DocFlatted;
import io.resys.thena.models.doc.DocQueries.DocQuery;
import io.resys.thena.models.doc.DocQueries.FlattedCriteria;
import io.resys.thena.storesql.SqlBuilder;
import io.resys.thena.storesql.SqlMapper;
import io.resys.thena.storesql.support.SqlClientWrapper;
import io.resys.thena.support.ErrorHandler;
import io.resys.thena.support.ErrorHandler.SqlFailed;
import io.resys.thena.support.ErrorHandler.SqlTupleFailed;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j(topic = LogConstants.SHOW_SQL)
@RequiredArgsConstructor
public class DocQuerySqlPool implements DocQuery {
  private final SqlClientWrapper wrapper;
  private final SqlMapper sqlMapper;
  private final SqlBuilder sqlBuilder;
  private final ErrorHandler errorHandler;

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
