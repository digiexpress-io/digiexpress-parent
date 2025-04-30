package io.digiexpress.eveli.client.spi.task;

import java.util.concurrent.CompletableFuture;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

import io.digiexpress.eveli.client.api.AuthClient;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.Task;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class TaskViewerPublisher {
  
  private final ApplicationEventPublisher publisher;
  private final TaskClient taskClient;
  
  public TaskViewerPublisher(ApplicationEventPublisher publisher, TaskClient taskClient) {
    this.publisher = publisher;
    this.taskClient = taskClient;
  }
  
  public void publicTaskViewedByWorkerEvent(Task task, AuthClient.User user) {
    publisher.publishEvent(new TaskViewedByWorkerEvent(task, user));
  }
  
  @Data
  @AllArgsConstructor
  public static class TaskViewedByWorkerEvent {
    private final Task task;
    private final AuthClient.User user;
  }
  
  @Async
  @EventListener
  public CompletableFuture<?> handleWorkerViewdTask(TaskViewedByWorkerEvent event) {
    return taskClient.taskBuilder()
        .userId(event.getUser().getPrincipal().getUsername(), null)
        .addWorkerCommitViewer(event.getTask().getId())
        .subscribeAsCompletionStage().toCompletableFuture();
        
  }
}
