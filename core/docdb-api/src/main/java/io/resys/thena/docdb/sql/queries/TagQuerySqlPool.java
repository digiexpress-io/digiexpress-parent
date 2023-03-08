package io.resys.thena.docdb.sql.queries;

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

import io.resys.thena.docdb.api.models.Objects.Tag;
import io.resys.thena.docdb.spi.ClientQuery.DeleteResult;
import io.resys.thena.docdb.spi.ClientQuery.TagQuery;
import io.resys.thena.docdb.spi.ErrorHandler;
import io.resys.thena.docdb.spi.ImmutableDeleteResult;
import io.resys.thena.docdb.sql.SqlBuilder;
import io.resys.thena.docdb.sql.SqlMapper;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = LogConstants.SHOW_SQL)
@RequiredArgsConstructor
public class TagQuerySqlPool implements TagQuery {
  
  private final io.vertx.mutiny.sqlclient.Pool client;
  private final SqlMapper sqlMapper;
  private final SqlBuilder sqlBuilder;
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
    if(log.isDebugEnabled()) {
      log.debug("Tag delete query, with props: {} \r\n{}", 
          sql.getProps().deepToString(),
          sql.getValue());
    }
    return client.preparedQuery(sql.getValue())
        .execute(sql.getProps())
        .onItem()
        .transform(result -> (DeleteResult) ImmutableDeleteResult.builder().deletedCount(1).build())
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't delete 'TAG' by name: '" + name + "'!", e));
  }
  @Override
  public Uni<Tag> getFirst() {
    final var sql = sqlBuilder.tags().getFirst();
    if(log.isDebugEnabled()) {
      log.debug("Tag getFirst query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return client.preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.tag(row))
        .execute()
        .onItem()
        .transform((RowSet<Tag> rowset) -> {
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
      if(log.isDebugEnabled()) {
        log.debug("Tag findAll query, with props: {} \r\n{}", 
            "",
            sql.getValue());
      }
      return client.preparedQuery(sql.getValue())
          .mapping(row -> sqlMapper.tag(row))
          .execute()
          .onItem()
          .transformToMulti((RowSet<Tag> rowset) -> Multi.createFrom().iterable(rowset))
          .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'TAG'!", e));      
    }
    final var sql = sqlBuilder.tags().getByName(name);
    
    if(log.isDebugEnabled()) {
      log.debug("Tag getByName query, with props: {} \r\n{}", 
          sql.getProps().deepToString(),
          sql.getValue());
    }
    return client.preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.tag(row))
        .execute(sql.getProps())
        .onItem()
        .transformToMulti((RowSet<Tag> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'TAG' by name: '" + name + "'!", e));   
  }
}
