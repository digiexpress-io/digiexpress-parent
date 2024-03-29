package io.resys.thena.storesql.builders;

import io.resys.thena.api.LogConstants;
import io.resys.thena.api.entities.git.ImmutableTree;
import io.resys.thena.api.entities.git.Tree;
import io.resys.thena.api.entities.git.TreeValue;
import io.resys.thena.api.registry.GitRegistry;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlFailed;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlTupleFailed;
import io.resys.thena.structures.git.GitQueries.GitTreeQuery;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = LogConstants.SHOW_SQL)
public class GitTreeQuerySqlPool implements GitTreeQuery {

  private final ThenaSqlDataSource wrapper;
  private final GitRegistry registry;
  private final ThenaSqlDataSourceErrorHandler errorHandler;
  
  public GitTreeQuerySqlPool(ThenaSqlDataSource dataSource) {
    this.wrapper = dataSource;
    this.registry = dataSource.getRegistry().git();
    this.errorHandler = dataSource.getErrorHandler();
  }
  
  @Override
  public Uni<Tree> getById(String tree) {
    final var sql = registry.treeValues().getByTreeId(tree);
    if(log.isDebugEnabled()) {
      log.debug("Tree: {} getById query, with props: {} \r\n{}",
          GitTreeQuerySqlPool.class,
          sql.getProps().deepToString(),
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(registry.treeValues().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transform((RowSet<TreeValue> rowset) -> {
          final var builder = ImmutableTree.builder().id(tree);
          final var it = rowset.iterator();
          while(it.hasNext()) {
            TreeValue item = it.next();
            builder.putValues(item.getName(), item);
          }
          return (Tree) builder.build();
        })
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't find/load 'TREE': " + tree + "!", sql, e)));
  }
  @Override
  public Multi<Tree> findAll() {
    final var sql = registry.trees().findAll();
    if(log.isDebugEnabled()) {
      log.debug("Tree: {} findAll query, with props: {} \r\n{}", 
          GitTreeQuerySqlPool.class,
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(registry.trees().defaultMapper())
        .execute()
        .onItem()
        .transformToMulti((RowSet<Tree> rowset) -> Multi.createFrom().iterable(rowset))
        .onItem().transformToUni((Tree tree) -> getById(tree.getId()))
        .concatenate()
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlFailed("Can't find 'TREE'!", sql, e)));
  }
}
