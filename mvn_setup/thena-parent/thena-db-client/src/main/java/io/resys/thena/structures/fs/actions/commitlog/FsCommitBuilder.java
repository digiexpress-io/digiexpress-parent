package io.resys.thena.structures.fs.actions.commitlog;

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

import com.google.common.base.Objects;

import io.resys.thena.api.entities.BatchStatus;
import io.resys.thena.api.entities.fs.FsCommit;
import io.resys.thena.api.entities.fs.FsCommitTree.FsCommitTreeOperation;
import io.resys.thena.api.entities.fs.ImmutableFsCommit;
import io.resys.thena.api.entities.fs.ImmutableFsCommitTree;
import io.resys.thena.api.entities.fs.ThenaFsObject.IsFsObject;
import io.resys.thena.structures.fs.ImmutableFsBatchDirents;
import io.resys.thena.support.OidUtils;
import io.vertx.core.json.JsonObject;



public class FsCommitBuilder {
  private final String tenantId;
  private final String commitId;
  private final String author;
  private final ImmutableFsCommit.Builder commit;
  private final ImmutableFsBatchDirents.Builder next;
  
  private final FsCommitLogger logger;
  private final OffsetDateTime createdAt;
  private boolean isTreePresent = false;
  
  public FsCommitBuilder(String tenantId, FsCommit commit) {
    super();
    this.commitId = commit.getCommitId();
    this.tenantId = tenantId;
    this.commit = ImmutableFsCommit.builder().from(commit);
    this.next = ImmutableFsBatchDirents.builder()
        .tenantId(tenantId)
        .status(BatchStatus.OK)
        .log("");
    this.logger = new FsCommitLogger(tenantId, commit);
    this.createdAt = commit.getCreatedAt();
    this.author = commit.getCommitAuthor();
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
  public FsCommitBuilder add(IsFsObject entity) {
    isTreePresent = true;
    this.next.addCommitTrees(ImmutableFsCommitTree.builder()
        .id(OidUtils.gen())
        .commitId(commitId)
        .operationType(FsCommitTreeOperation.ADD)
        .bodyAfter(JsonObject.mapFrom(entity))
        .build());
    this.logger.add(entity);
    return this;
  }
  public FsCommitBuilder merge(IsFsObject previous, IsFsObject next) {
    
    final var a = JsonObject.mapFrom(previous);
    final var b = JsonObject.mapFrom(next);
    
    FsCommitLogger.SKIP.forEach(key -> {
      a.remove(key);
      b.remove(key);
    });
    
    if(Objects.equal(a, b)) {
      return this;
    }
    
    isTreePresent = true;
    this.next.addCommitTrees(ImmutableFsCommitTree.builder()
        .id(OidUtils.gen())
        .commitId(commitId)
        .operationType(FsCommitTreeOperation.MERGE)
        .bodyBefore(JsonObject.mapFrom(previous))
        .bodyAfter(JsonObject.mapFrom(next))
        .build());
    this.logger.merge(previous, next);
    return this;
  }
  public FsCommitBuilder rm(IsFsObject current) {
    isTreePresent = true;
    this.next.addCommitTrees(ImmutableFsCommitTree.builder()
        .id(OidUtils.gen())
        .commitId(commitId)
        .operationType(FsCommitTreeOperation.REMOVE)
        .bodyBefore(JsonObject.mapFrom(current))
        .bodyAfter(null)
        .build());
    this.logger.remove(current);
    return this;
  }
  public ImmutableFsBatchDirents close() {
    if(this.isTreePresent) {
      this.next.addCommits(this.commit.commitLog(this.logger.build()).build());
    }
    
    return this.next.log("").build();
  }
  
  public FsCommitBuilder withDirentId(String missionId) {
    this.commit.direntId(missionId);
    return this;
  }
  public String getAuthor() {
    return author;
  }
}
