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

import io.digiexpress.eveli.client.api.CrmClient;
import io.digiexpress.eveli.client.api.CrmClient.Customer;
import io.digiexpress.eveli.client.api.GamutClient.ProcessNotFoundException;
import io.digiexpress.eveli.client.api.GamutClient.UserMessage;
import io.digiexpress.eveli.client.api.GamutClient.UserMessagesQuery;
import io.digiexpress.eveli.client.api.ImmutableUserMessage;
import io.digiexpress.eveli.client.api.ProcessClient;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.TaskComment;
import io.digiexpress.eveli.client.spi.asserts.TaskAssert;
import io.digiexpress.eveli.client.spi.task.TaskMapper;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class UserMessagesQueryImpl implements UserMessagesQuery {
  private final ProcessClient processRepository;
  private final TaskClient taskClient;
  private final CrmClient authClient;
  
  @Override
  public List<UserMessage> findAllByActionId(String actionId) throws ProcessNotFoundException {
    TaskAssert.notNull(actionId, () -> "actionId can't be null!");
    
    final var process = processRepository.queryInstances().findOneById(actionId)
        .orElseThrow(() -> new ProcessNotFoundException("Process not found by id: " + actionId + "!"));
    
    
    final var customer = authClient.getCustomer();
    final var taskId = process.getTaskId();
    final var comments = taskClient.queryTaskComments()
        .findAllByTaskId(taskId)
        .await().atMost(TaskMapper.atMost)
        .stream()
        .filter(comment -> Boolean.TRUE.equals(comment.getExternal()))
        .map(comment -> visitUserMessage(comment, customer))
        .toList();
    
    taskClient.taskBuilder()
      .userId(customer.getPrincipal().getUsername(), null)
      .addCustomerCommitViewer(taskId)
      .await().atMost(TaskMapper.atMost);
    
    
    return comments;
  }
  
  @Override
  public List<UserMessage> findAllByUserId() {
    final var customer = authClient.getCustomer();
    final var comments = taskClient.queryTaskComments()
        .findAllByReporterId(customer.getPrincipal().getUsername())
        .await().atMost(TaskMapper.atMost)
        .stream()
        .filter(comment -> Boolean.TRUE.equals(comment.getExternal()))
        .map(comment -> visitUserMessage(comment, customer))
        .toList();
    return comments;
  }
  
  public static UserMessage visitUserMessage(TaskComment msg, Customer customer) {
    final var replyToId = msg.getReplyToId();
    final var userMsg = ImmutableUserMessage.builder()
        .id(msg.getId().toString())
        .taskId(msg.getTaskId())
        .replyToId(replyToId)
        .created(msg.getCreated().toString())
        .userName(UserMessagesQueryImpl.visitMessageUserName(msg, customer))
        .commentText(msg.getCommentText())
        .build();
    
    return userMsg;
  }

  public static String visitMessageUserName(TaskComment entity, Customer customer) {
    
    final var user = customer.getPrincipal();
    
    final String userName;
    if(user.getRepresentedPerson() != null) {
      final var personNames = user.getRepresentedPerson().getRepresentativeName();
      userName = personNames[1] + " " + personNames[0];
    } else if(user.getRepresentedCompany() != null) {
      userName = user.getRepresentedCompany().getName();
    } else {
      userName = null;
    }
    
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
