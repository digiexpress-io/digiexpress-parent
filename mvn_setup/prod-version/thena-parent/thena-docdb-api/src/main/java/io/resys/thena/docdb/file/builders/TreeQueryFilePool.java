package io.resys.thena.docdb.file.builders;

import java.util.Collection;

/*-
 * #%L
 * thena-docdb-pgsql
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import io.resys.thena.docdb.api.models.ImmutableTree;
import io.resys.thena.docdb.api.models.Objects.Tree;
import io.resys.thena.docdb.api.models.Objects.TreeValue;
import io.resys.thena.docdb.file.FileBuilder;
import io.resys.thena.docdb.file.tables.Table.FileMapper;
import io.resys.thena.docdb.file.tables.Table.FilePool;
import io.resys.thena.docdb.spi.ClientQuery.TreeQuery;
import io.resys.thena.docdb.spi.ErrorHandler;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TreeQueryFilePool implements TreeQuery {

  private final FilePool client;
  private final FileMapper mapper;
  private final FileBuilder sqlBuilder;
  private final ErrorHandler errorHandler;
  
  @Override
  public Uni<Tree> id(String tree) {
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
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't find/load 'TREE': " + tree + "!", e));
  }
  @Override
  public Multi<Tree> find() {
    final var sql = sqlBuilder.trees().findAll();
    return client.preparedQuery(sql)
        .mapping(row -> mapper.tree(row))
        .execute()
        .onItem()
        .transformToMulti((Collection<Tree> rowset) -> Multi.createFrom().iterable(rowset))
        .onItem().transformToUni((Tree tree) -> id(tree.getId()))
        .concatenate()
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'TREE'!", e));
  }
}
