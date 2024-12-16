package io.resys.thena.api.entities.org;

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

import java.util.Collection;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.resys.thena.api.entities.org.ThenaOrgObject.IsOrgObject;
import io.resys.thena.api.entities.org.ThenaOrgObject.IsOrgVersionObject;
import io.resys.thena.api.registry.ThenaRegistryService.ThenaTable;
import io.vertx.core.json.JsonObject;

@Value.Immutable
public interface OrgParty extends ThenaOrgObject, IsOrgObject, IsOrgVersionObject, ThenaTable {
  String getId();
  String getCommitId();
  String getCreatedWithCommitId();
  
  @Nullable String getExternalId();
  @Nullable String getParentId();
  
  OrgDocSubType getPartySubType();
  String getPartyName(); 
  String getPartyDescription();

  OrgActorStatusType getStatus();
  @Nullable JsonObject getDataExtension();
  
  
  @JsonIgnore @Override default public OrgDocType getDocType() { return OrgDocType.OrgGroup; };
  
  default boolean isMatch(String idOrNameOrExtId) {
    return idOrNameOrExtId.equals(getExternalId()) ||
        idOrNameOrExtId.equals(getPartyName()) ||
        idOrNameOrExtId.equals(getId());
  }
  default boolean isMatch(Collection<String> idOrNameOrExtId) {
    return idOrNameOrExtId.contains(getExternalId()) ||
        idOrNameOrExtId.contains(getPartyName()) ||
        idOrNameOrExtId.contains(getId());
  }
  
}
