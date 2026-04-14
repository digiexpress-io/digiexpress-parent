package io.digiexpress.eveli.client.spi.gamut;

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

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import io.digiexpress.eveli.client.api.GamutClient.UserAction;
import io.digiexpress.eveli.client.api.GamutClient.UserActionBuilder;
import io.digiexpress.eveli.client.api.GamutClient.UserActionNotAllowedException;
import io.digiexpress.eveli.client.api.GamutClient.WorkflowNotFoundException;
import io.digiexpress.eveli.client.api.ImmutableUserAction;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.ProcessInstance;
import io.digiexpress.eveli.client.spi.asserts.TaskAssert;
import io.resys.limaone.program.ImmutableWorkflowDefaultProps;
import io.resys.limaone.program.ProgramInput.Participant;
import io.resys.limaone.program.WorkflowProgram.WorkflowFormResult;
import io.resys.thena.api.entities.grim.GrimProcess.GrimProcessType;
import io.smallrye.mutiny.Uni;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Data @Accessors(fluent = true)
public class UserActionsBuilderImpl implements UserActionBuilder {

  private final io.resys.limaone.program.Runtime runtime;
  private final TaskClient taskClient;
  
  private boolean customerAssignment = false;
  private Participant customer;
  private String cockpitId;
  private String actionId;
  private String taskId;
  private String clientLocale; 
  private String inputContextId;
  private String inputParentContextId;
  
  @Override
  public UserActionBuilder inputContextId(String inputContextId) {
    this.inputContextId = UserActionsBuilderImpl.visitArticleName(inputContextId);
    return this;
  }
  @Override
  public UserActionBuilder inputParentContextId(String inputParentContextId) {
    this.inputParentContextId = UserActionsBuilderImpl.visitArticleName(inputParentContextId);
    return this;
  }
  
  
  public UserActionBuilder participant(Participant customer) {
    TaskAssert.notNull(customer, () -> "customer can't be null!");
    this.customer = customer;
    return this;
  }
  public Uni<UserAction> createOne() throws UserActionNotAllowedException, WorkflowNotFoundException {
    TaskAssert.notNull(actionId, () -> "actionId can't be null!");
    TaskAssert.notNull(clientLocale, () -> "clientLocale can't be null!");
    TaskAssert.notNull(customer, () -> "customer can't be null!");

    final var props = ImmutableWorkflowDefaultProps.builder()
        .parentArticleName(inputParentContextId)
        .articleName(inputContextId)
        .locale(clientLocale)
        .build();    
    final var wk = runtime.withTenant(Optional.ofNullable(cockpitId))
        .getBundle()
        .queryWorkflows()
        .externalId(actionId)
        .name(actionId.length() > 3 ? actionId.substring(0, actionId.length() - 3) : null)
        .locale(clientLocale).findOne();
    
    if(wk.isEmpty()) {
      throw new WorkflowNotFoundException(new StringBuilder()
        .append("Can't find stencil service for locale: '").append(clientLocale).append("'!")
        .toString());
    }
    
    final var wkResult = wk.get().runForm(customer, props);
    
    if(wkResult.getAccessAllowed()) {
      return createUserAction(wkResult);
    }
    throw new UserActionNotAllowedException("Process: " + actionId + " blocked!");
  }
 
  
  private Uni<UserAction> createUserAction(WorkflowFormResult wk) {    
    final var stencilService = wk.getForm().get();
    final var expiresInSeconds = stencilService.getExpiresInSeconds();
    
    return taskClient.createProcess()
      .questionnaireId(stencilService.getFormSessionId())
      .userId(customer.getIdentity())
      .expiresInSeconds(expiresInSeconds)
      .expiresAt(stencilService.getExpiresAt())
      .anon(customer.getAnon())
      .taskId(taskId)
      
      .workflowName(stencilService.getWorkflowName())
      .articleName(inputContextId)
      .parentArticleName(inputParentContextId)
      .flowName(stencilService.getFlowName())
      .formName(stencilService.getFormName())
      
      .formTagName(stencilService.getFormVersion())
      .stencilTagName(runtime.getBundle().getName())
      .wrenchTagName(runtime.getBundle().getName())
      .customerAssignment(customerAssignment)
      .cockpitId(cockpitId)
      .commitAuthor(customer.getIdentity())
      .commitMessage("creating default proc")
      .build()
      .onItem().transform(process -> {
        final UserAction action = ImmutableUserAction.builder()
          .id(process.getId().toString())
          .status(process.getStatus().name())
          .created(process.getCreated())
          .updated(process.getUpdated())
          .name(process.getWorkflowName())
          .inputContextId(visitArticleName(process.getArticleName()))
          .inputParentContextId(process.getParentArticleName())
          .formId(process.getQuestionnaireId())
          .formInProgress(true)
          .assigned(process.getType() == GrimProcessType.CUSTOMER_ASSIGNMENT ? true : false)
          .viewed(true)
          .taskId(process.getTaskId())
          .cockpitId(process.getCockpitId())
          .build();
        return action;
      });
  }
  
  public static UserAction map(ProcessInstance process) {
    return ImmutableUserAction.builder()
        .id(process.getId().toString())
        .status(process.getStatus().name())
        .created(process.getCreated())
        .updated(process.getUpdated())
        .name(process.getWorkflowName())
        .inputContextId(visitArticleName(process.getArticleName()))
        .inputParentContextId(process.getParentArticleName())
        .formId(process.getQuestionnaireId())
        .formInProgress(true)
        .assigned(process.getType() == GrimProcessType.CUSTOMER_ASSIGNMENT ? true : false)
        .viewed(true)
        .taskId(process.getTaskId())
        .build();
  }

  private static String visitArticleName(String articleName) {
    if(StringUtils.isEmpty(articleName) || articleName.length() < 3) {
      return null;
    }
    if(articleName.charAt(3) == '_') {
      return articleName.substring(4);      
    }
    // no ordering
    return articleName;
  }
}
