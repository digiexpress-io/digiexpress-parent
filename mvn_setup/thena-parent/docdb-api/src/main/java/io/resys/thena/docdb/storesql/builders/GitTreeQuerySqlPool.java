package io.resys.thena.docdb.storesql.builders;

import io.resys.thena.docdb.api.LogConstants;
import io.resys.thena.docdb.api.models.ImmutableTree;
import io.resys.thena.docdb.api.models.ThenaGitObject.Tree;
import io.resys.thena.docdb.api.models.ThenaGitObject.TreeValue;
import io.resys.thena.docdb.models.git.GitQueries.GitTreeQuery;
import io.resys.thena.docdb.storesql.SqlBuilder;
import io.resys.thena.docdb.storesql.SqlMapper;
import io.resys.thena.docdb.storesql.support.SqlClientWrapper;
import io.resys.thena.docdb.support.ErrorHandler;
import io.resys.thena.docdb.support.ErrorHandler.SqlFailed;
import io.resys.thena.docdb.support.ErrorHandler.SqlTupleFailed;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = LogConstants.SHOW_SQL)
@RequiredArgsConstructor
public class GitTreeQuerySqlPool implements GitTreeQuery {

  private final SqlClientWrapper wrapper;
  private final SqlMapper sqlMapper;
  private final SqlBuilder sqlBuilder;
  private final ErrorHandler errorHandler;
  @Override
  public Uni<Tree> getById(String tree) {
    final var sql = sqlBuilder.treeItems().getByTreeId(tree);
    if(log.isDebugEnabled()) {
      log.debug("Tree: {} getById query, with props: {} \r\n{}",
          GitTreeQuerySqlPool.class,
          sql.getProps().deepToString(),
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.treeItem(row))
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
    final var sql = sqlBuilder.trees().findAll();
    if(log.isDebugEnabled()) {
      log.debug("Tree: {} findAll query, with props: {} \r\n{}", 
          GitTreeQuerySqlPool.class,
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.tree(row))
        .execute()
        .onItem()
        .transformToMulti((RowSet<Tree> rowset) -> Multi.createFrom().iterable(rowset))
        .onItem().transformToUni((Tree tree) -> getById(tree.getId()))
        .concatenate()
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlFailed("Can't find 'TREE'!", sql, e)));
  }
}
