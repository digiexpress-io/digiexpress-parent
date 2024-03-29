package io.resys.thena.storesql.builders;

import io.resys.thena.api.LogConstants;
import io.resys.thena.api.entities.git.Branch;
import io.resys.thena.api.registry.GitRegistry;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlFailed;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlTupleFailed;
import io.resys.thena.structures.git.GitQueries.GitRefQuery;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.extern.slf4j.Slf4j;


@Slf4j(topic = LogConstants.SHOW_SQL)
public class GitRefQuerySqlPool implements GitRefQuery {

  private final ThenaSqlDataSource wrapper;
  private final ThenaSqlDataSourceErrorHandler errorHandler;
  private final GitRegistry registry;

  public GitRefQuerySqlPool(ThenaSqlDataSource dataSource) {
    this.wrapper = dataSource;
    this.registry = dataSource.getRegistry().git();
    this.errorHandler = dataSource.getErrorHandler();
  }
  
  @Override
  public Uni<Branch> nameOrCommit(String refNameOrCommit) {
    RepoAssert.notEmpty(refNameOrCommit, () -> "refNameOrCommit must be defined!");
    final var sql = registry.branches().getByNameOrCommit(refNameOrCommit);
    if(log.isDebugEnabled()) {
      log.debug("Ref refNameOrCommit query, with props: {} \r\n{}", 
          sql.getProps().deepToString(),
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
      .mapping(registry.branches().defaultMapper())
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
    final var sql = registry.branches().getFirst();
    if(log.isDebugEnabled()) {
      log.debug("Ref get query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }

    return wrapper.getClient().preparedQuery(sql.getValue())
      .mapping(registry.branches().defaultMapper())
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
    final var sql = registry.branches().findAll();
    if(log.isDebugEnabled()) {
      log.debug("Ref findAll query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
      .mapping(registry.branches().defaultMapper())
      .execute()
      .onItem()
      .transformToMulti((RowSet<Branch> rowset) -> Multi.createFrom().iterable(rowset))
      .onFailure().invoke(e -> errorHandler.deadEnd(new SqlFailed("Can't find 'REF'!", sql, e)));
  }
  @Override
  public Uni<Branch> name(String name) {
    RepoAssert.notEmpty(name, () -> "name must be defined!");
    final var sql = registry.branches().getByName(name);
    
    if(log.isDebugEnabled()) {
      log.debug("Ref getByName query, with props: {} \r\n{}", 
          sql.getProps().deepToString(),
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
      .mapping(registry.branches().defaultMapper())
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
