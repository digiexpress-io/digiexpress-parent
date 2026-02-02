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

import io.digiexpress.eveli.client.api.GamutAuthClient.Customer;
import io.digiexpress.eveli.client.api.GamutClient.ProcessNotFoundException;
import io.digiexpress.eveli.client.api.GamutClient.ReplayToInit;
import io.digiexpress.eveli.client.api.GamutClient.ReplyToBuilder;
import io.digiexpress.eveli.client.api.GamutClient.UserMessage;
import io.digiexpress.eveli.client.api.ImmutableCreateTaskCommentCommand;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.TaskCommentSource;
import io.digiexpress.eveli.client.spi.asserts.TaskAssert;
import io.digiexpress.eveli.client.spi.mq.MqEventPublisher;
import io.smallrye.mutiny.Uni;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;


@RequiredArgsConstructor
@Data @Accessors(fluent = true)
public class ReplyToBuilderImpl implements ReplyToBuilder {
  private final TaskClient taskClient;
  private final MqEventPublisher mqEventPublisher;
  private String actionId;
  private ReplayToInit from;
  private Customer customer;

  @Override
  public Uni<UserMessage> createOne() throws ProcessNotFoundException {
    TaskAssert.notNull(actionId, () -> "actionId can't be null!");
    TaskAssert.notNull(from, () -> "from can't be null!");
    TaskAssert.notNull(customer, () -> "customer can't be null!");
    
    return taskClient.queryTaskProcesess().findOneById(actionId)
      .onItem().transformToUni(found -> {
        final var process = found.orElseThrow(() -> new ProcessNotFoundException("Process not found by id: " + actionId + "!"));
        final var customer = this.customer.getPrincipal();    
        final var taskId = process.getTaskId();
        
        return taskClient.taskBuilder()
          .userId(customer.getUsername(), null)
          .createTaskComment(ImmutableCreateTaskCommentCommand.builder()
            .taskId(taskId)
            .commentText(from.getText())
            .external(true)
            .source(TaskCommentSource.PORTAL)
            .build()
          );
      })
      .onItem().call(savedMsg -> taskClient.queryTasks()
          .getOneById(savedMsg.getTaskId())
          .onItem().invoke((task) -> mqEventPublisher.publishMqEvent(task, TaskCommentSource.PORTAL))
      )
      .onItem().transform(savedComment -> UserMessagesQueryImpl.visitUserMessage(savedComment, this.customer));
  }
}
