package io.digiexpress.eveli.client.spi.process;

import java.time.OffsetDateTime;

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
import io.digiexpress.eveli.client.api.TaskClient.ProcessInstance;
import io.digiexpress.eveli.client.persistence.entities.ProcessEntity;
import io.digiexpress.eveli.client.persistence.repositories.ProcessRepository;
import io.digiexpress.eveli.client.spi.asserts.ProcessAssert;
import io.resys.thena.api.entities.grim.GrimProcess.GrimProcessStatus;
import io.resys.thena.api.entities.grim.GrimProcess.GrimProcessType;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor @Data @Accessors(fluent=true)
public class CreateProcessInstanceImpl implements CreateProcessInstance {

  private final ProcessRepository processRepository;
  
  private String questionnaireId;
  private String userId;
  private String workflowName;
  private String articleName;
  private String parentArticleName;
  private OffsetDateTime expiresAt;
  private Long expiresInSeconds;
  
  private boolean anon = false;
  private boolean customerAssignment = false;
  private String formName;
  private String flowName;
  private String taskId;
  private String formTagName;
  private String stencilTagName;
  private String wrenchTagName;
  private String cockpitId;
  
  
  public ProcessInstance create() {
    ProcessAssert.notNull(workflowName, () -> "workflowName must be defined!");
    ProcessAssert.notNull(userId, () -> "userId must be defined!");
    ProcessAssert.notNull(anon, () -> "anon must be defined!");
    
    if(taskId == null) {
      ProcessAssert.notNull(formName, () -> "formName must be defined!");
      ProcessAssert.notNull(questionnaireId, () -> "questionnaireId must be defined!");
      ProcessAssert.notNull(articleName, () -> "articleName must be defined!");
      ProcessAssert.notNull(flowName, () -> "flowName must be defined!");
    }
        
    final var entity = processRepository.save(new ProcessEntity()
      .setExpiresAt(expiresAt)
      .setStatus(GrimProcessStatus.CREATED)
      .setExpiresInSeconds(expiresInSeconds)
      .setExpiresAt(expiresAt)
      
      .setQuestionnaireId(questionnaireId)
      .setUserId(userId)
      
      .setWorkflowName(workflowName)
      .setArticleName(articleName)
      .setParentArticleName(parentArticleName)
      .setAnon(anon)
      .setFormName(formName)
      .setFlowName(flowName)
      .setTaskId(taskId)
      .setCockpitId(cockpitId)
      
      .setFormTagName(formTagName)
      .setStencilTagName(stencilTagName)
      .setWrenchTagName(wrenchTagName)
      .setType(customerAssignment ? GrimProcessType.CUSTOMER_ASSIGNMENT : null)
      );

    return map(entity);
  }

  
  public static ProcessInstance map(ProcessEntity entity) {
    return ImmutableProcessInstance.builder()
      .id(entity.getId())
      .status(entity.getStatus())
      .questionnaireId(entity.getQuestionnaireId())
      .taskId(entity.getTaskId())
      .cockpitId(entity.getCockpitId())
      .userId(entity.getUserId())
      .created(entity.getCreated())
      .updated(entity.getUpdated())
      
      .workflowName(entity.getWorkflowName())
      .articleName(entity.getArticleName())
      .parentArticleName(entity.getParentArticleName())
      .anon(Boolean.TRUE.equals(entity.getAnon()))
      .formName(entity.getFormName())
      .flowName(entity.getFlowName())
      .type(entity.getType())
      
      
      .formTagName(entity.getFormTagName())
      .stencilTagName(entity.getStencilTagName())
      .wrenchTagName(entity.getWrenchTagName())
      
      .expiresInSeconds(entity.getExpiresInSeconds())
      .expiresAt(entity.getExpiresAt())
      .build();
  }
}
