package io.resys.thena.docdb.sql.queries;

import io.resys.thena.docdb.api.LogConstants;
import io.resys.thena.docdb.api.models.ThenaObject.Branch;
import io.resys.thena.docdb.spi.ClientQuery.RefQuery;
import io.resys.thena.docdb.spi.ErrorHandler;
import io.resys.thena.docdb.spi.support.RepoAssert;
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
public class RefQuerySqlPool implements RefQuery {

  private final SqlClientWrapper wrapper;
  private final SqlMapper sqlMapper;
  private final SqlBuilder sqlBuilder;
  private final ErrorHandler errorHandler;

  @Override
  public Uni<Branch> nameOrCommit(String refNameOrCommit) {
    RepoAssert.notEmpty(refNameOrCommit, () -> "refNameOrCommit must be defined!");
    final var sql = sqlBuilder.refs().getByNameOrCommit(refNameOrCommit);
    if(log.isDebugEnabled()) {
      log.debug("Ref refNameOrCommit query, with props: {} \r\n{}", 
          sql.getProps().deepToString(),
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
      .mapping(row -> sqlMapper.ref(row))
      .execute(sql.getProps())
      .onItem()
      .transform((RowSet<Branch> rowset) -> {
        final var it = rowset.iterator();
        if(it.hasNext()) {
          return it.next();
        }
        return null;
      })
      .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'REF' by refNameOrCommit: '" + refNameOrCommit + "'!", e));
  }
  @Override
  public Uni<Branch> get() {
    final var sql = sqlBuilder.refs().getFirst();
    if(log.isDebugEnabled()) {
      log.debug("Ref get query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }

    return wrapper.getClient().preparedQuery(sql.getValue())
      .mapping(row -> sqlMapper.ref(row))
      .execute()
      .onItem()
      .transform((RowSet<Branch> rowset) -> {
        final var it = rowset.iterator();
        if(it.hasNext()) {
          return it.next();
        }
        return null;
      })
      .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'REF'!", e));
  }
  @Override
  public Multi<Branch> findAll() {
    final var sql = sqlBuilder.refs().findAll();
    if(log.isDebugEnabled()) {
      log.debug("Ref findAll query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
      .mapping(row -> sqlMapper.ref(row))
      .execute()
      .onItem()
      .transformToMulti((RowSet<Branch> rowset) -> Multi.createFrom().iterable(rowset))
      .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'REF'!", e));
  }
  @Override
  public Uni<Branch> name(String name) {
    RepoAssert.notEmpty(name, () -> "name must be defined!");
    final var sql = sqlBuilder.refs().getByName(name);
    
    if(log.isDebugEnabled()) {
      log.debug("Ref getByName query, with props: {} \r\n{}", 
          sql.getProps().deepToString(),
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
      .mapping(row -> sqlMapper.ref(row))
      .execute(sql.getProps())
      .onItem()
      .transform((RowSet<Branch> rowset) -> {
        final var it = rowset.iterator();
        if(it.hasNext()) {
          return it.next();
        }
        return null;
      })
      .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'REF' by name: '" + name + "'!", e));
  }
}
