package io.resys.thena.docdb.sql.queries.doc;

import io.resys.thena.docdb.api.LogConstants;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocBranch;
import io.resys.thena.docdb.spi.DocDbQueries.DocBranchQuery;
import io.resys.thena.docdb.spi.ErrorHandler;
import io.resys.thena.docdb.sql.SqlBuilder;
import io.resys.thena.docdb.sql.SqlMapper;
import io.resys.thena.docdb.sql.support.SqlClientWrapper;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j(topic = LogConstants.SHOW_SQL)
@RequiredArgsConstructor
public class DocBranchQuerySqlPool implements DocBranchQuery {
  private final SqlClientWrapper wrapper;
  private final SqlMapper sqlMapper;
  private final SqlBuilder sqlBuilder;
  private final ErrorHandler errorHandler;

  @Override
  public Uni<DocBranch> getById(String id) {
    final var sql = sqlBuilder.docBranches().getById(id);
    if(log.isDebugEnabled()) {
      log.debug("DocBranch byId query, with props: {} \r\n{}", 
          sql.getProps().deepToString(),
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.docBranch(row))
        .execute(sql.getProps())
        .onItem()
        .transform((RowSet<DocBranch> rowset) -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return it.next();
          }
          return null;
        })
        .onFailure(e -> errorHandler.notFound(e)).recoverWithNull()
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't get 'DOC_BRANCH' by 'id': '" + id + "'!", e));
  }
  @Override
  public Multi<DocBranch> findAll() {
    final var sql = sqlBuilder.docBranches().findAll();
    if(log.isDebugEnabled()) {
      log.debug("DocLog findAll query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.docBranch(row))
        .execute()
        .onItem()
        .transformToMulti((RowSet<DocBranch> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'DOC_BRANCH'!", e));
  }
}
