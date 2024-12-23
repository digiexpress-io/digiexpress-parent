package io.resys.thena.api.entities.grim;

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

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.resys.thena.api.entities.TenantEntity;
import io.resys.thena.api.entities.grim.ThenaGrimObject.IsGrimObject;
import io.resys.thena.api.envelope.ThenaContainer;

@Value.Immutable
public interface GrimCommitViewer extends IsGrimObject, TenantEntity, ThenaContainer {
  String getId();
  String getCommitId();
  String getMissionId();
  
  OffsetDateTime getCreatedAt();
  OffsetDateTime getUpdatedAt();
  
  String getObjectId();
  GrimDocType getObjectType();
  
  String getUsedBy();
  String getUsedFor();
  
  @JsonIgnore @Override default public GrimDocType getDocType() { return GrimDocType.GRIM_COMMIT_VIEWER; };
}
