package io.digiexpress.eveli.client.api;

/*-
 * #%L
 * eveli-client
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.digiexpress.eveli.client.api.TaskClient.TaskStatus;
import io.resys.hdes.client.api.programs.FlowProgram.FlowResult;
import jakarta.annotation.Nullable;


public interface ProcessClient {
  PaginateProcessInstances paginateInstances();
  QueryProcessInstances queryInstances();
  ProcessInstanceStatusBuilder changeInstanceStatus();
  ProcessAuthorizationQuery queryAuthorization();
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
    CreateProcessInstance expiresAt(LocalDateTime expiresAt);
    CreateProcessInstance workflowName(String name);
    
    CreateProcessInstance articleName(String articleName);
    CreateProcessInstance parentArticleName(String parentArticleName);
    

    CreateProcessInstance formName(String formName);
    CreateProcessInstance flowName(String flowName);

    CreateProcessInstance formTagName(String formTagName);
    CreateProcessInstance stencilTagName(String stencilTagName);
    CreateProcessInstance wrenchTagName(String wrenchTagName);
    CreateProcessInstance workflowTagName(String workflowTagName);
    
    ProcessInstance create();
  }
  
  interface ProcessAuthorizationQuery {
    ProcessAuthorization get(InitProcessAuthorization init);
  }

  interface PaginateProcessInstances {
    PaginateProcessInstances status(@Nullable List<String> status);
    PaginateProcessInstances name(@Nullable String name);
    PaginateProcessInstances userId(@Nullable String userId);
    PaginateProcessInstances page(@Nullable Pageable pageable);
    Page<ProcessInstance> findAll();
  }
  
  interface QueryProcessInstances {
    Optional<ProcessInstance> findOneById(String id);
    Optional<ProcessInstance> findOneByTaskId(Long taskId);    
    Optional<ProcessInstance> findOneByQuestionnaireId(String questionnaireId);    
    
    void deleteOneById(String id);
    List<ProcessInstance> findAll();
    List<ProcessInstance> findAllAnswered();
    List<ProcessInstance> findAllExpired();
    List<ProcessInstance> findAllByUserId(String userId);    
  }

  interface ProcessInstanceStatusBuilder {
    void answered(String id);
    void answeredByQuestionnaire(String questionnaireId, String taskId); // used by assets
    void taskStatusChange(Long taskId, TaskStatus taskStatus);
    void inProgress(String id);
    void completed(String id);
    void rejected(String id);
  }
  
  //@Relation(collectionRelation = "processDataList", itemRelation = "processDataList", value = "processDataList" )
  @Value.Immutable
  @JsonSerialize(as = ImmutableProcessInstance.class)
  @JsonDeserialize(as = ImmutableProcessInstance.class)
  interface ProcessInstance {
    Long getId();
    ProcessStatus getStatus();
    LocalDateTime getCreated();
    LocalDateTime getUpdated();
    
    @Nullable LocalDateTime getExpiresAt();
    @Nullable Long getExpiresInSeconds();
    
    String getWorkflowName();
    @Nullable String getFormName();
    @Nullable String getFlowName();
    @Nullable String getArticleName();
    @Nullable String getParentArticleName();
    
    // Entity links
    @Nullable String getQuestionnaireId();
    @Nullable Long getTaskId();    
    @Nullable String getUserId();

    // Asset links
    String getFormTagName();
    String getStencilTagName();
    String getWrenchTagName();
    String getWorkflowTagName();
  }
  

  enum ProcessStatus {
    
    CREATED,
    ANSWERING,    
    ANSWERED, // 
    
    IN_PROGRESS,
    WAITING,
    COMPLETED,
    REJECTED,
    WAITING_FOR_SYNC // complete event arrived from form, waiting to launch flow
  }
  
  

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
    List<String> getUserRoles();
  }
  
  
}
