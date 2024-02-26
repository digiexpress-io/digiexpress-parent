package io.resys.thena.docdb.store.file.queries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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

import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.spi.DbCollections;
import io.resys.thena.docdb.spi.DbState.RepoBuilder;
import io.resys.thena.docdb.store.file.FileBuilder;
import io.resys.thena.docdb.store.file.tables.Table.FileMapper;
import io.resys.thena.docdb.store.file.tables.Table.FilePool;
import io.resys.thena.docdb.store.file.tables.Table.FilePreparedQuery;
import io.resys.thena.docdb.store.file.tables.Table.FileStatement;
import io.resys.thena.docdb.store.file.tables.Table.FileTuple;
import io.resys.thena.docdb.store.file.tables.Table.FileTupleList;
import io.resys.thena.docdb.support.ErrorHandler;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.UniJoin.JoinAllStrategy;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RepoBuilderFilePool implements RepoBuilder {
  private final FilePool client;
  private final DbCollections names;
  private final FileMapper mapper;
  private final FileBuilder builder;
  private final ErrorHandler errorHandler;


  @Override
  public Uni<Repo> getByName(String name) {
    final var sql = builder.repo().getByName(name);
    return client.preparedQuery(sql)
        .mapping(row -> mapper.repo(row))
        .execute()
        .onItem()
        .transform((Collection<Repo> rowset) -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return it.next();
          }
          return null;
        })
        .onFailure(e -> errorHandler.notFound(e)).recoverWithNull()
        .onFailure().invoke(e -> {
          
          
          errorHandler.deadEnd("Can't find 'REPOS' by 'name'!", e);
        });
  }

  @Override
  public Uni<Repo> getByNameOrId(String nameOrId) {
    final var sql = builder.repo().getByNameOrId(nameOrId);
    return client.preparedQuery(sql)
        .mapping(row -> mapper.repo(row))
        .execute()
        .onItem()
        .transform((Collection<Repo> rowset) -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return it.next();
          }
          return null;
        })
        .onFailure(e -> errorHandler.notFound(e)).recoverWithNull()
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'REPOS' by 'name' or 'id'!", e));
  }
  
  @Override
  public Uni<Repo> insert(final Repo newRepo) {
    final var next = names.toRepo(newRepo);
    final var sqlBuilder = this.builder.withOptions(next);
    final var repoInsert = sqlBuilder.repo().insertOne(newRepo);
    final var tablesCreate = Arrays.asList(
      sqlBuilder.blobs().create(),
      sqlBuilder.commits().create(),
      sqlBuilder.treeItems().create(),
      sqlBuilder.trees().create(),
      sqlBuilder.refs().create(),
      sqlBuilder.tags().create(),
      
      sqlBuilder.commits().constraints(),
      sqlBuilder.refs().constraints(),
      sqlBuilder.tags().constraints(),
      sqlBuilder.treeItems().constraints()
    );
      
    
    final Uni<Void> create = client.preparedQuery(sqlBuilder.repo().create()).execute()
        .onItem().transformToUni(data -> Uni.createFrom().voidItem())
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't create table 'REPOS'!", e));;
    
    
    final Uni<Void> insert = client.preparedQuery(repoInsert).execute()
        .onItem().transformToUni(rowSet -> Uni.createFrom().voidItem())
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't insert into 'REPO': '" + repoInsert.getValue() + "'!", e));
    
    
    final List<Uni<Void>> commands = new ArrayList<Uni<Void>>();
    commands.add(create);
    commands.add(insert);
    
    for(final var table : tablesCreate) {
      FilePreparedQuery<?> query = null;
      if(table instanceof FileStatement) {
        query = client.preparedQuery((FileStatement) table);
      } else if(table instanceof FileTuple) {
        query = client.preparedQuery((FileTuple) table);        
      } else {
        query = client.preparedQuery((FileTupleList) table);
      }
      
      final Uni<Void> nested = query.execute()
          .onItem().transformToUni(rowSet -> Uni.createFrom().voidItem())
          .onFailure().invoke(e -> errorHandler.deadEnd("Can't create tables: " + tablesCreate, e));
      commands.add(nested);
    }    
    
    @SuppressWarnings("unchecked")
    final JoinAllStrategy<Void> join = Uni.join().all(commands.toArray(new Uni[] {}));
        
    return join.andFailFast().onItem().transform((items) -> newRepo);
  }

  @Override
  public Multi<Repo> findAll() {
    return client.preparedQuery(this.builder.repo().findAll())
    .mapping(row -> mapper.repo(row))
    .execute()
    .onItem()
    .transformToMulti((Collection<Repo> rowset) -> Multi.createFrom().iterable(rowset))
    .onFailure(e -> errorHandler.notFound(e)).recoverWithCompletion()
    .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'REPOS'!", e));
  }

  @Override
  public Uni<Repo> delete(Repo repo) {
    throw new RuntimeException("Not implemented!!!");
  }
}
