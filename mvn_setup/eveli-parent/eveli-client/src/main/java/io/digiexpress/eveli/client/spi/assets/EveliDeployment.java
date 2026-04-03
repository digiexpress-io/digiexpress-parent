package io.digiexpress.eveli.client.spi.assets;

/*-
 * #%L
 * eveli-client
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

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.model.Deployment;
import io.resys.limaone.model.Deployment.BundleStatus;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.ModelWorld;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;



@JsonSerialize(as = ImmutableEveliDeployment.class)
@JsonDeserialize(as = ImmutableEveliDeployment.class)
@Value.Immutable
public interface EveliDeployment {
  String getId();
  String getName();
  @Nullable String getExternalId();
  @Nullable String getCockpitId();
  String getCreatedBy();
  OffsetDateTime getCreatedAt();
  OffsetDateTime getStartsAt();

  String getDescription();
  @Nullable JsonObject getErrors();
  BundleStatus getStatus();
  
  @Nullable Boolean getExternal();

  // Null when user has requested sources to be not loaded on api level
  @Nullable ModelWorld getSources();
  
  
  public static EveliDeployment from(Model<Deployment> model, @Nullable ModelWorld world) {
    return ImmutableEveliDeployment.builder()
        .id(model.getId())
        .name(model.getBody().getName())
        .externalId(model.getBody().getExternalId())
        .cockpitId(model.getBody().getCockpitId())
        .createdBy(model.getBody().getCreatedBy())
        .createdAt(model.getBody().getCreatedAt())
        .startsAt(model.getBody().getStartsAt())
        .description(model.getBody().getDescription())
        .errors(model.getBody().getErrors())
        .status(model.getBody().getStatus())
        .external(model.getBody().getExternal())
        .sources(world)
        .build();
  }
}
