package io.digiexpress.eveli.client.spi.task;

import java.util.HashSet;

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
import io.digiexpress.eveli.client.api.TaskClient.ModifyTaskCommand;
import io.digiexpress.eveli.client.event.TaskNotificator;
import io.digiexpress.eveli.client.persistence.entities.TaskEntity;
import io.digiexpress.eveli.client.persistence.repositories.TaskAccessRepository;
import io.digiexpress.eveli.client.persistence.repositories.TaskRepository;
import io.digiexpress.eveli.client.web.resources.worker.TaskControllerBase;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ModifyOneTask {
  private final String userId;
  private final String email;
  private final TaskRepository taskRepository;
  private final TaskNotificator notificator;
  private final TaskAccessRepository taskAccessRepository;
  
  public TaskClient.Task modify(long taskId, ModifyTaskCommand command) {

    
    final var current = taskRepository.getOneById(taskId);
    final var previousVersion = PaginateTasksImpl.map(current);
    
    current.setAssignedUser(command.getAssignedUser());
    current.setAssignedUserEmail(command.getAssignedUserEmail());
    current.setCompleted(command.getCompleted());
    current.setDescription(command.getDescription());
    current.setDueDate(command.getDueDate());
    current.setPriority(command.getPriority());
    current.setStatus(command.getStatus());
    current.setSubject(command.getSubject());
    current.setVersion(command.getVersion());
    current.setClientIdentificator(command.getClientIdentificator());
    current.setAssignedRoles(new HashSet<>(command.getAssignedRoles()));
    current.setUpdaterId(userId);
    
    final TaskEntity savedTask = taskRepository.save(current);
    new TaskControllerBase(taskAccessRepository).registerUserTaskAccess(savedTask.getId(), Optional.of(savedTask), userId);
    final var immutable = PaginateTasksImpl.map(savedTask);
    
    notificator.handleTaskUpdate(immutable, previousVersion, email);
    return immutable;
  }
}
