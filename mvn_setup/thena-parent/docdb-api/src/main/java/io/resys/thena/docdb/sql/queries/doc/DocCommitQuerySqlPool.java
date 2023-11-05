package io.resys.thena.docdb.sql.queries.doc;

import io.resys.thena.docdb.api.LogConstants;
import io.resys.thena.docdb.api.models.ImmutableDocCommitLock;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocCommit;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocCommitLock;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocCommitLockStatus;
import io.resys.thena.docdb.spi.DocDbQueries.DocCommitQuery;
import io.resys.thena.docdb.spi.DocDbQueries.DocLockCriteria;
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
public class DocCommitQuerySqlPool implements DocCommitQuery {
  private final SqlClientWrapper wrapper;
  private final SqlMapper sqlMapper;
  private final SqlBuilder sqlBuilder;
  private final ErrorHandler errorHandler;

  @Override
  public Uni<DocCommit> getById(String commit) {
    final var sql = sqlBuilder.docCommits().getById(commit);
    if(log.isDebugEnabled()) {
      log.debug("DocCommit byId query, with props: {} \r\n{}", 
          sql.getProps().deepToString(),
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.docCommit(row))
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
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't get 'DOC_COMMIT' by 'id': '" + commit + "'!", e));
  }
  @Override
  public Multi<DocCommit> findAll() {
    final var sql = sqlBuilder.commits().findAll();
    if(log.isDebugEnabled()) {
      log.debug("DocCommit findAll query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.docCommit(row))
        .execute()
        .onItem()
        .transformToMulti((RowSet<DocCommit> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'DOC_COMMIT'!", e));
  }
  @Override
  public Uni<DocCommitLock> getLock(DocLockCriteria crit) {
    final var sql = sqlBuilder.docCommits().getLock(crit);
    if(log.isDebugEnabled()) {
      log.debug("DocCommit: {} getLock query, with props: {} \r\n{}",
          DocCommitQuerySqlPool.class,
          sql.getProps().deepToString(),
          sql.getValue());
    }
    if(crit.getBranchId().isEmpty()) {
      return Uni.createFrom().item(ImmutableDocCommitLock.builder().status(DocCommitLockStatus.NOT_FOUND).build());
    }
    
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.docCommitLock(row))
        .execute(sql.getProps())
        .onItem()
        .transform((RowSet<DocCommitLock> rowset) -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return it.next();
          }
          return null;
        })
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't lock commit for branch: '" + crit.getBranchId() + "'!", e));
  }
}
