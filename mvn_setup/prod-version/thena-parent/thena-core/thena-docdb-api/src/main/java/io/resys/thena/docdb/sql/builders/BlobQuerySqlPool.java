package io.resys.thena.docdb.sql.builders;

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

import java.util.ArrayList;
import java.util.List;

import io.resys.thena.docdb.api.models.Objects.Blob;
import io.resys.thena.docdb.api.models.Objects.Tree;
import io.resys.thena.docdb.spi.ErrorHandler;
import io.resys.thena.docdb.spi.ClientQuery.BlobQuery;
import io.resys.thena.docdb.sql.SqlBuilder;
import io.resys.thena.docdb.sql.SqlMapper;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BlobQuerySqlPool implements BlobQuery {

  private final io.vertx.mutiny.sqlclient.Pool client;
  private final SqlMapper sqlMapper;
  private final SqlBuilder sqlBuilder;
  private final ErrorHandler errorHandler;

  @Override
  public Uni<Blob> id(String blobId) {
    final var sql = sqlBuilder.blobs().getById(blobId);
    return client.preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.blob(row))
        .execute(sql.getProps())
        .onItem()
        .transform((RowSet<Blob> rowset) -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return it.next();
          }
          return null;
        })
        .onFailure(e -> errorHandler.notFound(e)).recoverWithNull()
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'BLOB' by 'id': '" + blobId + "'!", e));
  }
  @Override
  public Uni<List<Blob>> id(List<String> blobId) {
    final var sql = sqlBuilder.blobs().findByIds(blobId);
    return client.preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.blob(row))
        .execute(sql.getProps())
        .onItem()
        .transform((RowSet<Blob> rowset) -> {
          List<Blob> result = new ArrayList<Blob>();
          for(final var item : rowset) {
            result.add(item);
          }
          return result;
        })
        .onFailure(e -> errorHandler.notFound(e)).recoverWithNull()
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'BLOB' by 'id'-s: '" + String.join(",", blobId) + "'!", e));
  }
  @Override
  public Multi<Blob> find() {
    final var sql = sqlBuilder.blobs().findAll();
    return client.preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.blob(row))
        .execute()
        .onItem()
        .transformToMulti((RowSet<Blob> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'BLOB'!", e));
  }
  @Override
  public Multi<Blob> find(Tree tree) {
    final var sql = sqlBuilder.blobs().findByTree(tree);
    return client.preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.blob(row))
        .execute(sql.getProps())
        .onItem()
        .transformToMulti((RowSet<Blob> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'BLOB' by tree: " + tree.getId() + "!", e));
  }
}
