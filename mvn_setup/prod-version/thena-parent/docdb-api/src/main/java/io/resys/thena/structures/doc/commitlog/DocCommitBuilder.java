package io.resys.thena.structures.doc.commitlog;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.api.entities.doc.DocCommit;
import io.resys.thena.api.entities.doc.DocCommitTree;
import io.resys.thena.api.entities.doc.DocCommitTree.DocCommitTreeOperation;
import io.resys.thena.api.entities.doc.DocEntity.DocType;
import io.resys.thena.api.entities.doc.DocEntity.IsDocObject;
import io.resys.thena.api.entities.doc.ImmutableDocBranch;
import io.resys.thena.api.entities.doc.ImmutableDocCommit;
import io.resys.thena.api.entities.doc.ImmutableDocCommitTree;
import io.resys.thena.jsonpatch.JsonPatch;
import io.resys.thena.support.OidUtils;
import io.smallrye.mutiny.tuples.Tuple2;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;



public class DocCommitBuilder {
  private final String tenantId;
  private final String commitId;
  private final ImmutableDocCommit.Builder commit;
  private final List<DocCommitTree> trees = new ArrayList<>();
  private final DocCommitLogger logger;
  private final OffsetDateTime createdAt;
  private final String docId;
  private final Optional<String> branchId;
  public DocCommitBuilder(String tenantId, DocCommit commit) {
    super();
    this.commitId = commit.getId();
    this.tenantId = tenantId;
    this.docId = commit.getDocId();
    this.commit = ImmutableDocCommit.builder().from(commit);
    this.logger = new DocCommitLogger(tenantId, commit);
    this.createdAt = commit.getCreatedAt();
    this.branchId = commit.getBranchId();
  }
  public String getTenantId() {
    return tenantId;
  }
  public String getCommitId() {
    return commitId;
  }
  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }
  
  
  public DocCommitBuilder add(IsDocObject entity) {
    if(entity instanceof DocBranch) {
      final var branch = (DocBranch) entity;
      final var emptyJsonObject = new JsonObject("{}");
      final var diff = JsonPatch.diff(emptyJsonObject, branch.getValue());

      this.trees.add(ImmutableDocCommitTree.builder()
          .id(OidUtils.gen())
          .commitId(commitId)
          .docId(docId)
          .branchId(branchId)
          .operationType(DocCommitTreeOperation.ADD)
          .bodyType(entity.getDocType().name())
          .bodyAfter(JsonObject.mapFrom(ImmutableDocBranch.builder().from(branch)
              .value(emptyJsonObject)
              .build()))
          .build());
      this.trees.add(ImmutableDocCommitTree.builder()
          .id(OidUtils.gen())
          .commitId(commitId)
          .docId(docId)
          .branchId(branchId)
          .operationType(DocCommitTreeOperation.ADD)
          .bodyPatch(diff.getValue())
          .bodyType(DocType.DOC_BRANCH_PATCH.name())
          .build());
      this.logger.add(ImmutableDocBranchPatch.builder()
          .id(entity.getId())
          .patchValue(diff.getValue())
          .build());
      return this;
    }
    
    this.trees.add(ImmutableDocCommitTree.builder()
        .id(OidUtils.gen())
        .commitId(commitId)
        .docId(docId)
        .branchId(branchId)
        .operationType(DocCommitTreeOperation.ADD)
        .bodyAfter(JsonObject.mapFrom(entity))
        .bodyType(entity.getDocType().name())
        .build());
    this.logger.add(entity);
    return this;
  }
  public DocCommitBuilder merge(IsDocObject previous, IsDocObject next) {
    

    if(previous instanceof DocBranch) {
      final var branchPrev = (DocBranch) previous;
      final var branchNext = (DocBranch) next;
      final var diff = JsonPatch.diff(branchPrev.getValue(), branchNext.getValue());
      final var emptyJsonObject = new JsonObject("{}");
      this.trees.add(ImmutableDocCommitTree.builder()
          .id(OidUtils.gen())
          .commitId(commitId)
          .docId(docId)
          .branchId(branchId)
          .operationType(DocCommitTreeOperation.MERGE)
          .bodyBefore(JsonObject.mapFrom(ImmutableDocBranch.builder().from(branchPrev)
              .value(emptyJsonObject)
              .build()))
          .bodyAfter(JsonObject.mapFrom(ImmutableDocBranch.builder().from(branchNext)
              .value(emptyJsonObject)
              .build()))
          .bodyType(next.getDocType().name())
          .build());
      this.trees.add(ImmutableDocCommitTree.builder()
          .id(OidUtils.gen())
          .commitId(commitId)
          .docId(docId)
          .branchId(branchId)
          .operationType(DocCommitTreeOperation.MERGE)
          .bodyPatch(diff.getValue())
          .bodyType(DocType.DOC_BRANCH_PATCH.name())
          .build());
      this.logger.add(ImmutableDocBranchPatch.builder()
          .id(branchPrev.getId())
          .patchValue(diff.getValue())
          .build());
      return this;
    }
    
    this.trees.add(ImmutableDocCommitTree.builder()
        .id(OidUtils.gen())
        .commitId(commitId)
        .docId(docId)
        .branchId(branchId)
        .operationType(DocCommitTreeOperation.MERGE)
        .bodyBefore(JsonObject.mapFrom(previous))
        .bodyAfter(JsonObject.mapFrom(next))
        .bodyType(next.getDocType().name())
        .build());
    this.logger.merge(previous, next);
    return this;
  }
  public DocCommitBuilder rm(IsDocObject current) {
    this.trees.add(ImmutableDocCommitTree.builder()
        .id(OidUtils.gen())
        .docId(docId)
        .branchId(branchId)
        .commitId(commitId)
        .operationType(DocCommitTreeOperation.REMOVE)
        .bodyBefore(JsonObject.mapFrom(current))
        .bodyType(current.getDocType().name())
        .bodyAfter(null)
        .build());
    this.logger.remove(current);
    return this;
  }
  public Tuple2<DocCommit, List<DocCommitTree>> close() {
    final var commit = this.commit.commitLog(this.logger.build()).build();
    return Tuple2.of(commit, Collections.unmodifiableList(this.trees));
  }
  
  @Value.Immutable
  interface DocBranchPatch extends IsDocObject {
    
    JsonArray getPatchValue();
    
    @Override
    default DocType getDocType() {
      return DocType.DOC_BRANCH_PATCH;
    }
  }
  
}
