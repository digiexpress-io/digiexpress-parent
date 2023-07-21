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

import io.resys.thena.docdb.api.models.ThenaObject.Tag;
import io.resys.thena.docdb.file.FileBuilder;
import io.resys.thena.docdb.file.tables.Table.FileMapper;
import io.resys.thena.docdb.file.tables.Table.FilePool;
import io.resys.thena.docdb.spi.ClientQuery.DeleteResult;
import io.resys.thena.docdb.spi.ClientQuery.TagQuery;
import io.resys.thena.docdb.spi.ErrorHandler;
import io.resys.thena.docdb.spi.ImmutableDeleteResult;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TagQueryFilePool implements TagQuery {
  
  private final FilePool client;
  private final FileMapper mapper;
  private final FileBuilder sqlBuilder;
  private final ErrorHandler errorHandler;

  private String name;

  @Override
  public TagQuery name(String name) {
    this.name = name;
    return this;
  }
  @Override
  public Uni<DeleteResult> delete() {
    final var sql = sqlBuilder.tags().deleteByName(name);
    return client.preparedQuery(sql)
        .execute()
        .onItem()
        .transform(result -> (DeleteResult) ImmutableDeleteResult.builder().deletedCount(1).build())
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't delete 'TAG' by name: '" + name + "'!", e));
  }
  @Override
  public Uni<Tag> getFirst() {
    final var sql = sqlBuilder.tags().getFirst();
    return client.preparedQuery(sql)
        .mapping(row -> mapper.tag(row))
        .execute()
        .onItem()
        .transform((Collection<Tag> rowset) -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return it.next();
          }
          return null;
        })
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'TAG'!", e));      
  }
  @Override
  public Multi<Tag> find() {
    if(name == null || name.isBlank()) {
      final var sql = sqlBuilder.tags().findAll();
      return client.preparedQuery(sql)
          .mapping(row -> mapper.tag(row))
          .execute()
          .onItem()
          .transformToMulti((Collection<Tag> rowset) -> Multi.createFrom().iterable(rowset))
          .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'TAG'!", e));      
    }
    final var sql = sqlBuilder.tags().getByName(name);
    return client.preparedQuery(sql)
        .mapping(row -> mapper.tag(row))
        .execute()
        .onItem()
        .transformToMulti((Collection<Tag> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'TAG' by name: '" + name + "'!", e));   
  }
}
