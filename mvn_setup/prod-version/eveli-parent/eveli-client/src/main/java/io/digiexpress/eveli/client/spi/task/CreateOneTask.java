package io.digiexpress.eveli.client.spi.task;

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

import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.CreateTaskCommand;
import io.digiexpress.eveli.client.api.TaskClient.TaskStatus;
import io.digiexpress.eveli.client.event.TaskNotificator;
import io.digiexpress.eveli.client.persistence.entities.TaskEntity;
import io.digiexpress.eveli.client.persistence.entities.TaskRefGenerator;
import io.digiexpress.eveli.client.persistence.repositories.TaskAccessRepository;
import io.digiexpress.eveli.client.persistence.repositories.TaskRepository;
import io.digiexpress.eveli.client.web.resources.worker.TaskControllerBase;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateOneTask {
  private final String userId;
  private final TaskRepository taskRepository;
  private final TaskRefGenerator taskRefGenerator;
  private final TaskNotificator notificator;
  private final TaskAccessRepository taskAccessRepository;
  
  public TaskClient.Task create(CreateTaskCommand commmand) {
    final var newTask = new TaskEntity()
        .setAssignedUser(commmand.getAssignedUser())
        .setAssignedUserEmail(commmand.getAssignedUserEmail())
        .setClientIdentificator(commmand.getClientIdentificator())
        .setCompleted(commmand.getCompleted())
        .setDescription(commmand.getDescription())
        .setDueDate(commmand.getDueDate())
        .setPriority(commmand.getPriority())
        .setStatus(commmand.getStatus() == null ? TaskStatus.NEW: commmand.getStatus())
        .setSubject(commmand.getSubject())
        .setTaskRef(taskRefGenerator.generateTaskRef())
        .setUpdaterId(userId);
    
    final var savedTask = taskRepository.save(newTask);
    
    // TODO
    new TaskControllerBase(taskAccessRepository).registerUserTaskAccess(savedTask.getId(), Optional.of(savedTask), userId);
    final var immutable = PaginateTasksImpl.map(savedTask);
    
    // TODO
    notificator.handleTaskCreation(immutable, userId);
    return immutable;
  }
}
