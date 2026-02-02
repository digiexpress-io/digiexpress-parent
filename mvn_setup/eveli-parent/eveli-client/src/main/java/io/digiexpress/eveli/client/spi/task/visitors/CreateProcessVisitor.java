package io.digiexpress.eveli.client.spi.task.visitors;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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

import io.digiexpress.eveli.client.api.TaskClient.CreateProcess;
import io.digiexpress.eveli.client.api.TaskClient.ProcessInstance;
import io.digiexpress.eveli.client.spi.asserts.ProcessAssert;
import io.digiexpress.eveli.client.spi.task.TaskException;
import io.digiexpress.eveli.client.spi.task.TaskMapper;
import io.digiexpress.eveli.client.spi.task.TaskStore;
import io.resys.thena.api.entities.grim.GrimProcess.GrimProcessStatus;
import io.resys.thena.api.entities.grim.GrimProcess.GrimProcessType;
import io.resys.thena.api.envelope.CommitResultStatus;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor @Data @Accessors(fluent=true)
public class CreateProcessVisitor implements CreateProcess {
  private final TaskStore ctx;
  
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
  private String formBody;
  private String flowName;
  private String taskId;
  private String formTagName;
  private String stencilTagName;
  private String wrenchTagName;
  private String cockpitId;
  
  private String commitAuthor;
  private String commitMessage;
  
  
  @Override
  public Uni<ProcessInstance> build() {
    ProcessAssert.notNull(workflowName, () -> "workflowName must be defined!");
    ProcessAssert.notNull(userId, () -> "userId must be defined!");
    ProcessAssert.notNull(anon, () -> "anon must be defined!");
    
    ProcessAssert.notEmpty(commitAuthor, () -> "commitAuthor must be defined!");
    ProcessAssert.notEmpty(commitMessage, () -> "commitMessage must be defined!");
    
    if(taskId == null) {
      ProcessAssert.notNull(formName, () -> "formName must be defined!");
      ProcessAssert.notNull(questionnaireId, () -> "questionnaireId must be defined!");
      ProcessAssert.notNull(articleName, () -> "articleName must be defined!");
      ProcessAssert.notNull(flowName, () -> "flowName must be defined!");
    }
    final var config = ctx.getConfig();
    final var grim = config.getClient().grim(config.getTenantName());
    
    return grim.commit().createOneProc()
        .commitAuthor(commitAuthor)
        .commitMessage(commitMessage)
        .proc((newProc) -> 
          
          newProc
          .expiresAt(expiresAt)
          .status(GrimProcessStatus.CREATED)
          .expiresInSeconds(expiresInSeconds)
          .expiresAt(expiresAt)
          
          .questionnaireId(questionnaireId)
          .userId(userId)
          
          .workflowName(workflowName)
          .articleName(articleName)
          .parentArticleName(parentArticleName)
          .anon(anon)
          .formName(formName)
          .flowName(flowName)
          .missionId(taskId)
          .formBody(formBody == null ? null : new JsonObject(formBody))
          .cockpitId(cockpitId)
          
          .formTagName(formTagName)
          .stencilTagName(stencilTagName)
          .wrenchTagName(wrenchTagName)
          .type(customerAssignment ? GrimProcessType.CUSTOMER_ASSIGNMENT : null)
          .build()
          
        )
        .build()
        .onItem().transform(e -> {
          if(e.getStatus() != CommitResultStatus.OK || e.getProc() == null) {
            throw TaskException.builder("MODIFY_ONE_TASK_PROC_FAIL").add(grim, e).build();
          }
          return TaskMapper.map(e.getProc());
        });
  }

}
