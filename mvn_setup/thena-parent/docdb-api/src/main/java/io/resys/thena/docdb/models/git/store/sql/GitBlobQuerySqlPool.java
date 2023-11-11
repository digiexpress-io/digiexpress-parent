package io.resys.thena.docdb.models.git.store.sql;

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

import io.resys.thena.docdb.api.LogConstants;
import io.resys.thena.docdb.api.actions.PullActions.MatchCriteria;
import io.resys.thena.docdb.api.models.ThenaGitObject.Blob;
import io.resys.thena.docdb.models.git.GitDbQueries.GitBlobQuery;
import io.resys.thena.docdb.store.sql.SqlBuilder;
import io.resys.thena.docdb.store.sql.SqlMapper;
import io.resys.thena.docdb.store.sql.support.SqlClientWrapper;
import io.resys.thena.docdb.support.ErrorHandler;
import io.resys.thena.docdb.support.RepoAssert;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = LogConstants.SHOW_SQL)
@RequiredArgsConstructor
public class GitBlobQuerySqlPool implements GitBlobQuery {

  private final SqlClientWrapper wrapper;
  private final SqlMapper sqlMapper;
  private final SqlBuilder sqlBuilder;
  private final ErrorHandler errorHandler;
  
  @Override
  public Uni<Blob> getById(String blobId) {
    final var sql = sqlBuilder.blobs().getById(blobId);
    if(log.isDebugEnabled()) {
      log.debug("Blob: {} get byId query, with props: {} \r\n{}",
          GitBlobQuerySqlPool.class,
          sql.getProps().deepToString(), 
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
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
  public Multi<Blob> findAll() {
    final var sql = sqlBuilder.blobs().findAll();
    if(log.isDebugEnabled()) {
      log.debug("Blob findAll query, with props: {} \r\n{}", 
          "", 
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.blob(row))
        .execute()
        .onItem()
        .transformToMulti((RowSet<Blob> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'BLOB'!", e));
  }
  @Override
  public Multi<Blob> findAll(String treeId, List<MatchCriteria> criteria) {
    final var sql = sqlBuilder.blobs().findByTree(treeId, criteria);
    if(log.isDebugEnabled()) {
      log.debug("Blob: {} findByTreeId query, with props: {} \r\n{}",
          GitBlobQuerySqlPool.class,
          sql.getProps().deepToString(), 
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.blob(row))
        .execute(sql.getProps())
        .onItem()
        .transformToMulti((RowSet<Blob> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'BLOB' by tree: " + treeId + "!", e));
  }
  @Override
  public Multi<Blob> findAll(String treeId, List<String> docIds, List<MatchCriteria> criteria) {
    RepoAssert.isTrue(!docIds.isEmpty(), () -> "docIds is not defined!");
    
    final var sql = sqlBuilder.blobs().findByTree(treeId, docIds, criteria);
    if(log.isDebugEnabled()) {
      log.debug("Blob: {} findByTreeId query, with props: {} \r\n{}",
          GitBlobQuerySqlPool.class,
          sql.getProps().deepToString(), 
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.blob(row))
        .execute(sql.getProps())
        .onItem()
        .transformToMulti((RowSet<Blob> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'BLOB' by tree: " + treeId + "!", e)); 
  }
  
  public Uni<List<Blob>> findById(List<String> blobId) {
    final var sql = sqlBuilder.blobs().findByIds(blobId);
    if(log.isDebugEnabled()) {
      log.debug("Blob findById query, with props: {} \r\n{}", 
          sql.getProps().deepToString(), 
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
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
}
