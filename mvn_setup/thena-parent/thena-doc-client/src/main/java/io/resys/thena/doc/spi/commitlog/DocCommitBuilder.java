package io.resys.thena.doc.spi.commitlog;

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
import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.api.entities.doc.DocCommit;
import io.resys.thena.api.entities.doc.DocCommitTree;
import io.resys.thena.api.entities.doc.DocCommitTree.DocCommitTreeOperation;
import io.resys.thena.api.entities.doc.DocEntity.DocType;
import io.resys.thena.api.entities.doc.DocEntity.IsDocObject;
import io.resys.thena.api.entities.doc.DocLock;
import io.resys.thena.api.entities.doc.DocLock.DocBranchLock;
import io.resys.thena.api.entities.doc.ImmutableDocBranch;
import io.resys.thena.api.entities.doc.ImmutableDocCommit;
import io.resys.thena.api.entities.doc.ImmutableDocCommitTree;
import io.resys.thena.doc.api.DocInserts.DocBatchForOne;
import io.resys.thena.doc.api.ImmutableDocBatchForOne;
import io.resys.thena.jsonpatch.JsonPatch;
import io.resys.thena.support.OidUtils;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;



public class DocCommitBuilder {
  private final String tenantId;
  private final String commitId;
  private final ImmutableDocCommit.Builder commit;
  private final List<DocCommitTree> trees = new ArrayList<>();
  private final DocCommitLogger logger;
  private final OffsetDateTime createdAt;
  private final String docId;
  private final Optional<String> branchId;
  private final boolean excludeBranchContentFromLog;
  private final boolean updateCommit;
  private boolean commitTreeEnabled = true;


  
  private DocCommitBuilder(
      String tenantId, 
      Boolean excludeBranchContentFromLog, 
      DocCommit commit, 
      boolean commitTreeEnabled,
      boolean updateCommit) {
    super();
    this.commitId = commit.getId();
    this.tenantId = tenantId;
    this.docId = commit.getDocId();
    this.commit = ImmutableDocCommit.builder().from(commit);
    this.logger = new DocCommitLogger(tenantId, commit);
    this.createdAt = commit.getCreatedAt();
    this.branchId = commit.getBranchId();
    this.excludeBranchContentFromLog = Boolean.TRUE.equals(excludeBranchContentFromLog);
    this.commitTreeEnabled = commitTreeEnabled;
    this.updateCommit = updateCommit;
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
      final var diff = JsonPatch.diff(emptyJsonObject, excludeBranchContentFromLog ? new JsonObject("{}") : branch.getValue());

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
    
    
    final var bodyAfter = JsonObject.mapFrom(entity);
    if(excludeBranchContentFromLog && entity instanceof DocBranch) {
      bodyAfter.put("value", new JsonObject("{}"));
    }
    
    
    this.trees.add(ImmutableDocCommitTree.builder()
        .id(OidUtils.gen())
        .commitId(commitId)
        .docId(docId)
        .branchId(branchId)
        .operationType(DocCommitTreeOperation.ADD)
        .bodyAfter(bodyAfter)
        .bodyType(entity.getDocType().name())
        .build());
    this.logger.add(entity);
    return this;
  }
  public DocCommitBuilder merge(IsDocObject previous, IsDocObject next) {
    
    if(previous instanceof DocBranch) {
      final var branchPrev = (DocBranch) previous;
      final var branchNext = (DocBranch) next;
      final var diff = JsonPatch.diff(branchPrev.getValue(), excludeBranchContentFromLog ? new JsonObject("{}") : branchNext.getValue());
      
      
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
    
    
    final var bodyAfter = JsonObject.mapFrom(next);
    final var bodyBefore = JsonObject.mapFrom(previous);
    if(excludeBranchContentFromLog && previous instanceof DocBranch) {
      bodyAfter.put("value", new JsonObject("{}"));
      bodyBefore.put("value", new JsonObject("{}"));
    }
    
    this.trees.add(ImmutableDocCommitTree.builder()
        .id(OidUtils.gen())
        .commitId(commitId)
        .docId(docId)
        .branchId(branchId)
        .operationType(DocCommitTreeOperation.MERGE)
        .bodyBefore(bodyBefore)
        .bodyAfter(bodyAfter)
        .bodyType(next.getDocType().name())
        .build());
    this.logger.merge(previous, next);
    return this;
  }
  public DocCommitBuilder rm(IsDocObject current) {
    final var bodyBefore = JsonObject.mapFrom(current);
    if(excludeBranchContentFromLog && current instanceof DocBranch) {
      bodyBefore.put("value", new JsonObject("{}"));
    }
    
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
  public DocBatchForOne close() {
    final var commit = this.commit
          .commitLog(excludeBranchContentFromLog ? "": this.logger.build())
          .build();
    
    final var builder = ImmutableDocBatchForOne.builder()
        .doc(Optional.empty())
        
        .log(commit.getCommitLog());
    
    if(commitTreeEnabled) {
      builder.addAllDocCommitTree(this.trees);
    }
    
    if(!commitTreeEnabled && this.updateCommit) {
      builder.addDocCommitsToUpdate(commit);
    } else {
      builder.addDocCommit(commit);
    }
    
    return builder.build();
  }
  
  @Value.Immutable
  interface DocBranchPatch extends IsDocObject {
    
    JsonArray getPatchValue();
    
    @Override
    default DocType getDocType() {
      return DocType.DOC_BRANCH_PATCH;
    }
  }
  
  
  @RequiredArgsConstructor
  @Data @Accessors(fluent = true, chain = true)
  public static class DocCommitBuilderFactory {
    private final String tenantId;
    private final String docId;
    private final String parentCommitId;
    private final DocLock docLock;
    private final DocBranchLock branchLock;
    
    private Boolean excludeBranchContentFromLog;
    private boolean commitTreeEnabled;
    private String commitAuthor;
    private String commitMessage;
    private String branchId;
    
    public DocCommitBuilder create() {
      
      final String commitId;
      final String parentCommit;
      final boolean updateCommit;
      // commit tree is disabled, keep only first and last commit
      if(!commitTreeEnabled && docLock != null) {
        commitId = docLock.getCommit().get().getId();
        parentCommit = docLock.getCommit().get().getParent().orElse(null);
        updateCommit = true;
      } else if(!commitTreeEnabled && branchLock != null) {
        commitId = branchLock.getCommit().get().getId();
        parentCommit = branchLock.getCommit().get().getParent().orElse(null);;
        updateCommit = true;
      } else {
        commitId = OidUtils.gen();
        parentCommit = parentCommitId;
        updateCommit = false;
      }
      
      return new DocCommitBuilder(tenantId, excludeBranchContentFromLog, ImmutableDocCommit.builder()
          .id(commitId)
          .docId(docId)
          .branchId(Optional.ofNullable(branchId))
          .createdAt(OffsetDateTime.now())
          .commitAuthor(commitAuthor)
          .commitMessage(commitMessage)
          .parent(Optional.ofNullable(parentCommit))
          .commitLog("")
          .build(), 
          commitTreeEnabled,
          updateCommit
        );
    }
  }
  public static DocCommitBuilderFactory from(String tenantId, DocLock lock) {
    return new DocCommitBuilderFactory(tenantId, lock.getDoc().get().getId(), lock.getDoc().get().getCommitId(), lock, null);
  }
  
  public static DocCommitBuilderFactory from(String tenantId, DocBranchLock lock) {
    final var doc = lock.getDoc().get();
    return new DocCommitBuilderFactory(tenantId, doc.getId(), lock.getCommit().get().getId(), null, lock);
  }
  
  public static DocCommitBuilderFactory from(String tenantId, String docId) {
    return new DocCommitBuilderFactory(tenantId, docId, null, null, null);
  }
}
