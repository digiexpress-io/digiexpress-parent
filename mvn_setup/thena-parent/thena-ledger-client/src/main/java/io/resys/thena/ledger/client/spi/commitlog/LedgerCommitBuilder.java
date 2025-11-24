package io.resys.thena.ledger.client.spi.commitlog;

/*-
 * #%L
 * thena-ledger-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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
import java.util.Optional;

import com.google.common.base.Objects;

import io.resys.thena.api.entities.BatchStatus;
import io.resys.thena.ledger.client.entities.Commit;
import io.resys.thena.ledger.client.entities.ImmutableBlackBook;
import io.resys.thena.ledger.client.entities.CommitTree.CommitTreeOperation;
import io.resys.thena.ledger.client.entities.ImmutableCommit;
import io.resys.thena.ledger.client.entities.ImmutableCommitTree;
import io.resys.thena.ledger.client.entities.LedgerEntity;
import io.resys.thena.ledger.client.tables.BbDbBuilder.PersistenceUnit;
import io.resys.thena.ledger.client.tables.ImmutablePersistenceUnit;
import io.resys.thena.support.OidUtils;
import io.vertx.core.json.JsonObject;

public class LedgerCommitBuilder {
  private final String tenantId;
  private final String commitId;
  private final String ledgerId;
  private final ImmutableCommit.Builder commit;
  private final ImmutablePersistenceUnit.Builder next;
  private final LedgerCommitLogger logger;
  private final OffsetDateTime createdAt;
  
  private boolean isTreePresent = false;
  private Optional<String> currentBlackBookId;
  
  public LedgerCommitBuilder(String tenantId, Commit commit, Optional<String> currentBlackBookId) {
    super();
    this.commitId = commit.getCommitId();
    this.tenantId = tenantId;
    this.ledgerId = commit.getLedgerId().orElseThrow();
    this.commit = ImmutableCommit.builder().from(commit);
    this.logger = new LedgerCommitLogger(tenantId, commit);
    this.createdAt = commit.getCreatedAt();
    this.next = createPersistenceUnit();
    this.currentBlackBookId = currentBlackBookId;
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
  public LedgerCommitBuilder withLedgerId(String contractId) {
    this.commit.ledgerId(contractId);
    return this;
  }
  
  public ImmutableBlackBook.Builder withNextBlackBook(String blackBookId) {
    final var result = ImmutableBlackBook.builder()
        .id(blackBookId)
        .ledgerId(ledgerId)
        .createdCommitId(getCommitId())
        .parentBlackBookId(this.currentBlackBookId.orElse(null));
    this.currentBlackBookId = Optional.of(blackBookId);
    return result;
  }
  
  public LedgerCommitBuilder add(LedgerEntity entity) {
    isTreePresent = true;
    this.next.addCommitTreeInserts(ImmutableCommitTree.builder()
        .id(OidUtils.genUUID())
        .commitId(commitId)
        .operationType(CommitTreeOperation.ADD)
        .bodyAfter(JsonObject.mapFrom(entity))
        .build());
    this.logger.add(entity);
    return this;
  }
  
  public LedgerCommitBuilder merge(LedgerEntity previous, LedgerEntity next) {
    final var a = JsonObject.mapFrom(previous);
    final var b = JsonObject.mapFrom(next);
    
    LedgerCommitLogger.SKIP.forEach(key -> {
      a.remove(key);
      b.remove(key);
    });
    
    if(Objects.equal(a, b)) {
      return this;
    }
    
    isTreePresent = true;
    this.next.addCommitTreeInserts(ImmutableCommitTree.builder()
        .id(OidUtils.genUUID())
        .commitId(commitId)
        .operationType(CommitTreeOperation.MERGE)
        .bodyBefore(JsonObject.mapFrom(previous))
        .bodyAfter(JsonObject.mapFrom(next))
        .build());
    this.logger.merge(previous, next);
    return this;
  }
  
  public LedgerCommitBuilder remove(LedgerEntity entity) {
    isTreePresent = true;
    this.next.addCommitTreeInserts(ImmutableCommitTree.builder()
        .id(OidUtils.genUUID())
        .commitId(commitId)
        .operationType(CommitTreeOperation.REMOVE)
        .bodyBefore(JsonObject.mapFrom(entity))
        .build());
    this.logger.remove(entity);
    return this;
  }
  
  public PersistenceUnit close() {
    if(isTreePresent) {
      this.next.addCommitInserts(commit.commitLog(logger.close()).build());
    }
    return next.build();
  }
  
  public ImmutablePersistenceUnit.Builder createPersistenceUnit() {
    return ImmutablePersistenceUnit.builder()
        .tenantId(getTenantId())
        .status(BatchStatus.OK)
        .log("");
  }
  public String getLedgerId() {
    return ledgerId;
  }
  
  public Optional<String> getCurrentBlackBookId() {
    return this.currentBlackBookId;
  }
}