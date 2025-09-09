package io.digiexpress.eveli.client.api;

/*-
 * #%L
 * eveli-client
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
import java.util.List;
import java.util.Set;

import org.immutables.value.Value;

import io.resys.thena.api.entities.grim.GrimCommitTree;
import io.smallrye.mutiny.Multi;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

public interface HealthClient {

  HealthQuery createHealthQuery();
  UserActivityQuery createUserActivityQuery();
  
  
  interface HealthQuery {
    Multi<HealthEntry> findAll();
  }
  
  interface UserActivityQuery {
    Multi<UserActivity> findAllAfter(OffsetDateTime createdFromInclusive);
  }
  
  
  @Value.Immutable
  interface UserActivity {
    String getId();
    String getUserName();
    UserActivityType getType();
    
    String getTargetId();
    String getTargetIdType();    
    OffsetDateTime getCreatedAt();
    
    @Nullable String getTaskRef();
    @Nullable String getUsedFor();
    List<GrimCommitTree> getChange(); 

  }
  
  

  interface HealthEntry {
    String getId();
    HealthEntryType getType();
    long getAgeInDays();
    String getName();
    OffsetDateTime getCreatedAt();
    
    @Nullable String getTaskRef();
    @Nullable DiagnosisType getDiagnosis();
    @Nullable String getDiagnosisDescription();
    @Nullable String getCustomerId();

    @Nullable String getFormName();
    @Nullable String getFlowName();
    
    @Nullable JsonObject getFlowBody();
    @Nullable JsonObject getFormBody();

  }
  
  @Value.Immutable
  interface ProcessHealth extends HealthEntry {
    ProcessStatus getStatus();
    
    default HealthEntryType getType() {
      return HealthEntryType.PROCESS;
    }
  }
  
  @Value.Immutable
  interface TaskHealth extends HealthEntry {
    String getSubject();
    Set<String> getAssignedRoles();
    String getTaskStatus();
    @Nullable Boolean getViewed();
    @Nullable String getProcessId();
    
    default HealthEntryType getType() {
      return HealthEntryType.TASK;
    }
  }
  
  enum UserActivityType {
    ACCESS, CHANGE
  }
  
  enum DiagnosisType {
    WARNING, ERROR, OK
  }
  
  enum ProcessStatus {
    RUNNING, COMPLETED, ERRORS
  }
  
  enum HealthEntryType {
    PROCESS, TASK
  }
}
