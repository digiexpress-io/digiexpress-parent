package io.digiexpress.eveli.client.spi.task.visitors;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;

import io.digiexpress.eveli.client.api.CustomerAccountClient.CustomerAccount;
import io.digiexpress.eveli.client.api.CustomerAccountClient.CrmAccountType;

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

import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.CreateTaskCommand;
import io.digiexpress.eveli.client.api.TaskClient.Task;
import io.digiexpress.eveli.client.api.TaskClient.TaskPriority;
import io.digiexpress.eveli.client.api.TaskClient.TaskStatus;
import io.digiexpress.eveli.client.event.TaskNotificator;
import io.digiexpress.eveli.client.spi.task.TaskException;
import io.digiexpress.eveli.client.spi.task.TaskMapper;
import io.digiexpress.eveli.client.spi.task.TaskStoreConfig;
import io.resys.thena.api.ThenaClient.GrimStructuredTenant;
import io.resys.thena.api.actions.GrimCommitActions.CreateOneMission;
import io.resys.thena.api.actions.GrimCommitActions.OneMissionEnvelope;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewMission;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateOneTask implements TaskStoreConfig.CreateOneTaskVisitor<TaskClient.Task> {
  private final String userId;
  private final TaskNotificator notificator;
  private final CreateTaskCommand command;
  @Nullable private final CustomerAccount account;
  
  private void createTask(CreateTaskCommand commmand, NewMission mission) {
    final var status = commmand.getStatus() == null ? TaskStatus.NEW: commmand.getStatus();
    final var priority = commmand.getPriority() == null ? TaskPriority.NORMAL: commmand.getPriority();
    final var usedFor = commmand.getQuestionnaireId() == null ? TaskMapper.VIEWER_WORKER : TaskMapper.VIEWER_CUSTOMER;
    
    mission
      .reporterId(commmand.getClientIdentificator())
      .title(commmand.getSubject())
      .description(commmand.getDescription())
      .dueDate(commmand.getDueDate())
      .status(status.name())
      .priority(priority.name())
      .questionnaireId(commmand.getQuestionnaireId())
      .addViewer(newViewer -> newViewer.userId(userId).usedFor(usedFor).build());
    
    // add roles
    for(final var role : commmand.getAssignedRoles()) {
      mission
      .addAssignees(newAss -> newAss
        .assignee(role)
        .assignmentType(TaskMapper.ASSIGNMENT_TYPE_TASK_ROLE)
        .build()
      );
    }

    // assign to given user    
    if(commmand.getAssignedUser() != null) {
      mission
      .addAssignees(newAss -> newAss
        .assignee(commmand.getAssignedUser())
        .assigneeContact(commmand.getAssignedUserEmail())
        .assignmentType(TaskMapper.ASSIGNMENT_TYPE_TASK_USER)
        .build()
      );
    }
    
    
    // set client locale    
    if(command.getQuestionnaireId() != null) {
      mission
      .addLink(newLink -> newLink
        .linkValue(Optional.ofNullable(commmand.getClientLanguage() ).orElse(TaskMapper.DEFAULT_CLIENT_LANG))
        .linkType(TaskMapper.LINK_TYPE_CLIENT_LOCALE)
        .build()
      );
    }
    
    
    // set additional info 
    if(commmand.getAdditionalInfo() != null) {
      mission
      .addLink(newLink -> newLink
        .linkValue(commmand.getAdditionalInfo())
        .linkType(TaskMapper.LINK_TYPE_ADDITIONAL_INFO)
        .build()
      );
    }
    
    // set the keywords
    for(final var keyword : commmand.getKeyWords()) {
      mission.addLabels(newLabel -> newLabel
          .labelType(TaskMapper.LABEL_TYPE_KEYWORD)
          .labelValue(keyword)
          .build());
    }

    // set the features
    for(final var feature : commmand.getFeatures()) {
      mission.addLabels(newLabel -> newLabel
          .labelType(TaskMapper.LABEL_TYPE_FEATURES)
          .labelValue(feature)
          .build());
    }
    
    
    final var features = ImmutableList.<String>builder()
        .addAll(getCsvFeatures(command))
        .addAll(getCrmFeatures(account))
        .build();
    
    for(final var feature : features) {
      mission.addLabels(newLabel -> newLabel
          .labelType(TaskMapper.LABEL_TYPE_FEATURES)
          .labelValue(feature)
          .build());
    }
  
    
    mission.build();
  }
  
  
  private List<String> getCrmFeatures(CustomerAccount account) {
    if(account == null) {
      return Collections.emptyList();
    }
    if(account.getType() == CrmAccountType.ANON) {
      return Arrays.asList(TaskMapper.TASK_FEATURE_ANON);
    }
    return Collections.emptyList();
  }
  
  private List<String> getCsvFeatures(CreateTaskCommand commmand) {
    if(commmand.getFeaturesAsCsv() != null && !commmand.getFeaturesAsCsv().trim().isEmpty()) {
      return Arrays.asList(commmand.getFeaturesAsCsv().split(","));
    }
    return Collections.emptyList();
  }
  
  @Override
  public CreateOneMission start(GrimStructuredTenant config, CreateOneMission builder) {
    
    /*
    if(command.getQuestionnaireId() != null && StringUtils.isEmpty(command.getClientLanguage())) {
      throw TaskException.builder("CREATE_TASKS_SAVE_FAIL")
        .add("clientLanguageMustBeDefined", "command is missing clientLanguage", JsonObject.of("questionnaireId", command.getQuestionnaireId()))
        .build(); 
    }*/
    
    builder.mission(newMission -> createTask(command, newMission));
    return builder
        .commitAuthor(userId)
        .commitMessage("Creating task by: " + CreateOneTask.class.getSimpleName());
  }

  @Override
  public OneMissionEnvelope visitEnvelope(GrimStructuredTenant config, OneMissionEnvelope envelope) {
    if(envelope.getStatus() == CommitResultStatus.OK) {
      return envelope;
    }
    throw TaskException.builder("CREATE_TASKS_SAVE_FAIL").add(config, envelope).build(); 
  }

  @Override
  public Uni<Task> end(GrimStructuredTenant config, OneMissionEnvelope commited) {
    final var task = TaskMapper.map(
        commited.getMission(), 
        commited.getAssignments(), 
        commited.getRemarks(), 
        commited.getLinks(),
        commited.getLabels());
    notificator.handleTaskCreation(task, userId); 
    return Uni.createFrom().item(task);
  }
  
  
}
