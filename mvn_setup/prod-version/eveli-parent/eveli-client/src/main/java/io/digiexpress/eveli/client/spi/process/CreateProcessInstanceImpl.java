package io.digiexpress.eveli.client.spi.process;

import java.time.LocalDateTime;
import java.util.Optional;

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

import io.digiexpress.eveli.client.api.ImmutableProcessInstance;
import io.digiexpress.eveli.client.api.ProcessClient.CreateProcessInstance;
import io.digiexpress.eveli.client.api.ProcessClient.ProcessInstance;
import io.digiexpress.eveli.client.api.ProcessClient.ProcessStatus;
import io.digiexpress.eveli.client.persistence.entities.ProcessEntity;
import io.digiexpress.eveli.client.persistence.repositories.ProcessRepository;
import io.digiexpress.eveli.client.spi.asserts.ProcessAssert;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor @Data @Accessors(fluent=true)
public class CreateProcessInstanceImpl implements CreateProcessInstance {

  private final ProcessRepository processRepository;
  
  private String questionnaire;
  private String userId;
  private String workflowName;
  private String inputContextId;
  private String inputParentContextId;
  private LocalDateTime expiresAt;
  private Long expiresInSeconds;
  
  public ProcessInstance create() {
    
    ProcessAssert.notNull(questionnaire, () -> "questionnaire must be defined!");
    ProcessAssert.notNull(userId, () -> "userId must be defined!");
    ProcessAssert.notNull(workflowName, () -> "workflowName must be defined!");
    ProcessAssert.notNull(inputContextId, () -> "inputContextId must be defined!");
    ProcessAssert.notNull(inputParentContextId, () -> "inputParentContextId must be defined!");
    
    final var entity = processRepository.save(new ProcessEntity()
      .setExpiresAt(expiresAt)
      .setQuestionnaire(questionnaire)
      .setUserId(userId)
      .setStatus(ProcessStatus.CREATED)
      .setWorkflowName(workflowName)
      .setInputContextId(inputContextId)
      .setExpiresInSeconds(expiresInSeconds)
      .setExpiresAt(expiresAt)
      .setInputParentContextId(inputParentContextId));
    return map(entity);
  }

  
  public static ProcessInstance map(ProcessEntity entity) {
    return ImmutableProcessInstance.builder()
      .id(entity.getId())
      .status(entity.getStatus())
      .workflowName(entity.getWorkflowName())
      .questionnaire(entity.getQuestionnaire())
      .task(Optional.ofNullable(entity.getTask()).map(Long::parseLong).orElse(null))
      .userId(entity.getUserId())
      .created(entity.getCreated())
      .updated(entity.getUpdated())
      .inputContextId(entity.getInputContextId())
      .inputParentContextId(entity.getInputParentContextId())
      .expiresInSeconds(entity.getExpiresInSeconds())
      .expiresAt(entity.getExpiresAt())
      .build();
  }

}
