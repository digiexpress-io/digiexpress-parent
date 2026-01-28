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
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.digiexpress.eveli.client.api.TaskClient.ProcessInstance;
import io.digiexpress.eveli.client.api.TaskClient.TaskStatus;
import io.resys.hdes.client.api.programs.FlowProgram.FlowResult;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

// should be migrated to task client ... and remove all JPA dependents
@Deprecated
public interface ProcessClient {
  QueryProcessInstances queryInstances();
  ProcessInstanceStatusBuilder changeInstanceStatus();
  ProcessAuthorizationQuery queryAuthorization();
  ProcessQuestionnaireQuery queryProcessQuestionnaire();
  
  CreateProcessInstance createInstance();
  CreateProcessExecutor createExecutor();
  
  ProcessInstanceBodyBuilder createBodyBuilder();
  
  interface ProcessInstanceBodyBuilder {
    ProcessInstanceBodyBuilder processInstanceId(Long id);
    ProcessInstanceBodyBuilder formBody(String formBody);
    ProcessInstanceBodyBuilder flowBody(String flowBody);
    ProcessInstance build();
  }
  
  interface CreateProcessExecutor {
    CreateProcessExecutor processInstance(ProcessInstance process);
    FlowResult execute();
  }
  
  interface CreateProcessInstance {
    CreateProcessInstance questionnaireId(String questionnaire);
    CreateProcessInstance userId(String userId);
    CreateProcessInstance expiresInSeconds(Long expires_in_seconds);
    CreateProcessInstance expiresAt(OffsetDateTime expiresAt);
    CreateProcessInstance workflowName(String name);
    
    CreateProcessInstance anon(boolean anon);
    
    CreateProcessInstance articleName(String articleName);
    CreateProcessInstance parentArticleName(String parentArticleName);
    
    CreateProcessInstance taskId(@Nullable String taskId);
    CreateProcessInstance formName(String formName);
    CreateProcessInstance flowName(String flowName);

    CreateProcessInstance formTagName(String formTagName);
    CreateProcessInstance stencilTagName(String stencilTagName);
    CreateProcessInstance wrenchTagName(String wrenchTagName);
    CreateProcessInstance customerAssignment(boolean isCustomerAssignment);
    
    CreateProcessInstance cockpitId(@Nullable String cockpitId);
    
    
    ProcessInstance create();
  }
  
  interface ProcessAuthorizationQuery {
    ProcessAuthorization get(InitProcessAuthorization init);
  }

  
  interface QueryProcessInstances {
    Optional<ProcessInstance> findOneById(String id);
    Optional<ProcessInstance> findOneByTaskId(String taskId);    
    Optional<ProcessInstance> findOneByQuestionnaireId(String questionnaireId);
    Optional<ProcessInstance> findOneByIdAndLock(String id);
    
    void deleteOneById(Long id);
    List<ProcessInstance> findAll();
    List<ProcessInstance> findAllAnswered();
    List<ProcessInstance> findAllAnsweredFrom(OffsetDateTime pickupFrom);
    
    List<ProcessInstance> findAllExpired();
  }
  
  interface ProcessQuestionnaireQuery {
    Optional<JsonObject> findOneByTaskId(String taskId);    
  }

  interface ProcessInstanceStatusBuilder {
    void answered(String id);
    void answeredByQuestionnaire(String questionnaireId, String taskId); // used by assets
    void taskStatusChange(String taskId, TaskStatus taskStatus);
    void inProgress(String id);
    void completed(String id);
    void rejected(String id);
  }
  
  //@Relation(collectionRelation = "processDataList", itemRelation = "processDataList", value = "processDataList" )


  @Value.Immutable
  @JsonSerialize(as = ImmutableProcessAuthorization.class)
  @JsonDeserialize(as = ImmutableProcessAuthorization.class)
  interface ProcessAuthorization {
    List<String> getUserRoles();
    List<String> getAllowedProcessNames();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableInitProcessAuthorization.class)
  @JsonDeserialize(as = ImmutableInitProcessAuthorization.class)
  interface InitProcessAuthorization {
    @Nullable String getCockpitId();
    List<String> getUserRoles();
  }
  
  
}
