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

import io.digiexpress.eveli.client.api.TaskCommands.TaskStatus;
import io.digiexpress.eveli.client.persistence.entities.ProcessEntity;
import jakarta.annotation.Nullable;


public interface ProcessCommands {
  ProcessQuery query();
  ProcessStatusBuilder status();
  Process create(InitProcess request);
  void delete(String processId);
  
  interface ProcessQuery {
    Optional<Process> get(String id);
    Optional<Process> getByQuestionnaireId(String id);
    Optional<Process> getByTaskId(String id);
    Page<ProcessEntity> find(String name, List<String> status, String userId, Pageable page);
    List<Process> findAll();
  }

  interface ProcessStatusBuilder {
    void answered(String id);
    void answeredByQuestionnaire(String questionnaireId, String taskId);
    void taskStatusChange(String taskId, TaskStatus taskStatus);
    void inProgress(String id);
    void completed(String id);
    void rejected(String id);
  }
  
  //@Relation(collectionRelation = "processDataList", itemRelation = "processDataList", value = "processDataList" )
  @Value.Immutable
  @JsonSerialize(as = ImmutableProcess.class)
  @JsonDeserialize(as = ImmutableProcess.class)
  interface Process {
    Long getId();
    String getWorkflowName();
    ProcessStatus getStatus();
    String getQuestionnaire();
    @Nullable
    String getTask();
    @Nullable
    String getUserId();
    LocalDateTime getCreated();
    LocalDateTime getUpdated();
    @Nullable
    String getInputContextId();
    @Nullable
    String getInputParentContextId();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableInitProcess.class)
  @JsonDeserialize(as = ImmutableInitProcess.class)
  interface InitProcess {
    String getIdentity();
    String getWorkflowName();
    Boolean getProtectionOrder();    

    @Nullable
    String getCompanyName();
    @Nullable
    String getFirstName();
    @Nullable
    String getLastName();
    @Nullable
    String getLanguage();
    @Nullable
    String getEmail();
    @Nullable
    String getAddress();

    @Nullable
    String getRepresentativeFirstName();
    @Nullable
    String getRepresentativeLastName();
    @Nullable
    String getRepresentativeIdentity();
    @Nullable
    String getInputContextId();
    @Nullable
    String getInputParentContextId();
  }
  
  enum ProcessStatus {
    CREATED,
    ANSWERING,
    ANSWERED,
    IN_PROGRESS,
    WAITING,
    COMPLETED,
    REJECTED
  }
}
