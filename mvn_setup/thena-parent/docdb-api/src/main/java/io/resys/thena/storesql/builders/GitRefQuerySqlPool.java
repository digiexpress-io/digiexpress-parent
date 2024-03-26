package io.resys.thena.storesql.builders;

import io.resys.thena.api.LogConstants;
import io.resys.thena.api.models.ThenaGitObject.Branch;
import io.resys.thena.models.git.GitQueries.GitRefQuery;
import io.resys.thena.storesql.SqlBuilder;
import io.resys.thena.storesql.SqlMapper;
import io.resys.thena.storesql.support.SqlClientWrapper;
import io.resys.thena.support.ErrorHandler;
import io.resys.thena.support.RepoAssert;
import io.resys.thena.support.ErrorHandler.SqlFailed;
import io.resys.thena.support.ErrorHandler.SqlTupleFailed;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j(topic = LogConstants.SHOW_SQL)
@RequiredArgsConstructor
public class GitRefQuerySqlPool implements GitRefQuery {

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
      .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't find 'REF' by refNameOrCommit: '" + refNameOrCommit + "'!", sql, e)));
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
      .onFailure().invoke(e -> errorHandler.deadEnd(new SqlFailed("Can't find 'REF'!", sql, e)));
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
      .onFailure().invoke(e -> errorHandler.deadEnd(new SqlFailed("Can't find 'REF'!", sql, e)));
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
      .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't find 'REF' by name: '" + name + "'!", sql, e)));
  }
}
