package io.resys.thena.api.entities.grim;

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

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.thena.api.entities.TenantEntity;
import io.resys.thena.api.entities.grim.ThenaGrimObject.IsGrimObject;
import jakarta.annotation.Nullable;


@JsonSerialize(as = ImmutableGrimProcess.class)
@JsonDeserialize(as = ImmutableGrimProcess.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Value.Immutable
public interface GrimProcess extends IsGrimObject, TenantEntity {
   String getId();
   OffsetDateTime getCreated();
   OffsetDateTime getUpdated();
   String getWorkflowName();
   
   @Nullable String getStatus();
   @Nullable OffsetDateTime getExpiresAt();
   @Nullable Long getExpiresInSeconds();  
   @Nullable String getQuestionnaireId();
   @Nullable String getTaskId();
   @Nullable String getTaskRef();
   @Nullable String getUserId();
   @Nullable Boolean getAnon();

   @Nullable String getArticleName();
   @Nullable String getParentArticleName();
   @Nullable String getFlowName();
   @Nullable String getFormName();
   @Nullable String getFormTagName();
   @Nullable String getStencilTagName();
  
   @Nullable String getWrenchTagName();
   @Nullable String getFormBody();
   @Nullable String getFlowBody();

   
   @Override default public GrimDocType getDocType() { return GrimDocType.GRIM_PROCESS; };
}
