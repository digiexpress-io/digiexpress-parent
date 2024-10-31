package io.digiexpress.eveli.client.spi.gamut;

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

import java.util.List;
import java.util.Optional;

import io.digiexpress.eveli.client.api.CrmClient;
import io.digiexpress.eveli.client.api.CrmClient.Customer;
import io.digiexpress.eveli.client.api.GamutClient.ProcessNotFoundException;
import io.digiexpress.eveli.client.api.GamutClient.UserMessagesQuery;
import io.digiexpress.eveli.client.persistence.entities.TaskCommentEntity;
import io.digiexpress.eveli.client.persistence.repositories.CommentRepository;
import io.digiexpress.eveli.client.persistence.repositories.ProcessRepository;
import io.digiexpress.eveli.client.persistence.repositories.TaskAccessRepository;
import io.digiexpress.eveli.client.persistence.repositories.TaskRepository;
import io.digiexpress.eveli.client.spi.asserts.TaskAssert;
import io.digiexpress.eveli.client.web.resources.TaskControllerBase;
import io.thestencil.iam.api.ImmutableUserMessage;
import io.thestencil.iam.api.UserActionsClient.UserMessage;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class UserMessagesQueryImpl implements UserMessagesQuery {
  private final ProcessRepository processRepository;
  private final CommentRepository commentRepository;
  private final TaskRepository taskRepository;
  private final TaskAccessRepository taskAccessRepository;
  private final CrmClient authClient;
  
  @Override
  public List<UserMessage> findAllByActionId(String actionId) throws ProcessNotFoundException {
    TaskAssert.notNull(actionId, () -> "actionId can't be null!");
    
    final var process = processRepository.findById(Long.parseLong(actionId))
        .orElseThrow(() -> new ProcessNotFoundException("Process not found by id: " + actionId + "!"));
    
    
    final var customer = authClient.getCustomer();
    final var taskId = Long.parseLong(process.getTask());
    final var comments = commentRepository.findByTaskIdAndExternalTrue(taskId).stream()
        .map(comment -> visitUserMessage(comment, customer))
        .toList();
    final var task = taskRepository.findById(taskId);
    
    new TaskControllerBase(taskAccessRepository).registerUserTaskAccess(taskId, task,  customer.getPrincipal().getUsername());
    
    return comments;
  }
  
  public static UserMessage visitUserMessage(TaskCommentEntity msg, Customer customer) {
    final var replyToId = Optional.ofNullable(msg.getReplyTo()).map(replay -> replay.getId().toString()).orElse(null);
    final var userMsg = ImmutableUserMessage.builder()
        .id(msg.getId().toString())
        .taskId(msg.getTask().getId().toString())
        .replyToId(replyToId)
        .created(msg.getCreated().toString())
        .userName(UserMessagesQueryImpl.visitMessageUserName(msg, customer))
        .commentText(msg.getCommentText())
        .build();
    
    return userMsg;
  }

  public static String visitMessageUserName(TaskCommentEntity entity, Customer customer) {
    
    final var user = customer.getPrincipal();
    final var personNames = user.getRepresentedPerson() == null ? null : user.getRepresentedPerson().getRepresentativeName();
    final var userName = personNames != null ? personNames[1] + " " + personNames[0]: user.getRepresentedCompany().getName();
    final var representativeUserName = user.getUsername();
    
    if(entity.getUserName().equals(userName)) {
      return entity.getUserName();
    }
    if(entity.getUserName().equals(representativeUserName)) {
      return entity.getUserName();
    } 
    return "";
  }
}