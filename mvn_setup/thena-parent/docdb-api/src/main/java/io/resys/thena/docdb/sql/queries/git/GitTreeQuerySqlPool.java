package io.resys.thena.docdb.sql.queries.git;

import io.resys.thena.docdb.api.LogConstants;

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
import io.resys.thena.docdb.api.models.ThenaGitObject.Tree;
import io.resys.thena.docdb.api.models.ThenaGitObject.TreeValue;
import io.resys.thena.docdb.spi.GitDbQueries.GitTreeQuery;
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
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't find/load 'TREE': " + tree + "!", e));
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
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'TREE'!", e));
  }
}
