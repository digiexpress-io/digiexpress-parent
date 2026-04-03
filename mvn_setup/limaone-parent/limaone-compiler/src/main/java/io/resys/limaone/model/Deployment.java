package io.resys.limaone.model;

/*-
 * #%L
 * limaone-compiler
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

import java.time.OffsetDateTime;
import java.util.UUID;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.model.Model.BodyType;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;


@JsonSerialize(as = ImmutableDeployment.class)
@JsonDeserialize(as = ImmutableDeployment.class)
@Value.Immutable
public interface Deployment extends Model.Body {
  UUID getFromCommitId();
  @Nullable UUID getFromRefId();
  
  String getName();
  @Nullable String getExternalId();
  @Nullable String getCockpitId();
  String getCreatedBy();
  OffsetDateTime getCreatedAt();
  @Nullable OffsetDateTime getStartsAt();

  String getDescription();
  @Nullable JsonObject getErrors();
  BundleStatus getStatus();
  
  @Nullable Boolean getExternal();

  // Null when user has requested sources to be not loaded on api level
  @Nullable Model.ModelWorld getSources();

  enum BundleStatus {
    BUILDING, READY, ERROR, DEPLOYED, UNKNOWN
  }
  
  default BodyType getBodyType() {
    return BodyType.DEPLOYMENT;
  }
}
