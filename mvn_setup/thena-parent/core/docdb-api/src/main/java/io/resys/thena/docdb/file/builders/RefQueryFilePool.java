package io.resys.thena.docdb.file.builders;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
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

import java.util.Collection;

import io.resys.thena.docdb.api.models.ThenaObject.Branch;
import io.resys.thena.docdb.file.FileBuilder;
import io.resys.thena.docdb.file.tables.Table.FileMapper;
import io.resys.thena.docdb.file.tables.Table.FilePool;
import io.resys.thena.docdb.spi.ClientQuery.RefQuery;
import io.resys.thena.docdb.spi.ErrorHandler;
import io.resys.thena.docdb.spi.support.RepoAssert;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class RefQueryFilePool implements RefQuery {

  private final FilePool client;
  private final FileMapper mapper;
  private final FileBuilder sqlBuilder;
  private final ErrorHandler errorHandler;

  @Override
  public Uni<Branch> nameOrCommit(String refNameOrCommit) {
    RepoAssert.notEmpty(refNameOrCommit, () -> "refNameOrCommit must be defined!");
    final var sql = sqlBuilder.refs().getByNameOrCommit(refNameOrCommit);
    return client.preparedQuery(sql)
      .mapping(row -> mapper.ref(row))
      .execute()
      .onItem()
      .transform((Collection<Branch> rowset) -> {
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
    return client.preparedQuery(sql)
      .mapping(row -> mapper.ref(row))
      .execute()
      .onItem()
      .transform((Collection<Branch> rowset) -> {
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
    return client.preparedQuery(sql)
      .mapping(row -> mapper.ref(row))
      .execute()
      .onItem()
      .transformToMulti((Collection<Branch> rowset) -> Multi.createFrom().iterable(rowset))
      .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'REF'!", e));
  }
  @Override
  public Uni<Branch> name(String name) {
    RepoAssert.notEmpty(name, () -> "name must be defined!");
    final var sql = sqlBuilder.refs().getByName(name);
    return client.preparedQuery(sql)
      .mapping(row -> mapper.ref(row))
      .execute()
      .onItem()
      .transform((Collection<Branch> rowset) -> {
        final var it = rowset.iterator();
        if(it.hasNext()) {
          return it.next();
        }
        return null;
      })
      .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'REF' by name: '" + name + "'!", e));
  }
}
