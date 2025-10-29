package io.resys.thena.contract.client.spi.commitlog;

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
import io.resys.thena.api.entities.grim.GrimCommitTree.GrimCommitTreeOperation;
import io.resys.thena.api.entities.grim.ImmutableGrimCommitTree;
import io.resys.thena.contract.client.entities.Commit;
import io.resys.thena.contract.client.entities.ContractEntity;
import io.resys.thena.contract.client.entities.ImmutableCommit;
import io.resys.thena.contract.client.tables.ImmutablePersistenceUnit;
import io.resys.thena.support.OidUtils;
import io.vertx.core.json.JsonObject;



public class ContractCommitBuilder {
  private final String tenantId;
  private final String commitId;
  private final String author;
  private final ImmutableCommit.Builder commit;
  private final ImmutablePersistenceUnit.Builder next;
  
  private final ContractCommitLogger logger;
  private final OffsetDateTime createdAt;
  private boolean isTreePresent = false;
  
  public ContractCommitBuilder(String tenantId, Commit commit) {
    super();
    this.commitId = commit.getCommitId();
    this.tenantId = tenantId;
    this.commit = ImmutableCommit.builder().from(commit);
    this.next = ImmutablePersistenceUnit.builder()
        .tenantId(tenantId)
        .status(BatchStatus.OK)
        .log("");
    this.logger = new ContractCommitLogger(tenantId, commit);
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
  public ContractCommitBuilder add(ContractEntity entity) {
    isTreePresent = true;
    this.next.addCommitTrees(ImmutableGrimCommitTree.builder()
        .id(OidUtils.gen())
        .commitId(commitId)
        .operationType(GrimCommitTreeOperation.ADD)
        .bodyAfter(JsonObject.mapFrom(entity))
        .build());
    this.logger.add(entity);
    return this;
  }
  public ContractCommitBuilder merge(ContractEntity previous, ContractEntity next) {
    
    final var a = JsonObject.mapFrom(previous);
    final var b = JsonObject.mapFrom(next);
    
    ContractCommitLogger.SKIP.forEach(key -> {
      a.remove(key);
      b.remove(key);
    });
    
    if(Objects.equal(a, b)) {
      return this;
    }
    
    isTreePresent = true;
    this.next.addCommitTrees(ImmutableGrimCommitTree.builder()
        .id(OidUtils.gen())
        .commitId(commitId)
        .operationType(GrimCommitTreeOperation.MERGE)
        .bodyBefore(JsonObject.mapFrom(previous))
        .bodyAfter(JsonObject.mapFrom(next))
        .build());
    this.logger.merge(previous, next);
    return this;
  }
  public ContractCommitBuilder rm(ContractEntity current) {
    isTreePresent = true;
    this.next.addCommitTrees(ImmutableGrimCommitTree.builder()
        .id(OidUtils.gen())
        .commitId(commitId)
        .operationType(GrimCommitTreeOperation.REMOVE)
        .bodyBefore(JsonObject.mapFrom(current))
        .bodyAfter(null)
        .build());
    this.logger.remove(current);
    return this;
  }
  public ContractCommitBuilder withContractId(String contractId) {
    this.commit.contractId(contractId);
    return this;
  }
  public ImmutablePersistenceUnit close() {
    if(this.isTreePresent) {
      this.next.addCommits(this.commit.commitLog(this.logger.build()).build());
    }
    
    return this.next.log("").build();
  }
  

  public String getAuthor() {
    return author;
  }
}
