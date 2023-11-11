package io.resys.thena.docdb.models.doc.store.sql.queries;

import io.resys.thena.docdb.api.LogConstants;
import io.resys.thena.docdb.api.models.ThenaDocObject.Doc;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocFlatted;
import io.resys.thena.docdb.models.doc.DocDbQueries.DocQuery;
import io.resys.thena.docdb.models.doc.DocDbQueries.FlattedCriteria;
import io.resys.thena.docdb.store.sql.SqlBuilder;
import io.resys.thena.docdb.store.sql.SqlMapper;
import io.resys.thena.docdb.store.sql.support.SqlClientWrapper;
import io.resys.thena.docdb.support.ErrorHandler;
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
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't get 'DOC' by 'id': '" + id + "'!", e));
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
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'DOC'!", e));
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
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'DOC FLATTED'!", e));
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
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'DOC FLATTED' by any id!", e));
  }
}
