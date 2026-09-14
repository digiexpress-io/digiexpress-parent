package io.resys.thena.api.entities.grim;

/*-
 * #%L
 * thena-grim-client
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.thena.api.entities.AnyTenantEntity;
import io.resys.thena.api.entities.grim.ThenaGrimObject.IsGrimObject;
import jakarta.annotation.Nullable;

@JsonSerialize(as = ImmutableGrimRps.class)
@JsonDeserialize(as = ImmutableGrimRps.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Value.Immutable
public interface GrimRps extends IsGrimObject, AnyTenantEntity {
  String getId();
  OffsetDateTime getCreatedAt();
  
  String getExternalId();
  String getLocale();
  
  String getWorkflowName();
  String getFormName();
  String getFormVersion();
  Integer getRating();
  @Nullable String getComment();

  @Override default public GrimDocType getDocType() { return GrimDocType.GRIM_RPS; };
}
