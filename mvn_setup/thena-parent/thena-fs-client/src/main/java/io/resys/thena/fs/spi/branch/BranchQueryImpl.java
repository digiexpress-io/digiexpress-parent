package io.resys.thena.fs.spi.branch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*-
 * #%L
 * thena-fs-client
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import io.resys.thena.fs.api.branches.BranchQuery;
import io.resys.thena.fs.api.trees.NameExpressionBuilder;
import io.resys.thena.fs.entities.Index;
import io.resys.thena.fs.entities.Ref;
import io.resys.thena.fs.spi.FileSystemConfig;
import io.resys.thena.fs.tables.FsDb;
import io.resys.thena.fs.tables.filters.ImmutableRefTableFilter;
import io.resys.thena.fs.tables.filters.ImmutableRefTableNameFilter;
import io.resys.thena.fs.tables.filters.NameExpressionBuilderImpl;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BranchQueryImpl implements BranchQuery {

  private final Uni<FsDb> db_uni;
  @SuppressWarnings("unused")
  private final String tenantId;
  private final List<String> blobTypes = new ArrayList<>();
  private final List<String> docIds = new ArrayList<>();
  private final FileSystemConfig config;
  private Consumer<NameExpressionBuilder> nameExpr;
  private String branchId;
  private UUID commitId;
  
  private Boolean includeBlobs = true;
  
  @Override
  public BranchQuery commitId(UUID commitId) {
    this.commitId = commitId;
    return this;
  }
  @Override
  public BranchQuery docIds(List<String> docIds) {
    this.docIds.addAll(docIds);
    return this;
  }
  @Override
  public BranchQuery includeBlobs(boolean includeBlobs) {
    this.includeBlobs = includeBlobs;
    return this;
  }
  @Override
  public BranchQuery branchId(String branchId) {
    this.branchId = RepoAssert.notEmpty(branchId, () -> "branchId can't be empty!");
    return this;
  }  
  @Override
  public BranchQuery branchName(Consumer<NameExpressionBuilder> nameExpr) {
    this.nameExpr = RepoAssert.notNull(nameExpr, () -> "nameExpr can't be empty!");
    return this;
  }

  @Override
  public BranchQuery blobTypes(String... type) {
    this.blobTypes.addAll(Arrays.asList(type));
    return this;
  } 
  @Override
  public Multi<Ref> findAll() {
    return baseline().onItem().transformToUni(this::join).concatenate();
  }
  
  @Override
  public Uni<Optional<Ref>> findOne() {
    return baseline().collect().asList()
      .onItem().transformToUni(found -> {
        final var actual = found.size();
        if(actual > 1) {
          throw new BranchQueryException("Expecting exactly 1 or 0 result but found: " + actual + "!", 
            JsonObject.of(
              "branchId", branchId,
              "isNameExpr", nameExpr == null ? false : true 
            )
          );
        }
        final var ref = found.stream().findFirst();
        if(ref.isEmpty()) {
          return Uni.createFrom().item(Optional.empty());
        }
        return join(ref.get()).map(Optional::of);
      });
  }
  @Override
  public Uni<Ref> getOne() {
    return baseline().collect().asList()
      .onItem().transformToUni(found -> {
        final var actual = found.size();

        if(actual != 1) {
          final var branchName = getBranchName();
          throw new BranchNotFoundException("Expecting exactly 1 result but found: " + actual + "!", 
            JsonObject.of(
              "branchId", branchId,
              "branchName", branchName
            )
          );
        }
        return join(found.stream().findFirst().get());
      });
  }


  
  private Multi<Ref> baseline() {
    if(commitId != null) {
      return db_uni.onItem().transformToMulti(db -> db.query().queryRef()
        .findAllByFilterWithoutRef(
          Boolean.TRUE.equals(includeBlobs), 
          new JsonArray(docIds), new JsonArray(blobTypes), 
          commitId)
        );
    }
    
    final ImmutableRefTableFilter.Builder filter = ImmutableRefTableFilter.builder();
    if(branchId == null && nameExpr == null) {
      filter.branchId(branchId).nameExpr(nameExpr);
    } else {
      filter.branchId(BranchConstants.DEFAULT_BRANCH);
    }
    filter
      .docIds(docIds)
      .blobTypes(blobTypes)
      .includeBlobs(Boolean.TRUE.equals(includeBlobs));
    
    
//    final var isBlobs = !excludeBlobs && !excludeNodes;
    
//    // load all 
//    if(isBlobs) {
//      return db_uni
//        .onItem().transformToUni(tx -> tx.query().queryCommit().getByIdWithNodesAndBlobs(new NodesAndBlobsFilter(tag.getCommitId(), blobTypes)))
//        .map(tuple -> tag.withTransitives(tuple.getItem1(), tuple.getItem2())); 
//    }
//    
//    // load nodes
//    final var isNodes = !excludeNodes;
//    if(isNodes) {
//      return db_uni
//        .onItem().transformToUni(tx -> tx.query().queryCommit().getByIdWithNodes(new NodesFilter(tag.getCommitId(), blobTypes)))
//        .map(tuple -> tag.withTransitives(tuple.getItem1(), tuple.getItem2()));
//    }    
//    
    return db_uni.onItem().transformToMulti(db -> db.query().queryRef().findAllByFilter(filter.build()));
  }
  
  private Uni<Ref> join(Ref tag) {
    if(tag.getTransitives() != null && tag.getTransitives().getTree() != null) {
      config.getCache().cacheOneTree(tag.getTransitives().getTree());
    }
    return Uni.createFrom().item(tag);
  }
  @Override
  public Multi<Index> findIndexOnly() {
    RepoAssert.isTrue(branchId != null || nameExpr != null, () -> "branchId can't be empty!");

    final var filter = ImmutableRefTableNameFilter.builder()
        .nameExpr(nameExpr)
        .branchId(branchId)
        .build();
    
    return db_uni.onItem().transformToMulti(db -> db.query().queryRef().findAllByName(filter))
        .collect().asList()
        .onItem().transformToMulti(branches -> {
          if(branches.isEmpty()) {
            return Multi.createFrom().empty();
          }
          return db_uni.onItem()
              .transformToMulti(db -> db.query().queryTreeIndex().findAllByByBranchId(branches.getFirst().getId()));          
        });
  }

  private Optional<String> getBranchName() {
    final List<Object> props = new ArrayList<>();
    new NameExpressionBuilderImpl("", next -> { props.add(next); return 0; }, new StringBuilder())
      .close();
    
    if(props.size() == 0) {
      return Optional.empty();
    }
    
    if(props.size() == 1 && props.getFirst() instanceof String &&
        !BranchConstants.DEFAULT_BRANCH.equals(props.getFirst())) {
      return Optional.ofNullable((String) props.getFirst());
    }
    return Optional.empty();
  }
}
