package io.resys.thena.api.registry.grim;

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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

import io.resys.thena.api.actions.GrimQueryActions.GrimArchiveQueryType;
import jakarta.annotation.Nullable;

@Value.Immutable
public interface GrimMissionFilter {
  Optional<List<String>> getMissionIds();
  List<GrimLinkFilter> getLinks();
  
  @Nullable GrimArchiveQueryType getArchived();
  @Nullable LocalDate getFromCreatedOrUpdated();
  
  List<GrimAssignmentFilter> getAssignments();
  
  @Nullable String getReporterId();
  @Nullable String getLikeTitle();
  @Nullable String getLikeDescription();

  List<String> getStatus();
  List<String> getPriority();
  @Nullable Boolean getOverdue(); // return tasks that are overdue
  
  // TODO
  @Nullable String getLikeRole(); // find task assigned to the role
  List<String> getRequireAnyRoles(); // secondary role filter, must contain at least one of these
   
  
  @Value.Immutable
  interface GrimAssignmentFilter {
    String getAssignmentType();
    boolean isExact();
    List<String> getAssignmentValue(); 
  }
  
  @Value.Immutable
  interface GrimLinkFilter {
    String getLinkType();
    String getLinkValue(); 
  }
}
