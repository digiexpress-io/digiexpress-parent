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

import java.util.Optional;

import io.digiexpress.eveli.client.api.CrmClient;
import io.digiexpress.eveli.client.api.GamutClient.ProcessNotFoundException;
import io.digiexpress.eveli.client.api.GamutClient.ReplayToInit;
import io.digiexpress.eveli.client.api.GamutClient.ReplyToBuilder;
import io.digiexpress.eveli.client.api.ProcessClient;
import io.digiexpress.eveli.client.api.TaskClient.TaskCommentSource;
import io.digiexpress.eveli.client.persistence.entities.TaskCommentEntity;
import io.digiexpress.eveli.client.persistence.repositories.CommentRepository;
import io.digiexpress.eveli.client.persistence.repositories.TaskAccessRepository;
import io.digiexpress.eveli.client.persistence.repositories.TaskRepository;
import io.digiexpress.eveli.client.spi.asserts.TaskAssert;
import io.digiexpress.eveli.client.web.resources.worker.TaskControllerBase;
import io.thestencil.iam.api.UserActionsClient.UserMessage;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;


@RequiredArgsConstructor
@Data @Accessors(fluent = true)
public class ReplyToBuilderImpl implements ReplyToBuilder {
  private final ProcessClient processRepository;
  private final CommentRepository commentRepository;
  private final TaskRepository taskRepository;
  private final TaskAccessRepository taskAccessRepository;
  private final CrmClient authClient;
  private String actionId;
  private ReplayToInit from;


  @Override
  public UserMessage createOne() throws ProcessNotFoundException {
    TaskAssert.notNull(actionId, () -> "actionId can't be null!");
    TaskAssert.notNull(from, () -> "from can't be null!");
    
    final var process = processRepository.queryInstances().findOneById(actionId)
        .orElseThrow(() -> new ProcessNotFoundException("Process not found by id: " + actionId + "!"));
    
    final var customer = authClient.getCustomer().getPrincipal();    
    final var taskId = process.getTaskId();
    final var commentTask = taskRepository.getOneById(taskId);
    final var entity = new TaskCommentEntity()
        .setTask(commentTask)
        .setUserName(customer.getUsername())
        .setCommentText(from.getText())
        .setExternal(true)
        .setSource(TaskCommentSource.PORTAL);
    
    final var isUnread = taskRepository.findUnreadExternalTasks(customer.getUsername()).contains(taskId);
    final var savedComment = commentRepository.save(entity);
    
    if(!isUnread) {
      new TaskControllerBase(taskAccessRepository).registerUserTaskAccess(taskId, Optional.of(commentTask), customer.getUsername());
    }
    return UserMessagesQueryImpl.visitUserMessage(savedComment, authClient.getCustomer());
  }

}
