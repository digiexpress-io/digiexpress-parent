package io.resys.thena.storefile.queries;

import java.util.Collection;

import io.resys.thena.api.entities.git.ImmutableTree;
import io.resys.thena.api.entities.git.Tree;
import io.resys.thena.api.entities.git.TreeValue;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlSchemaFailed;
import io.resys.thena.storefile.FileBuilder;
import io.resys.thena.storefile.tables.Table.FileMapper;
import io.resys.thena.storefile.tables.Table.FilePool;
import io.resys.thena.structures.git.GitQueries.GitTreeQuery;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TreeQueryFilePool implements GitTreeQuery {

  private final FilePool client;
  private final FileMapper mapper;
  private final FileBuilder sqlBuilder;
  private final ThenaSqlDataSourceErrorHandler errorHandler;
  
  @Override
  public Uni<Tree> getById(String tree) {
    final var sql = sqlBuilder.treeItems().getByTreeId(tree);
    return client.preparedQuery(sql)
        .mapping(row -> mapper.treeItem(row))
        .execute()
        .onItem()
        .transform((Collection<TreeValue> rowset) -> {
          final var builder = ImmutableTree.builder().id(tree);
          final var it = rowset.iterator();
          while(it.hasNext()) {
            TreeValue item = it.next();
            builder.putValues(item.getName(), item);
          }
          return (Tree) builder.build();
        })
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlSchemaFailed("Can't find/load 'TREE': " + tree + "!", "", e)));
  }
  @Override
  public Multi<Tree> findAll() {
    final var sql = sqlBuilder.trees().findAll();
    return client.preparedQuery(sql)
        .mapping(row -> mapper.tree(row))
        .execute()
        .onItem()
        .transformToMulti((Collection<Tree> rowset) -> Multi.createFrom().iterable(rowset))
        .onItem().transformToUni((Tree tree) -> getById(tree.getId()))
        .concatenate()
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlSchemaFailed("Can't find 'TREE'!", "", e)));
  }
}
