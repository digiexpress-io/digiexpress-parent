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
  private boolean isBuilt;
  private boolean isSkipped;
  
  private Optional<String> missionId;
  private Optional<String> flowName;
  private Optional<JsonObject> flowBody;
  private Optional<JsonObject> formBody;
  private Optional<String> status;
  private Optional<OffsetDateTime> expiresAt;
  private Optional<Long> expiresInSeconds;  
  private boolean isUpdated = false;
  private GrimProcess built;
  
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
  public GrimProcess skip() {
    this.isSkipped = true;
    this.isBuilt = true;
    
    return this.grimProcPrevious;
  }
  @Override
  public GrimProcess build() {
    this.isBuilt = true;
    
    
    if(missionId != null) {
      final var newValue = missionId.orElse(null);
      next.missionId(newValue);
      isUpdated(newValue, grimProcPrevious.getMissionId());
    }
    
    if(flowName != null) {
      final var newValue = flowName.orElse(null); 
      next.flowName(newValue);
      isUpdated(newValue, grimProcPrevious.getFlowName());
    }
    
    if(flowBody != null) {
      final var newValue = flowBody.orElse(null);
      next.flowBody(newValue);
      isUpdated(newValue, grimProcPrevious.getFlowBody());
    }
    if(formBody != null) {
      final var newValue = formBody.orElse(null); 
      next.formBody(newValue);
      isUpdated(newValue, grimProcPrevious.getFormBody());
    }
    if(status != null) {
      final var newValue = status.orElse(null); 
      next.status(newValue);
      isUpdated(newValue, grimProcPrevious.getStatus());
    }
    if(expiresAt != null) {
      final var newValue = expiresAt.orElse(null);
      next.expiresAt(newValue);
      isUpdated(newValue, grimProcPrevious.getExpiresAt());
    }
    if(expiresInSeconds != null) {
      final var newValue = expiresInSeconds.orElse(null);
      next.expiresInSeconds(newValue);
      isUpdated(newValue, grimProcPrevious.getExpiresInSeconds());
    }
    
    if(this.isUpdated) {
      next.updated(OffsetDateTime.now());
    }

    this.built = next.build();
    return this.built;
  }
  public boolean isSkipped() {
    return isSkipped;
  }

  public GrimProcess close() {
    RepoAssert.isTrue(isBuilt, () -> "you must call MergeProc.build() to finalize proc UPDATE!");
    return this.built;
  }
  
  private void isUpdated(Object o1, Object o2) {
    if(!java.util.Objects.equals(o1, o2)) {
      isUpdated = true;
    }
  }

  public GrimProcess getCurrentState() {
    return grimProcPrevious;
  }
}
