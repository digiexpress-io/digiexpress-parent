package io.resys.thena.grim.spi.modify;

/*-
 * #%L
 * thena-grim-client
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

import io.resys.thena.api.entities.grim.GrimProcess;
import io.resys.thena.api.entities.grim.ImmutableGrimProcess;
import io.resys.thena.api.entities.grim.ThenaGrimMergeObject.MergeProc;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;



public class MergeProcImpl implements MergeProc {
  private final GrimProcess grimProcPrevious;
  private final ImmutableGrimProcess.Builder next;
  private boolean built;
  
  private Optional<String> missionId;
  private Optional<String> flowName;
  private Optional<JsonObject> flowBody;
  private Optional<JsonObject> formBody;
  private Optional<String> status;
  private Optional<OffsetDateTime> expiresAt;
  private Optional<Long> expiresInSeconds;  
    
  public MergeProcImpl(GrimProcess state) {
    super();
    this.grimProcPrevious = state;
    this.next = ImmutableGrimProcess.builder().from(state);
  }

  @Override
  public MergeProc missionId(String missionId) {
    this.missionId = Optional.ofNullable(missionId);
    return this;
  }
  @Override
  public MergeProc flowName(String flowName) {
    this.flowName = Optional.ofNullable(flowName);
    return this;
  }
  @Override
  public MergeProc flowBody(JsonObject flowBody) {
    this.flowBody = Optional.ofNullable(flowBody);
    return this;
  }
  @Override
  public MergeProc formBody(JsonObject formBody) {
    this.formBody = Optional.ofNullable(formBody);
    return this;
  }
  @Override
  public MergeProc status(String status) {
    this.status = Optional.ofNullable(status);
    return this;
  }
  @Override
  public MergeProc expiresAt(OffsetDateTime expiresAt) {
    this.expiresAt = Optional.ofNullable(expiresAt);
    return this;
  }
  @Override
  public MergeProc expiresInSeconds(Long expiresInSeconds) {
    this.expiresInSeconds = Optional.ofNullable(expiresInSeconds);
    return this;
  }

  @Override
  public void build() {
    this.built = true;
  }
  
  public GrimProcess close() {
    RepoAssert.isTrue(built, () -> "you must call MergeProc.build() to finalize proc UPDATE!");
    if(missionId != null) {
      next.missionId(missionId.orElse(null));
    }
    if(flowName != null) {
      next.flowName(flowName.orElse(null));
    }
    if(flowBody != null) {
      next.flowBody(flowBody.map(e -> e.encode()).orElse(null));
    }
    if(formBody != null) {
      next.formBody(formBody.map(e -> e.encode()).orElse(null));
    }
    if(status != null) {
      next.status(status.orElse(null));
    }
    if(expiresAt != null) {
      next.expiresAt(expiresAt.orElse(null));
    }
    if(expiresInSeconds != null) {
      next.expiresInSeconds(expiresInSeconds.orElse(null));
    }

    return next.build();
  }
}
