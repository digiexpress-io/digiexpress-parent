package io.resys.thena.docdb.file.builders;

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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import io.resys.thena.docdb.api.actions.PullActions.MatchCriteria;
import io.resys.thena.docdb.api.actions.PullActions.MatchCriteriaType;
import io.resys.thena.docdb.api.models.ImmutableTree;
import io.resys.thena.docdb.api.models.ThenaGitObject.Blob;
import io.resys.thena.docdb.api.models.ThenaGitObject.TreeValue;
import io.resys.thena.docdb.file.FileBuilder;
import io.resys.thena.docdb.file.tables.Table.FileMapper;
import io.resys.thena.docdb.file.tables.Table.FilePool;
import io.resys.thena.docdb.spi.GitDbQueries.BlobQuery;
import io.resys.thena.docdb.spi.ErrorHandler;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BlobQueryFilePool implements BlobQuery {

  private final FilePool client;
  private final FileMapper mapper;
  private final FileBuilder builder;
  private final ErrorHandler errorHandler;

  @Override
  public Uni<Blob> getById(String blobId) {
    final var sql = builder.blobs().getById(blobId);
    return client.preparedQuery(sql)
        .mapping(row -> mapper.blob(row))
        .execute()
        .onItem()
        .transform((Collection<Blob> rowset) -> {
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
    final var sql = builder.blobs().findAll();
    return client.preparedQuery(sql)
        .mapping(row -> mapper.blob(row))
        .execute()
        .onItem()
        .transformToMulti((Collection<Blob> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'BLOB'!", e));
  }
  @Override
  public Multi<Blob> findAll(String treeId, List<String> blobNames, List<MatchCriteria> blobCriteria) {
    final var blobSql = builder.blobs().findByTreeId(treeId);
    final var treeSql = builder.trees().getById(treeId);
    
    return client.preparedQuery(treeSql) 
    .mapping(row -> mapper.tree(row))
    .execute()
    .onItem()
    .transform(rowset -> {
      final var tree = rowset.iterator().next();
      final var values = new HashMap<String, TreeValue>();
      
      for(final var item : tree.getValues().values()) {
        if(blobNames.contains(item.getName())) {
          values.put(item.getName(), item);
        }
      }
      return ImmutableTree.builder()
          .id(tree.getId())
          .values(values)
          .build();
    }).onItem().transformToUni(tree -> {
      
      return client.preparedQuery(blobSql) 
          .mapping(row -> mapper.blob(row))
          .execute()
          .onItem()
          .transform((Collection<Blob> rowset) -> {
            List<Blob> result = new ArrayList<Blob>();
            for(final var item : rowset) {
              if(isMatch(item, blobCriteria) && tree.getValues().containsKey(item.getId())) {
                result.add(item);
              }
            }
            return result;
          });
    })
    .onItem()
    .transformToMulti((List<Blob> rowset) -> Multi.createFrom().iterable(rowset))
    .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'BLOB' by tree: " + treeId + "!", e));
  }
  @Override
  public Multi<Blob> findAll(String treeId, List<MatchCriteria> criteria) {
    return findAll(treeId, Collections.emptyList(), criteria);
  }
  public static boolean isMatch(Blob item, List<MatchCriteria> blobCriteria) {
    var found = true;
    for(final var crit : blobCriteria) {
      final var field = crit.getKey();
      
      // TODO :  EXACT or LIKE for undefined case???
      if(!item.getValue().containsKey(field)) {
        found = false;
        break;
      }
      
      final var jsonValue = item.getValue().getValue(field);
      if(( crit.getType() == MatchCriteriaType.EQUALS ||
           crit.getType() == MatchCriteriaType.LIKE)
              
          && jsonValue == null && crit.getValue() == null) {
        found = true;
        break;
      }
      if(jsonValue == null) {
        found = false;
        break;
      }
      if(crit.getType() == MatchCriteriaType.EQUALS && jsonValue.toString().equals(crit.getValue())) {
        continue;
      }
      if(crit.getType() == MatchCriteriaType.LIKE && jsonValue.toString().indexOf(crit.getValue()) > -1) {
        continue;
      }
      found = false;
      break;
    }
    return found;
  }
}
