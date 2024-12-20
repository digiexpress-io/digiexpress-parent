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

import javax.annotation.Nullable;

import org.immutables.value.Value;

@Value.Immutable
public
interface OrgMemberHierarchyEntry extends ThenaOrgObject {
	String getPartyId();
	String getPartyName();
	String getPartyDescription();
	@Nullable String getPartyParentId();
	@Nullable String getMembershipId();
	@Nullable String getMemberId();
	@Nullable OrgActorStatusType getPartyStatus();
	
  @Nullable String getRightId();
  @Nullable String getRightName();
  @Nullable String getRightDescription();
  @Nullable OrgActorStatusType getRightStatus();    
}