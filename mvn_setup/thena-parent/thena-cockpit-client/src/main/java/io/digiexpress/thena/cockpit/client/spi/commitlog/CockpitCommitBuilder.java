package io.digiexpress.thena.cockpit.client.spi.commitlog;

/*-
 * #%L
 * thena-cockpit-client
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

import com.google.common.base.Objects;

import io.digiexpress.thena.cockpit.client.api.entities.CockpitCommit;
import io.digiexpress.thena.cockpit.client.api.entities.CockpitCommitTree.CockpitConfigCommitTreeOperation;
import io.digiexpress.thena.cockpit.client.api.entities.CockpitEntity;
import io.digiexpress.thena.cockpit.client.api.entities.ImmutableCockpitCommit;
import io.digiexpress.thena.cockpit.client.api.entities.ImmutableCockpitCommitTree;
import io.digiexpress.thena.cockpit.client.tables.CockpitDbBuilder.PersistenceUnit;
import io.digiexpress.thena.cockpit.client.tables.ImmutablePersistenceUnit;
import io.resys.thena.api.entities.BatchStatus;
import io.resys.thena.support.OidUtils;
import io.vertx.core.json.JsonObject;

public class CockpitCommitBuilder {
  private final String tenantId;
  private final String commitId;
  private final String author;
  private final ImmutableCockpitCommit.Builder commit;
  private final ImmutablePersistenceUnit.Builder next;
  
  private final CockpitCommitLogger logger;
  private final OffsetDateTime createdAt;
  private boolean isTreePresent = false;
  private String configId;
  
  public CockpitCommitBuilder(String tenantId, CockpitCommit commit) {
    super();
    this.commitId = commit.getId();
    this.tenantId = tenantId;
    this.commit = ImmutableCockpitCommit.builder().from(commit);
    this.next = ImmutablePersistenceUnit.builder()
        .tenantId(tenantId)
        .status(BatchStatus.OK)
        .log("");
    this.logger = new CockpitCommitLogger(tenantId, commit);
    this.createdAt = commit.getCreatedAt();
    this.author = commit.getCommitAuthor();
    this.configId = commit.getConfigId();
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
  
  public String getConfigId() {
    return configId;
  }
  
  public CockpitCommitBuilder add(CockpitEntity entity) {
    isTreePresent = true;
    this.next.addCockpitCommitTreeInserts(ImmutableCockpitCommitTree.builder()
        .id(OidUtils.genUUID())
        .commitId(commitId)
        .operationType(CockpitConfigCommitTreeOperation.ADD)
        .bodyAfter(JsonObject.mapFrom(entity))
        .build());
    this.logger.add(entity);
    return this;
  }
  
  public CockpitCommitBuilder merge(CockpitEntity previous, CockpitEntity next) {
    final var a = JsonObject.mapFrom(previous);
    final var b = JsonObject.mapFrom(next);
    
    CockpitCommitLogger.SKIP.forEach(key -> {
      a.remove(key);
      b.remove(key);
    });
    
    if(Objects.equal(a, b)) {
      return this;
    }
    
    isTreePresent = true;
    this.next.addCockpitCommitTreeInserts(ImmutableCockpitCommitTree.builder()
        .id(OidUtils.genUUID())
        .commitId(commitId)
        .operationType(CockpitConfigCommitTreeOperation.MERGE)
        .bodyBefore(JsonObject.mapFrom(previous))
        .bodyAfter(JsonObject.mapFrom(next))
        .build());
    this.logger.merge(previous, next);
    return this;
  }
  
  public CockpitCommitBuilder rm(CockpitEntity current) {
    isTreePresent = true;
    this.next.addCockpitCommitTreeInserts(ImmutableCockpitCommitTree.builder()
        .id(OidUtils.genUUID())
        .commitId(commitId)
        .operationType(CockpitConfigCommitTreeOperation.REMOVE)
        .bodyBefore(JsonObject.mapFrom(current))
        .bodyAfter((JsonObject) null)
        .build());
    this.logger.remove(current);
    return this;
  }
  
  public CockpitCommitBuilder withConfigId(String configId) {
    this.configId = configId;
    return this;
  }
  
  public PersistenceUnit close() {
    if(this.isTreePresent) {
      this.next.addCockpitCommitInserts(this.commit.build());
    }
    
    return this.next.log("").build();
  }
  
  public String getAuthor() {
    return author;
  }
}