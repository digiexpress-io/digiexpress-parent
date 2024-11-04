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

import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.event.TaskNotificator;
import io.digiexpress.eveli.client.persistence.repositories.TaskAccessRepository;
import io.digiexpress.eveli.client.persistence.repositories.TaskRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeleteOneTask {
  private final String userId;
  private final String email;
  private final TaskRepository taskRepository;
  private final TaskNotificator notificator;
  private final TaskAccessRepository taskAccessRepository;
  
  public TaskClient.Task delete(long taskId) {

    
    final var current = taskRepository.getOneById(taskId);
    taskRepository.deleteById(taskId);
    return PaginateTasksImpl.map(current);
  }
}
